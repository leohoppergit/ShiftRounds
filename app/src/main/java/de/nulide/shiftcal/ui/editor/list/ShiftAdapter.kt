package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.utils.ColorHelper

class ShiftAdapter(context: Context, val shifts: MutableList<Shift>) :
    ArrayAdapter<Shift>(context, R.layout.item_row_shift, shifts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_row_shift, parent, false)
        val holder = (rowView.tag as? ViewHolder) ?: ViewHolder(rowView).also {
            rowView.tag = it
        }

        val shiftBox = holder.shiftBox
        val shortNameText = holder.shortNameText
        val nameText = holder.nameText
        val moreButton = holder.moreButton
        moreButton.visibility = View.GONE
        moreButton.isClickable = false
        moreButton.isFocusable = false

        val shift = shifts[position]
        rowView.setBackgroundColor(context.getColor(android.R.color.transparent))
        shiftBox.setBackgroundResource(R.drawable.calendar_box)
        shortNameText.text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            shortNameText.setTextColor(context.getColor(R.color.textColorBlack))
        } else {
            shortNameText.setTextColor(context.getColor(R.color.textColorWhite))
        }
        shortNameText.background.setTint(shift.color)
        nameText.text = shift.name
        nameText.setTextColor(context.getColor(R.color.textColor))
        if (shift.id < 0) {
            rowView.setBackgroundColor(context.getColor(android.R.color.transparent))
            shiftBox.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 34f
                setColor(adjustSpecialRowColor(shift.color))
            }
            nameText.setTextColor(context.getColor(R.color.shiftRoundsTitleInk))
            shortNameText.backgroundTintList = ColorStateList.valueOf(shift.color)
            shortNameText.setTextColor(
                if (ColorHelper.isTooBright(shift.color)) {
                    context.getColor(R.color.textColorBlack)
                } else {
                    context.getColor(R.color.textColorWhite)
                }
            )
        }
        return rowView
    }

    private fun adjustSpecialRowColor(color: Int): Int {
        val alpha = 235
        return android.graphics.Color.argb(
            alpha,
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    override fun getItem(position: Int): Shift {
        return shifts[position]
    }

    private class ViewHolder(view: View) {
        val shiftBox: LinearLayout = view.findViewById(R.id.shiftBox)
        val shortNameText: TextView = view.findViewById(R.id.textViewSName)
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val moreButton: ImageButton = view.findViewById(R.id.buttonMore)
    }
}
