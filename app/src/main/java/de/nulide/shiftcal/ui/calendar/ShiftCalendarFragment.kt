package de.nulide.shiftcal.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.monthyearpicker.MonthYearPickerBuilder
import de.nulide.monthyearpicker.OnMonthYearSelectedListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.FragmentShiftCalendarBinding
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarDataUseCase
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase
import de.nulide.shiftcal.ui.helper.SFragment
import de.nulide.shiftcal.ui.intro.FabMenuIntro
import de.nulide.shiftcal.utils.Runner
import de.nulide.shiftcal.utils.Snack
import de.nulide.shiftcal.utils.pdf.PDFCreator
import de.nulide.shiftcal.utils.pdf.PDFHelper
import de.nulide.shiftcal.ui.settings.export.CalendarIcsExporter
import de.nulide.shiftcal.ui.settings.export.ExportFileNameHelper
import java.io.File
import java.time.Instant
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId


class ShiftCalendarFragment : SFragment(),
    View.OnClickListener, OnMonthYearSelectedListener {

    private lateinit var binding: FragmentShiftCalendarBinding

    override val fragmentName = "calendar"

    lateinit var sc: SCRepoManager
    lateinit var settings: SettingsRepository

    lateinit var calViewModel: CalViewModel

    private lateinit var todayButton: View
    private lateinit var shareButton: View
    private lateinit var switchCalendarViewUseCase: SwitchCalendarViewUseCase

    private lateinit var switchCalendarDataUseCase: SwitchCalendarDataUseCase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentShiftCalendarBinding.inflate(layoutInflater)
        root = binding.root

        sc = SCRepoManager.getInstance(ctx)
        settings = SettingsRepository.getInstance(ctx)
        calViewModel = ViewModelProvider(main)[CalViewModel::class]

        sendToViewModelReceiver()

        switchCalendarViewUseCase = SwitchCalendarViewUseCase(settings, calViewModel)
        switchCalendarViewUseCase.switchToLast()

        switchCalendarDataUseCase = SwitchCalendarDataUseCase(settings, sc, calViewModel)
        switchCalendarDataUseCase.switchToLast()

        calViewModel.getCurrentMonthLive().observe(main) { _ ->
            updateActivity()
        }

        calViewModel.getLiveEditMode().observe(main) { toEdit ->
            if (toEdit) {
                Runner.runLate(150) {
                    FabMenuIntro(
                        ctx,
                        this,
                        binding
                    ).showIntro()
                }
            }
        }

        val touchListener = { event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    disableViewPagerInput()
                }

                MotionEvent.ACTION_UP -> {
                    enableViewPagerInput()
                }

                MotionEvent.ACTION_CANCEL -> {
                    enableViewPagerInput()
                }
            }
        }
        binding.monthCal.interceptedTouchListener = touchListener
        binding.weekCal.interceptedTouchListener = touchListener

    }

    override fun updateActivity() {
        val months = DateFormatSymbols().months.toList()
        val yearMonth = calViewModel.getCurrentMonth()
        setTitle(months[yearMonth.month.value - 1] + "\t\t\t" + yearMonth.year.toString())
    }

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_shift_calendar, menu)
        todayButton = createMenuItem(menu, R.id.today, R.drawable.ic_calendar)
        shareButton = createMenuItem(menu, R.id.share, R.drawable.ic_share)
    }

    fun createMenuItem(menu: Menu?, itemRes: Int, iconRes: Int): View {
        val actionView =
            layoutInflater.inflate(
                R.layout.action_view_menu_item,
                FrameLayout(requireContext()),
                false
            ) as FrameLayout
        val actionButton = actionView.findViewById<AppCompatImageButton>(R.id.actionButton)
        actionButton.setImageResource(iconRes)
        actionButton.setOnClickListener(this)
        menu?.findItem(itemRes)?.actionView = actionView
        return actionButton
    }

    override fun onResume() {
        super.onResume()
        calViewModel.trigger(calViewModel.resume)
    }

    override fun onTitleClicked() {
        showMonthYearPicker()
    }

    private fun showMonthYearPicker() {
        val monthYearPicker = MonthYearPickerBuilder(ctx)
        monthYearPicker.setYearRange(
            calViewModel.getOldestMonth().year,
            calViewModel.getNewestMonth().year,
            true
        )
        monthYearPicker.setOnMonthYearSelectedListener(this)
        monthYearPicker.show()
    }

    override fun onPause() {
        super.onPause()
        //TODO
        /* if (calViewModel.getEditMode()) {
            onClick(binding.)
        }*/
    }

    @SuppressLint("ResourceType")
    override fun onClick(v: View?) {
        when (v) {
            todayButton -> {
                calViewModel.trigger(calViewModel.scrollTo, LocalDate.now())
            }

            shareButton -> {
                showShareScopeDialog()
            }
        }
    }

    override fun onMonthYearSelected(yearMonth: YearMonth) {
        val date = TimeFactory.convYearMonthToLocalDate(yearMonth)
        var scrollToDate: LocalDate?
        if (yearMonth.isBefore(calViewModel.getOldestMonth())) {
            scrollToDate = TimeFactory.convYearMonthToLocalDate(
                calViewModel.getOldestMonth()
            )
        } else if (yearMonth.isAfter(calViewModel.getNewestMonth())) {
            scrollToDate = TimeFactory.convYearMonthToLocalDate(
                calViewModel.getNewestMonth()
            )
        } else {
            scrollToDate = date
        }
        calViewModel.trigger(calViewModel.scrollTo, scrollToDate)
    }

    private fun showShareScopeDialog() {
        val options = arrayOf(
            getString(R.string.share_calendar_scope_current_month),
            getString(R.string.export_calendar_scope_all),
            getString(R.string.export_calendar_scope_month),
            getString(R.string.export_calendar_scope_range)
        )
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.share_calendar_scope_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showShareFormatDialog(
                        ShareScope(
                            start = calViewModel.getCurrentMonth().atDay(1),
                            end = calViewModel.getCurrentMonth().atEndOfMonth()
                        )
                    )
                    1 -> {
                        val allWorkDays = sc.workDays.getAll()
                        if (allWorkDays.isEmpty()) {
                            Snack.not(binding.root, getString(R.string.pdf_no_calendar))
                        } else {
                            showShareFormatDialog(
                                ShareScope(
                                    start = allWorkDays.minOf { it.day },
                                    end = allWorkDays.maxOf { it.day }
                                )
                            )
                        }
                    }
                    2 -> showShareMonthPicker()
                    3 -> showShareRangePicker()
                }
            }
            .show()
    }

    private fun showShareMonthPicker() {
        val picker = MonthYearPickerBuilder(ctx)
        picker.setYearRange(
            calViewModel.getOldestMonth().year,
            calViewModel.getNewestMonth().year,
            true
        )
        picker.setOnMonthYearSelectedListener(object : OnMonthYearSelectedListener {
            override fun onMonthYearSelected(yearMonth: YearMonth) {
                showShareFormatDialog(
                    ShareScope(
                        start = yearMonth.atDay(1),
                        end = yearMonth.atEndOfMonth()
                    )
                )
            }
        })
        picker.show()
    }

    private fun showShareRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.export_calendar_scope_range)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first
            val endMillis = selection.second
            if (startMillis == null || endMillis == null) {
                Snack.not(binding.root, getString(R.string.export_range_invalid))
                return@addOnPositiveButtonClickListener
            }
            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            if (end.isBefore(start)) {
                Snack.not(binding.root, getString(R.string.export_range_invalid))
                return@addOnPositiveButtonClickListener
            }
            showShareFormatDialog(ShareScope(start, end))
        }
        picker.show(parentFragmentManager, "share-range")
    }

    private fun showShareFormatDialog(scope: ShareScope) {
        val options = arrayOf(
            getString(R.string.share_calendar_format_pdf),
            getString(R.string.share_calendar_format_ics)
        )
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.share_calendar_format_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareCalendarAsPdf(scope)
                    1 -> shareCalendarAsIcs(scope)
                }
            }
            .show()
    }

    private fun shareCalendarAsPdf(scope: ShareScope) {
        if (PDFCreator.convertRangeToPDF(ctx, sc, scope.start, scope.end)) {
            val pdfSource = File(ctx.cacheDir, PDFHelper.PDF_FILE)
            val pdfTarget = File(ctx.cacheDir, buildShareCalendarFileName(scope, "pdf"))
            pdfSource.copyTo(pdfTarget, overwrite = true)
            PDFHelper.shareFile(ctx, pdfTarget, "application/pdf")
        } else {
            Snack.not(binding.root, getString(R.string.pdf_no_calendar))
        }
    }

    private fun shareCalendarAsIcs(scope: ShareScope) {
        val workDays = sc.workDays.getAll().filter { !it.day.isBefore(scope.start) && !it.day.isAfter(scope.end) }
        if (workDays.isEmpty()) {
            Snack.not(binding.root, getString(R.string.pdf_no_calendar))
            return
        }
        val events = workDays
            .map { workDay -> WorkDayDTO(workDay, sc.shifts.get(workDay.shiftId)) }
            .sortedWith(
                compareBy<WorkDayDTO>(
                    { it.wday.day },
                    { it.shift.startTime.timeInMinutes },
                    { it.shift.endDayOffset },
                    { it.shift.endTime.timeInMinutes },
                    { it.shift.id }
                )
            )

        val icsContent = CalendarIcsExporter.create(ctx, events)
        val fileName = buildShareCalendarFileName(scope, "ics")
        val targetFile = File(ctx.cacheDir, fileName)
        targetFile.writeText(icsContent)
        PDFHelper.shareFile(ctx, targetFile, "text/calendar")
    }

    private fun buildShareCalendarFileName(scope: ShareScope, extension: String): String {
        val workDays = sc.workDays.getAll()
        val fullRange: ClosedRange<LocalDate>? = if (workDays.isEmpty()) {
            null
        } else {
            workDays.minOf { it.day }..workDays.maxOf { it.day }
        }
        return ExportFileNameHelper.calendarFile(scope.start, scope.end, extension, fullRange)
    }

    override fun backPressed(): Boolean {
        return false
    }

    fun sendToViewModelReceiver() {
        val receivers = mutableListOf<ViewModelReceiver>()
        val list = mutableListOf<ViewModelReceiver>()
        findViewModelReceiver(binding.root, list)
        for (receiver in list) {
            receiver.receiveViewModel(lifecycle, calViewModel)
        }
    }

    private fun findViewModelReceiver(view: View, list: MutableList<ViewModelReceiver>) {
        if (view is ViewModelReceiver) {
            list.add(view)
        }
        if (view is ViewGroup) {
            for (child in view.children) {
                findViewModelReceiver(child, list)
            }
        }
    }

    private data class ShareScope(
        val start: LocalDate,
        val end: LocalDate
    )
}
