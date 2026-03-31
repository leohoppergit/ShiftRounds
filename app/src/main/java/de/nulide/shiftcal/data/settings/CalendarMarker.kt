package de.nulide.shiftcal.data.settings

data class CalendarMarker(
    val id: String = "",
    val name: String = "",
    val type: String = CalendarMarkerType.CUSTOM,
    val startDate: String = "",
    val endDate: String = ""
)

object CalendarMarkerType {
    const val PUBLIC_HOLIDAY = "PUBLIC_HOLIDAY"
    const val SCHOOL_BREAK = "SCHOOL_BREAK"
    const val KINDERGARTEN_CLOSURE = "KINDERGARTEN_CLOSURE"
    const val CUSTOM = "CUSTOM"
}

object HolidayRegion {
    const val AUSTRIA_NATIONAL = "AT_NATIONAL"
}
