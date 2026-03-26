package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.InsertShiftUseCase
import de.nulide.shiftcal.ui.editor.ShiftCreatorActivity
import de.nulide.shiftcal.ui.editor.list.ShiftAdapter
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.utils.ColorHelper

class ShiftSelector @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ExtendedFloatingActionButton(context, attrs), ViewModelReceiver, View.OnClickListener,
    AdapterView.OnItemClickListener {

    private val sc = SCRepoManager.getInstance(context)
    private lateinit var calViewModel: CalViewModel

    lateinit var shiftSelectorAdapter: ShiftAdapter

    private var shiftSelectorDialog: androidx.appcompat.app.AlertDialog? = null
    private var shiftSelectorDefaultBackground = background.mutate()
    private var defaultTextColor = currentTextColor

    var addInitiated = false
    var lastShiftSize = 0
    private var selectedShiftID = -1

    private lateinit var insertShiftUseCase: InsertShiftUseCase


    init {
        setOnClickListener(this)
    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        insertShiftUseCase =
            InsertShiftUseCase(sc, calViewModel, SettingsRepository.getInstance(context))
        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            insertShiftUseCase.insertInto(day, selectedShiftID)
        }
        calViewModel.register(lifecycle, calViewModel.shiftBlockSelected) {
            revertShiftSelectorFab()
        }
        calViewModel.register(lifecycle, calViewModel.resume) { checkForAddInitiated() }
    }

    override fun onClick(p0: View?) {
        val dialogLayout =
            LayoutInflater.from(context).inflate(R.layout.dialog_shift_selector, null)
        val builder = MaterialAlertDialogBuilder(context)
        val listViewShifts = dialogLayout as ListView
        val shifts = sc.shifts.getNotArchived().toMutableList()

        shifts.add(SpecialShifts.getDeleteShift(context))
        shifts.add(SpecialShifts.getCreateNewShift(context))
        shiftSelectorAdapter = ShiftAdapter(context, shifts)
        listViewShifts.adapter = shiftSelectorAdapter
        listViewShifts.onItemClickListener = this
        builder.setView(dialogLayout)
        shiftSelectorDialog = builder.create()
        shiftSelectorDialog?.show()
    }

    fun revertShiftSelectorFab() {
        text = ContextCompat.getString(context, R.string.shift_creator_shift_title)
        setTextColor(defaultTextColor)
        icon = AppCompatResources.getDrawable(context, R.drawable.ic_shift_fab)
        background = shiftSelectorDefaultBackground.constantState?.newDrawable()
        selectedShiftID = -1
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val shift = shiftSelectorAdapter.getItem(position)
        if (shift == SpecialShifts.getCreateNewShift(context)) {
            addInitiated = true
            lastShiftSize = sc.shifts.getNotArchived().size
            val intent = Intent(context, ShiftCreatorActivity::class.java)
            intent.putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, SpecialShifts.NONE_ID)
            context.startActivity(intent)
        } else {
            calViewModel.trigger(calViewModel.shiftSelected)
            setShiftSelectorFab(shift)
            selectedShiftID = shift.id
        }
        shiftSelectorDialog?.dismiss()
    }

    fun checkForAddInitiated() {
        if (calViewModel.getEditMode() && addInitiated) {
            val currentShifts = sc.shifts.getNotArchived()
            if (lastShiftSize < currentShifts.size) {
                val newShift = currentShifts[currentShifts.size - 1]
                setShiftSelectorFab(newShift)
                addInitiated = false
                selectedShiftID = newShift.id
            }
        }
    }

    private fun setShiftSelectorFab(shift: Shift) {
        background = shiftSelectorDefaultBackground.constantState?.newDrawable()
        icon = null

        background.setTint(shift.color)
        text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            setTextColor(context.getColor(R.color.textColorBlack))
        } else {
            setTextColor(context.getColor(R.color.textColorWhite))
        }
    }

}