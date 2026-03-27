package de.nulide.shiftcal.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityMainBinding
import de.nulide.shiftcal.ui.calendar.ShiftCalendarFragment
import de.nulide.shiftcal.ui.helper.SFragment
import de.nulide.shiftcal.ui.helper.SFragmentPagerAdapter
import de.nulide.shiftcal.ui.importer.SwiftShiftImportHelper
import de.nulide.shiftcal.ui.settings.SettingsFragment
import de.nulide.shiftcal.ui.stats.StatsFragment

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    OnClickListener {

    lateinit var binding: ActivityMainBinding

    private lateinit var adapter: SFragmentPagerAdapter

    private var ignoreReturnViaBack = false
    private lateinit var settings: SettingsRepository
    private lateinit var sc: SCRepoManager

    private val swiftShiftImportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val imported = try {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        SwiftShiftImportHelper(this).importLegacyJson(reader.readText())
                    } ?: false
                } catch (_: Exception) {
                    false
                }
                if (imported) {
                    settings.set(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED, true)
                    recreate()
                } else {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        getString(R.string.swiftshift_import_failed),
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

    private val currentFragment: SFragment?
        get() {
            val item = binding.fragmentPager.currentItem
            val tag = "f${item}"
            return supportFragmentManager.findFragmentByTag(tag) as SFragment?
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        settings = SettingsRepository.getInstance(this)
        sc = SCRepoManager.getInstance(this)

        //ActionBar
        binding.topAppBar.title = null
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.titleToolbar.setOnClickListener(this)

        binding.bottomNavigation.setOnItemSelectedListener(this)

        val fragments = mutableListOf<SFragment>()

        fragments.add(ShiftCalendarFragment())
        fragments.add(StatsFragment())
        fragments.add(SettingsFragment())
        adapter = SFragmentPagerAdapter(this, fragments)
        binding.fragmentPager.adapter = adapter
        binding.fragmentPager.offscreenPageLimit = fragments.size
        binding.fragmentPager.isUserInputEnabled = false

        binding.fragmentPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                invalidateOptionsMenu()
                binding.bottomNavigation.menu[position].isChecked = true
            }
        })
        var lastPos = 0
        if (savedInstanceState != null) {
            lastPos = savedInstanceState.getInt(KEY_LAST_POS, 0)
        }
        binding.fragmentPager.currentItem = lastPos

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToCalendar()
            }
        }
        onBackPressedDispatcher.addCallback(backCallback)

        maybePromptSwiftShiftImport()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_LAST_POS, binding.fragmentPager.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var pos = -1
        binding.bottomNavigation.menu.forEachIndexed { i, it ->
            if (item.itemId == it.itemId) {
                pos = i
            }
        }

        if (pos != -1) {
            if (binding.fragmentPager.currentItem != pos) {
                invalidateOptionsMenu()
                binding.fragmentPager.setCurrentItem(pos, true)

                return true
            } else {
                if (pos == 0) {
                    returnToCalendar(false)
                }
            }
        }
        return false
    }


    fun setTitle(title: String) {
        binding.titleToolbar.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        currentFragment?.onCreateMenu(menu, inflater)
        return true
    }

    companion object {
        const val KEY_LAST_POS = "LAST_POS"
    }

    override fun onClick(v: View?) {
        currentFragment?.onTitleClicked()
    }

    fun returnToCalendar(exit: Boolean = true) {
        if (binding.fragmentPager.currentItem != 0) {
            ignoreReturnViaBack = true
            binding.fragmentPager.currentItem = 0
            binding.bottomNavigation.selectedItemId = R.id.calendar
        } else {
            if (!ignoreReturnViaBack) {
                if (currentFragment?.backPressed() == false) {
                    if (exit) {
                        finish()
                    }
                }
            }
            ignoreReturnViaBack = false
        }
    }

    private fun maybePromptSwiftShiftImport() {
        if (settings.getBoolean(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED)) return
        if (!isOriginalSwiftShiftInstalled()) return
        if (sc.shifts.getAll().isNotEmpty() || sc.workDays.getAll().isNotEmpty() ||
            sc.monthNotes.getAll().isNotEmpty() || sc.shiftBlocks.getAll().isNotEmpty()
        ) {
            settings.set(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED, true)
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.swiftshift_import_title)
            .setMessage(R.string.swiftshift_import_prompt)
            .setPositiveButton(R.string.swiftshift_import_start) { _, _ ->
                openSwiftShiftImportFilePicker()
            }
            .setNegativeButton(R.string.swiftshift_import_skip) { _, _ ->
                settings.set(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED, true)
            }
            .setOnCancelListener {
                settings.set(Settings.SWIFTSHIFT_IMPORT_PROMPT_HANDLED, true)
            }
            .show()
    }

    private fun openSwiftShiftImportFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
        }
        swiftShiftImportLauncher.launch(intent)
    }

    private fun isOriginalSwiftShiftInstalled(): Boolean {
        val packageName = "de.nulide.shiftcal"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

}
