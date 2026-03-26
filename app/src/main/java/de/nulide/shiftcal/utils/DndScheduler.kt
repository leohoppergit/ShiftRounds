package de.nulide.shiftcal.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.receiver.DNDReceiver
import java.util.Calendar

class DndScheduler(private val context: Context) {

    private val settings = SettingsRepository.getInstance(context)
    private val sc = SCRepoManager.getInstance(context)

    fun setDndAlarm() {
        if (!settings.getBoolean(Settings.DND)) {
            removeAll()
            return
        }

        removeAll()

        var toDnd = sc.fromLocal { sc.workDays.getRunning() }
        if (toDnd == null) {
            toDnd = sc.fromLocal { sc.workDays.getUpcoming(false, 0) }
            if (toDnd == null) {
                return
            }
        }

        val shift = sc.fromLocal { sc.shifts.get(toDnd.shiftId) }
        val start = Calendar.getInstance().apply {
            set(
                toDnd.day.year,
                toDnd.day.monthValue - 1,
                toDnd.day.dayOfMonth,
                shift.startTime.hour,
                shift.startTime.minute,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        val stop = Calendar.getInstance().apply {
            set(
                toDnd.day.year,
                toDnd.day.monthValue - 1,
                toDnd.day.dayOfMonth,
                shift.endTime.hour,
                shift.endTime.minute,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }

        stop.add(Calendar.DAY_OF_MONTH, shift.endDayOffset)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createSilentAlarm(
            PendingIntent.getBroadcast(
                context,
                DNDReceiver.DND_ID_START,
                Intent(context, DNDReceiver::class.java).putExtra(
                    DNDReceiver.DND_START_STOP,
                    DNDReceiver.START
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            ),
            alarmManager,
            start
        )
        createSilentAlarm(
            PendingIntent.getBroadcast(
                context,
                DNDReceiver.DND_ID_STOP,
                Intent(context, DNDReceiver::class.java).putExtra(
                    DNDReceiver.DND_START_STOP,
                    DNDReceiver.STOP
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            ),
            alarmManager,
            stop
        )
    }

    private fun removeAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        PendingIntent.getBroadcast(
            context,
            DNDReceiver.DND_ID_START,
            Intent(context, DNDReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )?.let(alarmManager::cancel)
        PendingIntent.getBroadcast(
            context,
            DNDReceiver.DND_ID_STOP,
            Intent(context, DNDReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )?.let(alarmManager::cancel)
    }

    private fun createSilentAlarm(
        pendingIntent: PendingIntent,
        alarmManager: AlarmManager,
        date: Calendar
    ) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            date.timeInMillis,
            pendingIntent
        )
    }
}
