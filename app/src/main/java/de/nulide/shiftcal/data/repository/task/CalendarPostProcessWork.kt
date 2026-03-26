package de.nulide.shiftcal.data.repository.task

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository

class CalendarPostProcessWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val settings = SettingsRepository.getInstance(context)
        if (settings.getBoolean(Settings.LAST_POST_PROCESS_FAILED)) {
            val sc = SCRepoManager.getInstance(context)
            sc.postProcess(context)
        }
        return Result.success()
    }

}