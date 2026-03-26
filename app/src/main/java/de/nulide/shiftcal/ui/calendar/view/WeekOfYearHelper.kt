package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarMonth
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import java.time.LocalDate
import java.time.temporal.IsoFields

class WeekOfYearHelper(
    val context: Context,
    val weekOfYearRow: LinearLayout,
    val settings: SettingsRepository
) {

    companion object {
        fun getWeekOfYearText(date: LocalDate): String {
            // Using IsoFields ensures we follow the ISO-8601 standard,
            // which correctly handles the 53/1 week transition at year boundaries.
            return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()
        }
    }

    fun updateWeekOfYearRow(currentYearMonth: CalendarMonth) {
        if (settings.getBoolean(Settings.WEEK_OF_YEAR)) {
            weekOfYearRow.visibility = View.VISIBLE
            val numberOfRows = currentYearMonth.weekDays.size

            weekOfYearRow.weightSum = numberOfRows.toFloat() + 1
            val weekOfYearContainer =
                weekOfYearRow.findViewById<LinearLayout>(R.id.weekOfYearContainer)
            weekOfYearContainer.removeAllViewsInLayout()
            weekOfYearContainer.weightSum = numberOfRows.toFloat()
            val layoutParams = weekOfYearContainer.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = numberOfRows.toFloat()
            weekOfYearContainer.layoutParams = layoutParams
            val inflater = LayoutInflater.from(context)
            for (i in 0..numberOfRows - 1) {
                val view = inflater.inflate(
                    R.layout.calendar_week_of_year_text,
                    weekOfYearContainer,
                    false
                ) as TextView
                val weeks = currentYearMonth.weekDays
                view.text = getWeekOfYearText(weeks[i][0].date)
                weekOfYearContainer.addView(view)
            }
        } else {
            weekOfYearRow.visibility = View.GONE
        }

    }
}