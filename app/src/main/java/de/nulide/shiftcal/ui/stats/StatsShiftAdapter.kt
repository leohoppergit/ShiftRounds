package de.nulide.shiftcal.ui.stats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay


class StatsShiftAdapter(
    val context: Context,
    val shifts: List<Shift>,
    val workDays: List<WorkDay>
) :
    RecyclerView.Adapter<StatsShiftViewHolder>() {

    val sortedMap: Map<Int, Int>

    init {
        val shiftCounterMap: MutableMap<Int, Int> = mutableMapOf()
        for (wday in workDays) {
            shiftCounterMap[wday.shiftId] = shiftCounterMap[wday.shiftId]?.plus(1) ?: 1
        }
        sortedMap = shiftCounterMap.entries.sortedByDescending { it.value }
            .associate { it.toPair() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsShiftViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_stats_shift, parent, false)
        return StatsShiftViewHolder(context, view)
    }

    override fun getItemCount(): Int {
        return sortedMap.size
    }

    override fun onBindViewHolder(holder: StatsShiftViewHolder, position: Int) {
        val shift = shifts.find { it.id == sortedMap.keys.toList()[position] }
        if (shift != null) {
            val shiftCount = sortedMap[shift.id] ?: 0
            holder.bind(shift, shiftCount)
        }
    }
}