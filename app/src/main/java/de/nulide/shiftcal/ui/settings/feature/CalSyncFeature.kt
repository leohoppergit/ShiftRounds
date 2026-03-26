package de.nulide.shiftcal.ui.settings.feature

import android.content.Context
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.sync.CalendarController
import de.nulide.shiftcal.sync.SyncHandler
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionManager

class CalSyncFeature(
    context: Context,
    permissionManager: PermissionManager,
    featureStateListener: FeatureStateListener
) : Feature(
    Settings.SYNC,
    mutableListOf(Perm.CALENDAR),
    context, permissionManager, featureStateListener
) {

    override fun afterEnabled() {
        super.afterEnabled()
        SyncHandler.sync(context)
    }

    override fun afterDisabled() {
        CalendarController.deleteCalendar(context)
    }
}