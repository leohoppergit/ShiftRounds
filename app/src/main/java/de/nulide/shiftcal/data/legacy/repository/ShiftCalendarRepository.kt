package de.nulide.shiftcal.data.legacy.repository

import android.content.Context
import de.nulide.shiftcal.utils.SingletonHolder
import java.io.File

open class ShiftCalendarRepository(context: Context) : CommonCalendarRepository(context) {

    companion object :
        SingletonHolder<ShiftCalendarRepository, Context>(::ShiftCalendarRepository) {
        const val SHIFT_CAL_FILENAME = "shift-calendar.json"
        const val OLD_SHIFT_CAL_FILENAME = "sc.json"
    }

    override fun migration() {
        if (shiftCalendar.dataVersion < 1) {
            for ((i, wday) in shiftCalendar.calendar.withIndex()) {
                wday.id = i
            }
            shiftCalendar.dataVersion = 1
            saveShiftCalendar()
        }
    }

    override fun getFile(): File {
        val scFile = File(context.filesDir, SHIFT_CAL_FILENAME)
        if (!scFile.exists()) {
            val oldScFile = File(context.filesDir, OLD_SHIFT_CAL_FILENAME)
            if (oldScFile.exists()) {
                oldScFile.renameTo(scFile)
            }
        }
        return scFile
    }
}
