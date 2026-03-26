package de.nulide.shiftcal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var netUuid: String?,
    var name: String,
    var password: String,
    var removed: Boolean,
    val active: Boolean,
    val shared: Boolean,
    val subscribed: Boolean,
    val local: Boolean
) {
    constructor() : this(0, null, "", "", false, false, false, false, false)
}