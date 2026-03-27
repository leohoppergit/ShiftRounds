package de.nulide.shiftcal.utils.permission

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PermissionManager(
    private val activity: AppCompatActivity
) : PermissionGrantedListener, DefaultLifecycleObserver {

    private val permissionGrantedListeners = mutableListOf<PermissionGrantedListener?>()

    private val permissionLauncher: ActivityResultLauncher<String>
    private val multiPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val intentLauncher: ActivityResultLauncher<Intent>
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
        intentLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { _ ->
            run {}
        }
        activity.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
    }

    @SuppressLint("BatteryLife")
    fun request(perm: String) {
        lastRequestedPermission = perm
        when (perm) {
            Perm.ONEPLUS_BACKGROUND -> {
                OnePlusBackgroundActivityPermissionDialog(
                    activity,
                    this
                ).show()
            }

            Perm.OVERLAY -> {
                val intent = Intent(perm, ("package:" + activity.packageName).toUri())
                intentLauncher.launch(intent)
            }

            Perm.BAT -> {
                val intent =
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.setData(("package:" + activity.packageName).toUri())
                intentLauncher.launch(intent)
            }

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
