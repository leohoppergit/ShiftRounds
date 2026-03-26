package de.nulide.shiftcal.utils.permission

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository

class OnePlusBackgroundActivityPermissionDialog(
    context: Context,
    val permissionGrantedListener: PermissionGrantedListener?
) : MaterialAlertDialogBuilder(context),
    View.OnClickListener, DialogInterface.OnDismissListener {

    private val settings = SettingsRepository.getInstance(context)

    init {
        setTitle(R.string.permission_oneplus_background_activity)
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_with_button, null) as ScrollView
        setView(dialogView)

        val content = dialogView.findViewById<TextView>(R.id.content)
        content.text =
            context.getString(R.string.permission_oneplus_background_activity_description)

        val button = dialogView.findViewById<Button>(R.id.button)
        button.text = context.getString(R.string.perm_dialog_fix)
        button.setOnClickListener(this)

        setPositiveButton(R.string.perm_dialog_already_fixed) { _, _ ->
            settings.set(Settings.PERM_ONE_PLUS_BACKGROUND_ACTIVITY, true)
        }

        setOnDismissListener(this)
    }

    override fun onClick(v: View?) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.setData(uri)
        context.startActivity(intent)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (settings.getBoolean(Settings.PERM_ONE_PLUS_BACKGROUND_ACTIVITY)) {
            permissionGrantedListener?.onPermissionGranted(Settings.PERM_ONE_PLUS_BACKGROUND_ACTIVITY)
        }
    }

}