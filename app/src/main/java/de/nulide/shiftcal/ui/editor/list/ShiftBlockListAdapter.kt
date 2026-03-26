package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO

class ShiftBlockListAdapter(context: Context, val shiftBlocks: List<ShiftBlockDTO>) :
    ArrayAdapter<ShiftBlockDTO>(context, R.layout.item_row_shift, shiftBlocks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val rowView = inflater.inflate(R.layout.item_row_shift, parent, false)

        val shortNameText = rowView.findViewById<TextView>(R.id.textViewSName)
        val nameText = rowView.findViewById<TextView>(R.id.textViewName)

        val shiftBlock = shiftBlocks[position]

        shortNameText.text = ""
        shortNameText.background = ShiftBlockGradientBackground.getBackground(context, shiftBlock)
        nameText.text = shiftBlock.block.name
        return rowView
    }

    override fun getItem(position: Int): ShiftBlockDTO {
        return shiftBlocks[position]
    }
}