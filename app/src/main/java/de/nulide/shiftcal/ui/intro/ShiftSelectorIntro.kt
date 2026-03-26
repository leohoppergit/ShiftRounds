package de.nulide.shiftcal.ui.intro

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.databinding.FragmentShiftCalendarBinding
import de.nulide.shiftcal.utils.TooltipHelper

class ShiftSelectorIntro(
    context: Context,
    val lifecycleOwner: LifecycleOwner,
    val binding: FragmentShiftCalendarBinding
) : Intro(context, Settings.INTRO_SHIFT_SELECTOR) {

    override fun onNextBalloon(step: Int): Boolean {
        when (step) {
            0 -> {
                TooltipHelper.createTooltipWithListener(
                    context, lifecycleOwner, TooltipHelper.TOP, context.getString(
                        R.string.shift_selector_intro_select_date
                    ), this
                ).showAtCenter(binding.monthCal)
            }

            1 -> {
                settings.set(Settings.INTRO_SHIFT_SELECTOR, true)
                return false
            }
        }
        return true
    }

}