package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "month_note",
    primaryKeys = ["calendarId", "year", "month"],
    foreignKeys = [
        ForeignKey(
            entity = ShiftCalendar::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MonthNote(
    var calendarId: Int,
    val year: Int,
    val month: Int,
    var msg: String
) {
    constructor(year: Int, month: Int, msg: String) : this(0, year, month, msg)

    constructor() : this(0, 0, 0, "")
}