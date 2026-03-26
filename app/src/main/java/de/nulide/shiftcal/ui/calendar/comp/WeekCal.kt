package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.WeekScrollListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.CompWeekShiftCalBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import de.nulide.shiftcal.ui.calendar.view.ShiftWeekDayBinder
import de.nulide.shiftcal.ui.calendar.view.UserNameRowHelper
import de.nulide.shiftcal.ui.calendar.view.WeekOfYearHelper
import de.nulide.shiftcal.ui.helper.TouchInterceptedLinearLayout
import de.nulide.shiftcal.utils.Runner
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WeekCal(context: Context, attrs: AttributeSet) :
    TouchInterceptedLinearLayout(context, attrs), ViewModelReceiver,
    WeekScrollListener {

    private val binding: CompWeekShiftCalBinding
    private val sc = SCRepoManager.getInstance(context)
    private val settings = SettingsRepository.getInstance(context)
    private lateinit var calViewModel: CalViewModel

    lateinit var firstLoadedDay: LocalDate
    lateinit var lastLoadedDay: LocalDate

    var ignoreOneScrolling = false

    init {
        val layoutInflator = LayoutInflater.from(context)
        binding = CompWeekShiftCalBinding.inflate(layoutInflator, this, true)
    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        setupCalendar()
        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            binding.familyCalendarView.notifyDateChanged(day.date)
        }
        calViewModel.register(lifecycle, calViewModel.scrollTo) { day ->
            binding.familyCalendarView.scrollToDate(day)
        }
        calViewModel.register(lifecycle, calViewModel.switchDisp) { disp ->
            if (disp == DISP.WEEK) {
                show()
            } else {
                hide()
            }
        }
        calViewModel.register(lifecycle, calViewModel.resume) {
            if (calViewModel.getCurrentDisp() == DISP.WEEK) {
                ignoreOneScrolling = true
                setupCalendar()
            }
        }
    }

    fun setupCalendar() {
        //Calendar
        val firstDayOfWeekIndex = settings.getInt(Settings.START_OF_WEEK)
        val daysOfWeek = daysOfWeek(DayOfWeek.of(firstDayOfWeekIndex + 1))
        val titlesContainer = binding.root.findViewById<ViewGroup>(R.id.weekTitlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title.subSequence(0, 1)
            }

        val shiftDayBinder = ShiftWeekDayBinder(
            context, calViewModel,
            binding.familyCalendarView
        )

        binding.familyCalendarView.dayBinder = shiftDayBinder

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
        firstLoadedDay = TimeFactory.convYearMonthToLocalDate(startMonth)
        lastLoadedDay = TimeFactory.convYearMonthToLocalDate(endMonth)
        binding.familyCalendarView.setup(firstLoadedDay, lastLoadedDay, daysOfWeek.first())
        Runner.runLate(100) {
            scrollTo(LocalDate.now())
        }
        binding.familyCalendarView.weekScrollListener = this

        UserNameRowHelper(context, binding)

    }

    private var currentDate = LocalDate.now()

    override fun invoke(week: Week) {
        if (!ignoreOneScrolling && isVisible) {
            if (week.days[0].date != currentDate) {
                this.currentDate = week.days[0].date
                val yearMonth = TimeFactory.convLocalDateToYearMonth(week.days[6].date)
                calViewModel.setCurrentMonth(yearMonth)
            }
            binding.root.findViewById<TextView>(R.id.familyWeekOfYearText).text =
                WeekOfYearHelper.getWeekOfYearText(week.days[0].date)
        }
        ignoreOneScrolling = false
        if (!calViewModel.getEditMode()) {
            var selectedDay = calViewModel.getLastSelectedDay()
            for (weekday in week.days) {
                val day = weekday.date
                if (day == selectedDay.date) {
                    calViewModel.setLastSelectedDay(selectedDay)
                    break
                }
            }
        }
    }

    fun simulateToday() {
        val today = LocalDate.now()
        val weekDay = WeekDay(today, WeekDayPosition.RangeDate)
        val week = Week(List(7) { weekDay })
        invoke(week)
    }


    fun scrollTo(scrollTo: LocalDate) {
        var scrollToDecision = scrollTo
        if (scrollTo.isBefore(firstLoadedDay)) {
            scrollToDecision = firstLoadedDay
        } else if (scrollTo.isAfter(lastLoadedDay)) {
            scrollToDecision = lastLoadedDay
        }
        binding.familyCalendarView.scrollToDate(scrollToDecision)
    }

    fun show() {
        visibility = VISIBLE
        ignoreOneScrolling = true
        setupCalendar()
        scrollTo(TimeFactory.convYearMonthToLocalDate(calViewModel.getCurrentMonth()))
    }

    fun hide() {
        visibility = GONE
    }

}