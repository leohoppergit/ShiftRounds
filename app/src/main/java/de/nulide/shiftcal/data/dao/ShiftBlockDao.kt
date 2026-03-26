package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.nulide.shiftcal.data.model.ShiftBlock

@Dao
interface ShiftBlockDao {

    @Insert
    fun add(shiftBlock: ShiftBlock)

    @Delete
    fun delete(shiftBlock: ShiftBlock)

    @Query("SELECT * FROM shift_block WHERE calendarId = :cal AND id = :id")
    fun get(cal: Int, id: Int): ShiftBlock

    @Query("SELECT * FROM shift_block WHERE calendarId = :cal")
    fun getAll(cal: Int): List<ShiftBlock>

    @Query("SELECT EXISTS(SELECT * FROM shift_block WHERE calendarId = :cal)")
    fun hasAny(cal: Int): Boolean

    @Query("SELECT MAX(id) FROM shift_block WHERE calendarId = :cal")
    fun getHighestID(cal: Int): Int

    @Update
    fun update(shiftBlock: ShiftBlock)

}