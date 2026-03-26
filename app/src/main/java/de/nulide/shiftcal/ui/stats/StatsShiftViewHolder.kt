package de.nulide.shiftcal.ui.stats

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.utils.ColorHelper

class StatsShiftViewHolder(val context: Context, itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    private val shiftShortNameText: TextView = itemView.findViewById(R.id.shiftShortNameText)
    private val shiftNameText: TextView = itemView.findViewById(R.id.shiftNameText)
    private val shiftCount: TextView = itemView.findViewById(R.id.shiftCountText)

    fun bind(shift: Shift, shiftDayCount: Int) {
        shiftShortNameText.text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            shiftShortNameText.setTextColor(context.getColor(R.color.textColorBlack))
        } else {
            shiftShortNameText.setTextColor(context.getColor(R.color.textColorWhite))
        }
        shiftShortNameText.background.setTint(shift.color)
        shiftNameText.text = shift.name
        shiftCount.text = context.getString(R.string.numberformat, shiftDayCount)

    }

}