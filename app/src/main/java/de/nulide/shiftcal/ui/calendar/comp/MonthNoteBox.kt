package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.MonthNote
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.CompMonthNoteBoxBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.ExpandableLinearLayout
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.OnExpandableLinearLayoutExpandedListener
import de.nulide.shiftcal.utils.Runner
import java.time.YearMonth

class MonthNoteBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ExpandableLinearLayout(context, attrs), View.OnClickListener,
    OnExpandableLinearLayoutExpandedListener,
    ViewModelReceiver {

    val binding: CompMonthNoteBoxBinding
    val sc = SCRepoManager.getInstance(context)
    lateinit var calViewModel: CalViewModel

    init {
        val layoutInflator = LayoutInflater.from(context)
        binding = CompMonthNoteBoxBinding.inflate(layoutInflator, this, true)
        binding.monthNoteBox.setOnClickListener(this)
    }

    fun editMonthNote() {
        val yearMonth = calViewModel.getCurrentMonth()
        val noteEditView =
            LayoutInflater.from(context).inflate(R.layout.dialog_content_month_note, null)
        val noteEdit = noteEditView.findViewById<EditText>(R.id.monthNoteEdit)
        val note: MonthNote? =
            sc.monthNotes.get(yearMonth.year, yearMonth.monthValue)
        if (note != null) {
            noteEdit.setText(note.msg)
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.fab_month_note)
            .setMessage(R.string.month_note_description)
            .setView(noteEditView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val noteText = noteEdit.text.toString()
                Runner.runCo {
                    sc.monthNotes.set(
                        yearMonth.year,
                        yearMonth.month.value,
                        noteText
                    )
                    updateMonthNote(yearMonth)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun updateMonthNote(yearMonth: YearMonth) {
        Runner.runCo {
            val note: MonthNote? = sc.monthNotes.get(yearMonth.year, yearMonth.monthValue)
            if (note != null) {
                binding.monthNoteText.text = note.msg
                binding.monthNoteBox.setOnExpandableLinerLayoutExpandedListener(this)
                binding.monthNoteBox.expand()
            } else {
                hide()
            }
        }
    }

    fun hide() {
        binding.monthNoteBox.collapse()
        binding.monthNoteText.text = ""
    }

    override fun onExpanded(expandableLinearLayout: ExpandableLinearLayout) {
        binding.monthNoteBox.expand()
    }

    override fun onClick(p0: View?) {
        if (!sc.familyMode) {
            editMonthNote()
        }
    }

    override fun receiveViewModel(lifecycle: Lifecycle, calViewModel: CalViewModel) {
        this.calViewModel = calViewModel
        calViewModel.register(lifecycle, calViewModel.newMonth) { yearMonth ->
            updateMonthNote(yearMonth)
        }
        calViewModel.register(lifecycle, calViewModel.editMonthNote) { onClick(null) }

        calViewModel.register(lifecycle, calViewModel.switchDisp) { disp ->
            if (disp == DISP.WEEK) {
                hide()
            }
        }
    }

}