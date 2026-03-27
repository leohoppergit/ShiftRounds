package de.nulide.shiftcal.ui.editor

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityShiftBlockCreatorBinding
import de.nulide.shiftcal.ui.editor.list.ShiftAdapter
import de.nulide.shiftcal.ui.editor.list.ShiftBlockAdapter
import de.nulide.shiftcal.ui.helper.OnItemClickedListener
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.ui.helper.WarningDialog
import de.nulide.shiftcal.ui.intro.ShiftBlockCreatorIntro
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Snack

class ShiftBlockCreatorActivity : AppCompatActivity(), OnItemClickedListener,
    AdapterView.OnItemClickListener, OnClickListener {

    lateinit var binding: ActivityShiftBlockCreatorBinding
    private lateinit var sc: SCRepoManager


    private var toEditShiftBlockId = SpecialShifts.NONE_ID
    private lateinit var toEditShiftBlock: ShiftBlockDTO

    lateinit var shifts: MutableList<Shift>
    private var shiftSelectorAdapter: ShiftAdapter? = null
    private var shiftSelectorDialog: AlertDialog? = null
    private var selectedShiftID = SpecialShifts.NONE_ID

    private var shiftSelectorBackground: Drawable? = null

    private lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftBlockCreatorBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        settings = SettingsRepository.getInstance(this)
        sc = SCRepoManager.getInstance(this)
        shifts = sc.shifts.getNotArchived().toMutableList()
        shifts.add(SpecialShifts.getDeleteShift(this))

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            toEditShiftBlockId = bundle.getInt("toedit")
        } else {
            finish()
        }

        setContentView(binding.root)

        shiftSelectorBackground = binding.shiftSelectorFab.background

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shiftBlockCreator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.blockContainer.layoutManager = GridLayoutManager(this, 7)
        binding.blockContainer.isNestedScrollingEnabled = false
        binding.blockContainer.setHasFixedSize(true)

        binding.shiftSelectorFab.setOnClickListener(this)

        binding.fabDoneShiftBlock.setOnClickListener(this)

        loadShiftBlock()
        updateAdapter()

        ShiftBlockCreatorIntro(this, this, binding).showIntro()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (toEditShiftBlockId != SpecialShifts.NONE_ID) {
                    val newShiftBlock = genShiftBlock()
                    val oldShiftBlock = sc.shiftBlocks.get(toEditShiftBlockId)
                    if (newShiftBlock != oldShiftBlock) {
                        val warning = WarningDialog(
                            this@ShiftBlockCreatorActivity, WarningDialog.ICON_WARNING,
                            this@ShiftBlockCreatorActivity.getString(R.string.warning_shift_not_saved)
                        )
                        warning.enableSaveButton { onClick(binding.fabDoneShiftBlock) }
                        warning.enablePositiveButton {
                            this.isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                        warning.enableNegativeButton()
                        warning.show()
                    } else {
                        this.isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                } else {
                    this.isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(backCallback)

    }

    fun loadShiftBlock() {
        if (toEditShiftBlockId != -1) {
            toEditShiftBlock = sc.shiftBlocks.get(toEditShiftBlockId)
            binding.shiftBlockNameEdit.setText(toEditShiftBlock.block.name)
        } else {
            toEditShiftBlock = ShiftBlockDTO()
        }
    }

    //On Shift in ShiftBlock Clicked
    override fun invoke(position: Int) {
        if (selectedShiftID >= 0) {
            val shift = sc.shifts.get(selectedShiftID)
            if (position + 1 > toEditShiftBlock.getMaxDays()) {
                toEditShiftBlock.add(position, shift.id)
            } else {
                val shiftsInPos = toEditShiftBlock.getAtPos(position)
                if (!(settings.getBoolean(Settings.DUAL_SHIFT) && shiftsInPos.size == 1)) {
                    toEditShiftBlock.removeAt(position)
                }
                toEditShiftBlock.add(position, selectedShiftID)
            }
        } else if (selectedShiftID == SpecialShifts.DELETE_ID) {
            if (position <= toEditShiftBlock.getMaxDays()) {
                toEditShiftBlock.removeAt(position)
            }
        } else {
            val shiftBlockCreatorIntro = ShiftBlockCreatorIntro(this, this, binding)
            shiftBlockCreatorIntro.restartForLevel8()
            shiftBlockCreatorIntro.showIntro()
        }
        updateAdapter()
    }

    //Updates the shift block view
    fun updateAdapter() {
        binding.blockContainer.adapter = ShiftBlockAdapter(this, toEditShiftBlock, 31, this)
    }

    fun showShiftList() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_shift_selector, null)
        val builder = MaterialAlertDialogBuilder(this)
        val listViewShifts = dialogLayout as ListView

        shiftSelectorAdapter = ShiftAdapter(this, shifts)
        listViewShifts.adapter = shiftSelectorAdapter
        listViewShifts.onItemClickListener = this
        builder.setView(dialogLayout)
        shiftSelectorDialog = builder.create()
        shiftSelectorDialog?.show()
    }

    private fun setShiftSelectorFab(shift: Shift) {
        binding.shiftSelectorFab.background = shiftSelectorBackground
        binding.shiftSelectorFab.icon = null
        binding.shiftSelectorFab.background.setTint(shift.color)
        binding.shiftSelectorFab.text = shift.shortName
        if (ColorHelper.isTooBright(shift.color)) {
            binding.shiftSelectorFab.setTextColor(getColor(R.color.textColorBlack))
        } else {
            binding.shiftSelectorFab.setTextColor(getColor(R.color.textColorWhite))
        }
    }

    //On Shift Selected
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val shift = shiftSelectorAdapter?.getItem(position)
        if (shift != null) {
            setShiftSelectorFab(shift)
            selectedShiftID = shift.id
            shiftSelectorDialog?.dismiss()
        }
    }

    private fun saveShiftBlock() {
        toEditShiftBlock = genShiftBlock()
        if (toEditShiftBlock.block.name.isNotEmpty() && toEditShiftBlock.getMaxDays() > 0) {
            if (toEditShiftBlockId == -1) {
                sc.shiftBlocks.add(toEditShiftBlock)
            } else {
                sc.shiftBlocks.set(toEditShiftBlock)
            }
            this.finish()
        } else {
            if (toEditShiftBlock.block.name.isEmpty()) {
                binding.shiftBlockNameEdit.background.setTint(Color.RED)
            } else {
                binding.shiftBlockNameEdit.background.setTint(getColor(R.color.textColor))
            }
            Snack.not(binding.root, getString(R.string.shift_creator_error_information))
        }
    }

    fun genShiftBlock(): ShiftBlockDTO {
        val name = binding.shiftBlockNameEdit.text.toString()
        toEditShiftBlock.block.name = name
        return toEditShiftBlock
    }

    //On fab clicked
    override fun onClick(v: View?) {
        when (v) {
            binding.shiftSelectorFab -> {
                showShiftList()
            }

            binding.fabDoneShiftBlock -> {
                saveShiftBlock()
            }
        }
    }
}
