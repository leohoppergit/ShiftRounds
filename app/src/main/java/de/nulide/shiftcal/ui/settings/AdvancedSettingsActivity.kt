package de.nulide.shiftcal.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityAdvancedSettingsBinding
import de.nulide.shiftcal.ui.importer.SwiftShiftImportActivity
import de.nulide.shiftcal.ui.settings.feature.CalSyncFeature
import de.nulide.shiftcal.ui.settings.feature.Feature
import de.nulide.shiftcal.ui.settings.feature.FeatureStateListener
import de.nulide.shiftcal.utils.permission.PermissionManager
import java.text.DateFormatSymbols
import java.util.LinkedList


class AdvancedSettingsActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    AdapterView.OnItemClickListener, FeatureStateListener {

    companion object {
        const val EXTRA_SCROLL_TO = "SCROLL_TO"
    }

    internal lateinit var binding: ActivityAdvancedSettingsBinding

    lateinit var settings: SettingsRepository

    private lateinit var calSyncFeature: CalSyncFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdvancedSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = SettingsRepository.getInstance(this)

        val permissionManager = PermissionManager(this)

        calSyncFeature = CalSyncFeature(this, permissionManager, this)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { _: View? ->
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.advancedSettings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        val listWeekDays = LinkedList(DateFormatSymbols().weekdays.toList())
        listWeekDays.removeFirst()
        listWeekDays.add(listWeekDays.first())
        listWeekDays.removeFirst()

        val adapterWeekDays = ArrayAdapter(
            applicationContext,
            R.layout.item_spinner, listWeekDays
        )

        binding.firstDayOfWeekSpinner.setAdapter(adapterWeekDays)
        binding.firstDayOfWeekSpinner.setText(
            listWeekDays[settings.getInt(Settings.START_OF_WEEK)],
            false
        )
        binding.firstDayOfWeekSpinner.onItemClickListener = this

        updateViews()

        binding.dualShiftCheckBox.setOnCheckedChangeListener(this)

        binding.syncCheckBox.setOnCheckedChangeListener(this)

        binding.weekOfYearSwitch.setOnCheckedChangeListener(this)
        binding.swiftShiftImportButton.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()

        binding.advancedSettingsContent.post {
            val scrollTo = intent.getIntExtra(EXTRA_SCROLL_TO, -1)
            if (scrollTo != -1) {

                val view = binding.advancedSettingsContent.findViewById<View>(scrollTo)
                if (view != null) {
                    val nestedScrollViewLocation = IntArray(2)
                    val scrollToViewLocation = IntArray(2)

                    binding.advancedSettingsContent.getLocationOnScreen(nestedScrollViewLocation)
                    view.getLocationOnScreen(scrollToViewLocation)
                    val y = scrollToViewLocation[1] - nestedScrollViewLocation[1]
                    binding.advancedSettingsContent.smoothScrollTo(0, y)
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView == binding.syncCheckBox) {
            if (isChecked) {
                calSyncFeature.enable()
            } else {
                calSyncFeature.disable()
            }
        } else if (buttonView == binding.dualShiftCheckBox) {
            settings.set(Settings.DUAL_SHIFT, isChecked)
        } else if (buttonView == binding.weekOfYearSwitch) {
            settings.set(Settings.WEEK_OF_YEAR, isChecked)
        }
    }

    override fun onClick(v: View?) {
        if (v == binding.swiftShiftImportButton) {
            startActivity(Intent(this, SwiftShiftImportActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateViews()
    }

    private fun updateViews() {
        //Week Of Year
        binding.weekOfYearSwitch.isChecked = settings.getBoolean(Settings.WEEK_OF_YEAR)

        //Dual Shift
        binding.dualShiftCheckBox.isChecked = settings.getBoolean(Settings.DUAL_SHIFT)

        //Sync
        val syncEnabled = calSyncFeature.isEnabled()
        binding.syncCheckBox.isChecked = syncEnabled
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent!!.adapter == binding.firstDayOfWeekSpinner.adapter) {
            if (!settings.has(Settings.START_OF_WEEK) || settings.getInt(Settings.START_OF_WEEK) != position) {
                settings.set(Settings.START_OF_WEEK, position)
            }
        }
    }

    override fun onFeatureStateChanged(state: Feature.Companion.STATE) {
        updateViews()
    }

}
