package de.nulide.shiftcal.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.ActivityShiftsBinding
import de.nulide.shiftcal.sync.SyncHandler
import de.nulide.shiftcal.ui.editor.list.ShiftListAdapter
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.ui.helper.SpecialShifts.Companion.getArchivedShift
import de.nulide.shiftcal.ui.helper.WarningDialog
import java.util.LinkedList

class ShiftsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var sc: SCRepoManager
    private var shifts: LinkedList<Shift>? = null
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
            shifts = LinkedList<Shift>(sc.shifts.getArchived())
        } else {
            shifts = LinkedList<Shift>(sc.shifts.getNotArchived())
            if (sc.shifts.hasArchived()) {
                shifts!!.add(getArchivedShift(this))
            }
        }
        adapter = ShiftListAdapter(
            this,
            shifts!!,
            onClick = { index -> onShiftClicked(index) },
            onLongPressDrag = { viewHolder -> itemTouchHelper.startDrag(viewHolder) }
        )
        binding.listViewShifts.adapter = adapter
    }

    override fun onClick(view: View?) {
        val myIntent = Intent(this, ShiftCreatorActivity::class.java)
        myIntent.putExtra(ShiftCreatorActivity.SHIFT_ID_TAG, SpecialShifts.NONE_ID)
        startActivity(myIntent)
    }

    private fun onShiftClicked(i: Int) {
        val shiftId = shifts!![i].id
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
                val currentShifts = shifts ?: return false
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
                    val reordered = shifts?.filter { it.id >= 0 } ?: return
                    sc.shifts.reorder(reordered)
                }
            }
        }).also { it.attachToRecyclerView(binding.listViewShifts) }
    }
}
