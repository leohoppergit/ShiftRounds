package de.nulide.shiftcal.utils.permission

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PermissionManager(
    private val activity: AppCompatActivity
) : PermissionGrantedListener, DefaultLifecycleObserver {

    private val permissionGrantedListeners = mutableListOf<PermissionGrantedListener?>()

    private val permissionLauncher: ActivityResultLauncher<String>
    private val multiPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var lastRequestedPermission = ""

    init {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                informGranted(lastRequestedPermission)
            } else {
                informNotGranted(lastRequestedPermission)
            }
        }
        multiPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.values.first()) {
                informGranted(lastRequestedPermission)
            } else {
                informNotGranted(lastRequestedPermission)
            }
        }
        activity.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
    }

    fun request(perm: String) {
        lastRequestedPermission = perm
        when (perm) {
            Perm.CALENDAR -> {
                multiPermissionLauncher.launch(
                    arrayOf(
                        Perm.CALENDAR,
                        Perm.CALENDAR_READ
                    )
                )
            }

            else -> {
                permissionLauncher.launch(perm)
            }
        }
    }

    fun check(perm: String): Boolean {
        return Perm.check(activity, perm)
    }

    fun addPermissionGrantedListener(permissionGrantedListener: PermissionGrantedListener) {
        this.permissionGrantedListeners.add(permissionGrantedListener)
    }

    fun informGranted(perm: String) {
        for (listener in permissionGrantedListeners) {
            listener?.onPermissionGranted(perm)
        }
    }

    fun informNotGranted(perm: String) {
        for (listener in permissionGrantedListeners) {
            listener?.onPermissionNotGranted(perm)
        }
    }

    override fun onPermissionGranted(perm: String) {
        informGranted(perm)
    }

    override fun onPermissionNotGranted(perm: String) {
        informNotGranted(perm)
    }
}
