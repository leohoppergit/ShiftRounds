package de.nulide.shiftcal.net.listener

interface VersionListener {
    fun onVersionAcquired(version: String)
}