package de.nulide.shiftcal.ui.settings.export

import android.content.Context
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CalendarIcsExporter {

    private val localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val utcDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun create(context: Context, events: List<WorkDayDTO>): String {
        val stamp = LocalDateTime.now()
        val builder = StringBuilder()
        builder.appendLine("BEGIN:VCALENDAR")
        builder.appendLine("VERSION:2.0")
        builder.appendLine("PRODID:-//ShiftRounds//EN")
        builder.appendLine("CALSCALE:GREGORIAN")
        builder.appendLine("METHOD:PUBLISH")

        for (event in events) {
            val shift = event.shift
            val workDay = event.wday
            val start = LocalDateTime.of(
                workDay.day.year,
                workDay.day.monthValue,
                workDay.day.dayOfMonth,
                shift.startTime.hour,
                shift.startTime.minute
            )
            val end = LocalDateTime.of(
                workDay.day.year,
                workDay.day.monthValue,
                workDay.day.dayOfMonth,
                shift.endTime.hour,
                shift.endTime.minute
            ).plusDays(shift.endDayOffset.toLong())

            val balanceText = when {
                shift.customBalanceMinutes == null -> context.getString(R.string.export_event_balance_default)
                shift.customBalanceMinutes < 0 -> context.getString(R.string.export_event_balance_negative)
                else -> context.getString(R.string.export_event_balance_positive)
            }
            val noteText = workDay.note.ifBlank { context.getString(R.string.export_event_note_none) }
            val adjustmentText = if (workDay.overtimeMinutes == 0) {
                context.getString(R.string.export_event_adjustment_none)
            } else {
                formatDuration(workDay.overtimeMinutes)
            }

            builder.appendLine("BEGIN:VEVENT")
            builder.appendLine("UID:${workDay.calendarId}-${workDay.id}-${shift.id}@shiftrounds")
            builder.appendLine("DTSTAMP:${stamp.format(utcDateTimeFormatter)}")
            builder.appendLine("DTSTART:${start.format(localDateTimeFormatter)}")
            builder.appendLine("DTEND:${end.format(localDateTimeFormatter)}")
            builder.appendLine("SUMMARY:${escapeText(context.getString(R.string.export_event_summary, shift.name))}")
            builder.appendLine(
                "DESCRIPTION:${escapeText(context.getString(R.string.export_event_description, shift.shortName, balanceText, noteText, adjustmentText))}"
            )
            builder.appendLine("END:VEVENT")
        }

        builder.appendLine("END:VCALENDAR")
        return builder.toString()
    }

    private fun formatDuration(minutes: Int): String {
        val sign = if (minutes < 0) "-" else "+"
        val absolute = kotlin.math.abs(minutes)
        return "$sign${absolute / 60}:${(absolute % 60).toString().padStart(2, '0')} h"
    }

    private fun escapeText(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace(",", "\\,")
            .replace(";", "\\;")
    }
}
