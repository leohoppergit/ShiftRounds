package de.nulide.shiftcal.data.calendar

import java.time.LocalDate

data class CalendarSpecialDate(
    val id: String,
    val name: String,
    val type: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
