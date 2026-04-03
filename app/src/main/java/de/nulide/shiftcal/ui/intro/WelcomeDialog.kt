package de.nulide.shiftcal.ui.intro

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.ui.helper.OnCloseListener

class WelcomeDialog(context: Context) : MaterialAlertDialogBuilder(context), View.OnClickListener,
    DialogInterface.OnDismissListener {

    private lateinit var dialog: AlertDialog

    var onCloseListener: OnCloseListener? = null
        set(value) {
            field = value
        }

    init {
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_welcome, FrameLayout(context), false)
        setView(dialogView)

        val welcomeIntroButton = dialogView.findViewById<Button>(R.id.welcomeIntroButton)
        welcomeIntroButton.setOnClickListener(this)
    }

    override fun show(): AlertDialog {
        dialog = super.show()
        dialog.setOnDismissListener(this)
        return dialog
    }

    override fun onClick(v: View?) {
        dialog.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onCloseListener?.onClose()
    }


}
