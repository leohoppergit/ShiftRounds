package de.nulide.shiftcal.data.factory

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class TimeFactory {

    companion object {
        fun convDaysAndMinutesToLocalDateTime(days: Long, minutes: Int): LocalDateTime {
            val date = LocalDate.ofEpochDay(days)
            return convLocalDateAndMinutesToLocalDateTime(date, minutes)
        }

        fun convLocalDateAndMinutesToLocalDateTime(day: LocalDate, minutes: Int): LocalDateTime {
            return day.atStartOfDay().plusMinutes(minutes.toLong())
        }

        fun getNow(): Date {
            return Date()
        }

        fun convLocalDateToYearMonth(day: LocalDate): YearMonth {
            return YearMonth.from(day)
        }

        fun convYearMonthToLocalDate(yearMonth: YearMonth): LocalDate {
            return LocalDate.of(yearMonth.year, yearMonth.month, 1)
        }

        fun getTimeUntil(days: Long, minutes: Int): Pair<Long, Long> {
            val dateTime = TimeFactory.convDaysAndMinutesToLocalDateTime(days, minutes)
            return getTimeUntil(dateTime)
        }

        fun getTimeUntil(targetTime: LocalDateTime): Pair<Long, Long> {
            val now = LocalDateTime.now()
            val duration = Duration.between(now, targetTime)

            val time = duration.toMinutes()
            val hours = time / 60
            val minutes = time % 60

            return Pair(hours, minutes)
        }

        fun localDateTimeToMillis(time: LocalDateTime): Long {
            return time.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

    }
}