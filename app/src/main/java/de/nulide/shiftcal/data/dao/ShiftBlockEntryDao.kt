package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.nulide.shiftcal.data.model.ShiftBlockEntry

@Dao
interface ShiftBlockEntryDao {

    @Insert
    fun add(shiftBlockEntry: ShiftBlockEntry)

    @Query("DELETE FROM shift_block_entry WHERE calendarId = :cal AND shiftBlockId = :shiftBlockId")
    fun deleteAllOf(cal: Int, shiftBlockId: Int)

    @Query("SELECT * FROM shift_block_entry WHERE calendarId = :cal AND shiftBlockId = :shiftBlockId")
    fun getAllOf(cal: Int, shiftBlockId: Int): MutableList<ShiftBlockEntry>

}