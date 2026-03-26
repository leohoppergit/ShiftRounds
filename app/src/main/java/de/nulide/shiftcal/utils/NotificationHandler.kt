package de.nulide.shiftcal.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import de.nulide.shiftcal.R

class NotificationHandler {
    companion object {
        const val DAY_SHIFT_CHANNEL = "DAY_SHIFT_CHANNEL"

        fun initNotificationChannels(context: Context) {

            val dayShiftChannel = NotificationChannel(
                DAY_SHIFT_CHANNEL,
                context.getString(R.string.notification_day_shift_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_day_shift_description)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }


            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(dayShiftChannel)
        }
    }

}
