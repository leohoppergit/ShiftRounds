package de.nulide.shiftcal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.nulide.shiftcal.data.dao.CalendarDao
import de.nulide.shiftcal.data.dao.MonthNoteDao
import de.nulide.shiftcal.data.dao.ShiftBlockDao
import de.nulide.shiftcal.data.dao.ShiftBlockEntryDao
import de.nulide.shiftcal.data.dao.ShiftDao
import de.nulide.shiftcal.data.dao.UserDao
import de.nulide.shiftcal.data.dao.WorkDayDao
import de.nulide.shiftcal.data.factory.Converter
import de.nulide.shiftcal.data.model.MonthNote
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftBlock
import de.nulide.shiftcal.data.model.ShiftBlockEntry
import de.nulide.shiftcal.data.model.ShiftCalendar
import de.nulide.shiftcal.data.model.User
import de.nulide.shiftcal.data.model.WorkDay

@Database(
    entities = [User::class, ShiftCalendar::class, Shift::class, MonthNote::class, ShiftBlockEntry::class, ShiftBlock::class, WorkDay::class],
    version = 7
)
@TypeConverters(value = [Converter::class])
abstract class ShiftSwiftDB : RoomDatabase() {

    companion object {
        const val DB_NAME = "shift_swift.db"
    }

    abstract fun calendarDao(): CalendarDao
    abstract fun shiftDao(): ShiftDao
    abstract fun workDayDao(): WorkDayDao
    abstract fun shiftBlockDao(): ShiftBlockDao
    abstract fun shiftBlockEntryDao(): ShiftBlockEntryDao
    abstract fun monthNoteDao(): MonthNoteDao
    abstract fun userDao(): UserDao

}
