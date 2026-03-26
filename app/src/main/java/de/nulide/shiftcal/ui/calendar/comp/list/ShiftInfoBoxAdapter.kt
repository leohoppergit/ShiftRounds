package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import java.time.LocalDate

class ShiftInfoBoxAdapter(
    val sc: SCRepoManager,
    val context: Context,
    val shiftInfoBoxContainer: RecyclerView?
) :
    RecyclerView.Adapter<ShiftInfoBoxViewHolder>() {

    val updateList = mutableListOf<Int>()

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
        val isUpdate = updateList.contains(position)
        if (isUpdate) {
            updateList.remove(position)
        }
        holder.bind(wdays[position], position, isUpdate)
    }

    fun updateData(newWDays: List<WorkDayDTO>) {
        // Add and update new
        val today = LocalDate.now()
        for ((i, wday) in newWDays.withIndex()) {
            if (i in wdays.indices) {
                val newWorkDay = wday.wday.day
                val oldWorkDay = wdays[i].wday.day
                if (wdays[i].shift != wday.shift || wdays[i].wday.icons != wday.wday.icons
                    || newWorkDay == today || oldWorkDay == today
                ) {
                    wdays[i] = wday
                    notifyItemChanged(i)
                    updateList.add(i)
                }
            } else {
                wdays.add(wday)
                notifyItemInserted(i)
            }
        }

        //RemoveOld
        for ((i, wday) in wdays.withIndex()) {
            if (i !in newWDays.indices) {
                val view =
                    shiftInfoBoxContainer?.findViewHolderForAdapterPosition(i) as ShiftInfoBoxViewHolder
                view.dismiss()
            }
        }

    }

    fun removeItem(wday: WorkDayDTO, position: Int) {
        wdays.remove(wday)
        notifyItemRemoved(position)
    }

    fun clear() {
        for (i in 0..itemCount) {
            val viewItem = shiftInfoBoxContainer?.findViewHolderForAdapterPosition(i)
            if (viewItem != null) {
                val view = viewItem as ShiftInfoBoxViewHolder
                view.dismiss()
            }
        }
    }
}