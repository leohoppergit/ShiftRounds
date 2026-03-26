package de.nulide.shiftcal.ui.intro

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.databinding.FragmentShiftCalendarBinding
import de.nulide.shiftcal.utils.TooltipHelper

class FabMenuIntro(
    context: Context,
    val lifecycleOwner: LifecycleOwner,
    val binding: FragmentShiftCalendarBinding
) : Intro(context, Settings.INTRO_FAB_MENU_EXPLANATION) {

    private val sc = SCRepoManager.getInstance(context)

    override fun onNextBalloon(step: Int): Boolean {
        when (step) {
            0 -> {
                TooltipHelper.createDefaultTooltipWithListener(
                    context,
                    lifecycleOwner,
                    TooltipHelper.END,
                    context.getString(R.string.calendar_shift_selector_tooltip),
                    this
                ).showAlignStart(binding.root.findViewById(R.id.shiftSelectorFab))
                if (sc.shiftBlocks.hasAny()) {
                    TooltipHelper.createDefaultTooltipWithListener(
                        context,
                        lifecycleOwner,
                        TooltipHelper.END,
                        context.getString(R.string.calendar_shift_block_selector_tooltip), this
                    ).showAlignStart(binding.root.findViewById(R.id.shiftBlockSelectorFab))
                }
                TooltipHelper.createDefaultTooltipWithListener(
                    context,
                    lifecycleOwner,
                    TooltipHelper.END,
                    context.getString(R.string.calendar_month_note_editor_tooltip), this
                ).showAlignStart(binding.root.findViewById(R.id.monthNoteFab))
            }

            1 -> {
                val shiftSelectorCounter = settings.getInt(Settings.INFO_SHIFT_SELECTOR_COUNT)
                if (shiftSelectorCounter < 3) {
                    settings.set(Settings.INFO_SHIFT_SELECTOR_COUNT, shiftSelectorCounter + 1)
                } else {
                    settings.set(Settings.INTRO_FAB_MENU_EXPLANATION, true)
                }
                return false
            }
        }
        return true
    }


}