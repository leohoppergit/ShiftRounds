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
import de.nulide.monthyearpicker.MonthYearPickerBuilder
import de.nulide.monthyearpicker.OnMonthYearSelectedListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
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
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.YearMonth


class ShiftCalendarFragment : SFragment(),
    View.OnClickListener, OnMonthYearSelectedListener {

    private lateinit var binding: FragmentShiftCalendarBinding

    override val fragmentName = "calendar"

    lateinit var sc: SCRepoManager
    lateinit var settings: SettingsRepository

    lateinit var calViewModel: CalViewModel

    private lateinit var goToButton: View
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
        goToButton = createMenuItem(menu, R.id.goTo, R.drawable.ic_goto)
        shareButton = createMenuItem(menu, R.id.share, R.drawable.ic_share)
    }

    fun createMenuItem(menu: Menu?, itemRes: Int, iconRes: Int): View {
        val actionView =
            layoutInflater.inflate(R.layout.action_view_menu_item, null) as FrameLayout
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
            shareButton -> {
                if (PDFCreator.convertMonthToPDF(ctx, sc, calViewModel.getCurrentMonth())) {
                    PDFHelper.sharePDF(ctx)
                } else {
                    Snack.not(binding.root, getString(R.string.pdf_no_calendar))
                }
            }

            goToButton -> {
                showMonthYearPicker()
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

    override fun backPressed(): Boolean {
        val now = YearMonth.now()
        if (calViewModel.getCurrentMonth() == now) {
            return false
        } else {
            calViewModel.trigger(calViewModel.scrollTo, LocalDate.now())
        }
        return true
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

}
