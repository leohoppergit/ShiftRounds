package de.nulide.shiftcal.ui.settings.export

import de.nulide.shiftcal.data.model.Shift

data class SettingsExportData(
    val exportedAt: String,
    val appName: String,
    val settings: Map<String, String>
)

data class ShiftsExportData(
    val exportedAt: String,
    val appName: String,
    val shifts: List<Shift>
)
