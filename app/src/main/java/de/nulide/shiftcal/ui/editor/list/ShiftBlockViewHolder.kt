package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.utils.ColorHelper

class ShiftBlockViewHolder(val context: Context, itemView: View, onItemClicked: (Int) -> Unit) :
    RecyclerView.ViewHolder(itemView) {

    private val shiftText: TextView = itemView.findViewById(R.id.shiftText)
    private val secondShiftText: TextView = itemView.findViewById(R.id.secondShiftText)
    private val shiftContainer: LinearLayout = itemView.findViewById(R.id.shiftDayContainer)

    init {
        itemView.setOnClickListener {
            onItemClicked(bindingAdapterPosition)
        }
    }

    fun bind(shifts: List<Shift>) {
        if (shifts.size == 1) {
            bind(shifts[0])
        } else {
            shiftText.text = shifts[0].shortName
            secondShiftText.text = shifts[1].shortName
            if (ColorHelper.isTooBright(shifts[0].color)) {
                shiftText.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                shiftText.setTextColor(context.getColor(R.color.textColorWhite))
            }
            if (ColorHelper.isTooBright(shifts[1].color)) {
                secondShiftText.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                secondShiftText.setTextColor(context.getColor(R.color.textColorWhite))
            }
            shiftContainer.background.setTint(shifts[0].color)
            val secondBoxDrawable = ContextCompat.getDrawable(context, R.drawable.rounded_box)
            val halfBoxDrawable =
                ClipDrawable(secondBoxDrawable, Gravity.RIGHT, ClipDrawable.HORIZONTAL)
            halfBoxDrawable.setLevel(5000)
            halfBoxDrawable.setTint(shifts[1].color)
            val layer = arrayOfNulls<Drawable>(2)
            layer[0] = shiftContainer.background
            layer[1] = halfBoxDrawable
            shiftContainer.background = LayerDrawable(layer)
        }
    }

    fun bind(shift: Shift) {
        secondShiftText.visibility = View.GONE
        shiftText.text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            shiftText.setTextColor(context.getColor(R.color.textColorBlack))
        } else {
            shiftText.setTextColor(context.getColor(R.color.textColorWhite))
        }
        shiftContainer.background.setTint(shift.color)
    }
}