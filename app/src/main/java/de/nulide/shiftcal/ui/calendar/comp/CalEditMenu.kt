package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.CompCalEditMenuBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.utils.EasterEggHandler

class CalEditMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), ViewModelReceiver, View.OnClickListener {

    private val binding: CompCalEditMenuBinding
    private val sc = SCRepoManager.getInstance(context)
    private lateinit var calViewModel: CalViewModel
    private var easterEggHandler: EasterEggHandler


    init {
        val layoutInflator = LayoutInflater.from(context)
        binding = CompCalEditMenuBinding.inflate(layoutInflator, this, true)

        easterEggHandler = EasterEggHandler(context, binding.editFab)
        binding.editFab.setOnClickListener(this)
        binding.fabContainer.hideItems(false)

    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            updateVisiblity()
        }

        calViewModel.register(lifecycle, calViewModel.resume) {
            updateVisiblity()
        }
    }

    fun updateVisiblity() {
        if (sc.familyMode) {
            if (calViewModel.getEditMode()) {
                onClick(binding.editFab)
            }
            binding.editFab.visibility = GONE
        } else {
            binding.editFab.visibility = VISIBLE
        }
    }

    override fun onClick(p0: View?) {
        easterEggHandler.onClick()
        if (calViewModel.getEditMode()) {
            binding.editFab.setIconResource(R.drawable.ic_edit)
            binding.fabContainer.collapse()
            calViewModel.setEditMode(false)

            calViewModel.trigger(calViewModel.shiftSelected)
            calViewModel.trigger(calViewModel.shiftBlockSelected)

        } else {
            binding.fabContainer.expand()
            binding.editFab.setIconResource(R.drawable.ic_done)
            calViewModel.setEditMode(true)
        }
    }

}