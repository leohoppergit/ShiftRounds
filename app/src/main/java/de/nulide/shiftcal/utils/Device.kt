package de.nulide.shiftcal.utils

import android.os.Build

class Device {

    companion object {
        fun isOnePlus(): Boolean {
            return Build.MANUFACTURER.equals("OnePlus", ignoreCase = true)
        }
    }
}