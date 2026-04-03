package de.nulide.shiftcal.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.size
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.ActivityShiftsBinding
import de.nulide.shiftcal.ui.editor.list.ShiftListAdapter
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.ui.helper.SpecialShifts.Companion.getArchivedShift
import de.nulide.shiftcal.ui.helper.WarningDialog
import java.util.LinkedList

class ShiftsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val MENU_DUPLICATE_SHIFT = 1001
    }

    private lateinit var sc: SCRepoManager
    private var shifts: LinkedList<Shift> = LinkedList()
    private var adapter: ShiftListAdapter? = null

    lateinit var binding: ActivityShiftsBinding
    private val ARCHIVED_MODE_TAG = "archived"

    private var archivedMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityShiftsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        ViewCompat.setOnApplyWindowInsetsListener(binding.shifts) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sc = SCRepoManager.getInstance(this)
        sc.switchToLocal()

        binding.fabAddShift.setOnClickListener(this)
        binding.listViewShifts.layoutManager = LinearLayoutManager(this)

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            if (bundle.getBoolean(ARCHIVED_MODE_TAG, false)) {
                setShiftsToArchived()
                archivedMode = true
            }
        }

        updateShifts()
    }

    fun setShiftsToArchived() {
        binding.topAppBar.title = getString(R.string.Settings_Archived_Shifts)
        binding.fabAddShift.visibility = View.GONE
    }

    fun updateShifts() {
        if (archivedMode) {
            shifts = LinkedList(sc.shifts.getArchived())
        } else {
            shifts = LinkedList(sc.shifts.getNotArchived())
            if (sc.shifts.hasArchived()) {
                shifts.add(getArchivedShift(this))
            }
        }
        adapter = ShiftListAdapter(
            this,
            shifts,
            onClick = { index -> onShiftClicked(index) },
            onLongPressDrag = { viewHolder -> itemTouchHelper.startDrag(viewHolder) },
            onMoreClick = { index, anchor -> showShiftActions(index, anchor) }
        )
        binding.listViewShifts.adapter = adapter
    }

    override fun onClick(view: View?) {
        val myIntent = Intent(this, ShiftCreatorActivity::class.java)
        myIntent.putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, SpecialShifts.NONE_ID)
        startActivity(myIntent)
    }

    private fun onShiftClicked(i: Int) {
        val shiftId = shifts.getOrNull(i)?.id ?: return
        val myIntent: Intent?
        if (shiftId == SpecialShifts.ARCHIVE_ID) {
            myIntent = Intent(this, ShiftsActivity::class.java)
            myIntent.putExtra(ARCHIVED_MODE_TAG, true)
        } else {
            myIntent = Intent(this, ShiftCreatorActivity::class.java)
            myIntent.putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, shiftId)
        }
        startActivity(myIntent)
    }

    override fun onResume() {
        super.onResume()
        updateShifts()
    }

    private fun showShiftActions(index: Int, anchor: View) {
        val shift = shifts.getOrNull(index) ?: return
        if (shift.id < 0) return
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(
                if (archivedMode) R.menu.menu_archived_shifts_actions else R.menu.menu_shifts_actions,
                menu
            )
            menu.add(0, MENU_DUPLICATE_SHIFT, menu.size, getString(R.string.shift_duplicate))
            setOnMenuItemClickListener { item -> onShiftActionSelected(item, shift) }
            show()
        }
    }

    private fun onShiftActionSelected(item: MenuItem, shift: Shift): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                startActivity(
                    Intent(this, ShiftCreatorActivity::class.java).apply {
                        putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, shift.id)
                    }
                )
                true
            }

            R.id.action_delete -> {
                confirmDeleteShift(shift)
                true
            }

            R.id.action_archive -> {
                sc.shifts.update(shift.copy(archived = true))
                updateShifts()
                true
            }

            R.id.action_unarchive -> {
                sc.shifts.update(shift.copy(archived = false))
                updateShifts()
                true
            }

            MENU_DUPLICATE_SHIFT -> {
                duplicateShift(shift)
                true
            }

            else -> false
        }
    }

    private fun confirmDeleteShift(shift: Shift) {
        val warningDialog = WarningDialog(
            this,
            getString(R.string.warning_delete_item, shift.name)
        )
        warningDialog.setPositiveButton(R.string.yes) { _, _ ->
            sc.shifts.delete(shift)
            updateShifts()
        }
        warningDialog.enableNegativeButton()
        warningDialog.show()
    }

    private fun duplicateShift(source: Shift) {
        val nextSortOrder = sc.shifts.getAll()
            .filter { it.id >= 0 }
            .maxOfOrNull { it.sortOrder }
            ?.plus(1)
            ?: 0
        val duplicate = source.copy(
            id = sc.shifts.getNextId(),
            calendarId = sc.curCalId,
            name = getString(R.string.shift_duplicate_name, source.name),
            sortOrder = nextSortOrder
        )
        sc.shifts.add(duplicate)
        Toast.makeText(this, R.string.shift_duplicate_success, Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, ShiftCreatorActivity::class.java).apply {
                putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, duplicate.id)
            }
        )
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                val currentShifts = shifts
                if (from !in currentShifts.indices || to !in currentShifts.indices) return false
                val fromShift = currentShifts[from]
                val toShift = currentShifts[to]
                if (fromShift.id < 0 || toShift.id < 0) return false
                adapter?.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun isLongPressDragEnabled(): Boolean = false

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (!archivedMode) {
                    val reordered = shifts.filter { it.id >= 0 }
                    sc.shifts.reorder(reordered)
                }
            }
        }).also { it.attachToRecyclerView(binding.listViewShifts) }
    }
}
