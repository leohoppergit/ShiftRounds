package de.nulide.shiftcal.data.model

import java.time.LocalTime
import kotlin.math.log10

class ShiftTime {
    var hour: Int
    var minute: Int

    companion object {
        fun fromMinutes(minutes: Int): ShiftTime {
            val hour = minutes / 60
            val minutes = minutes % 60
            return ShiftTime(hour, minutes)
        }

        fun now(): ShiftTime {
            val now = LocalTime.now()
            return ShiftTime(now.hour, now.minute)
        }
    }

    constructor(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var hourLength = (log10(hour.toDouble()) + 1).toInt()
        if (hour == 0) {
            hourLength = 1
        }
        var minuteLength = (log10(minute.toDouble()) + 1).toInt()
        if (minute == 0) {
            minuteLength = 1
        }
        if (hourLength == 1) {
            sb.append(0)
        }
        sb.append(hour).append(":")
        if (minuteLength == 1) {
            sb.append(0)
        }
        sb.append(minute)

        return sb.toString()
    }

    val timeInMinutes: Int
        get() = (this.hour * 60) + this.minute

    fun isNextDayOf(compareTo: ShiftTime): Boolean {
        if (compareTo.hour > hour) {
            return true
        } else if (compareTo.hour == hour) {
            return compareTo.minute >= minute
        }
        return false
    }

    fun minus(toSubtract: ShiftTime): Int {
        var res = this.timeInMinutes - toSubtract.timeInMinutes
        if (res <= 0) {
            res += 24 * 60
        }
        return res
    }

    fun minus(minutes: Int): ShiftTime {
        var nTimeInMinutes = timeInMinutes
        nTimeInMinutes -= minutes
        if (nTimeInMinutes < 0) {
            nTimeInMinutes = 1440 + (nTimeInMinutes % 1440)
        }
        val nHours = nTimeInMinutes / 60
        val nMinute = nTimeInMinutes % 60
        return ShiftTime(nHours, nMinute)
    }

    constructor() : this(0, 0)
}
