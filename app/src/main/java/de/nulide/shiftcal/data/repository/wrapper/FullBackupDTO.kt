package de.nulide.shiftcal.data.repository.wrapper

import de.nulide.shiftcal.data.model.MonthNote
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.WorkDay

data class FullBackupDTO(
    val backupVersion: Int,
    val exportedAt: String,
    val appName: String,
    val userName: String,
    val settings: Map<String, String>,
    val shifts: List<Shift>,
    val monthNotes: List<MonthNote>,
    val workDays: List<WorkDay>,
    val shiftBlocks: List<ShiftBlockDTO>
) {
    constructor() : this(
        1,
        "",
        "",
        "",
        emptyMap(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    )
}
