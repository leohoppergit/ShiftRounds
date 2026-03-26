package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO
import de.nulide.shiftcal.ui.helper.OnItemClickedListener
import de.nulide.shiftcal.ui.helper.SpecialShifts

class ShiftBlockAdapter(
    val context: Context,
    val shiftBlock: ShiftBlockDTO,
    val limitPos: Int,
    val onItemClickedListener: OnItemClickedListener
) :
    RecyclerView.Adapter<ShiftBlockViewHolder>() {

    val sc = SCRepoManager.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftBlockViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_shift_block, parent, false)
        return ShiftBlockViewHolder(context, view, onItemClicked = onItemClickedListener)
    }

    override fun getItemCount(): Int {
        if (shiftBlock.getMaxDays() == limitPos) {
            return limitPos
        }
        return shiftBlock.getMaxDays() + 1
    }

    override fun onBindViewHolder(holder: ShiftBlockViewHolder, position: Int) {
        val shiftBlocksOnDay = shiftBlock.getAtPos(position)
        if (shiftBlocksOnDay.isEmpty()) {
            holder.bind(SpecialShifts.getCreateNewShiftNumbered(context, position + 1))
        } else {
            val shiftsOnDay = mutableListOf<Shift>()
            for (shiftBlockEntry in shiftBlocksOnDay) {
                shiftsOnDay.add(sc.shifts.get(shiftBlockEntry.shiftId))
            }
            holder.bind(shiftsOnDay)
        }
    }
}