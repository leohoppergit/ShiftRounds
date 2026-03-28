package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import java.time.LocalDate
import java.time.LocalDateTime

class WorkDayRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val workDayDao = db.workDayDao()
    private val shiftDao = db.shiftDao()
    private val dayWorkComparator = compareBy<WorkDayDTO>(
        { it.shift.startTime.timeInMinutes },
        { it.shift.endDayOffset },
        { it.shift.endTime.timeInMinutes },
        { it.shift.sortOrder },
        { it.shift.id },
        { it.wday.id }
    )

    fun add(wday: WorkDay) {
        wday.calendarId = calId
        wday.id = workDayDao.getHighestID(calId) + 1
        workDayDao.insert(wday)
        postDataChange()
    }

    fun hasWork(day: LocalDate): Boolean {
        return workDayDao.hasWork(calId, day)
    }

    fun deleteAllOn(day: LocalDate) {
        workDayDao.deleteAllOn(calId, day)
        postDataChange()
    }

    fun getWorkDaysOfMonth(year: Int, month: Int): List<WorkDay> {
        val (start, end) = getStartEndMonth(year, month)
        return workDayDao.getBetween(calId, start, end)
    }

    fun getWorkMinutesForMonth(year: Int, month: Int): Int {
        val (start, end) = getStartEndMonth(year, month)
        return workDayDao.getWorkMinutesBetween(calId, start, end)
    }

    fun getBreakMinutesForMonth(year: Int, month: Int): Int {
        val (start, end) = getStartEndMonth(year, month)
        return workDayDao.getBreakMinutesBetween(calId, start, end).toInt()
    }

    fun getOvertimeMinutesForMonth(year: Int, month: Int): Int {
        val (start, end) = getStartEndMonth(year, month)
        return workDayDao.getOvertimeMinutesBetween(calId, start, end)
    }

    fun getSpecialAccountMinutesForMonth(year: Int, month: Int, accountId: String): Int {
        val (start, end) = getStartEndMonth(year, month)
        return workDayDao.getSpecialAccountMinutesBetween(calId, start, end, accountId)
    }

    fun getUpcoming(respectAlarm: Boolean, preMinutes: Int): WorkDay? {
        val day = LocalDate.now()
        val time = ShiftTime.now()
        return workDayDao.getUpcoming(calId, day, time, respectAlarm, preMinutes)
    }

    fun update(wday: WorkDay) {
        workDayDao.update(wday)
        postDataChange()
    }

    fun getOnDay(date: LocalDate): List<WorkDay> {
        return workDayDao.getOn(calId, date)
    }

    fun getCombinedOnDay(date: LocalDate): List<WorkDayDTO> {
        val wdays = workDayDao.getOn(calId, date)
        val res = mutableListOf<WorkDayDTO>()
        for (wday in wdays) {
            val shift = shiftDao.get(calId, wday.shiftId)
            res.add(WorkDayDTO(wday, shift))
        }
        return res.sortedWith(dayWorkComparator)
    }

    fun getOldest(): WorkDay? {
        return workDayDao.getOldest(calId)
    }

    fun getAfter(day: LocalDate): List<WorkDay> {
        return workDayDao.getAfter(calId, day)
    }

    fun getAll(): List<WorkDay> {
        return workDayDao.getAll(calId)
    }

    fun getRunning(): WorkDay? {
        return workDayDao.getIn(calId, LocalDateTime.now())
    }

    fun hasDualShift(): Boolean {
        return workDayDao.hasDualShift(calId)
    }

}
