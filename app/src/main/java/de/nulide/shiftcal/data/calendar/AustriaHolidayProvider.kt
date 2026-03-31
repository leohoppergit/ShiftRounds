package de.nulide.shiftcal.data.calendar

import de.nulide.shiftcal.data.settings.CalendarMarkerType
import java.time.LocalDate
import java.time.Month

object AustriaHolidayProvider {

    fun getForYear(year: Int): List<CalendarSpecialDate> {
        val easterSunday = calculateEasterSunday(year)
        return listOf(
            singleDay("at_$year-01-01", "Neujahr", LocalDate.of(year, Month.JANUARY, 1)),
            singleDay("at_$year-01-06", "Heilige Drei Koenige", LocalDate.of(year, Month.JANUARY, 6)),
            singleDay("at_${year}_easter_monday", "Ostermontag", easterSunday.plusDays(1)),
            singleDay("at_$year-05-01", "Staatsfeiertag", LocalDate.of(year, Month.MAY, 1)),
            singleDay("at_${year}_ascension", "Christi Himmelfahrt", easterSunday.plusDays(39)),
            singleDay("at_${year}_whit_monday", "Pfingstmontag", easterSunday.plusDays(50)),
            singleDay("at_${year}_corpus_christi", "Fronleichnam", easterSunday.plusDays(60)),
            singleDay("at_$year-08-15", "Maria Himmelfahrt", LocalDate.of(year, Month.AUGUST, 15)),
            singleDay("at_$year-10-26", "Nationalfeiertag", LocalDate.of(year, Month.OCTOBER, 26)),
            singleDay("at_$year-11-01", "Allerheiligen", LocalDate.of(year, Month.NOVEMBER, 1)),
            singleDay("at_$year-12-08", "Maria Empfaengnis", LocalDate.of(year, Month.DECEMBER, 8)),
            singleDay("at_$year-12-25", "Christtag", LocalDate.of(year, Month.DECEMBER, 25)),
            singleDay("at_$year-12-26", "Stefanitag", LocalDate.of(year, Month.DECEMBER, 26)),
        )
    }

    private fun singleDay(id: String, name: String, date: LocalDate): CalendarSpecialDate {
        return CalendarSpecialDate(
            id = id,
            name = name,
            type = CalendarMarkerType.PUBLIC_HOLIDAY,
            startDate = date,
            endDate = date
        )
    }

    // Meeus/Jones/Butcher Gregorian algorithm
    private fun calculateEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
