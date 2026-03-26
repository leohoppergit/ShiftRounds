package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "shift_block",
    primaryKeys = ["id", "calendarId"],
    indices = [
        Index(value = ["calendarId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ShiftCalendar::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShiftBlock(
    var id: Int,
    var calendarId: Int,
    var name: String,
) {
    constructor(name: String) : this(0, 0, name)
}