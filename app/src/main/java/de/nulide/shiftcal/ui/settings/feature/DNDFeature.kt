package de.nulide.shiftcal.ui.settings.feature

import android.content.Context
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionManager

class DNDFeature(
    context: Context, permissionManager: PermissionManager,
    featureStateListener: FeatureStateListener
) : Feature(
    Settings.DND,
    mutableListOf(Perm.DND),
    context, permissionManager, featureStateListener
)