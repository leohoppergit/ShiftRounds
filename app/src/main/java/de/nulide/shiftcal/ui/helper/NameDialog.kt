package de.nulide.shiftcal.ui.helper

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.net.ShiftSwiftServerRestApi

class NameDialog(activity: AppCompatActivity, onCloseListener: OnCloseListener?) :
    MaterialAlertDialogBuilder(activity) {

    init {
        val nameView = activity.layoutInflater.inflate(R.layout.dialog_content_name, null)
        val nameEdit = nameView.findViewById<EditText>(R.id.nameEdit)

        val sc = SCRepoManager.getInstance(activity)
        nameEdit.setText(sc.users.getName())

        val restApi = ShiftSwiftServerRestApi(context)

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.family_sync_name_selector_title)
            .setMessage(R.string.family_sync_name_description)
            .setView(nameView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val newName = nameEdit.text.toString()
                if (newName.isNotEmpty()) {
                    val sc = SCRepoManager.getInstance(activity)
                    sc.users.setName(newName)
                    if (sc.calendar.getNonLocal() != null) {
                        val subscribed = sc.users.getSubscribed()
                        if (subscribed != null) {
                            restApi.setSpectatorName(subscribed.netUuid!!, newName, null, null)
                        }
                        var localUuid = sc.users.getLocal().netUuid
                        if (localUuid != null) {
                            restApi.setSharingName(localUuid, newName, null, null)
                        }
                    }
                    onCloseListener?.onClose()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}