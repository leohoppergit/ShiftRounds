package de.nulide.shiftcal.utils.debug

import android.util.Log
import java.util.Calendar

class Timer {
    companion object {
        private var lastCalendar = Calendar.getInstance()
        private var i = 0

        fun printTime(tag: String) {
            val newCalendar = Calendar.getInstance()
            Log.v(
                "TimeDiff $tag",
                (newCalendar.timeInMillis - lastCalendar.timeInMillis).toString()
            )
            lastCalendar = newCalendar
        }

        fun printTime() {
            i += 1
            printTime(i.toString())
        }


    }
}