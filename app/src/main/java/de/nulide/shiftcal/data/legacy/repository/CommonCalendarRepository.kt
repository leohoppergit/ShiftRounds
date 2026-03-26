package de.nulide.shiftcal.data.legacy.repository

import android.content.Context
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.legacy.model.ShiftCalendar
import java.io.File

abstract class CommonCalendarRepository(val context: Context) {

    var shiftCalendar: ShiftCalendar
    private var newVersion = false

    init {
        val file = getFile()
        shiftCalendar = JIO.readAsJSON(ShiftCalendar::class.java, file)
        migration()
    }

    open fun migration() {

    }

    abstract fun getFile(): File

    open fun saveShiftCalendar() {
        JIO.saveAsJSON(shiftCalendar, getFile())
    }

    fun resetCalendar() {
        shiftCalendar = ShiftCalendar()
        saveShiftCalendar()
    }

    fun increaseVersion() {
        newVersion = true
    }

}