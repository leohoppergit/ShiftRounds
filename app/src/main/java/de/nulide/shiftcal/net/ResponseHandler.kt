package de.nulide.shiftcal.net

import com.android.volley.Response
import com.android.volley.Response.ErrorListener
import com.android.volley.VolleyError
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.net.dto.SpectatorList
import de.nulide.shiftcal.net.listener.AccountDeleteListener
import de.nulide.shiftcal.net.listener.ActiveSpectatorListListener
import de.nulide.shiftcal.net.listener.CalendarListener
import de.nulide.shiftcal.net.listener.OTTAcquiredListener
import de.nulide.shiftcal.net.listener.OwnerNameListener
import de.nulide.shiftcal.net.listener.RegistrationListener
import de.nulide.shiftcal.net.listener.RemoveSpectatorSuccessListener
import de.nulide.shiftcal.net.listener.SetNameSuccessListener
import de.nulide.shiftcal.net.listener.SpectatorListener
import de.nulide.shiftcal.net.listener.VersionListener

class ResponseHandler : Response.Listener<String>, ErrorListener {

    var registrationListener: RegistrationListener? = null
    var spectatorListener: SpectatorListener? = null
    var calendarListener: CalendarListener? = null
    var activeSpectatorListListener: ActiveSpectatorListListener? = null
    var errorListener: ErrorListener? = null
    var setNameSuccessListener: SetNameSuccessListener? = null
    var removeSpectatorSuccessListener: RemoveSpectatorSuccessListener? = null
    var accountDeleteListener: AccountDeleteListener? = null
    var versionListener: VersionListener? = null
    var ottAcquiredListener: OTTAcquiredListener? = null

    var ownerNameListener: OwnerNameListener? = null

    constructor(registrationListener: RegistrationListener?, errorListener: ErrorListener?) {
        this.registrationListener = registrationListener
        this.errorListener = errorListener
    }

    constructor(spectatorListener: SpectatorListener?, errorListener: ErrorListener?) {
        this.spectatorListener = spectatorListener
        this.errorListener = errorListener
    }

    constructor(calendarListener: CalendarListener?, errorListener: ErrorListener?) {
        this.calendarListener = calendarListener
        this.errorListener = errorListener
    }

    constructor(
        activeSpectatorListListener: ActiveSpectatorListListener?,
        errorListener: ErrorListener?
    ) {
        this.activeSpectatorListListener = activeSpectatorListListener
        this.errorListener = errorListener
    }

    constructor(setNameSuccessListener: SetNameSuccessListener?, errorListener: ErrorListener?) {
        this.setNameSuccessListener = setNameSuccessListener
        this.errorListener = errorListener
    }

    constructor(
        removeSpectatorSuccessListener: RemoveSpectatorSuccessListener?,
        errorListener: ErrorListener?
    ) {
        this.removeSpectatorSuccessListener = removeSpectatorSuccessListener
        this.errorListener = errorListener
    }

    constructor(accountDeleteListener: AccountDeleteListener?, errorListener: ErrorListener?) {
        this.accountDeleteListener = accountDeleteListener
        this.errorListener = errorListener
    }

    constructor(versionListener: VersionListener?, errorListener: ErrorListener?) {
        this.versionListener = versionListener
        this.errorListener = errorListener
    }

    constructor(ottAcquiredListener: OTTAcquiredListener?, errorListener: ErrorListener?) {
        this.ottAcquiredListener = ottAcquiredListener
        this.errorListener = errorListener
    }

    constructor(ownerNameListener: OwnerNameListener, errorListener: ErrorListener?) {
        this.ownerNameListener = ownerNameListener
        this.errorListener = errorListener
    }

    override fun onResponse(response: String?) {
        if (response != null) {
            if (response.isNotEmpty()) {
                registrationListener?.onRegistrationSucceeds(response)
                spectatorListener?.onSpectatorAcquired(response)
                calendarListener?.onNewCalendar(response)
                if (activeSpectatorListListener != null) {
                    val activeSpectatorList = JIO.fromJSON(response, SpectatorList::class.java)
                    activeSpectatorListListener?.onActiveSpectatorListenerReceived(
                        activeSpectatorList
                    )
                }
                setNameSuccessListener?.onSetNameSuccess()
                removeSpectatorSuccessListener?.onRemoveSpectatorSuccess()
                accountDeleteListener?.onAccountDelete()
                versionListener?.onVersionAcquired(response)
                ottAcquiredListener?.onOTTAcquired(response)
                ownerNameListener?.onGetOwnerName(response)
                return
            }
        }
        onErrorResponse(null)

    }

    override fun onErrorResponse(error: VolleyError?) {
        error?.printStackTrace()
        errorListener?.onErrorResponse(error)
    }


}