package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.utils.ColorHelper

class ShiftAdapter(context: Context, val shifts: MutableList<Shift>) :
    ArrayAdapter<Shift>(context, R.layout.item_row_shift, shifts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val rowView = inflater.inflate(R.layout.item_row_shift, parent, false)

        val shiftBox = rowView.findViewById<LinearLayout>(R.id.shiftBox)

        val shortNameText = rowView.findViewById<TextView>(R.id.textViewSName)
        val nameText = rowView.findViewById<TextView>(R.id.textViewName)

        val shift = shifts[position]
        shortNameText.text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            shortNameText.setTextColor(context.getColor(R.color.textColorBlack))
        } else {
            shortNameText.setTextColor(context.getColor(R.color.textColorWhite))
        }
        shortNameText.background.setTint(shift.color)
        nameText.text = shift.name
        if (shift.id < 0) {
            rowView.setBackgroundColor(shift.color)
            shiftBox.background = null
            if (ColorHelper.isTooBright(shift.color)) {
                nameText.setTextColor(context.getColor(R.color.textColorBlack))
                shortNameText.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                nameText.setTextColor(context.getColor(R.color.textColorWhite))
                shortNameText.setTextColor(context.getColor(R.color.textColorWhite))
            }
        }
        return rowView
    }

    override fun getItem(position: Int): Shift {
        return shifts[position]
    }
}
