package de.nulide.shiftcal.ui.intro

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.databinding.ActivityShiftBlockCreatorBinding
import de.nulide.shiftcal.ui.helper.OnCloseListener
import de.nulide.shiftcal.utils.TooltipHelper

class ShiftBlockCreatorIntro(
    context: Context,
    val lifecycleOwner: LifecycleOwner,
    val binding: ActivityShiftBlockCreatorBinding
) : Intro(context, Settings.INTRO_SHIFT_BLOCK_CREATOR),
    OnCloseListener {

    override fun onNextBalloon(step: Int): Boolean {
        when (step) {
            0 -> {
                TooltipHelper.createTooltipWithListener(
                    context, lifecycleOwner, TooltipHelper.TOP, context.getString(
                        R.string.intro_shift_block_creator_shift_selector
                    ), this
                ).showAlignTop(binding.shiftSelectorFab)
            }

            1 -> {
                settings.set(Settings.INTRO_SHIFT_BLOCK_CREATOR, true)
                return false
            }
        }
        return true
    }

}