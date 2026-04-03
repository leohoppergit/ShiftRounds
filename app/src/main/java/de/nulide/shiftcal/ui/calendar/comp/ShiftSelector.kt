package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.google.android.material.button.MaterialButton
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
import kotlin.math.min
import kotlin.math.roundToInt

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
        val listViewShifts = dialogLayout.findViewById<ListView>(R.id.shift_selector_layout)
        val deleteButton = dialogLayout.findViewById<MaterialButton>(R.id.shift_selector_delete_button)
        val createButton = dialogLayout.findViewById<MaterialButton>(R.id.shift_selector_create_button)
        val shifts = sc.shifts.getNotArchived().toMutableList()

        shiftSelectorAdapter = ShiftAdapter(context, shifts)
        listViewShifts.adapter = shiftSelectorAdapter
        constrainListHeight(listViewShifts, shifts.size)
        listViewShifts.onItemClickListener = this
        deleteButton.setOnClickListener {
            calViewModel.trigger(calViewModel.shiftSelected)
            setShiftSelectorFab(SpecialShifts.getDeleteShift(context))
            selectedShiftID = SpecialShifts.DELETE_ID
            shiftSelectorDialog?.dismiss()
        }
        createButton.setOnClickListener {
            addInitiated = true
            lastShiftSize = sc.shifts.getNotArchived().size
            val intent = Intent(context, ShiftCreatorActivity::class.java)
            intent.putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, SpecialShifts.NONE_ID)
            context.startActivity(intent)
            shiftSelectorDialog?.dismiss()
        }
        builder.setView(dialogLayout)
        shiftSelectorDialog = builder.create()
        shiftSelectorDialog?.show()
    }

    private fun constrainListHeight(listView: ListView, itemCount: Int) {
        val density = resources.displayMetrics.density
        val estimatedRowHeightPx = (92f * density).roundToInt()
        val maxVisibleHeight = (resources.displayMetrics.heightPixels * 0.48f).roundToInt()
        val desiredHeight = min(itemCount * estimatedRowHeightPx, maxVisibleHeight)
        listView.layoutParams = listView.layoutParams.apply {
            height = desiredHeight.coerceAtLeast(estimatedRowHeightPx)
        }
    }

    fun revertShiftSelectorFab() {
        extend()
        text = ContextCompat.getString(context, R.string.shift_creator_shift_title)
        setTextColor(defaultTextColor)
        icon = AppCompatResources.getDrawable(context, R.drawable.ic_shift_fab)
        iconTint = ContextCompat.getColorStateList(context, R.color.shiftRoundsActionInk)
        background = shiftSelectorDefaultBackground.constantState?.newDrawable()
        selectedShiftID = -1
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val shift = shiftSelectorAdapter.getItem(position)
        calViewModel.trigger(calViewModel.shiftSelected)
        setShiftSelectorFab(shift)
        selectedShiftID = shift.id
        shiftSelectorDialog?.dismiss()
    }

    fun checkForAddInitiated() {
        if (calViewModel.getEditMode() && addInitiated) {
            val currentShifts = sc.shifts.getNotArchived()
            if (lastShiftSize < currentShifts.size) {
                val newShift = currentShifts.maxByOrNull { it.id } ?: return
                setShiftSelectorFab(newShift)
                addInitiated = false
                selectedShiftID = newShift.id
            }
        }
    }

    private fun setShiftSelectorFab(shift: Shift) {
        background = shiftSelectorDefaultBackground.constantState?.newDrawable()
        background.setTint(shift.color)
        if (shift.id == SpecialShifts.DELETE_ID) {
            shrink()
            text = ""
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_delete)
            if (ColorHelper.isTooBright(shift.color)) {
                setTextColor(context.getColor(R.color.textColorBlack))
                iconTint = ContextCompat.getColorStateList(context, R.color.textColorBlack)
            } else {
                setTextColor(context.getColor(R.color.textColorWhite))
                iconTint = ContextCompat.getColorStateList(context, R.color.textColorWhite)
            }
        } else {
            extend()
            icon = null
            text = shift.shortName
            if (ColorHelper.isTooBright(shift.color)) {
                setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                setTextColor(context.getColor(R.color.textColorWhite))
            }
        }
    }

}
