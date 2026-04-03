package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay
import java.util.Locale

class WorkDayDetailsDialog(
    private val context: Context,
    private val shift: Shift,
    private val workDay: WorkDay,
    private val onSave: (WorkDay) -> Unit
) {

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_work_day_details, null)
        val noteEdit = dialogView.findViewById<EditText>(R.id.workDayNoteEdit)
        val overtimeHoursEdit = dialogView.findViewById<EditText>(R.id.workDayOvertimeHoursEdit)
        val overtimeMinutesEdit = dialogView.findViewById<EditText>(R.id.workDayOvertimeMinutesEdit)
        val timeTypeGroup = dialogView.findViewById<RadioGroup>(R.id.workDayTimeTypeGroup)
        val overtimeOption = dialogView.findViewById<RadioButton>(R.id.workDayOvertimeOption)
        val lessWorkOption = dialogView.findViewById<RadioButton>(R.id.workDayLessWorkOption)
        val multiplierInfoText = dialogView.findViewById<TextView>(R.id.workDayMultiplierInfoText)
        val multiplierPreviewText = dialogView.findViewById<TextView>(R.id.workDayMultiplierPreviewText)

        noteEdit.setText(workDay.note)
        val absoluteMinutes = kotlin.math.abs(workDay.overtimeMinutes)
        if (workDay.overtimeMinutes < 0) {
            lessWorkOption.isChecked = true
        } else {
            overtimeOption.isChecked = true
        }
        if (absoluteMinutes > 0) {
            val hours = absoluteMinutes / 60
            val minutes = absoluteMinutes % 60
            if (hours > 0) {
                overtimeHoursEdit.setText(formatWholeNumber(hours))
            }
            if (minutes > 0) {
                overtimeMinutesEdit.setText(formatWholeNumber(minutes))
            }
        } else {
            timeTypeGroup.check(overtimeOption.id)
        }

        fun updateMultiplierInfo() {
            if (shift.overtimeMultiplier == 1.0) {
                multiplierInfoText.visibility = View.GONE
                multiplierPreviewText.visibility = View.GONE
                return
            }

            multiplierInfoText.visibility = View.VISIBLE
            multiplierInfoText.text = context.getString(
                R.string.work_day_details_multiplier_active,
                formatMultiplier(shift.overtimeMultiplier)
            )

            val hours = overtimeHoursEdit.text.toString().toIntOrNull() ?: 0
            val minutesInput = overtimeMinutesEdit.text.toString().toIntOrNull() ?: 0
            val totalMinutes = (hours * 60) + minutesInput
            val showPreview = timeTypeGroup.checkedRadioButtonId == overtimeOption.id && totalMinutes > 0
            if (!showPreview) {
                multiplierPreviewText.visibility = View.GONE
                return
            }

            val adjusted = shift.adjustedOvertimeMinutes(totalMinutes)
            multiplierPreviewText.visibility = View.VISIBLE
            multiplierPreviewText.text = context.getString(
                R.string.work_day_details_multiplier_preview,
                totalMinutes / 60,
                totalMinutes % 60,
                adjusted / 60,
                adjusted % 60
            )
        }

        val multiplierWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateMultiplierInfo()
            }
        }
        overtimeHoursEdit.addTextChangedListener(multiplierWatcher)
        overtimeMinutesEdit.addTextChangedListener(multiplierWatcher)
        timeTypeGroup.setOnCheckedChangeListener { _, _ -> updateMultiplierInfo() }
        updateMultiplierInfo()

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.work_day_details_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val hours = overtimeHoursEdit.text.toString().toIntOrNull() ?: 0
                val minutesInput = overtimeMinutesEdit.text.toString().toIntOrNull() ?: 0
                val totalMinutes = (hours * 60) + minutesInput
                val normalizedMinutes = if (timeTypeGroup.checkedRadioButtonId == lessWorkOption.id) {
                    -totalMinutes
                } else {
                    totalMinutes
                }

                workDay.note = noteEdit.text.toString().trim()
                workDay.overtimeMinutes = normalizedMinutes
                onSave(workDay)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun formatMultiplier(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.1f", value)
        } else {
            value.toString()
        }
    }

    private fun formatWholeNumber(value: Int): String {
        return String.format(Locale.getDefault(), "%d", value)
    }
}
