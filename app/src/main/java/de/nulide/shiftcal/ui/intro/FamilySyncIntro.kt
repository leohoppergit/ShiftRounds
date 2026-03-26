package de.nulide.shiftcal.ui.intro

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.button.MaterialButton
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.ui.helper.OnCloseListener
import de.nulide.shiftcal.utils.TooltipHelper

class FamilySyncIntro(
    context: Context,
    val lifecycleOwner: LifecycleOwner,
    val familyButton: MaterialButton?
) : Intro(context, Settings.FAMILY_SYNC_INTRO_SHOWN),
    OnCloseListener {

    init {
        finished =
            !settings.getBoolean(Settings.WELCOME_INTRO_SHOWN) || finished   //Skip FamilySyncIntro if WelcomeIntro isn't finished
    }

    override fun onNextBalloon(step: Int): Boolean {
        when (step) {
            0 -> {
                if (familyButton != null) {
                    val sc = SCRepoManager.getInstance(context)
                    TooltipHelper.createTooltipWithListener(
                        context,
                        lifecycleOwner,
                        TooltipHelper.TOP,
                        context.getString(
                            R.string.intro_family_sync_switch,
                            sc.users.getSubscribed()?.name
                        ),
                        this
                    ).showAlignBottom(familyButton)
                }
            }

            1 -> {
                settings.set(Settings.FAMILY_SYNC_INTRO_SHOWN, true)
                return false
            }
        }
        return true
    }

}