package de.nulide.shiftcal.ui.settings.feature

import android.content.Context
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.utils.permission.PermissionGrantedListener
import de.nulide.shiftcal.utils.permission.PermissionManager

open class Feature(
    val setting: String,
    val permissions: MutableList<String>,
    val context: Context,
    val permissionManager: PermissionManager,
    val featureStateListener: FeatureStateListener
) : PermissionGrantedListener {

    companion object {
        enum class STATE { ENABLED, DISABLED }
    }

    val settings = SettingsRepository.getInstance(context)

    init {
        permissionManager.addPermissionGrantedListener(this)
    }

    fun isEnabled(): Boolean {
        val isIt = settings.getBoolean(setting)
        if (isPermitted()) {
            return isIt
        } else {
            if (isIt) {
                disable()
            }
            return false
        }
    }

    fun isPermitted(): Boolean {
        var isIt = true
        for (perm in permissions) {
            isIt = isIt && permissionManager.check(perm)
        }
        return isIt
    }

    fun enable(): Boolean {
        if (isPermitted()) {
            settings.set(setting, true)
            afterEnabled()
            featureStateListener.onFeatureStateChanged(STATE.ENABLED)
            return true
        } else {
            request()
        }
        return false
    }

    open fun afterEnabled() {

    }

    open fun afterDisabled() {

    }

    fun disable() {
        settings.set(setting, false)
        afterDisabled()
        featureStateListener.onFeatureStateChanged(STATE.DISABLED)
    }

    open fun request() {
        if (permissions.size == 1) {
            permissionManager.request(permissions[0])
        }
    }

    override fun onPermissionGranted(perm: String) {
        if (perm in permissions) {
            enable()
        }
    }

    override fun onPermissionNotGranted(perm: String) {
        if (perm in permissions) {
            disable()
        }
    }
}