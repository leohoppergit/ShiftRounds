package de.nulide.shiftcal.utils.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.utils.Device.Companion.isOnePlus

class Perm {

    companion object {
        const val CAMERA = Manifest.permission.CAMERA
        const val CALENDAR = Manifest.permission.WRITE_CALENDAR
        const val CALENDAR_READ = Manifest.permission.READ_CALENDAR

        @SuppressLint("InlinedApi")
        const val NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS
        const val ONEPLUS_BACKGROUND = Settings.PERM_ONE_PLUS_BACKGROUND_ACTIVITY
        const val OVERLAY = android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
        const val DND = android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS

        @SuppressLint("BatteryLife")
        const val BAT =
            android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS


        fun check(ctx: Context, perm: String): Boolean {
            when (perm) {
                NOTIFICATION -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        return true
                    }
                }

                ONEPLUS_BACKGROUND -> {
                    if (isOnePlus()) {
                        val settings = SettingsRepository.getInstance(ctx)
                        return settings.getBoolean(Settings.PERM_ONE_PLUS_BACKGROUND_ACTIVITY)
                    } else {
                        return true
                    }
                }

                DND -> {
                    return (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)
                        ?.isNotificationPolicyAccessGranted() == true
                }

                BAT -> {
                    return (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager?)
                        ?.isIgnoringBatteryOptimizations(ctx.packageName) == true
                }

                OVERLAY -> {
                    return android.provider.Settings.canDrawOverlays(ctx)
                }
            }
            val state = ContextCompat.checkSelfPermission(ctx, perm)
            return state == PackageManager.PERMISSION_GRANTED
        }

    }

}