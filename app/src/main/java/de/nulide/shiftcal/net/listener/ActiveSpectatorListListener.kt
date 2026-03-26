package de.nulide.shiftcal.net.listener

import de.nulide.shiftcal.net.dto.SpectatorList

interface ActiveSpectatorListListener {

    fun onActiveSpectatorListenerReceived(spectators: SpectatorList)

}