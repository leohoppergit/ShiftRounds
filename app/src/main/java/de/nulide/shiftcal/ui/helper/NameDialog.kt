package de.nulide.shiftcal.ui.helper

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager

class NameDialog(activity: AppCompatActivity, onCloseListener: OnCloseListener?) :
    MaterialAlertDialogBuilder(activity) {

    init {
        val nameView = activity.layoutInflater.inflate(R.layout.dialog_content_name, null)
        val nameEdit = nameView.findViewById<EditText>(R.id.nameEdit)

        val sc = SCRepoManager.getInstance(activity)
        nameEdit.setText(sc.users.getName())

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.name_dialog_title)
            .setMessage(R.string.name_dialog_description)
            .setView(nameView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val newName = nameEdit.text.toString()
                if (newName.isNotEmpty()) {
                    val sc = SCRepoManager.getInstance(activity)
                    sc.users.setName(newName)
                    onCloseListener?.onClose()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}
