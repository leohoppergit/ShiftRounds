package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
class ShiftInfoBoxAdapter(
    val sc: SCRepoManager,
    val context: Context,
    val shiftInfoBoxContainer: RecyclerView?
) :
    RecyclerView.Adapter<ShiftInfoBoxViewHolder>() {

    val wdays = mutableListOf<WorkDayDTO>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftInfoBoxViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_shift_info_box, parent, false)
        val shiftInfoBoxViewHolder = ShiftInfoBoxViewHolder(sc, context, view, this)
        return shiftInfoBoxViewHolder
    }

    override fun getItemCount(): Int {
        return wdays.size
    }

    override fun onBindViewHolder(holder: ShiftInfoBoxViewHolder, position: Int) {
        holder.bind(wdays[position], position)
    }

    fun updateData(newWDays: List<WorkDayDTO>) {
        wdays.clear()
        wdays.addAll(newWDays)
        notifyDataSetChanged()
    }

    fun removeItem(wday: WorkDayDTO, position: Int) {
        wdays.remove(wday)
        notifyItemRemoved(position)
    }

    fun clear() {
        if (wdays.isEmpty()) return
        wdays.clear()
        notifyDataSetChanged()
    }
}
