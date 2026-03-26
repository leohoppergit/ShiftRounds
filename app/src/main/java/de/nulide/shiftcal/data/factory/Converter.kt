package de.nulide.shiftcal.data.factory

import androidx.room.TypeConverter
import de.nulide.shiftcal.data.model.ShiftTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class Converter {

    @TypeConverter
    fun fromTimestamp(date: Long): LocalDate {
        return LocalDate.ofEpochDay(date)
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @TypeConverter
    fun localDateTimefromTimestamp(date: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(
            date,
            0,
            ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())
        )
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): Long {
        return date.toEpochSecond(ZoneId.systemDefault().rules.getOffset(date))
    }

    @TypeConverter
    fun iconsListToString(icons: MutableList<Int>): String {
        return icons.joinToString(",")
    }

    @TypeConverter
    fun stringToIconsList(icons: String): MutableList<Int> {
        if (icons.isEmpty()) {
            return mutableListOf()
        }
        return icons.split(",").map { a -> Integer.valueOf(a) }.toMutableList()
    }

    @TypeConverter
    fun shiftTimefromMinutes(time: Int): ShiftTime {
        return ShiftTime.fromMinutes(time)
    }

    @TypeConverter
    fun minutesFromShiftTIme(time: ShiftTime): Int {
        return time.timeInMinutes
    }

}