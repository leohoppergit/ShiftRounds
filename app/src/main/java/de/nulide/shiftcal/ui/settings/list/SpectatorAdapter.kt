package de.nulide.shiftcal.ui.settings.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.net.dto.SpectatorList

class SpectatorAdapter(
    val context: Context,
    val spectatorList: SpectatorList,
    val spectatorDeletePressedListener: SpectatorDeletePressedListener
) :
    RecyclerView.Adapter<SpectatorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpectatorViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_spectator, parent, false)
        return SpectatorViewHolder(context, view, spectatorDeletePressedListener)
    }

    override fun getItemCount(): Int {
        return spectatorList.size
    }

    override fun onBindViewHolder(holder: SpectatorViewHolder, position: Int) {
        val spectator = spectatorList.get(position)
        holder.bind(spectator)
    }
}