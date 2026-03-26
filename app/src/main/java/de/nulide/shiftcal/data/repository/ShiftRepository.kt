package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.Shift
import java.time.LocalDate

class ShiftRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val shiftDao = db.shiftDao()

    fun add(shift: Shift) {
        shift.calendarId = calId
        if (shift.sortOrder == 0) {
            shift.sortOrder = shift.id
        }
        shiftDao.insert(shift)
        postDataChange()
    }

    fun update(shift: Shift) {
        shift.calendarId = calId
        shiftDao.update(shift)
        postDataChange()
    }

    fun delete(shift: Shift) {
        shift.calendarId = calId
        shiftDao.delete(shift)
        postDataChange()
    }

    fun get(id: Int): Shift {
        return shiftDao.get(calId, id)
    }

    fun getAll(): List<Shift> {
        return shiftDao.getAll(calId)
    }

    fun getNotArchived(): List<Shift> {
        return shiftDao.getNotArchived(calId)
    }

    fun getArchived(): List<Shift> {
        return shiftDao.getArchived(calId)
    }

    fun hasArchived(): Boolean {
        return shiftDao.hasArchived(calId)
    }

    fun getOn(date: LocalDate): List<Shift> {
        return shiftDao.getOn(calId, date)
    }

    fun getNextId(): Int {
        return shiftDao.getHighestID(calId) + 1
    }

    fun reorder(shifts: List<Shift>) {
        shifts.forEachIndexed { index, shift ->
            shift.calendarId = calId
            shift.sortOrder = index
            shiftDao.update(shift)
        }
        postDataChange()
    }


}
