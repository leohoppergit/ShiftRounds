package de.nulide.shiftcal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.nulide.shiftcal.utils.DndScheduler;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DndScheduler scheduler = new DndScheduler(context);
        scheduler.setDndAlarm();
    }

}
