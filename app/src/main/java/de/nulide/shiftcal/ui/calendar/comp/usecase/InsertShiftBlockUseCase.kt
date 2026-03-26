package de.nulide.shiftcal.ui.calendar.comp.usecase

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import de.nulide.shiftcal.data.model.WorkDay
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.helper.SpecialShifts
import java.time.LocalDate

class InsertShiftBlockUseCase(val sc: SCRepoManager, val calViewModel: CalViewModel) {


    fun insertInto(date: CalendarDay, selectedShiftBlockID: Int) {
        if (date.position == DayPosition.MonthDate) {

            // Edit logic
            if (calViewModel.getEditMode()) {
                if (selectedShiftBlockID > SpecialShifts.NONE_ID) {
                    val shiftBlock =
                        sc.shiftBlocks.get(selectedShiftBlockID)
                    var selectedDate = date.date

                    for (i in 0..<shiftBlock.getMaxDays()) {

                        sc.workDays.deleteAllOn(selectedDate)

                        val shiftBlockEntriesOnDay = shiftBlock.getAtPos(i)

                        for (shiftBlock in shiftBlockEntriesOnDay) {
                            sc.workDays.add(
                                WorkDay(
                                    0, 0,
                                    selectedDate,
                                    shiftBlock.shiftId,
                                    false,
                                    mutableListOf(),
                                    "",
                                    0
                                )
                            )

                        }

                        calViewModel.trigger(calViewModel.dayUpdated, selectedDate)

                        selectedDate = selectedDate.plusDays(1)
                    }

                    val today = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    if (selectedDate == today || selectedDate == tomorrow) {
                        calViewModel.trigger(calViewModel.update, Unit)
                    }
                }
            }
        }
    }
}
