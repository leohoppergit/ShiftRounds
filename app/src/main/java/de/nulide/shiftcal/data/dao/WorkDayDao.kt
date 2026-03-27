package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.model.WorkDay
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface WorkDayDao {

    @Insert
    fun insert(workDay: WorkDay)

    @Delete
    fun delete(workDay: WorkDay)

    @Query("SELECT EXISTS(SELECT id FROM work_day WHERE calendarId = :cal AND day = :day)")
    fun hasWork(cal: Int, day: LocalDate): Boolean

    @Query("DELETE FROM work_day WHERE calendarId = :cal AND day = :day")
    fun deleteAllOn(cal: Int, day: LocalDate)

    @Query("Select * FROM work_day WHERE calendarId = :cal AND day = :day")
    fun getByDay(cal: Int, day: LocalDate): List<WorkDay>

    @Query("Select * FROM work_day WHERE calendarId = :cal AND day > :day")
    fun getWorkAfter(cal: Int, day: LocalDate): List<WorkDay>

    // Sum the whole working time(respect workdays that get over midnight and subtract the breaktime
    @Query(
        "SELECT SUM(CASE " +
                "WHEN s.customBalanceMinutes IS NOT NULL THEN s.customBalanceMinutes " +
                "ELSE (((s.endDayOffset * 24 * 60) + s.endTime) - s.startTime) - s.breakMinutes " +
                "END) FROM work_day w " +
                "JOIN shift s ON w.shiftId = s.id AND w.calendarId = s.calendarId" +
                " WHERE w.calendarId = :cal and day BETWEEN :start AND :end"
    )
    fun getWorkMinutesBetween(cal: Int, start: LocalDate, end: LocalDate): Int

    @Query(
        "SELECT SUM(s.breakMinutes) AS total FROM work_day w " +
                "JOIN shift s ON w.shiftId = s.id AND w.calendarId = s.calendarId" +
                " WHERE s.calendarId = :cal and day BETWEEN :start AND :end"
    )
    fun getBreakMinutesBetween(cal: Int, start: LocalDate, end: LocalDate): Long

    @Query(
        "SELECT COALESCE(SUM(overtimeMinutes), 0) FROM work_day " +
                "WHERE calendarId = :cal AND day BETWEEN :start AND :end"
    )
    fun getOvertimeMinutesBetween(cal: Int, start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM work_day WHERE calendarId = :cal and day BETWEEN :start AND :end")
    fun getBetween(cal: Int, start: LocalDate, end: LocalDate): List<WorkDay>

    @Query(
        "SELECT w.* FROM work_day w JOIN shift s ON w.shiftId = s.id AND w.calendarId = s.calendarId" +
                " WHERE w.calendarId = :cal and (day*24*60+startTime*60)*1000 <=:now" +
                " AND ((day + s.endDayOffset)*24*60+endTime*60)*1000 >=:now"
    )
    fun getIn(cal: Int, now: LocalDateTime): WorkDay?

    @Query(
        "SELECT w.*, (w.day*24*60+(s.startTime) - " +
                "CASE WHEN s.alarmLeadMinutes IS NULL THEN :alarmLeadTime ELSE s.alarmLeadMinutes END)" +
                "AS realAlarmStart FROM work_day w JOIN shift s ON w.shiftId = s.id AND w.calendarId = s.calendarId " +
                "WHERE w.calendarId=:cal AND (NOT :respectsAlarm OR (s.toAlarm AND NOT w.alarmDismissed))" +
                "AND realAlarmStart > (:day*24*60)+:time ORDER BY realAlarmStart ASC LIMIT 1"
    )
    @RewriteQueriesToDropUnusedColumns
    fun getUpcoming(
        cal: Int,
        day: LocalDate,
        time: ShiftTime,
        respectsAlarm: Boolean,
        alarmLeadTime: Int
    ): WorkDay?

    @Update
    fun update(wday: WorkDay)

    @Query(
        "SELECT * FROM work_day s WHERE calendarId = :cal" +
                " AND day BETWEEN :start AND :end"
    )
    fun getBetweenDays(cal: Int, start: Long, end: Long): List<WorkDay>

    @Query(
        "SELECT w.* FROM work_day w " +
                "JOIN shift s ON w.shiftId = s.id AND w.calendarId = s.calendarId " +
                "WHERE w.calendarId = :cal AND w.day = :day " +
                "ORDER BY s.sortOrder ASC, s.id ASC, w.id ASC"
    )
    fun getOn(cal: Int, day: LocalDate): List<WorkDay>

    @Query(
        "SELECT * FROM work_day WHERE calendarId = :cal ORDER BY day ASC LIMIT 1"
    )
    fun getOldest(cal: Int): WorkDay?

    @Query("SELECT MAX(id) FROM work_day WHERE calendarId = :cal")
    fun getHighestID(cal: Int): Int

    @Query("SELECT * FROM work_day WHERE calendarId = :cal AND day>:day")
    fun getAfter(cal: Int, day: LocalDate): List<WorkDay>

    @Query("SELECT * FROM work_day WHERE calendarId=:cal")
    fun getAll(cal: Int): List<WorkDay>

    @Query("SELECT CASE WHEN (SELECT COUNT(id) AS S_C FROM work_day WHERE calendarId = :cal GROUP BY day ORDER BY S_C DESC LIMIT 1) > 1 THEN 1 ELSE 0 END")
    fun hasDualShift(cal: Int): Boolean
}
