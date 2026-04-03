package de.nulide.shiftcal.ui.settings.export

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.monthyearpicker.MonthYearPickerBuilder
import de.nulide.monthyearpicker.OnMonthYearSelectedListener
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.FullBackupDTO
import de.nulide.shiftcal.data.repository.wrapper.FullBackupImportParser
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityExportBinding
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportActivity : AppCompatActivity(), OnMonthYearSelectedListener {

    private lateinit var binding: ActivityExportBinding
    private lateinit var sc: SCRepoManager
    private lateinit var settings: SettingsRepository

    private var pendingMimeType = "application/octet-stream"
    private var pendingFileName = "export.txt"
    private var pendingContent = ""

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null && writeExport(uri)) {
                    Toast.makeText(this, R.string.export_saved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val uri = result.data?.data ?: run {
                Toast.makeText(this, R.string.restore_failed, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                when (restoreBackup(uri)) {
                    BackupRestoreResult.SUCCESS -> Toast.makeText(this@ExportActivity, R.string.restore_success, Toast.LENGTH_SHORT).show()
                    BackupRestoreResult.INVALID_FILE -> Toast.makeText(this@ExportActivity, R.string.restore_invalid_file, Toast.LENGTH_SHORT).show()
                    BackupRestoreResult.FAILED -> Toast.makeText(this@ExportActivity, R.string.restore_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.exportRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sc = SCRepoManager.getInstance(this)
        sc.switchToLocal()
        settings = SettingsRepository.getInstance(this)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.exportSettingsCard.setOnClickListener { exportSettings() }
        binding.exportShiftsCard.setOnClickListener { exportShifts() }
        binding.exportCalendarCard.setOnClickListener { showCalendarExportOptions() }
        binding.exportBackupCard.setOnClickListener { exportFullBackup() }
        binding.restoreBackupCard.setOnClickListener { confirmRestore() }
    }

    private fun exportSettings() {
        val payload = SettingsExportData(
            exportedAt = Instant.now().toString(),
            appName = getString(R.string.app_name),
            settings = settings.exportSettings()
        )
        queueExport(
            mimeType = "application/json",
            fileName = ExportFileNameHelper.settingsJson(LocalDateTime.now()),
            content = JIO.toJSON(payload)
        )
    }

    private fun exportShifts() {
        val payload = ShiftsExportData(
            exportedAt = Instant.now().toString(),
            appName = getString(R.string.app_name),
            shifts = sc.shifts.getAll().filter { it.id >= 0 }
        )
        queueExport(
            mimeType = "application/json",
            fileName = ExportFileNameHelper.shiftsJson(LocalDateTime.now()),
            content = JIO.toJSON(payload)
        )
    }

    private fun exportFullBackup() {
        val payload = sc.createFullBackup(
            settingsRepository = settings,
            appName = getString(R.string.app_name),
            exportedAt = Instant.now().toString()
        )
        queueExport(
            mimeType = "application/json",
            fileName = ExportFileNameHelper.backupJson(LocalDateTime.now()),
            content = JIO.toJSON(payload)
        )
    }

    private fun confirmRestore() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restore_title)
            .setMessage(R.string.restore_warning)
            .setPositiveButton(R.string.restore_start) { _, _ -> openRestorePicker() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun openRestorePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        openDocumentLauncher.launch(intent)
    }

    private fun showCalendarExportOptions() {
        val options = arrayOf(
            getString(R.string.export_calendar_scope_all),
            getString(R.string.export_calendar_scope_month),
            getString(R.string.export_calendar_scope_range)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.export_calendar_scope_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportCalendarAll()
                    1 -> showMonthPicker()
                    2 -> showRangePicker()
                }
            }
            .show()
    }

    private fun exportCalendarAll() {
        val workDays = sc.workDays.getAll()
        if (workDays.isEmpty()) {
            Toast.makeText(this, R.string.export_calendar_no_entries, Toast.LENGTH_SHORT).show()
            return
        }
        val start = workDays.minOf { it.day }
        val end = workDays.maxOf { it.day }
        exportCalendarRange(start, end, ExportFileNameHelper.calendarFile(start, end, "ics", start..end))
    }

    private fun showMonthPicker() {
        val picker = MonthYearPickerBuilder(this)
        val currentYear = YearMonth.now().year
        picker.setYearRange(currentYear - 20, currentYear + 20, true)
        picker.setOnMonthYearSelectedListener(this)
        picker.show()
    }

    override fun onMonthYearSelected(yearMonth: YearMonth) {
        exportCalendarRange(
            yearMonth.atDay(1),
            yearMonth.atEndOfMonth(),
            ExportFileNameHelper.calendarFile(yearMonth.atDay(1), yearMonth.atEndOfMonth(), "ics")
        )
    }

    private fun showRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.export_calendar_scope_range)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first
            val endMillis = selection.second
            if (startMillis == null || endMillis == null) {
                Toast.makeText(this, R.string.export_range_invalid, Toast.LENGTH_SHORT).show()
                return@addOnPositiveButtonClickListener
            }
            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            if (end.isBefore(start)) {
                Toast.makeText(this, R.string.export_range_invalid, Toast.LENGTH_SHORT).show()
                return@addOnPositiveButtonClickListener
            }
            exportCalendarRange(
                start,
                end,
                ExportFileNameHelper.calendarFile(start, end, "ics")
            )
        }
        picker.show(supportFragmentManager, "export-range")
    }

    private fun exportCalendarRange(start: LocalDate, end: LocalDate, fileName: String) {
        val workDays = sc.workDays.getAll().filter { !it.day.isBefore(start) && !it.day.isAfter(end) }
        if (workDays.isEmpty()) {
            Toast.makeText(this, R.string.export_calendar_no_entries, Toast.LENGTH_SHORT).show()
            return
        }
        val events = workDays
            .map { workDay -> WorkDayDTO(workDay, sc.shifts.get(workDay.shiftId)) }
            .sortedWith(
                compareBy<WorkDayDTO>({ it.wday.day }, { it.shift.startTime.timeInMinutes }, { it.shift.endDayOffset }, { it.shift.endTime.timeInMinutes }, { it.shift.id })
            )

        queueExport(
            mimeType = "text/calendar",
            fileName = fileName,
            content = CalendarIcsExporter.create(this, events)
        )
    }

    private fun queueExport(mimeType: String, fileName: String, content: String) {
        pendingMimeType = mimeType
        pendingFileName = fileName
        pendingContent = content

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createDocumentLauncher.launch(intent)
    }

    private fun writeExport(uri: Uri): Boolean {
        return try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(pendingContent)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun restoreBackup(uri: Uri): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: return@withContext BackupRestoreResult.FAILED
            val backup = FullBackupImportParser.parse(json)
                ?: run {
                    BackupRestoreDebugState.setError("Backup parser rejected file as incompatible.")
                    return@withContext BackupRestoreResult.INVALID_FILE
                }
            if (backup.backupVersion <= 0 || backup.appName.isBlank()) {
                BackupRestoreDebugState.setError("Backup metadata is incomplete or invalid.")
                return@withContext BackupRestoreResult.INVALID_FILE
            }
            sc.restoreLocalBackup(backup, settings)
            BackupRestoreDebugState.clear()
            BackupRestoreResult.SUCCESS
        } catch (exception: Exception) {
            BackupRestoreDebugState.setException(exception)
            BackupRestoreResult.FAILED
        }
    }

}
