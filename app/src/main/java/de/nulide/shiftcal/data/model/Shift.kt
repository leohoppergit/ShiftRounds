package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.math.roundToInt

@Entity(
    tableName = "shift",
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
data class Shift(
    val id: Int,
    var calendarId: Int,
    val name: String,
    val shortName: String,
    val startTime: ShiftTime,
    val endTime: ShiftTime,
    val endDayOffset: Int,
    val customBalanceMinutes: Int?,
    val specialAccountId: String?,
    val specialAccountMinutes: Int?,
    val overtimeMultiplier: Double,
    var sortOrder: Int,
    val breakMinutes: Int,
    val alarmLeadMinutes: Int?,
    val color: Int,
    val toAlarm: Boolean,
    var archived: Boolean
) {
    constructor() : this(0, 0, "", "", ShiftTime(0, 0), ShiftTime(0, 0), 0, null, null, null, 1.0, 0, 0, null, 0, false, false)

    constructor(
        name: String,
        shortName: String,
        startTime: ShiftTime,
        endTime: ShiftTime,
        endDayOffset: Int,
        customBalanceMinutes: Int?,
        specialAccountId: String?,
        specialAccountMinutes: Int?,
        overtimeMultiplier: Double,
        sortOrder: Int,
        breakMinutes: Int,
        alarmLeadMinutes: Int?,
        color: Int,
        toAlarm: Boolean,
        archived: Boolean
    ) :
            this(
                0,
                0,
                name,
                shortName,
                startTime,
                endTime,
                endDayOffset,
                customBalanceMinutes,
                specialAccountId,
                specialAccountMinutes,
                overtimeMultiplier,
                sortOrder,
                breakMinutes,
                alarmLeadMinutes,
                color,
                toAlarm,
                archived
            )

    val isOverMidnight: Boolean
        get() = endDayOffset > 0

    val hasAlarmLeadMinutes: Boolean
        get() = alarmLeadMinutes != null

    val durationMinutes: Int
        get() = (endDayOffset * 24 * 60) + endTime.timeInMinutes - startTime.timeInMinutes

    val effectiveWorkMinutes: Int
        get() = if (specialAccountId != null && specialAccountMinutes != null) 0 else customBalanceMinutes ?: (durationMinutes - breakMinutes)

    fun adjustedOvertimeMinutes(rawMinutes: Int): Int {
        return if (rawMinutes > 0) {
            (rawMinutes * overtimeMultiplier).roundToInt()
        } else {
            rawMinutes
        }
    }

    override fun toString(): String {
        val s = StringBuilder()
        s.append(id).append("-").append(calendarId).append("\n").append(name).append("\n")
            .append(shortName)
            .append(startTime.toString()).append("|").append(endTime).append("|")
            .append(endDayOffset).append("|")
            .append(customBalanceMinutes).append("|")
            .append(specialAccountId).append("|")
            .append(specialAccountMinutes).append("|")
            .append(overtimeMultiplier).append("|")
            .append(sortOrder).append("|")
            .append(breakMinutes).append("|").append(alarmLeadMinutes)
            .append("\n").append("color").append("\n").append(toAlarm).append("\n").append(archived)
        return s.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other.toString() == this.toString()
    }

}
