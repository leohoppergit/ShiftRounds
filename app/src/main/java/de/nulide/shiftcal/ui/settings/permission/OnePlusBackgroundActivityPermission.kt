package de.nulide.shiftcal.ui.settings.permission

import androidx.appcompat.app.AppCompatActivity
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionGrantedListener
import de.nulide.shiftcal.utils.permission.PermissionManager

class OnePlusBackgroundActivityPermission(
    val activity: AppCompatActivity,
    private val permissionManager: PermissionManager
) : Permission(activity, R.string.permission_oneplus_background_activity),
    PermissionGrantedListener {

    init {
        permissionManager.addPermissionGrantedListener(this)
    }

    override fun requestPerm() {
        permissionManager.request(Perm.ONEPLUS_BACKGROUND)
    }

    override fun checkPerm(): Boolean {
        return permissionManager.check(Perm.ONEPLUS_BACKGROUND)
    }

    override fun onPermissionGranted(perm: String) {
        updatePerm()
    }

    override fun onPermissionNotGranted(perm: String) {
    }

}