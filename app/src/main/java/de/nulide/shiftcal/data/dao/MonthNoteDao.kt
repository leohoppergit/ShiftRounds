package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.nulide.shiftcal.data.model.MonthNote

@Dao
interface MonthNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(monthNote: MonthNote)

    @Update
    fun update(monthNote: MonthNote)

    @Query("SELECT * FROM month_note WHERE calendarId= :cal AND year = :year AND month = :month")
    fun get(cal: Int, year: Int, month: Int): MonthNote?

    @Query("SELECT * FROM month_note WHERE calendarId=:cal")
    fun getAll(cal: Int): List<MonthNote>

    @Query("DELETE FROM month_note WHERE calendarId = :cal AND year = :year AND month = :month")
    fun delete(cal: Int, year: Int, month: Int)

    @Query("DELETE FROM month_note WHERE calendarId = :cal")
    fun deleteAll(cal: Int)

}
