package de.nulide.shiftcal.ui.calendar.comp.usecase

import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel

class SwitchCalendarDataUseCase(
    val settings: SettingsRepository,
    val sc: SCRepoManager,
    val calViewModel: CalViewModel
) {

    fun switch() {
        if (sc.switchCalendar()) {
            settings.set(Settings.CALENDAR_IN_FAMILY_MODE, sc.familyMode)
        }
    }

    fun switchToLast() {
        if (settings.getBoolean(Settings.CALENDAR_IN_FAMILY_MODE)) {
            if (sc.switchToNet()) {
                calViewModel.trigger(calViewModel.calendarChange)
            }
        } else {
            sc.switchToLocal()
        }
    }

    fun switchToDefault() {
        if (sc.familyMode) {
            switch()
        }
    }

}