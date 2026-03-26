package de.nulide.shiftcal.ui.intro

import android.content.Context
import com.skydoves.balloon.OnBalloonDismissListener
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.helper.OnCloseListener
import de.nulide.shiftcal.utils.Runner

abstract class Intro(val context: Context, CONDITIONAL_SETTING: String) :
    OnBalloonDismissListener, OnCloseListener {

    var settings: SettingsRepository = SettingsRepository.getInstance(context)

    private var introStep = -1

    var finished = false

    init {
        finished = settings.getBoolean(CONDITIONAL_SETTING)
    }

    fun restartForLevel8() {
        finished = false
    }

    fun showIntro() {
        if (!finished) {
            Runner.runLate {
                Runner.run(::onStart)
            }
        }
    }

    open fun onStart() {
        nextBalloon()
    }

    override fun onClose() {
        nextBalloon()
    }

    abstract fun onNextBalloon(step: Int): Boolean

    fun nextBalloon() {
        onBalloonDismiss()
    }

    override fun onBalloonDismiss() {
        introStep += 1
        if (!onNextBalloon(introStep)) {
            finished = true
        }
    }
}