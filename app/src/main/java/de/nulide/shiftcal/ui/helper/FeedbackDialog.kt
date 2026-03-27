package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R

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
                if (msg.isNotEmpty()) {
                    Toast.makeText(context, R.string.feedback_thanks, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}
