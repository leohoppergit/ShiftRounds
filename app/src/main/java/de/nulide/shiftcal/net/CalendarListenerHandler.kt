package de.nulide.shiftcal.net

import android.content.Context
import de.nulide.shiftcal.crypt.AES
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.net.listener.CalendarListener

class CalendarListenerHandler(val context: Context, val uuid: String) : CalendarListener {

    private var calendarListener: CalendarListener? = null
    val sc = SCRepoManager.getInstance(context)
    val settings = SettingsRepository.getInstance(context)

    constructor(context: Context, uuid: String, calendarListener: CalendarListener?) : this(
        context,
        uuid
    ) {
        this.calendarListener = calendarListener
    }

    override fun onNewCalendar(calendar: String) {
        if (calendar == ServerCode.NO_ACCESS) {
            settings.set(Settings.SERVER_SYNC_SPECTATOR_LOGOUT, true)
            val subscription = sc.users.getSubscribed()
            if (subscription != null) {
                subscription.removed = true
                sc.users.update(subscription)
            }
        } else if (calendar == ServerCode.NOT_FOUND) {
            //Handle not found
        } else {
            val user = sc.users.getSubscribed()
            if (user != null) {
                val pw = user.password
                val decryptedCalendar = AES.decryptWithAes(calendar, pw)
                if (decryptedCalendar != null) {
                    sc.fromJSON(uuid, decryptedCalendar)
                    calendarListener?.onNewCalendar(decryptedCalendar)
                }
            }

        }
    }

}
