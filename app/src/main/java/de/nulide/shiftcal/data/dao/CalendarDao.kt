package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.nulide.shiftcal.data.model.ShiftCalendar

@Dao
interface CalendarDao {

    @Insert
    fun insert(shiftCal: ShiftCalendar): Long

    @Query("UPDATE shift_calendar SET owner = :name WHERE id = :id")
    fun setName(id: Int, name: String)

    @Query("SELECT s.id FROM shift_calendar s JOIN User u ON s.owner = u.id WHERE u.local = :local AND u.removed = 0")
    fun getByLocality(local: Boolean): Int?

    @Delete
    fun delete(shiftCal: ShiftCalendar)

    @Query("DELETE FROM shift_calendar WHERE owner IN (SELECT id FROM user WHERE netUuid = :netUuid)")
    fun deleteFromNet(netUuid: String)

    @Query("SELECT id FROM shift_calendar WHERE owner IN (SELECT id FROM user WHERE netUuid = :netUuid)")
    fun getIdFromNet(netUuid: String): Int?

}