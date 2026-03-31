package de.nulide.shiftcal.data.calendar

data class AustriaSchoolBreakDataset(
    val schemaVersion: Int = 1,
    val datasetVersion: String = "",
    val schoolYear: String = "",
    val source: String = "",
    val sourceUrl: String = "",
    val updatedAt: String = "",
    val entries: List<AustriaSchoolBreakEntry> = emptyList()
)

data class AustriaSchoolBreakEntry(
    val id: String = "",
    val name: String = "",
    val start: String = "",
    val end: String = "",
    val states: List<String> = emptyList()
)
