package de.nulide.shiftcal.ui.settings.permission

import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.Device
import de.nulide.shiftcal.utils.permission.PermissionManager

class PermDialogBuilder(val activity: AppCompatActivity, val permissionManager: PermissionManager) :
    MaterialAlertDialogBuilder(activity),
    OnDismissListener, DialogInterface.OnCancelListener, DefaultLifecycleObserver {

    private var disableBatteryPermission: DisableBatteryPermission
    private var notificationPermission: NotificationPermission
    private var overlayPermission: OverlayPermission
    private var onePlusBackgroundActivityPermission: OnePlusBackgroundActivityPermission? = null

    private var dialog: AlertDialog? = null

    private var activityOnDismissListener: OnDismissListener? = null

    init {
        setTitle(activity.getString(R.string.permission_dialog_title))
        val layoutInflater = LayoutInflater.from(activity)
        val dialogView =
            layoutInflater.inflate(R.layout.dialog_alarm_permissions, null) as LinearLayout
        setView(dialogView)
        setPositiveButton(activity.getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            this.dialog = null
            onCancel(dialog)
        }
        setOnDismissListener(this)
        setOnCancelListener(this)
        disableBatteryPermission = DisableBatteryPermission(activity, permissionManager)
        disableBatteryPermission.updatePerm()
        dialogView.addView(disableBatteryPermission.getView())

        notificationPermission = NotificationPermission(activity, permissionManager)
        notificationPermission.updatePerm()
        dialogView.addView(notificationPermission.getView())

        overlayPermission = OverlayPermission(activity, permissionManager)
        overlayPermission.updatePerm()
        dialogView.addView(overlayPermission.getView())

        if (Device.isOnePlus()) {
            onePlusBackgroundActivityPermission =
                OnePlusBackgroundActivityPermission(activity, permissionManager)
            onePlusBackgroundActivityPermission?.updatePerm()
            dialogView.addView(onePlusBackgroundActivityPermission?.getView())
        }

        activity.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        disableBatteryPermission.updatePerm()
        notificationPermission.updatePerm()
        overlayPermission.updatePerm()
        onePlusBackgroundActivityPermission?.updatePerm()
    }

    override fun show(): AlertDialog {
        if (dialog == null) {
            dialog = super.show()
        } else {
            onResume(activity)
        }
        return dialog!!
    }


    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.activityOnDismissListener = onDismissListener
    }

    override fun onDismiss(dialog: DialogInterface?) {
        this.dialog = null
        activityOnDismissListener?.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface?) {
        this.dialog = null
        activityOnDismissListener?.onDismiss(dialog)
    }
}
