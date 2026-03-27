package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.CompNextShiftBoxBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.utils.Runner

class NextShiftBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), ViewModelReceiver {

    val binding: CompNextShiftBoxBinding
    val sc = SCRepoManager.getInstance(context)
    lateinit var calViewModel: CalViewModel

    init {
        val layoutInflator = LayoutInflater.from(context)
        binding = CompNextShiftBoxBinding.inflate(layoutInflator, this, true)
    }

    fun updateNextText() {
        if (!sc.familyMode) {
            Runner.runCo {
                val nearest = sc.workDays.getUpcoming(true, 0)
                if (nearest != null) {
                    val shift = sc.shifts.get(nearest.shiftId)
                    val timeUntilShift =
                        TimeFactory.getTimeUntil(
                            nearest.day.toEpochDay(),
                            shift.startTime.timeInMinutes
                        )
                    if (timeUntilShift.first >= 24) {
                        binding.nextShiftText.text = ""
                        binding.nextShiftBox.visibility = GONE
                    } else {
                        binding.nextShiftBox.visibility = VISIBLE
                        binding.nextShiftText.text = context.getString(
                            R.string.calendar_time_till_next_shift,
                            timeUntilShift.first,
                            timeUntilShift.second,
                            shift.shortName
                        )
                    }
                } else {
                    binding.nextShiftText.text = ""
                    binding.nextShiftBox.visibility = GONE
                }
            }
        } else {
            binding.nextShiftText.text = ""
            binding.nextShiftBox.visibility = GONE
        }
    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        calViewModel.register(lifecycle, calViewModel.update) { updateNextText() }
        calViewModel.register(lifecycle, calViewModel.resume) { updateNextText() }
    }


}
