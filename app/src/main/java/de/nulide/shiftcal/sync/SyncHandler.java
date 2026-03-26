package de.nulide.shiftcal.sync;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import de.nulide.shiftcal.data.settings.Settings;
import de.nulide.shiftcal.data.settings.SettingsRepository;
import de.nulide.shiftcal.utils.permission.Perm;

public class SyncHandler {

    private static final String TAG = "SYNC_TASK";

    public static void sync(Context c) {
        if (!Perm.Companion.check(c, Perm.CALENDAR)) {
            return;
        }
        SettingsRepository settings = SettingsRepository.Companion.getInstance(c);
        if (settings.has(Settings.SYNC)) {
            if (!settings.getBoolean(Settings.SYNC)) {
                return;
            }
        }

        OneTimeWorkRequest jobRequest = new OneTimeWorkRequest.Builder(SyncWork.class)
                .addTag(TAG)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(c).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, jobRequest);
    }

}
