package de.nulide.shiftcal.data.repository

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.repository.task.CalendarPostProcessingScheduler
import de.nulide.shiftcal.data.repository.wrapper.FullBackupDTO
import de.nulide.shiftcal.data.repository.wrapper.ShiftCalendarDTO
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.sync.SyncHandler
import de.nulide.shiftcal.utils.SingletonHolder

class SCRepoManager(ctx: Context) {

    companion object :
        SingletonHolder<SCRepoManager, Context>(::SCRepoManager)

    var curCalId: Int
    var familyMode: Boolean

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE work_day ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE work_day ADD COLUMN overtimeMinutes INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift ADD COLUMN endDayOffset INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                "UPDATE shift SET endDayOffset = CASE " +
                    "WHEN endTime <= startTime THEN 1 ELSE 0 END"
            )
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift ADD COLUMN customBalanceMinutes INTEGER")
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE shift SET sortOrder = id")
        }
    }

    private val migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift ADD COLUMN specialAccountId TEXT")
            db.execSQL("ALTER TABLE shift ADD COLUMN specialAccountMinutes INTEGER")
        }
    }

    private val migration6To7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift ADD COLUMN overtimeMultiplier REAL NOT NULL DEFAULT 1.0")
        }
    }

    private val db = Room.databaseBuilder(ctx, ShiftSwiftDB::class.java, ShiftSwiftDB.DB_NAME)
        .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5, migration5To6, migration6To7)
        .allowMainThreadQueries().build()

    val calendar = CalendarRepository(db, this)
    val workDays = WorkDayRepository(db, this)
    val shifts = ShiftRepository(db, this)
    val shiftBlocks = ShiftBlockRepository(db, this)
    val monthNotes = MonthNoteRepository(db, this)
    val users = UserRepository(db, this)

    private val postProcessingScheduler = CalendarPostProcessingScheduler(this, ctx)

    init {
        curCalId = calendar.getLocal()
        familyMode = false
    }

    fun switchCalendar(): Boolean {
        if (familyMode) {
            curCalId = calendar.getLocal()
            familyMode = !familyMode
            return true
        } else {
            val familyCalId = calendar.getNonLocal()
            if (familyCalId != null) {
                curCalId = familyCalId
                familyMode = !familyMode
                return true
            }
        }
        return false
    }

    fun switchToLocal() {
        if (familyMode) {
            switchCalendar()
        }
    }

    fun switchToNet(): Boolean {
        if (!familyMode) {
            switchCalendar()
            return true
        }
        return false
    }

    fun <T> fromLocal(action: () -> T): T {
        val originalCalId = curCalId
        curCalId = calendar.getLocal()
        return try {
            action()
        } finally {
            curCalId = originalCalId
        }
    }

    fun <T> fromNet(action: () -> T): T? {
        val netId = calendar.getNonLocal()
        return if (netId != null) {
            val originalCalId = curCalId
            curCalId = netId
            try {
                action()
            } finally {
                curCalId = originalCalId
            }
        } else {
            null
        }
    }

    fun hasNetAcc(): Boolean {
        return users.getShared().isNotEmpty()
    }

    fun asJSON(): String {
        val scDTO = ShiftCalendarDTO(shifts.getAll(), monthNotes.getAll(), workDays.getAll())
        return JIO.toJSON(scDTO)
    }

    fun fromJSON(netUuid: String, json: String) {
        val scDTO = JIO.fromJSON(json, ShiftCalendarDTO::class.java)
        val netCalId = calendar.getFromNet(netUuid)
        calendar.deleteFromNet(netUuid)
        val user = users.getSubscribed()
        if (user != null && user.netUuid == netUuid) {
            val calUpdId = calendar.addNonLocal(user.id)
            for (shift in scDTO.shifts) {
                shift.calendarId = calUpdId
                db.shiftDao().insert(shift)
            }
            for (monthNote in scDTO.monthNotes) {
                monthNote.calendarId = calUpdId
                db.monthNoteDao().insert(monthNote)
            }
            for (workDay in scDTO.workDays) {
                workDay.calendarId = calUpdId
                db.workDayDao().insert(workDay)
            }
            if (netCalId != null && netCalId == curCalId) {
                curCalId = calUpdId
            }
        }
    }

    fun postDataChange() {
        postProcessingScheduler.startJob()
    }

    fun postProcess(ctx: Context) {
        SyncHandler.sync(ctx)
        SettingsRepository.getInstance(ctx).set(Settings.LAST_POST_PROCESS_FAILED, false)

    }

    fun createFullBackup(settingsRepository: SettingsRepository, appName: String, exportedAt: String): FullBackupDTO {
        return FullBackupDTO(
            backupVersion = 1,
            exportedAt = exportedAt,
            appName = appName,
            userName = users.getName(),
            settings = settingsRepository.exportSettings(),
            shifts = shifts.getAll().filter { it.id >= 0 },
            monthNotes = monthNotes.getAll(),
            workDays = workDays.getAll(),
            shiftBlocks = shiftBlocks.getAll()
        )
    }

    fun restoreLocalBackup(backup: FullBackupDTO, settingsRepository: SettingsRepository) {
        val localCalId = calendar.getLocal()

        db.shiftBlockEntryDao().deleteAll(localCalId)
        db.shiftBlockDao().deleteAll(localCalId)
        db.workDayDao().deleteAll(localCalId)
        db.monthNoteDao().deleteAll(localCalId)
        db.shiftDao().deleteAll(localCalId)

        backup.shifts
            .filter { it.id >= 0 }
            .sortedWith(compareBy<de.nulide.shiftcal.data.model.Shift>({ it.sortOrder }, { it.id }))
            .forEach { shift ->
                db.shiftDao().insert(shift.copy(calendarId = localCalId))
            }

        backup.monthNotes.forEach { monthNote ->
            db.monthNoteDao().insert(monthNote.copy(calendarId = localCalId))
        }

        backup.workDays
            .sortedWith(compareBy({ it.day }, { it.id }))
            .forEach { workDay ->
                db.workDayDao().insert(workDay.copy(calendarId = localCalId))
            }

        backup.shiftBlocks.forEach { shiftBlockDTO ->
            val block = shiftBlockDTO.block.copy(calendarId = localCalId)
            db.shiftBlockDao().add(block)
            shiftBlockDTO.entries
                .sortedBy { it.pos }
                .forEach { entry ->
                    db.shiftBlockEntryDao().add(
                        ShiftBlockEntry(
                            shiftBlockId = block.id,
                            id = 0,
                            calendarId = localCalId,
                            pos = entry.pos,
                            shiftId = entry.shiftId
                        )
                    )
                }
        }

        settingsRepository.importSettings(backup.settings)
        users.setName(backup.userName)
        curCalId = localCalId
        familyMode = false
        postDataChange()
    }

}
