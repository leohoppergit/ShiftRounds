package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.MonthNote

class MonthNoteRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val monthNoteDao = db.monthNoteDao()

    fun get(year: Int, month: Int): MonthNote? {
        return monthNoteDao.get(calId, year, month)
    }

    fun getAll(): List<MonthNote> {
        return monthNoteDao.getAll(calId)
    }

    fun set(year: Int, month: Int, msg: String) {
        if (msg != "") {
            val note = monthNoteDao.get(calId, year, month)
            if (note == null) {
                monthNoteDao.insert(MonthNote(calId, year, month, msg))
            } else {
                note.msg = msg
                monthNoteDao.update(note)
            }
        } else {
            monthNoteDao.delete(calId, year, month)
        }
        postDataChange()
    }

}