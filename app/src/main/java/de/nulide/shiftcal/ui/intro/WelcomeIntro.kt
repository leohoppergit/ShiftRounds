package de.nulide.shiftcal.ui.intro

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.databinding.FragmentShiftCalendarBinding
import de.nulide.shiftcal.ui.helper.OnCloseListener
import de.nulide.shiftcal.utils.TooltipHelper

class WelcomeIntro(
    context: Context,
    val lifecycleOwner: LifecycleOwner,
    val binding: FragmentShiftCalendarBinding
) : Intro(context, Settings.WELCOME_INTRO_SHOWN),
    OnCloseListener {

    override fun onStart() {
        val dialog = WelcomeDialog(context)
        dialog.onCloseListener = this
        dialog.show()
    }

    override fun onNextBalloon(step: Int): Boolean {
        when (step) {
            0 -> {
                TooltipHelper.createTooltipWithListener(
                    context,
                    lifecycleOwner,
                    TooltipHelper.TOP,
                    context.getString(R.string.welcome_calendar_info),
                    this
                ).showAtCenter(binding.monthCal)
            }

            1 -> {
                TooltipHelper.createTooltipWithListener(
                    context,
                    lifecycleOwner,
                    TooltipHelper.BOTTOM,
                    context.getString(R.string.welcome_shift_edit_info),
                    this
                ).showAlignTop(binding.root.findViewById(R.id.editFab))
            }

            2 -> {
                settings.set(Settings.WELCOME_INTRO_SHOWN, true)
                return false
            }
        }
        return true
    }

}