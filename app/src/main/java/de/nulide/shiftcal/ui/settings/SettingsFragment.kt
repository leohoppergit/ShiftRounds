package de.nulide.shiftcal.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.databinding.FragmentSettingsBinding
import de.nulide.shiftcal.ui.editor.ShiftBlocksActivity
import de.nulide.shiftcal.ui.editor.ShiftsActivity
import de.nulide.shiftcal.ui.helper.FeedbackDialog
import de.nulide.shiftcal.ui.helper.SFragment
import de.nulide.shiftcal.ui.settings.export.ExportActivity
import de.nulide.shiftcal.ui.settings.list.SettingsEntry
import de.nulide.shiftcal.ui.settings.list.SettingsEntry.Companion.getSettingsEntries
import de.nulide.shiftcal.ui.settings.list.SettingsViewAdapter

class SettingsFragment : SFragment(), AdapterView.OnItemClickListener {

    private lateinit var settingsEntryList: List<SettingsEntry>

    override val fragmentName = "settings"
    private lateinit var binding: FragmentSettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        settingsEntryList = getSettingsEntries(ctx)
        binding.settingslist.adapter = SettingsViewAdapter(ctx, settingsEntryList)
        binding.settingslist.onItemClickListener = this

    }

    override fun updateActivity() {
        setTitle(getString(R.string.settings_title))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent: Intent
        when (settingsEntryList[position].settingsId) {
            SettingsEntry.ID_SHIFTS -> {
                intent = Intent(ctx, ShiftsActivity::class.java)
                startActivity(intent)
            }

            SettingsEntry.ID_SHIFT_BLOCKS -> {
                intent = Intent(ctx, ShiftBlocksActivity::class.java)
                startActivity(intent)
            }

            SettingsEntry.ID_EXPORT -> {
                intent = Intent(ctx, ExportActivity::class.java)
                startActivity(intent)
            }

            SettingsEntry.ID_ADVANCED_SETTINGS -> {
                intent = Intent(ctx, AdvancedSettingsActivity::class.java)
                startActivity(intent)
            }

            SettingsEntry.ID_FEEDBACK -> FeedbackDialog(ctx)
            SettingsEntry.ID_ABOUT -> {
                intent = Intent(ctx, AboutActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
