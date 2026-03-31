package de.nulide.shiftcal.ui.calendar.specialdate

import android.content.Context
import androidx.annotation.ColorInt
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.CalendarMarkerType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CalendarSpecialDateUiHelper {

    private val rangeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @ColorInt
    fun getColor(context: Context, type: String): Int {
        return context.getColor(
            when (type) {
                CalendarMarkerType.PUBLIC_HOLIDAY -> R.color.calendarSpecialDateHoliday
                CalendarMarkerType.SCHOOL_BREAK -> R.color.calendarSpecialDateSchoolBreak
                CalendarMarkerType.KINDERGARTEN_CLOSURE -> R.color.calendarSpecialDateKindergartenClosure
                else -> R.color.calendarSpecialDateCustom
            }
        )
    }

    fun getTypeLabel(context: Context, type: String): String {
        return context.getString(
            when (type) {
                CalendarMarkerType.PUBLIC_HOLIDAY -> R.string.calendar_special_date_type_public_holiday
                CalendarMarkerType.SCHOOL_BREAK -> R.string.calendar_special_date_type_school_break
                CalendarMarkerType.KINDERGARTEN_CLOSURE -> R.string.calendar_special_date_type_kindergarten_closure
                else -> R.string.calendar_special_date_type_custom
            }
        )
    }

    fun formatRange(start: LocalDate, end: LocalDate): String {
        return if (start == end) {
            start.format(rangeFormatter)
        } else {
            "${start.format(rangeFormatter)} - ${end.format(rangeFormatter)}"
        }
    }
}
