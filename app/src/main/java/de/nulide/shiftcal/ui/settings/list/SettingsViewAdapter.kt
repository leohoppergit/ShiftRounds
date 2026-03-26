package de.nulide.shiftcal.ui.settings.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import de.nulide.shiftcal.R

class SettingsViewAdapter(context: Context, private var settingsEntries: List<SettingsEntry>) :
    BaseAdapter() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return settingsEntries.size
    }

    override fun getItem(position: Int): Any? {
        return settingsEntries[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.item_settings, parent, false)
        val entry = settingsEntries[position]
        view.findViewById<TextView>(R.id.settingsItemText).text = entry.title
        view.findViewById<ImageView>(R.id.settingsItemIcon).setImageDrawable(entry.icon)
        return view
    }

}
