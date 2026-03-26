package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R

class WarningDialog(context: Context, iconRes: Int, message: String) :
    MaterialAlertDialogBuilder(context), DialogInterface.OnClickListener {

    companion object {
        val ICON_WARNING = R.drawable.ic_warning
        val ICON_INFO = R.drawable.ic_info
    }

    constructor(context: Context, message: String) : this(context, ICON_WARNING, message)

    init {
        setTitle(R.string.warning)
        setMessage(message)
        setIcon(iconRes)
    }

    fun enablePositiveButton(listener: () -> Unit): WarningDialog {
        setPositiveButton(R.string.yes) { dialog, _ ->
            listener()
            dialog.dismiss()
        }
        return this
    }

    fun enableSaveButton(listener: (() -> Unit)): WarningDialog {
        setNeutralButton(R.string.save) { dialog, _ ->
            listener()
            dialog.dismiss()
        }
        return this
    }

    fun enableNeutralButton(): WarningDialog {
        setNeutralButton(R.string.ok, this)
        return this
    }

    fun enableNegativeButton() {
        setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        dialog?.dismiss()
    }

}