package de.nulide.shiftcal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.nulide.shiftcal.data.model.User

@Dao
interface UserDao {

    @Insert
    fun insert(user: User): Long

    @Delete
    fun delete(user: User)

    @Query("SELECT * FROM user u WHERE u.id IN (SELECT owner FROM shift_calendar WHERE id = :cal)")
    fun getUserFromCal(cal: Int): User

    @Query("UPDATE user SET name = :name WHERE id IN (SELECT owner FROM shift_calendar WHERE id = :cal)")
    fun setName(cal: Int, name: String)

    @Query("SELECT * FROM user WHERE shared = 1")
    fun getShared(): List<User>

    @Query("DELETE FROM user WHERE subscribed = 1")
    fun removeSubscriptions()

    @Query("SELECT * FROM user WHERE local = 1 LIMIT 1")
    fun getLocal(): User

    @Query("DELETE FROM user WHERE shared = 1")
    fun removeShared()

    @Query("SELECT * FROM user WHERE shared = :shared AND subscribed = :subscribed AND local = :local")
    fun getWith(shared: Boolean, subscribed: Boolean, local: Boolean): List<User>

    @Query("SELECT * FROM user WHERE subscribed = 1 AND removed = 0 LIMIT 1")
    fun getSubscribed(): User?

    @Update
    fun update(user: User)

    @Query("DELETE FROM user WHERE netUuid = :netUuid")
    fun removeByUuid(netUuid: String)

}