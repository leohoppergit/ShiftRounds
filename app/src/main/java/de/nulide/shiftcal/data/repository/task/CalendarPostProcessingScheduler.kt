package de.nulide.shiftcal.data.repository.task

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.sync.SyncWork
import de.nulide.shiftcal.utils.ACRAHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CalendarPostProcessingScheduler(val sc: SCRepoManager, val context: Context) {

    private val settings = SettingsRepository.getInstance(context)
    private val SHORT_DELAY = 500L
    private val LONG_DELAY = 30L
    private var debounceJob: Job? = null

    private val TAG = "CAL_PP"

    init {
        if (settings.getBoolean(Settings.LAST_POST_PROCESS_FAILED)) {
            startJob()
            settings.set(Settings.LAST_POST_PROCESS_FAILED, false)
        }
    }


    /**
     * Starts the post processing job.
     * It has a short term canclation that is non-persistant and lasts 500ms(for rapid entry)
     * and a persistant work schedule that starts after 10s
     * We try to finish the task while beeing in the foreground.
     * If the app gets killed the worker will start after 30 s.
     */
    fun startJob() {
        debounceJob?.cancel()
        debounceJob = CoroutineScope(Dispatchers.Default).launch {
            settings.set(Settings.LAST_POST_PROCESS_FAILED, true)
            delay(SHORT_DELAY)
            try {
                val jobRequest = OneTimeWorkRequest.Builder(SyncWork::class.java)
                    .addTag(TAG)
                    .setInitialDelay(LONG_DELAY, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, jobRequest)
                WorkManager.getInstance(context).enqueue(jobRequest)
                sc.postProcess(context)
                WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
            } catch (e: Exception) {
                ACRAHelper.sendCrash(e)
            }
        }
    }


}