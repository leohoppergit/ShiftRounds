package de.nulide.shiftcal.ui.settings

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.ui.helper.OnCloseListener
import de.nulide.shiftcal.utils.Runner

class PrivacySettingsDialog(context: Context) : MaterialAlertDialogBuilder(context),
    DialogInterface.OnDismissListener, OnCloseListener {

    private var dialog: AlertDialog? = null
    var onCloseListener: OnCloseListener? = null

    init {
        val layoutInflater = LayoutInflater.from(context)
        val privacySettingsView =
            layoutInflater.inflate(R.layout.layout_privacy, null) as NestedScrollView
        setTitle(R.string.Settings_Privacy)
        setView(privacySettingsView)
        val privacySettingsHandler = PrivacySettingsHandler(context, privacySettingsView, false)
        privacySettingsHandler.onCloseListener = this
    }

    fun showIntro() {
        Runner.runLate {
            Runner.run {
                dialog = super.show()
                dialog?.setOnDismissListener(this@PrivacySettingsDialog)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onCloseListener?.onClose()
    }

    override fun onClose() {
        dialog?.dismiss()
    }

}