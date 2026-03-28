package de.nulide.shiftcal.ui.importer

import android.content.Context
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.legacy.factory.TimeFactory
import de.nulide.shiftcal.data.legacy.model.ShiftCalendar
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.SCRepoManager
import java.time.DateTimeException

class SwiftShiftImportHelper(private val context: Context) {

    fun looksLikeLegacyJson(json: String): Boolean {
        val normalized = json.lowercase()
        if (!normalized.contains("shifts")) return false
        if (!normalized.contains("calendar")) return false

        val hasLegacyMarkers = listOf(
            "short_name",
            "shiftblocks",
            "dataversion",
            "timetillalarm",
            "breakminutes"
        ).any { normalized.contains(it) }

        return hasLegacyMarkers
    }

    fun importLegacyJson(json: String): Boolean {
        if (!looksLikeLegacyJson(json)) {
            return false
        }
        val legacyCalendar = try {
            JIO.fromJSON(json, ShiftCalendar::class.java)
        } catch (_: Exception) {
            return false
        }

        if (legacyCalendar.shifts.isEmpty() && legacyCalendar.calendar.isEmpty() &&
            legacyCalendar.notes.isEmpty() && legacyCalendar.shiftBlocks.isEmpty()
        ) {
            return false
        }

        val sc = SCRepoManager.getInstance(context)
        if (sc.shifts.getAll().isNotEmpty() || sc.workDays.getAll().isNotEmpty() ||
            sc.monthNotes.getAll().isNotEmpty() || sc.shiftBlocks.getAll().isNotEmpty()
        ) {
            return false
        }

        if (legacyCalendar.name.isNotEmpty()) {
            sc.users.setName(legacyCalendar.name)
        }

        for (note in legacyCalendar.notes) {
            sc.monthNotes.set(note.year, note.month, note.note)
        }

        for (shift in legacyCalendar.shifts) {
            val start = ShiftTime.fromMinutes(shift.startTime.toInt())
            val end = ShiftTime.fromMinutes(shift.endTime.toInt())
            val preAlarmMinutes = if (shift.timeTillAlarm == -1) null else shift.timeTillAlarm
            sc.shifts.add(
                Shift(
                    shift.id,
                    0,
                    shift.name,
                    shift.short_name,
                    start,
                    end,
                    if (end.timeInMinutes <= start.timeInMinutes) 1 else 0,
                    null,
                    null,
                    null,
                    1.0,
                    shift.id,
                    shift.breakMinutes,
                    preAlarmMinutes,
                    shift.color,
                    shift.isToAlarm,
                    shift.isArchived
                )
            )
        }

        for (workDay in legacyCalendar.calendar) {
            try {
                val day = TimeFactory.convertCalendarDateToLocalDate(workDay.date)
                sc.workDays.add(
                    WorkDay(
                        workDay.id,
                        0,
                        day,
                        workDay.shift,
                        workDay.isDismissed,
                        workDay.icons.toMutableList(),
                        "",
                        0
                    )
                )
            } catch (_: DateTimeException) {
            }
        }

        for (block in legacyCalendar.shiftBlocks) {
            val newBlock = ShiftBlock(block.name)
            val entries = mutableListOf<ShiftBlockEntry>()
            for (entry in block.shiftBlockEntries) {
                entries.add(ShiftBlockEntry(entry.pos, entry.shift))
            }
            sc.shiftBlocks.add(newBlock, entries)
        }

        return true
    }
}
