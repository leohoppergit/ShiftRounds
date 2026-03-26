package de.nulide.shiftcal.ui.calendar.comp.usecase

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.helper.SpecialShifts
import java.time.LocalDate

class InsertShiftUseCase(
    val sc: SCRepoManager,
    val calViewModel: CalViewModel,
    val settings: SettingsRepository
) {

    fun insertInto(date: CalendarDay, selectedShiftID: Int) {
        if (date.position == DayPosition.MonthDate) {

            // Edit logic
            if (calViewModel.getEditMode()) {
                if (selectedShiftID != SpecialShifts.NONE_ID) {
                    if (selectedShiftID != SpecialShifts.DELETE_ID) {
                        val existingShifts = sc.shifts.getOn(date.date)
                        val newWDay =
                            WorkDay(
                                0,
                                0,
                                date.date,
                                selectedShiftID,
                                false,
                                mutableListOf(),
                                "",
                                0
                            )
                        if (existingShifts.isEmpty()) {
                            sc.workDays.add(newWDay)
                        } else if (existingShifts.size == 1 && settings.getBoolean(Settings.DUAL_SHIFT)) {
                            if (existingShifts[0].id == selectedShiftID) {
                                sc.workDays.deleteAllOn(date.date)
                            }
                            sc.workDays.add(newWDay)
                        } else {
                            sc.workDays.deleteAllOn(date.date)
                            sc.workDays.add(newWDay)
                        }
                    } else {
                        if (sc.workDays.hasWork(date.date)) {
                            sc.workDays.deleteAllOn(date.date)
                        }
                    }

                    calViewModel.trigger(calViewModel.dayUpdated, date.date)

                }
                val today = LocalDate.now()
                val tomorrow = today.plusDays(1)
                if (date.date == today || date.date == tomorrow) {
                    calViewModel.trigger(calViewModel.update, Unit)
                }
            }

        }
    }
}
