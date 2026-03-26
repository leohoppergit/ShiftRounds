package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.ShiftCalendar
import de.nulide.shiftcal.data.model.User

class CalendarRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val calendarDao = db.calendarDao()
    private val userDao = db.userDao()

    fun getLocal(): Int {
        var id = calendarDao.getByLocality(true)
        if (id == null) {
            val uid =
                userDao.insert(User(0, null, "", "", false, false, false, false, true)).toInt()
            id = calendarDao.insert(ShiftCalendar(0, uid)).toInt()
        }
        return id
    }

    fun getNonLocal(): Int? {
        return calendarDao.getByLocality(false)
    }

    fun addNonLocal(ownerId: Int): Int {
        val id = calendarDao.insert(ShiftCalendar(0, ownerId)).toInt()
        return id
    }

    fun deleteFromNet(netUuid: String) {
        calendarDao.deleteFromNet(netUuid)
    }

    fun getFromNet(netUuid: String): Int? {
        return calendarDao.getIdFromNet(netUuid)
    }

}