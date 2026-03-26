package de.nulide.shiftcal.ui.calendar.comp.usecase

import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel

class SwitchCalendarViewUseCase(val settings: SettingsRepository, val calViewModel: CalViewModel) {

    enum class DISP {
        MONTH, WEEK
    }

    fun switch() {
        if (calViewModel.getCurrentDisp() == DISP.MONTH) {
            settings.set(Settings.CALENDAR_IN_FAMILY_WEEK_VIEW, true)
            calViewModel.setCurrentDisp(DISP.WEEK)
        } else {
            settings.set(Settings.CALENDAR_IN_FAMILY_WEEK_VIEW, false)
            calViewModel.setCurrentDisp(DISP.MONTH)
        }
    }

    fun switchToDefault() {
        if (calViewModel.getCurrentDisp() == DISP.WEEK) {
            switch()
        }
    }

    fun switchToLast() {
        if (settings.getBoolean(Settings.CALENDAR_IN_FAMILY_WEEK_VIEW)) {
            if (calViewModel.getCurrentDisp() == DISP.MONTH) {
                switch()
            }
        } else {
            switchToDefault()
        }
    }

}