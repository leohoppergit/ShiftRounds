package de.nulide.shiftcal.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.util.Pair
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.Toast
import de.nulide.shiftcal.data.calendar.AustriaSchoolBreakProvider
import de.nulide.shiftcal.data.calendar.AustriaSchoolBreakUpdateManager
import de.nulide.shiftcal.data.settings.CalendarMarker
import de.nulide.shiftcal.data.settings.CalendarMarkerType
import de.nulide.shiftcal.data.settings.HolidayRegion
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.SpecialAccount
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityAdvancedSettingsBinding
import de.nulide.shiftcal.ui.calendar.specialdate.CalendarSpecialDateUiHelper
import de.nulide.shiftcal.ui.settings.export.BackupDiagnostics
import de.nulide.shiftcal.ui.settings.export.BackupRestoreDebugState
import de.nulide.shiftcal.ui.settings.export.ExportFileNameHelper
import de.nulide.shiftcal.ui.settings.feature.CalSyncFeature
import de.nulide.shiftcal.ui.settings.feature.Feature
import de.nulide.shiftcal.ui.settings.feature.FeatureStateListener
import de.nulide.shiftcal.utils.permission.PermissionManager
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.text.DateFormatSymbols
import java.util.LinkedList
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AdvancedSettingsActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    AdapterView.OnItemClickListener, FeatureStateListener {

    companion object {
        const val EXTRA_SCROLL_TO = "SCROLL_TO"
    }

    internal lateinit var binding: ActivityAdvancedSettingsBinding

    lateinit var settings: SettingsRepository

    private lateinit var calSyncFeature: CalSyncFeature
    private lateinit var schoolBreakUpdateManager: AustriaSchoolBreakUpdateManager
    private var pendingDiagnosticsReport: String = ""

    private val backupDiagnosticsPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            analyzeBackupFile(uri)
        }

    private val diagnosticsExportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            if (writeDiagnosticsExport(uri)) {
                Toast.makeText(this, R.string.export_saved, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdvancedSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = SettingsRepository.getInstance(this)
        schoolBreakUpdateManager = AustriaSchoolBreakUpdateManager(this)

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
        val monthTileScaleOptions = listOf(
            getString(R.string.settings_month_tile_scale_compact),
            getString(R.string.settings_month_tile_scale_standard),
            getString(R.string.settings_month_tile_scale_large)
        )
        val monthTileScaleAdapter = ArrayAdapter(
            applicationContext,
            R.layout.item_spinner,
            monthTileScaleOptions
        )

        binding.firstDayOfWeekSpinner.setAdapter(adapterWeekDays)
        binding.firstDayOfWeekSpinner.setText(
            listWeekDays[settings.getInt(Settings.START_OF_WEEK)],
            false
        )
        binding.firstDayOfWeekSpinner.onItemClickListener = this
        binding.monthTileScaleSpinner.setAdapter(monthTileScaleAdapter)
        binding.monthTileScaleSpinner.setText(
            monthTileScaleOptions[settings.getInt(Settings.MONTH_TILE_SCALE).coerceIn(0, 2)],
            false
        )
        binding.monthTileScaleSpinner.onItemClickListener = this

        updateViews()

        binding.dualShiftCheckBox.setOnCheckedChangeListener(this)

        binding.syncCheckBox.setOnCheckedChangeListener(this)

        binding.weekOfYearSwitch.setOnCheckedChangeListener(this)
        binding.specialAccountsSwitch.setOnCheckedChangeListener(this)
        binding.addSpecialAccountButton.setOnClickListener(this)
        binding.holidaySwitch.setOnCheckedChangeListener(this)
        binding.schoolBreaksSwitch.setOnCheckedChangeListener(this)
        binding.addCalendarMarkerButton.setOnClickListener(this)
        binding.selectSchoolBreakStatesButton.setOnClickListener(this)
        binding.updateSchoolBreaksButton.setOnClickListener(this)
        binding.backupDiagnosticsButton.setOnClickListener(this)
        binding.showLastRestoreErrorButton.setOnClickListener(this)

        val holidayRegions = listOf(getString(R.string.settings_holiday_region_austria))
        val holidayRegionAdapter = ArrayAdapter(
            applicationContext,
            R.layout.item_spinner,
            holidayRegions
        )
        binding.holidayRegionSpinner.setAdapter(holidayRegionAdapter)
        binding.holidayRegionSpinner.setText(holidayRegions.first(), false)
        binding.holidayRegionSpinner.onItemClickListener = this

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
        } else if (buttonView == binding.specialAccountsSwitch) {
            settings.set(Settings.SPECIAL_ACCOUNTS_ENABLED, isChecked)
            updateSpecialAccountsUi()
        } else if (buttonView == binding.holidaySwitch) {
            settings.set(Settings.HOLIDAYS_ENABLED, isChecked)
        } else if (buttonView == binding.schoolBreaksSwitch) {
            settings.set(Settings.SCHOOL_BREAKS_ENABLED, isChecked)
            updateSchoolBreakStatesSummary()
        }
    }

    override fun onClick(v: View?) {
        if (v == binding.addSpecialAccountButton) {
            showSpecialAccountDialog(null)
        } else if (v == binding.addCalendarMarkerButton) {
            showCalendarMarkerRangePicker(null)
        } else if (v == binding.selectSchoolBreakStatesButton) {
            showSchoolBreakStatesDialog()
        } else if (v == binding.updateSchoolBreaksButton) {
            showSchoolBreakUpdatePreparedDialog()
        } else if (v == binding.backupDiagnosticsButton) {
            openBackupDiagnosticsPicker()
        } else if (v == binding.showLastRestoreErrorButton) {
            showLastRestoreError()
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

        binding.specialAccountsSwitch.isChecked = settings.getBoolean(Settings.SPECIAL_ACCOUNTS_ENABLED)
        updateSpecialAccountsUi()

        binding.holidaySwitch.isChecked = settings.getBoolean(Settings.HOLIDAYS_ENABLED)
        binding.schoolBreaksSwitch.isChecked = settings.getBoolean(Settings.SCHOOL_BREAKS_ENABLED)
        binding.holidayRegionSpinner.setText(
            when (settings.getHolidayRegion()) {
                HolidayRegion.AUSTRIA_NATIONAL -> getString(R.string.settings_holiday_region_austria)
                else -> getString(R.string.settings_holiday_region_austria)
            },
            false
        )
        binding.monthTileScaleSpinner.setText(
            listOf(
                getString(R.string.settings_month_tile_scale_compact),
                getString(R.string.settings_month_tile_scale_standard),
                getString(R.string.settings_month_tile_scale_large)
            )[settings.getInt(Settings.MONTH_TILE_SCALE).coerceIn(0, 2)],
            false
        )
        updateSchoolBreakStatesSummary()
        updateCalendarMarkersUi()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent?.adapter == binding.firstDayOfWeekSpinner.adapter) {
            if (!settings.has(Settings.START_OF_WEEK) || settings.getInt(Settings.START_OF_WEEK) != position) {
                settings.set(Settings.START_OF_WEEK, position)
            }
        } else if (parent?.adapter == binding.monthTileScaleSpinner.adapter) {
            if (!settings.has(Settings.MONTH_TILE_SCALE) || settings.getInt(Settings.MONTH_TILE_SCALE) != position) {
                settings.set(Settings.MONTH_TILE_SCALE, position)
            }
        } else if (parent?.adapter == binding.holidayRegionSpinner.adapter) {
            settings.set(Settings.HOLIDAY_REGION, HolidayRegion.AUSTRIA_NATIONAL)
        }
    }

    override fun onFeatureStateChanged(state: Feature.Companion.STATE) {
        updateViews()
    }

    private fun openBackupDiagnosticsPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        backupDiagnosticsPickerLauncher.launch(intent)
    }

    private fun analyzeBackupFile(uri: Uri) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: throw IllegalStateException("Datei konnte nicht geöffnet werden.")
                    BackupDiagnostics.inspect(json)
                }
            }

            result.onSuccess { report ->
                pendingDiagnosticsReport = report.details
                MaterialAlertDialogBuilder(this@AdvancedSettingsActivity)
                    .setTitle(R.string.backup_diagnostics_title)
                    .setMessage(report.details)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.backup_diagnostics_export) { _, _ ->
                        exportDiagnosticsReport()
                    }
                    .show()
            }.onFailure { exception ->
                BackupRestoreDebugState.setException(exception)
                MaterialAlertDialogBuilder(this@AdvancedSettingsActivity)
                    .setTitle(R.string.backup_diagnostics_title)
                    .setMessage(
                        getString(
                            R.string.backup_diagnostics_failed_with_reason,
                            exception.message ?: exception::class.java.simpleName
                        )
                    )
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun exportDiagnosticsReport() {
        if (pendingDiagnosticsReport.isBlank()) {
            Toast.makeText(this, R.string.backup_diagnostics_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, ExportFileNameHelper.backupDiagnosticsTxt(LocalDateTime.now()))
        }
        diagnosticsExportLauncher.launch(intent)
    }

    private fun writeDiagnosticsExport(uri: Uri): Boolean {
        return try {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(pendingDiagnosticsReport)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun showLastRestoreError() {
        val lastError = BackupRestoreDebugState.getError()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.backup_restore_last_error_title)
            .setMessage(lastError ?: getString(R.string.backup_restore_last_error_empty))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun updateSpecialAccountsUi() {
        val enabled = settings.getBoolean(Settings.SPECIAL_ACCOUNTS_ENABLED)
        binding.specialAccountsManageSection.visibility = if (enabled) View.VISIBLE else View.GONE

        val accounts = settings.getSpecialAccounts()
        binding.specialAccountsEmptyText.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
        binding.specialAccountsList.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (account in accounts) {
            val row = inflater.inflate(R.layout.item_special_account, binding.specialAccountsList, false)
            row.findViewById<TextView>(R.id.specialAccountNameText).text = account.name
            row.findViewById<ImageButton>(R.id.editSpecialAccountButton).setOnClickListener {
                showSpecialAccountDialog(account)
            }
            row.findViewById<ImageButton>(R.id.deleteSpecialAccountButton).setOnClickListener {
                deleteSpecialAccount(account)
            }
            binding.specialAccountsList.addView(row)
        }
    }

    private fun showSpecialAccountDialog(existingAccount: SpecialAccount?) {
        val nameEdit = EditText(this).apply {
            setText(existingAccount?.name.orEmpty())
            hint = getString(R.string.special_account_name_hint)
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existingAccount == null) R.string.special_account_add_title else R.string.special_account_edit_title)
            .setView(nameEdit)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameEdit.text.toString().trim()
                if (name.isNotEmpty()) {
                    val accounts = settings.getSpecialAccounts().toMutableList()
                    if (existingAccount == null) {
                        accounts.add(SpecialAccount(UUID.randomUUID().toString(), name))
                    } else {
                        val index = accounts.indexOfFirst { it.id == existingAccount.id }
                        if (index >= 0) {
                            accounts[index] = existingAccount.copy(name = name)
                        }
                    }
                    settings.setSpecialAccounts(accounts)
                    updateSpecialAccountsUi()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteSpecialAccount(account: SpecialAccount) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.special_account_delete_title)
            .setMessage(getString(R.string.special_account_delete_message, account.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                settings.setSpecialAccounts(settings.getSpecialAccounts().filter { it.id != account.id })
                updateSpecialAccountsUi()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateCalendarMarkersUi() {
        val markers = settings.getCalendarMarkers()
        binding.calendarMarkersEmptyText.visibility = if (markers.isEmpty()) View.VISIBLE else View.GONE
        binding.calendarMarkersList.removeAllViews()

        val inflater = LayoutInflater.from(this)
        markers.sortedWith(compareBy<CalendarMarker>({ it.startDate }, { it.name })).forEach { marker ->
            val row = inflater.inflate(R.layout.item_calendar_marker, binding.calendarMarkersList, false)
            row.findViewById<View>(R.id.calendarMarkerColorDot).backgroundTintList =
                android.content.res.ColorStateList.valueOf(CalendarSpecialDateUiHelper.getColor(this, marker.type))
            row.findViewById<TextView>(R.id.calendarMarkerNameText).text = marker.name
            row.findViewById<TextView>(R.id.calendarMarkerMetaText).text =
                getString(
                    R.string.settings_calendar_marker_meta,
                    CalendarSpecialDateUiHelper.getTypeLabel(this, marker.type),
                    formatMarkerRange(marker)
                )
            row.findViewById<ImageButton>(R.id.editCalendarMarkerButton).setOnClickListener {
                showCalendarMarkerRangePicker(marker)
            }
            row.findViewById<ImageButton>(R.id.deleteCalendarMarkerButton).setOnClickListener {
                deleteCalendarMarker(marker)
            }
            binding.calendarMarkersList.addView(row)
        }
    }

    private fun showSchoolBreakStatesDialog() {
        val states = AustriaSchoolBreakProvider.states
        val labels = states.map { it.label }.toTypedArray()
        val currentSelection = settings.getSchoolBreakStates()
        val checkedItems = states.map { currentSelection.contains(it.code) }.toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_school_break_states_select)
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(R.string.save) { _, _ ->
                val selectedStates = states
                    .filterIndexed { index, _ -> checkedItems[index] }
                    .map { it.code }
                    .toSet()
                settings.setSchoolBreakStates(selectedStates)
                updateSchoolBreakStatesSummary()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateSchoolBreakStatesSummary() {
        val selected = settings.getSchoolBreakStates()
        val labels = AustriaSchoolBreakProvider.states
            .filter { selected.contains(it.code) }
            .map { it.label }
        binding.schoolBreakStatesSummaryText.text = if (labels.isEmpty()) {
            getString(R.string.settings_school_break_states_empty)
        } else {
            labels.joinToString(", ")
        }
        val enabled = binding.schoolBreaksSwitch.isChecked
        binding.schoolBreakStatesSummaryText.alpha = if (enabled) 1f else 0.55f
        binding.selectSchoolBreakStatesButton.isEnabled = enabled
        binding.updateSchoolBreaksButton.alpha = if (enabled) 1f else 0.55f
        binding.schoolBreakDataSourceText.alpha = if (enabled) 1f else 0.55f
        binding.schoolBreakDataSourceText.text = schoolBreakUpdateManager.getDataSourceSummary()
    }

    private fun showSchoolBreakUpdatePreparedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_school_break_update)
            .setMessage(schoolBreakUpdateManager.getPreparedUpdateSummary())
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showCalendarMarkerRangePicker(existingMarker: CalendarMarker?) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(
                if (existingMarker == null) R.string.settings_calendar_marker_range_add
                else R.string.settings_calendar_marker_range_edit
            )
            .apply {
                val start = existingMarker?.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val end = existingMarker?.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                if (start != null && end != null) {
                    val zone = ZoneId.systemDefault()
                    setSelection(
                        Pair(
                            start.atStartOfDay(zone).toInstant().toEpochMilli(),
                            end.atStartOfDay(zone).toInstant().toEpochMilli()
                        )
                    )
                }
            }
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first
            val endMillis = selection.second
            if (startMillis == null || endMillis == null) return@addOnPositiveButtonClickListener
            val zone = ZoneId.systemDefault()
            val start = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
            val end = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()
            if (end.isBefore(start)) return@addOnPositiveButtonClickListener
            showCalendarMarkerDetailsDialog(existingMarker, start, end)
        }

        picker.show(supportFragmentManager, "calendar-marker-range")
    }

    private fun showCalendarMarkerDetailsDialog(existingMarker: CalendarMarker?, start: LocalDate, end: LocalDate) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calendar_marker, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.calendarMarkerNameEdit)
        val typeSpinner = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.calendarMarkerTypeSpinner)
        val rangeText = dialogView.findViewById<TextView>(R.id.calendarMarkerRangeText)

        val typeOptions = listOf(
            CalendarMarkerType.SCHOOL_BREAK to getString(R.string.calendar_special_date_type_school_break),
            CalendarMarkerType.KINDERGARTEN_CLOSURE to getString(R.string.calendar_special_date_type_kindergarten_closure),
            CalendarMarkerType.CUSTOM to getString(R.string.calendar_special_date_type_custom)
        )

        nameEdit.setText(existingMarker?.name.orEmpty())
        nameEdit.hint = getString(R.string.settings_calendar_marker_name_hint)
        rangeText.text = CalendarSpecialDateUiHelper.formatRange(start, end)

        val typeAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner,
            typeOptions.map { it.second }
        )
        typeSpinner.setAdapter(typeAdapter)
        val selectedIndex = typeOptions.indexOfFirst { it.first == existingMarker?.type }.takeIf { it >= 0 } ?: 0
        typeSpinner.setText(typeOptions[selectedIndex].second, false)

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existingMarker == null) R.string.settings_calendar_marker_add else R.string.settings_calendar_marker_edit)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameEdit.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton

                val chosenType = typeOptions.firstOrNull { it.second == typeSpinner.text.toString() }?.first
                    ?: CalendarMarkerType.CUSTOM

                val markers = settings.getCalendarMarkers().toMutableList()
                val marker = CalendarMarker(
                    id = existingMarker?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    type = chosenType,
                    startDate = start.toString(),
                    endDate = end.toString()
                )
                if (existingMarker == null) {
                    markers.add(marker)
                } else {
                    val index = markers.indexOfFirst { it.id == existingMarker.id }
                    if (index >= 0) {
                        markers[index] = marker
                    }
                }
                settings.setCalendarMarkers(markers)
                updateCalendarMarkersUi()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteCalendarMarker(marker: CalendarMarker) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_calendar_marker_delete)
            .setMessage(getString(R.string.settings_calendar_marker_delete_message, marker.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                settings.setCalendarMarkers(settings.getCalendarMarkers().filter { it.id != marker.id })
                updateCalendarMarkersUi()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun formatMarkerRange(marker: CalendarMarker): String {
        val start = runCatching { LocalDate.parse(marker.startDate) }.getOrNull()
        val end = runCatching { LocalDate.parse(marker.endDate) }.getOrNull()
        return if (start != null && end != null) {
            CalendarSpecialDateUiHelper.formatRange(start, end)
        } else {
            ""
        }
    }

}
