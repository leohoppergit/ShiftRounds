package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_block_entry",
    indices = [
        Index(value = ["calendarId"]),
        Index(value = ["shiftId", "calendarId"]),
        Index(value = ["shiftBlockId", "calendarId"])
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
            parentColumns = ["id", "calendarId"],
            childColumns = ["shiftId", "calendarId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShiftBlock::class,
            parentColumns = ["id", "calendarId"],
            childColumns = ["shiftBlockId", "calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShiftBlockEntry(
    var shiftBlockId: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var calendarId: Int,
    val pos: Int,
    val shiftId: Int
) {
    constructor(pos: Int, shiftId: Int) : this(0, 0, 0, pos, shiftId)
}