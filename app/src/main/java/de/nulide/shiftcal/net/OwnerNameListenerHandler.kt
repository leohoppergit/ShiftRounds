package de.nulide.shiftcal.net

import android.content.Context
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.net.listener.OwnerNameListener

class OwnerNameListenerHandler(val context: Context) : OwnerNameListener {

    val sc = SCRepoManager.getInstance(context)

    override fun onGetOwnerName(name: String) {
        val user = sc.users.getSubscribed()
        if (user != null) {
            user.name = name
            sc.users.update(user)
        }
    }
}