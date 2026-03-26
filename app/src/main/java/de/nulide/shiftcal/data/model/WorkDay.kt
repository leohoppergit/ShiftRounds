package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "work_day",
    primaryKeys = ["id", "calendarId"],
    indices = [
        Index(value = ["calendarId"]),
        Index(value = ["calendarId", "shiftId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ShiftCalendar::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["calendarId", "id"],
            childColumns = ["calendarId", "shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkDay(
    var id: Int,
    var calendarId: Int,
    val day: LocalDate,
    val shiftId: Int,
    var alarmDismissed: Boolean,
    val icons: MutableList<Int>,
    var note: String,
    var overtimeMinutes: Int
) {
    constructor(
        day: LocalDate,
        shiftId: Int,
        alarmDismissed: Boolean,
        icons: MutableList<Int>,
        note: String = "",
        overtimeMinutes: Int = 0
    ) : this(0, 0, day, shiftId, alarmDismissed, icons, note, overtimeMinutes)

    constructor(day: LocalDate, shiftId: Int) : this(
        0,
        0,
        day,
        shiftId,
        false,
        mutableListOf(),
        "",
        0
    )

    constructor() : this(LocalDate.now(), 0)

}
