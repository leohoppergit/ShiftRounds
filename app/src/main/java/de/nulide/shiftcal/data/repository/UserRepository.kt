package de.nulide.shiftcal.data.repository

import de.nulide.shiftcal.data.db.ShiftSwiftDB
import de.nulide.shiftcal.data.model.User

class UserRepository(db: ShiftSwiftDB, rm: SCRepoManager) : CommonRepository(db, rm) {

    private val userDao = db.userDao()

    fun getName(): String {
        return userDao.getUserFromCal(calId).name
    }

    fun getLocal(): User {
        return userDao.getLocal()
    }

    fun setName(name: String) {
        userDao.setName(calId, name)
    }

    fun addSubscription(
        uuid: String,
        name: String,
        password: String,
        removed: Boolean,
        active: Boolean
    ): Int {
        return userDao.insert(User(0, uuid, name, password, removed, active, false, true, false))
            .toInt()
    }

    fun addSubscription(
        uuid: String,
        password: String,
    ): Int {
        return userDao.insert(User(0, uuid, "", password, false, false, false, true, false))
            .toInt()
    }

    fun addSharing(uuid: String, password: String): Int {
        return userDao.insert(User(0, uuid, "", password, false, false, true, false, false))
            .toInt()
    }

    fun addSharing(uuid: String, name: String, password: String): Int {
        return userDao.insert(User(0, uuid, name, password, false, false, true, false, false))
            .toInt()
    }

    fun getSubscribed(): User? {
        return userDao.getSubscribed()
    }

    fun getShared(): List<User> {
        return userDao.getShared()
    }

    fun removeSubscriptions() {
        userDao.removeSubscriptions()
    }

    fun removeShared() {
        userDao.removeShared()
    }

    fun update(user: User) {
        userDao.update(user)
    }

    fun removeByUuid(netUuid: String) {
        userDao.removeByUuid(netUuid)
    }

}