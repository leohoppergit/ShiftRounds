package de.nulide.shiftcal.ui.settings.list

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.net.dto.Spectator

class SpectatorViewHolder(
    val context: Context,
    itemView: View,
    val spectatorDeletePressedListener: SpectatorDeletePressedListener
) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {

    private val spectatorName: TextView = itemView.findViewById(R.id.spectatorNameText)
    private val deleteButton: TextView = itemView.findViewById(R.id.spectatorDeleteButton)
    private var spectator: Spectator? = null

    fun bind(spectator: Spectator) {
        spectatorName.text = spectator.name
        deleteButton.setOnClickListener(this)
        this.spectator = spectator
    }

    override fun onClick(v: View?) {
        if (spectator != null) {
            spectatorDeletePressedListener.onSpectatorDeletePressed(
                spectator!!.name,
                spectator!!.uuid
            )
        }
    }

}