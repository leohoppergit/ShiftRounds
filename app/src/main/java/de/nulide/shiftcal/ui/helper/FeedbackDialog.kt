package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.ACRAHelper

class FeedbackDialog(context: Context) {

    init {
        val feedbackView =
            LayoutInflater.from(context).inflate(R.layout.dialog_content_feedback, null)
        val feedbackEdit = feedbackView.findViewById<EditText>(R.id.feedbackEdit)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.Settings_Feedback)
            .setMessage(R.string.feedback_description)
            .setView(feedbackView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val msg = feedbackEdit.text.toString()
                val now = System.currentTimeMillis()
                if (msg.isNotEmpty()) {
                    ACRAHelper.sendFeedback(msg)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}