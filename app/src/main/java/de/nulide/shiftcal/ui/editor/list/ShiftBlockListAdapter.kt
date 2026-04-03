package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO

class ShiftBlockListAdapter(context: Context, val shiftBlocks: List<ShiftBlockDTO>) :
    ArrayAdapter<ShiftBlockDTO>(context, R.layout.item_row_shift, shiftBlocks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_row_shift, parent, false)
        val holder = (rowView.tag as? ViewHolder) ?: ViewHolder(rowView).also {
            rowView.tag = it
        }

        val shiftBlock = shiftBlocks[position]

        holder.moreButton.visibility = View.GONE
        holder.moreButton.isClickable = false
        holder.moreButton.isFocusable = false
        holder.shiftBox.setBackgroundResource(R.drawable.calendar_box)
        holder.shortNameText.text = ""
        holder.shortNameText.background =
            ShiftBlockGradientBackground.getBackground(context, shiftBlock)
        holder.nameText.text = shiftBlock.block.name
        return rowView
    }

    override fun getItem(position: Int): ShiftBlockDTO {
        return shiftBlocks[position]
    }

    private class ViewHolder(view: View) {
        val shiftBox: LinearLayout = view.findViewById(R.id.shiftBox)
        val shortNameText: TextView = view.findViewById(R.id.textViewSName)
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val moreButton: ImageButton = view.findViewById(R.id.buttonMore)
    }
}
