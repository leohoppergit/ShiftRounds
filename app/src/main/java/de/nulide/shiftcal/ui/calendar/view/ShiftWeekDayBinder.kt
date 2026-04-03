package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.ui.calendar.CalViewModel
import java.util.Locale

class ShiftWeekDayBinder(
    val context: Context,
    val calViewModel: CalViewModel,
    val calendarView: WeekCalendarView
) : WeekDayBinder<WeekDayViewContainer>,
    OnDayClickedListener {

    private var defaultTextColor = -1

    val sc = SCRepoManager.getInstance(context)

    override fun bind(container: WeekDayViewContainer, data: WeekDay) {
        if (defaultTextColor == -1) {
            defaultTextColor = container.dayTextView.textColors.defaultColor
        }

        container.day = CalendarDay(data.date, DayPosition.MonthDate)
        container.dayTextView.text = String.format(Locale.getDefault(), "%d", data.date.dayOfMonth)
        container.calViewModel = calViewModel
        container.clearOnDayClickedListener()
        container.addOnDayClickedListener(this)

        if (calViewModel.getLastSelectedDay().date == data.date) {
            val selectorDrawable =
                ContextCompat.getDrawable(context, R.drawable.shift_rounds_selected_day_outline)
            container.root.background = selectorDrawable
        } else {
            container.root.background = null
        }


        var shiftC = 0
        var sizeAfterSC = shiftC + 1
        if (sc.fromLocal { sc.workDays.hasDualShift() }) {
            sizeAfterSC += 1
        }
        var sizeAfterFC = sizeAfterSC + 1
        if (sc.fromNet { sc.workDays.hasDualShift() } == true) {
            sizeAfterFC += 1
        }
        container.clear()

        if (sc.fromLocal { sc.workDays.hasWork(data.date) }) {
            val shifts = sc.fromLocal { sc.shifts.getOn(data.date) }
            for (shift in shifts) {
                container.addShift(context, shift)
                shiftC += 1
            }
        }
        while (shiftC < sizeAfterSC) {
            container.addShift(context, null)
            shiftC += 1
        }

        container.addDivider(context)

        if (sc.fromNet { sc.workDays.hasWork(data.date) } == true) {
            val shifts = sc.fromNet { sc.shifts.getOn(data.date) }
            if (shifts != null) {
                for (shift in shifts) {
                    container.addShift(context, shift)
                    shiftC += 1
                }
            }
        }
        while (shiftC < sizeAfterFC) {
            container.addShift(context, null)
            shiftC += 1
        }
    }


    override fun create(view: View): WeekDayViewContainer = WeekDayViewContainer(view)

    override fun onDayClicked(date: CalendarDay) {
        val lastSelectedDay = calViewModel.getLastSelectedDay()
        if (calViewModel.getEditMode() && lastSelectedDay.date == date.date) {
            calViewModel.trigger(calViewModel.daySelected, date)
            return
        }
        calViewModel.setLastSelectedDay(date)
        calendarView.notifyDateChanged(lastSelectedDay.date)
        calendarView.notifyDateChanged(date.date)
    }

}
