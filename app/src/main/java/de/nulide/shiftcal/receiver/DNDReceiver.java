package de.nulide.shiftcal.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.nulide.shiftcal.data.settings.Settings;
import de.nulide.shiftcal.data.settings.SettingsRepository;
import de.nulide.shiftcal.utils.DndScheduler;

public class DNDReceiver extends BroadcastReceiver {

    public static final int DND_ID_START = 9999991;
    public static final int DND_ID_STOP = 9999990;
    public static final String DND_START_STOP = "DNDSS";
    public static final int START = 1;
    public static final int STOP = 0;


    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsRepository settings = SettingsRepository.Companion.getInstance(context);
        DndScheduler scheduler = new DndScheduler(context);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int SS = bundle.getInt(DND_START_STOP);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (SS == START) {
                int dndBehaviour = settings.getInt(Settings.DND_BEHAVIOUR);
                switch (dndBehaviour) {
                    case 0:
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        break;
                    case 1:
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
                        break;
                    case 2:
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                        break;
                }
            } else {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                scheduler.setDndAlarm();

            }
        }
    }
}
