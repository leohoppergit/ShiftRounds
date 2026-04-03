package de.nulide.shiftcal.utils.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class Perm {

    companion object {
        const val CALENDAR = Manifest.permission.WRITE_CALENDAR
        const val CALENDAR_READ = Manifest.permission.READ_CALENDAR


        fun check(ctx: Context, perm: String): Boolean {
            val state = ContextCompat.checkSelfPermission(ctx, perm)
            return state == PackageManager.PERMISSION_GRANTED
        }

    }

}
