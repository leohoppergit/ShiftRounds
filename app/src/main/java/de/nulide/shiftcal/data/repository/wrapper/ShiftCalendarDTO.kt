package de.nulide.shiftcal.data.repository.wrapper

import de.nulide.shiftcal.data.model.MonthNote
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay

data class ShiftCalendarDTO(
    val shifts: List<Shift>,
    val monthNotes: List<MonthNote>,
    val workDays: List<WorkDay>
) {
    constructor() : this(emptyList<Shift>(), emptyList<MonthNote>(), emptyList<WorkDay>())
}