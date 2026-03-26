package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.InsertShiftBlockUseCase
import de.nulide.shiftcal.ui.editor.list.ShiftBlockGradientBackground
import de.nulide.shiftcal.ui.editor.list.ShiftBlockListAdapter
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.utils.Runner

class ShiftBlockSelector @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ExtendedFloatingActionButton(context, attrs), ViewModelReceiver, View.OnClickListener,
    AdapterView.OnItemClickListener {

    private val sc = SCRepoManager.getInstance(context)
    private lateinit var calViewModel: CalViewModel

    private val shiftBlockSelectorDefaultBackground = background.mutate()
    private val shiftBlockSelectorColorList = backgroundTintList
    private val defaultTextColor = currentTextColor

    private var shiftBlockSelectorAdapter: ShiftBlockListAdapter? = null
    private var shiftSelectorDialog: AlertDialog? = null
    private var selectedShiftBlockID = -1

    private lateinit var insertShiftBlockUseCase: InsertShiftBlockUseCase


    init {
        updateVisibility()
        setOnClickListener(this)
    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        insertShiftBlockUseCase = InsertShiftBlockUseCase(sc, calViewModel)
        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            insertShiftBlockUseCase.insertInto(day, selectedShiftBlockID)
        }
        calViewModel.register(lifecycle, calViewModel.shiftSelected) {
            revertShiftBlockSelectorFab()
        }
        calViewModel.register(lifecycle, calViewModel.resume) {
            updateVisibility()
        }
    }

    override fun onClick(p0: View?) {
        val dialogLayout =
            LayoutInflater.from(context).inflate(R.layout.dialog_shift_selector, null)
        val builder = MaterialAlertDialogBuilder(context)
        val listViewShifts = dialogLayout as ListView

        Runner.runCo {
            shiftBlockSelectorAdapter = ShiftBlockListAdapter(context, sc.shiftBlocks.getAll())
            listViewShifts.adapter = shiftBlockSelectorAdapter
            listViewShifts.onItemClickListener = this
            builder.setView(dialogLayout)
            shiftSelectorDialog = builder.create()
            shiftSelectorDialog?.show()
        }
    }

    fun updateVisibility() {
        if (sc.shiftBlocks.hasAny()) {
            visibility = VISIBLE
        } else {
            visibility = GONE
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val shiftBlock = shiftBlockSelectorAdapter?.getItem(position)
        if (shiftBlock != null) {
            calViewModel.trigger(calViewModel.shiftBlockSelected)
            selectedShiftBlockID = shiftBlock.block.id
            setShiftBlockSelectorFab(shiftBlock)
        }
        shiftSelectorDialog?.dismiss()
    }

    private fun setShiftBlockSelectorFab(shiftBlock: ShiftBlockDTO) {
        text = shiftBlock.block.name
        icon = null
        backgroundTintList = null
        background = ShiftBlockGradientBackground.getBackground(context, shiftBlock)
    }

    fun revertShiftBlockSelectorFab() {
        text = ContextCompat.getString(context, R.string.shift_block_creator_shift_block_title)
        setTextColor(defaultTextColor)
        icon = AppCompatResources.getDrawable(context, R.drawable.ic_multi_shift_fab)
        background = shiftBlockSelectorDefaultBackground.constantState?.newDrawable()
        backgroundTintList = shiftBlockSelectorColorList
        selectedShiftBlockID = SpecialShifts.NONE_ID
    }

}