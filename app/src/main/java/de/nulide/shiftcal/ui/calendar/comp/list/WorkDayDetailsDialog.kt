package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.WorkDay

class WorkDayDetailsDialog(
    private val context: Context,
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
                overtimeHoursEdit.setText(hours.toString())
            }
            if (minutes > 0) {
                overtimeMinutesEdit.setText(minutes.toString())
            }
        } else {
            timeTypeGroup.check(overtimeOption.id)
        }

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
}
