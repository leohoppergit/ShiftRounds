package de.nulide.shiftcal.data.migration

import android.content.Context
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.legacy.factory.TimeFactory
import de.nulide.shiftcal.data.legacy.repository.CommonCalendarRepository
import de.nulide.shiftcal.data.legacy.repository.ShiftCalendarRepository
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import java.time.DateTimeException

class DBMigrator(val ctx: Context) {

    val settings = SettingsRepository.getInstance(ctx)

    fun migrate() {
        if (!settings.getBoolean(Settings.DB_MIGRATION_COMPLETED)) {
            ctx.deleteDatabase(ShiftSwiftDB.DB_NAME)
            migrateLocalData()
        }
        if (!settings.getBoolean(Settings.DB_MIGRATION_SHARED_RETRIEVED_COMPLETED)) {
            settings.set(Settings.DB_MIGRATION_SHARED_RETRIEVED_COMPLETED, true)
            settings.resetSharingAccount()
        }
    }

    fun migrateLocalData() {
        val sc = SCRepoManager.getInstance(ctx)

        val old = ShiftCalendarRepository.getInstance(ctx)

        //Add local calendar
        addCalendar(sc, old)

        settings.set(Settings.DB_MIGRATION_COMPLETED, true)
    }

    fun addCalendar(sc: SCRepoManager, old: CommonCalendarRepository) {
        if (old.shiftCalendar.name.isNotEmpty()) {
            sc.users.setName(old.shiftCalendar.name)
        }
        for (n in old.shiftCalendar.notes) {
            sc.monthNotes.set(n.year, n.month, n.note)
        }
        for (s in old.shiftCalendar.shifts) {
            val start = ShiftTime.fromMinutes(s.startTime.toInt())
            val end = ShiftTime.fromMinutes(s.endTime.toInt())
            val preAlarmMinutes = if (s.timeTillAlarm == -1) null else s.timeTillAlarm
            sc.shifts.add(
                Shift(
                    s.id, 0, s.name, s.short_name,
                    start, end, if (end.timeInMinutes <= start.timeInMinutes) 1 else 0, null, s.id, s.breakMinutes,
                    preAlarmMinutes, s.color, s.isToAlarm, s.isArchived
                )
            )
        }
        for (w in old.shiftCalendar.calendar) {
            try {
                val day = TimeFactory.convertCalendarDateToLocalDate(w.date)
                val icons = w.icons.toMutableList()
                sc.workDays.add(WorkDay(w.id, 0, day, w.shift, w.isDismissed, icons, "", 0))
            } catch (de: DateTimeException) {
                de.printStackTrace()
            }
        }
        for (b in old.shiftCalendar.shiftBlocks) {
            val nblock = ShiftBlock(b.name)
            val entries = mutableListOf<ShiftBlockEntry>()
            for (e in b.shiftBlockEntries) {
                entries.add(ShiftBlockEntry(e.pos, e.shift))
            }
            sc.shiftBlocks.add(nblock, entries)
        }
    }

}
