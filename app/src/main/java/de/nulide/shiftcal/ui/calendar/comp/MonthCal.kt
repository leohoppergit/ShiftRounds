package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.TypedValue
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthScrollListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.CompMonthShiftCalBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import de.nulide.shiftcal.ui.calendar.view.ShiftMonthDayBinder
import de.nulide.shiftcal.ui.calendar.view.WeekOfYearHelper
import de.nulide.shiftcal.ui.helper.TouchInterceptedLinearLayout
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MonthCal(context: Context, attrs: AttributeSet) :
    TouchInterceptedLinearLayout(context, attrs), ViewModelReceiver, MonthScrollListener {

    private val binding: CompMonthShiftCalBinding
    private val sc = SCRepoManager.getInstance(context)
    private val settings = SettingsRepository.getInstance(context)
    private lateinit var calViewModel: CalViewModel
    private lateinit var weekOfYearHelper: WeekOfYearHelper
    private var monthDayBinder: ShiftMonthDayBinder? = null

    var ignoreOneScrolling = false

    init {
        val layoutInflator = LayoutInflater.from(context)
        binding = CompMonthShiftCalBinding.inflate(layoutInflator, this, true)
    }

    fun setupCalendar() {
        weekOfYearHelper =
            WeekOfYearHelper(context, binding.root.findViewById(R.id.weekOfYearRow), settings)
        applyMonthTileScale()

        //Calendar
        val firstDayOfWeekIndex = settings.getInt(Settings.START_OF_WEEK)
        val daysOfWeek = daysOfWeek(DayOfWeek.of(firstDayOfWeekIndex + 1))
        val titlesContainer = binding.root.findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title.subSequence(0, 1)
            }

        monthDayBinder = ShiftMonthDayBinder(
            context, calViewModel,
            binding.calendarView, sc,
        )
        binding.calendarView.dayBinder = monthDayBinder
        val currentMonth = calViewModel.getCurrentMonth()
        var startMonth = currentMonth.minusMonths(12)
        val oldestWday = sc.workDays.getOldest()
        if (oldestWday != null) {
            val oldestShiftMonth = TimeFactory.convLocalDateToYearMonth(oldestWday.day)
            if (startMonth.isAfter(oldestShiftMonth)) {
                startMonth = oldestShiftMonth
            }
        }
        startMonth = startMonth.minusMonths(startMonth.monthValue.toLong() - 1)
        val endMonth = currentMonth.plusMonths(12 + (12 - currentMonth.monthValue.toLong()))
        calViewModel.setOldestMonth(startMonth)
        calViewModel.setNewestMonth(endMonth)
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)
        binding.calendarView.monthScrollListener = this
        invoke(CalendarMonth(currentMonth, listOf()))
    }

    private fun applyMonthTileScale() {
        val scale = settings.getInt(Settings.MONTH_TILE_SCALE).coerceIn(0, 2)
        val (horizontalMargin, panelPadding, minDayHeight, titleSizeSp) = when (scale) {
            0 -> MonthTileScaleConfig(
                horizontalMarginDp = 16,
                panelPaddingDp = 14,
                minDayHeightDp = 40,
                titleSizeSp = 17f
            )
            2 -> MonthTileScaleConfig(
                horizontalMarginDp = 8,
                panelPaddingDp = 8,
                minDayHeightDp = 52,
                titleSizeSp = 19f
            )
            else -> MonthTileScaleConfig(
                horizontalMarginDp = 12,
                panelPaddingDp = 10,
                minDayHeightDp = 46,
                titleSizeSp = 18f
            )
        }

        binding.calendarBoxView.setPadding(
            dp(panelPadding),
            dp(panelPadding),
            dp(panelPadding),
            dp(panelPadding)
        )
        (binding.calendarBoxView.layoutParams as? MarginLayoutParams)?.let { params ->
            params.marginStart = dp(horizontalMargin)
            params.marginEnd = dp(horizontalMargin)
            binding.calendarBoxView.layoutParams = params
        }

        binding.root.findViewById<ViewGroup>(R.id.titlesContainer).children
            .map { it as TextView }
            .forEach { textView ->
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSizeSp)
            }

        binding.calendarView.post {
            binding.calendarView.children.forEach { monthView ->
                applyMinHeightToDayCells(monthView, dp(minDayHeight))
            }
        }
    }

    private fun applyMinHeightToDayCells(root: View, minHeightPx: Int) {
        if (root.id == R.id.dayContainer) {
            root.minimumHeight = minHeightPx
        }
        if (root is ViewGroup) {
            root.children.forEach { child ->
                applyMinHeightToDayCells(child, minHeightPx)
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private data class MonthTileScaleConfig(
        val horizontalMarginDp: Int,
        val panelPaddingDp: Int,
        val minDayHeightDp: Int,
        val titleSizeSp: Float
    )

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel

        setupCalendar()

        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            setupCalendar()
            calViewModel.trigger(calViewModel.update, Unit)
            calViewModel.setLastSelectedDay(calViewModel.getLastSelectedDay())
        }

        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            binding.calendarView.notifyDateChanged(day.date)
        }

        calViewModel.register(
            lifecycle, calViewModel.dayUpdated
        ) { day ->
            monthDayBinder?.invalidateDate(day)
            binding.calendarView.notifyDateChanged(day, DayPosition.InDate)
            binding.calendarView.notifyDateChanged(
                day,
                DayPosition.MonthDate
            )
            binding.calendarView.notifyDateChanged(
                day,
                DayPosition.OutDate
            )
        }

        calViewModel.register(lifecycle, calViewModel.scrollTo) { day ->
            binding.calendarView.smoothScrollToMonth(day.yearMonth)
        }

        calViewModel.register(lifecycle, calViewModel.switchDisp) { disp ->
            if (disp == DISP.MONTH) {
                show()
            } else {
                hide()
            }
        }

        calViewModel.register(lifecycle, calViewModel.resume) {
            if (calViewModel.getCurrentDisp() == DISP.MONTH) {
                ignoreOneScrolling = true
                setupCalendar()
            }
        }
    }

    override fun invoke(newCurrentYearMonth: CalendarMonth) {
        if (!ignoreOneScrolling && isVisible) {
            calViewModel.setCurrentMonth(newCurrentYearMonth.yearMonth)
            weekOfYearHelper.updateWeekOfYearRow(newCurrentYearMonth)
        }
        ignoreOneScrolling = false
    }

    fun scrollTo(scrollTo: YearMonth) {
        var scrollToDecision = scrollTo
        if (scrollToDecision.isBefore(calViewModel.getOldestMonth())) {
            scrollToDecision = calViewModel.getOldestMonth()
        } else if (scrollToDecision.isAfter(calViewModel.getNewestMonth())) {
            scrollToDecision = calViewModel.getNewestMonth()
        }
        binding.calendarView.scrollToMonth(scrollToDecision)
    }

    fun show() {
        visibility = VISIBLE
        ignoreOneScrolling = true
        setupCalendar()
        scrollTo(calViewModel.getCurrentMonth())
    }

    fun hide() {
        visibility = GONE
    }

}
