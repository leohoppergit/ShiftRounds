package de.nulide.shiftcal.ui.editor

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO
import de.nulide.shiftcal.databinding.ActivityShiftBlocksBinding
import de.nulide.shiftcal.ui.editor.list.ShiftBlockListAdapter
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.ui.helper.WarningDialog
import de.nulide.shiftcal.utils.Runner

class ShiftBlocksActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemClickListener {

    private lateinit var binding: ActivityShiftBlocksBinding
    private lateinit var sc: SCRepoManager

    private lateinit var shiftBlocks: MutableList<ShiftBlockDTO>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftBlocksBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        sc = SCRepoManager.getInstance(this)
        sc.switchToLocal()
        
        setContentView(binding.root)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { v: View? -> onBackPressedDispatcher.onBackPressed() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shiftBlocks)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fabAddShiftBlock.setOnClickListener(this)

        binding.listViewShiftBlocks.setOnItemClickListener(this)
        registerForContextMenu(binding.listViewShiftBlocks)
        updateShiftBlocks()
    }

    override fun onResume() {
        super.onResume()
        updateShiftBlocks()
    }

    private fun updateShiftBlocks() {
        Runner.runCo {
            shiftBlocks = sc.shiftBlocks.getAll()
            if (shiftBlocks.isNotEmpty()) {
                binding.shiftBlockDesc.visibility = View.GONE
            } else {
                binding.shiftBlockDesc.visibility = View.VISIBLE
            }
            val adapter = ShiftBlockListAdapter(this, shiftBlocks)
            binding.listViewShiftBlocks.setAdapter(adapter)
        }
    }

    override fun onClick(v: View?) {
        val intent = Intent(this, ShiftBlockCreatorActivity::class.java)
        intent.putExtra("toedit", SpecialShifts.NONE_ID)
        startActivity(intent)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val shiftBlockId: Int = shiftBlocks[position].block.id
        val intent = Intent(this, ShiftBlockCreatorActivity::class.java)
        intent.putExtra("toedit", shiftBlockId)
        startActivity(intent)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle(getString(R.string.shifts_menu_title))
        menuInflater.inflate(R.menu.menu_shift_blocks_actions, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo?
        val index = info?.position ?: return false
        if (item.itemId == R.id.action_edit) {
            val myIntent = Intent(this, ShiftBlockCreatorActivity::class.java)
            myIntent.putExtra("toedit", shiftBlocks[index].block.id)
            startActivity(myIntent)
        } else if (item.itemId == R.id.action_delete) {
            val warningDialog = WarningDialog(
                this,
                getString(R.string.warning_delete_item, shiftBlocks[index].block.name)
            )
            warningDialog.setPositiveButton(
                R.string.yes
            ) { _, _ ->
                sc.shiftBlocks.delete(shiftBlocks[index].block)
                updateShiftBlocks()
            }
            warningDialog.enableNegativeButton()
            warningDialog.show()


        } else {
            return false
        }
        return true
    }

}
