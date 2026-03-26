package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.utils.ColorHelper
import java.util.Collections

class ShiftListAdapter(
    private val context: Context,
    private val shifts: MutableList<Shift>,
    private val onClick: (Int) -> Unit,
    private val onLongPressDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<ShiftListAdapter.ShiftViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_shift, parent, false)
        return ShiftViewHolder(view, onClick, onLongPressDrag)
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        holder.bind(shifts[position])
    }

    override fun getItemCount(): Int = shifts.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        Collections.swap(shifts, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getItems(): List<Shift> = shifts

    class ShiftViewHolder(
        itemView: View,
        private val onClick: (Int) -> Unit,
        private val onLongPressDrag: (RecyclerView.ViewHolder) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val shiftBox = itemView.findViewById<LinearLayout>(R.id.shiftBox)
        private val shortNameText = itemView.findViewById<TextView>(R.id.textViewSName)
        private val nameText = itemView.findViewById<TextView>(R.id.textViewName)

        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onClick(bindingAdapterPosition)
                }
            }
            itemView.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onLongPressDrag(this)
                    true
                } else {
                    false
                }
            }
        }

        fun bind(shift: Shift) {
            shortNameText.text = shift.shortName
            if (ColorHelper.isTooBright(shift.color)) {
                shortNameText.setTextColor(itemView.context.getColor(R.color.textColorBlack))
            } else {
                shortNameText.setTextColor(itemView.context.getColor(R.color.textColorWhite))
            }
            shortNameText.background.setTint(shift.color)
            nameText.text = shift.name
            if (shift.id < 0) {
                itemView.setBackgroundColor(shift.color)
                shiftBox.background = null
                if (ColorHelper.isTooBright(shift.color)) {
                    nameText.setTextColor(itemView.context.getColor(R.color.textColorBlack))
                    shortNameText.setTextColor(itemView.context.getColor(R.color.textColorBlack))
                } else {
                    nameText.setTextColor(itemView.context.getColor(R.color.textColorWhite))
                    shortNameText.setTextColor(itemView.context.getColor(R.color.textColorWhite))
                }
            }
        }
    }
}
