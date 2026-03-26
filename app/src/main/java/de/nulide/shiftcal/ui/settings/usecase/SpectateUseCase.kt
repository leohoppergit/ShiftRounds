package de.nulide.shiftcal.ui.settings.usecase

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import de.nulide.shiftcal.data.model.User
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.net.ShiftSwiftServerRestApi
import de.nulide.shiftcal.net.listener.CalendarListener
import de.nulide.shiftcal.net.listener.SetNameSuccessListener
import de.nulide.shiftcal.net.listener.SpectatorListener
import de.nulide.shiftcal.utils.Runner

class SpectateUseCase(
    val context: Context,
    val errorListener: Response.ErrorListener,
    val activity: AppCompatActivity
) : SpectatorListener, SetNameSuccessListener, CalendarListener {

    val serverApi = ShiftSwiftServerRestApi(context)
    val sc = SCRepoManager.getInstance(context)
    val spectatorRegistration = User()

    fun registerAsSpectator(calShareString: String) {
        val split = calShareString.split("@@@")
        if (split.size == 2) {
            spectatorRegistration.password = split[1]
            serverApi.getSpectator(split[0], this, errorListener)
        }

    }

    override fun onSpectatorAcquired(id: String) {
        spectatorRegistration.netUuid = id
        serverApi.setSpectatorName(id, sc.fromLocal { sc.users.getName() }, this, errorListener)
    }

    override fun onSetNameSuccess() {
        sc.users.addSubscription(spectatorRegistration.netUuid!!, spectatorRegistration.password)
        Runner.runLate {
            serverApi.getCalendar(spectatorRegistration.netUuid!!, this) {
                Runner.runLate {
                    serverApi.getCalendar(spectatorRegistration.netUuid!!, this) {
                        onNewCalendar("")
                    }
                    serverApi.getOwnerName(spectatorRegistration.netUuid!!, null)
                }
            }
            serverApi.getOwnerName(spectatorRegistration.netUuid!!, null)
        }
    }

    override fun onNewCalendar(calendar: String) {
        activity.finish()
    }

}