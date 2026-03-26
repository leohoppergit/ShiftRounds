package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_calendar",
    indices = [
        Index(value = ["owner"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["owner"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShiftCalendar(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val owner: Int
)