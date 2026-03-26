package de.nulide.shiftcal.ui.settings.permission

import androidx.appcompat.app.AppCompatActivity
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionManager

class DisableBatteryPermission(
    val activity: AppCompatActivity,
    private val permissionManager: PermissionManager
) : Permission(activity, R.string.permission_battery_title) {

    override fun requestPerm() {
        permissionManager.request(Perm.BAT)
    }

    override fun checkPerm(): Boolean {
        return permissionManager.check(Perm.BAT)
    }

}