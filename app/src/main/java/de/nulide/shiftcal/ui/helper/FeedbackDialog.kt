package de.nulide.shiftcal.ui.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.BuildConfig
import de.nulide.shiftcal.R

class FeedbackDialog(context: Context) {

    companion object {
        private const val FEEDBACK_MAIL = "shiftrounds@tutamail.com"
    }

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
                    openMailDraft(context, msg)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun openMailDraft(context: Context, msg: String) {
        val body = buildString {
            append(context.getString(R.string.feedback_email_intro))
            append("\n\n")
            append("ShiftRounds-Version: ").append(BuildConfig.VERSION_NAME).append('\n')
            append("Android-Version: ").append(Build.VERSION.RELEASE).append('\n')
            append('\n')
            append(context.getString(R.string.feedback_email_message_label)).append('\n')
            append(msg.trim())
        }

        val mailUri = Uri.fromParts("mailto", FEEDBACK_MAIL, null)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = mailUri
            putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_MAIL))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_email_subject))
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
            Toast.makeText(context, R.string.feedback_thanks, Toast.LENGTH_SHORT).show()
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.feedback_no_mail_app, Toast.LENGTH_LONG).show()
        }
    }
}
