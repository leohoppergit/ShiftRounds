package de.nulide.shiftcal.ui.importer

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
import androidx.documentfile.provider.DocumentFile
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivitySwiftshiftImportBinding
import java.util.ArrayDeque

class SwiftShiftImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwiftshiftImportBinding
    private lateinit var settings: SettingsRepository
    private lateinit var sc: SCRepoManager
    private lateinit var importHelper: SwiftShiftImportHelper

    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                handleImport(uri)
            }
        }

    private val importTreeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val imported = findAndImportFromTree(uri)
                if (!imported) {
                    Toast.makeText(this, R.string.swiftshift_import_folder_failed, Toast.LENGTH_LONG).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySwiftshiftImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.swiftShiftImportRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        settings = SettingsRepository.getInstance(this)
        sc = SCRepoManager.getInstance(this)
        importHelper = SwiftShiftImportHelper(this)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.selectImportFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
            }
            importLauncher.launch(intent)
        }

        binding.scanImportFolderButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            importTreeLauncher.launch(intent)
        }
    }

    private fun handleImport(uri: Uri) {
        if (!canImportIntoCurrentState()) {
            Toast.makeText(this, R.string.swiftshift_import_existing_data, Toast.LENGTH_LONG).show()
            return
        }

        val imported = try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                importHelper.importLegacyJson(reader.readText())
            } ?: false
        } catch (_: Exception) {
            false
        }

        if (imported) {
            settings.set(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED, true)
            Toast.makeText(this, R.string.swiftshift_import_success, Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, R.string.swiftshift_import_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun findAndImportFromTree(treeUri: Uri): Boolean {
        if (!canImportIntoCurrentState()) {
            Toast.makeText(this, R.string.swiftshift_import_existing_data, Toast.LENGTH_LONG).show()
            return false
        }

        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val root = DocumentFile.fromTreeUri(this, treeUri) ?: return false
        val queue = ArrayDeque<DocumentFile>()
        queue.add(root)
        val targetNames = setOf("shift-calendar.json", "sc.json")

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current.isDirectory) {
                current.listFiles().forEach { queue.addLast(it) }
            } else if (current.isFile) {
                val lowerName = current.name?.lowercase().orEmpty()
                val isLikelyJson = lowerName.endsWith(".json") || lowerName.endsWith(".txt")
                if (lowerName in targetNames) {
                    handleImport(current.uri)
                    return true
                }
                if (isLikelyJson) {
                    val looksCompatible = try {
                        contentResolver.openInputStream(current.uri)?.bufferedReader()?.use { reader ->
                            importHelper.looksLikeLegacyJson(reader.readText())
                        } ?: false
                    } catch (_: Exception) {
                        false
                    }
                    if (looksCompatible) {
                        handleImport(current.uri)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun canImportIntoCurrentState(): Boolean {
        return sc.shifts.getAll().isEmpty() &&
            sc.workDays.getAll().isEmpty() &&
            sc.monthNotes.getAll().isEmpty() &&
            sc.shiftBlocks.getAll().isEmpty()
    }
}
