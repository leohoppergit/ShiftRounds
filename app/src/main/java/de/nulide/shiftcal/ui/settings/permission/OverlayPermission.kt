package de.nulide.shiftcal.ui.settings.permission

import androidx.appcompat.app.AppCompatActivity
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionManager

class OverlayPermission(
    activity: AppCompatActivity,
    private val permissionManager: PermissionManager
) : Permission(activity, R.string.permission_overlay_title) {

    override fun requestPerm() {
        permissionManager.request(Perm.OVERLAY)
    }

    override fun checkPerm(): Boolean {
        return permissionManager.check(Perm.OVERLAY)
    }
}