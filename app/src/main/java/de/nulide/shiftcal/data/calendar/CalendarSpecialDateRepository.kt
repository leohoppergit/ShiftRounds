package de.nulide.shiftcal.data.calendar

import android.content.Context
import de.nulide.shiftcal.data.settings.CalendarMarker
import de.nulide.shiftcal.data.settings.HolidayRegion
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import java.time.LocalDate

class CalendarSpecialDateRepository(
    context: Context,
    private val settings: SettingsRepository
) {

    private val austrianHolidayCache = mutableMapOf<Int, List<CalendarSpecialDate>>()
    private val schoolBreakProvider = AustriaSchoolBreakProvider(context)

    fun getEntriesOn(date: LocalDate): List<CalendarSpecialDate> {
        val entries = mutableListOf<CalendarSpecialDate>()

        if (settings.getBoolean(Settings.HOLIDAYS_ENABLED)) {
            entries.addAll(getHolidayEntriesOn(date))
        }

        if (settings.getBoolean(Settings.SCHOOL_BREAKS_ENABLED)) {
            entries.addAll(getSchoolBreakEntriesOn(date))
        }

        entries.addAll(
            settings.getCalendarMarkers()
                .mapNotNull { it.toCalendarSpecialDate() }
                .filter { date >= it.startDate && date <= it.endDate }
        )

        return entries.sortedWith(compareBy<CalendarSpecialDate>({ it.type }, { it.name }))
    }

    private fun getHolidayEntriesOn(date: LocalDate): List<CalendarSpecialDate> {
        return when (settings.getHolidayRegion()) {
            HolidayRegion.AUSTRIA_NATIONAL -> getAustriaHolidayEntriesOn(date)
            else -> emptyList()
        }
    }

    private fun getAustriaHolidayEntriesOn(date: LocalDate): List<CalendarSpecialDate> {
        val yearEntries = austrianHolidayCache.getOrPut(date.year) {
            AustriaHolidayProvider.getForYear(date.year)
        }
        return yearEntries.filter { date >= it.startDate && date <= it.endDate }
    }

    private fun getSchoolBreakEntriesOn(date: LocalDate): List<CalendarSpecialDate> {
        return schoolBreakProvider.getForSelectedStates(settings.getSchoolBreakStates())
            .filter { date >= it.startDate && date <= it.endDate }
    }

    private fun CalendarMarker.toCalendarSpecialDate(): CalendarSpecialDate? {
        val start = runCatching { LocalDate.parse(startDate) }.getOrNull() ?: return null
        val end = runCatching { LocalDate.parse(endDate) }.getOrNull() ?: return null
        if (name.isBlank()) return null
        return CalendarSpecialDate(
            id = id,
            name = name,
            type = type,
            startDate = start,
            endDate = end
        )
    }
}
