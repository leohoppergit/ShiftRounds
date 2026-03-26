package de.nulide.shiftcal.sync

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import java.time.LocalDate

class SyncWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val sc = SCRepoManager.getInstance(context)

        CalendarController.deleteCalendar(context)

        var calId = CalendarController.getCalendarId(context.contentResolver)
        if (calId == -1L) {
            val color = ContextCompat.getColor(context, R.color.syncCalColor);
            CalendarController.addShiftCalCalendar(context, color)
            calId = CalendarController.getCalendarId(context.contentResolver)
        }

        val ec = EventController(context.contentResolver, calId, sc)
        val startPoint = LocalDate.now().plusMonths(-6)
        for (wd in sc.fromLocal { sc.workDays.getAfter(startPoint) }) {
            ec.createEvent(wd)
        }
        return Result.success()
    }

}