package de.nulide.shiftcal.ui.settings.export

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object ExportFileNameHelper {

    private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

    fun settingsJson(now: LocalDateTime = LocalDateTime.now()): String =
        "ShiftRounds_Settings_${now.format(timestampFormatter)}.json"

    fun shiftsJson(now: LocalDateTime = LocalDateTime.now()): String =
        "ShiftRounds_Shifts_${now.format(timestampFormatter)}.json"

    fun backupJson(now: LocalDateTime = LocalDateTime.now()): String =
        "ShiftRounds_Backup_${now.format(timestampFormatter)}.json"

    fun backupDiagnosticsTxt(now: LocalDateTime = LocalDateTime.now()): String =
        "ShiftRounds_Backup_Diagnostics_${now.format(timestampFormatter)}.txt"

    fun calendarFile(start: LocalDate, end: LocalDate, extension: String, fullCalendarRange: ClosedRange<LocalDate>? = null): String {
        val isFullCalendar = fullCalendarRange != null &&
            start == fullCalendarRange.start &&
            end == fullCalendarRange.endInclusive

        return when {
            isFullCalendar -> "ShiftRounds_Full_Calendar.$extension"
            isExactMonth(start, end) -> {
                val monthName = start.month.name.lowercase(Locale.ENGLISH)
                    .replaceFirstChar { it.titlecase(Locale.ENGLISH) }
                "ShiftRounds_${monthName}_${start.year}.$extension"
            }
            else -> "ShiftRounds_${start}_to_${end}.$extension"
        }
    }

    private fun isExactMonth(start: LocalDate, end: LocalDate): Boolean {
        val yearMonth = YearMonth.from(start)
        return start == yearMonth.atDay(1) && end == yearMonth.atEndOfMonth()
    }
}
