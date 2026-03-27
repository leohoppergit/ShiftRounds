package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.nulide.shiftcal.data.model.Shift
import java.time.LocalDate

@Dao
interface ShiftDao {

    @Insert
    fun insert(shift: Shift)

    @Delete
    fun delete(shift: Shift)

    @Update
    fun update(shift: Shift)

    @Query("SELECT * FROM shift WHERE calendarId = :cal AND id = :id")
    fun get(cal: Int, id: Int): Shift

    @Query("SELECT * FROM shift WHERE calendarId=:cal ORDER BY sortOrder ASC, id ASC")
    fun getAll(cal: Int): List<Shift>

    @Query("SELECT * FROM shift WHERE calendarId = :cal AND archived = 0 ORDER BY sortOrder ASC, id ASC")
    fun getNotArchived(cal: Int): List<Shift>

    @Query("SELECT * FROM shift WHERE calendarId = :cal AND archived = 1 ORDER BY sortOrder ASC, id ASC")
    fun getArchived(cal: Int): List<Shift>

    @Query("SELECT EXISTS(SELECT * FROM shift WHERE calendarId = :cal AND archived = 1)")
    fun hasArchived(cal: Int): Boolean

    @Query(
        "SELECT s.* FROM shift s JOIN work_day w ON w.calendarId = s.calendarId AND w.shiftId=s.id " +
                "AND w.calendarId = :cal AND w.day = :day " +
                "ORDER BY s.sortOrder ASC, s.id ASC, w.id ASC"
    )
    fun getOn(cal: Int, day: LocalDate): List<Shift>

    @Query("SELECT MAX(id) FROM shift WHERE calendarId = :cal")
    fun getHighestID(cal: Int): Int

}
