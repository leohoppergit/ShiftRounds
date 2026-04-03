package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
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
        val oldWDays = wdays.toList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldWDays.size

            override fun getNewListSize(): Int = newWDays.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldWDays[oldItemPosition]
                val newItem = newWDays[newItemPosition]
                return oldItem.wday.calendarId == newItem.wday.calendarId &&
                    oldItem.wday.id == newItem.wday.id &&
                    oldItem.shift.id == newItem.shift.id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldWDays[oldItemPosition] == newWDays[newItemPosition]
            }
        })
        wdays.clear()
        wdays.addAll(newWDays)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItem(wday: WorkDayDTO, position: Int) {
        wdays.remove(wday)
        notifyItemRemoved(position)
    }

    fun clear() {
        if (wdays.isEmpty()) return
        updateData(emptyList())
    }
}
