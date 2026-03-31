package de.nulide.shiftcal.data.calendar

import android.content.Context
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.settings.CalendarMarkerType
import java.io.File
import java.time.LocalDate

class AustriaSchoolBreakProvider(private val context: Context) {

    data class State(val code: String, val label: String)

    companion object {
        private const val ASSET_PATH = "calendar/at_school_breaks_2025_2026.json"
        private const val OVERRIDE_FILE = "calendar/at_school_breaks_latest.json"

        val states = listOf(
            State("AT-1", "Burgenland"),
            State("AT-2", "Kaernten"),
            State("AT-3", "Niederoesterreich"),
            State("AT-4", "Oberoesterreich"),
            State("AT-5", "Salzburg"),
            State("AT-6", "Steiermark"),
            State("AT-7", "Tirol"),
            State("AT-8", "Vorarlberg"),
            State("AT-9", "Wien")
        )
    }

    private val allStates = states.map { it.code }.toSet()
    private val dataset: AustriaSchoolBreakDataset by lazy { loadDataset() }
    private val definitions: List<AustriaSchoolBreakEntry> by lazy { dataset.entries }

    fun getForSelectedStates(selectedStates: Set<String>): List<CalendarSpecialDate> {
        if (selectedStates.isEmpty()) return emptyList()
        return definitions
            .mapNotNull { definition ->
                val definitionStates = definition.states.toSet()
                val matchingStates = definitionStates.intersect(selectedStates)
                if (matchingStates.isEmpty()) return@mapNotNull null

                val startDate = runCatching { LocalDate.parse(definition.start) }.getOrNull()
                    ?: return@mapNotNull null
                val endDate = runCatching { LocalDate.parse(definition.end) }.getOrNull()
                    ?: return@mapNotNull null

                val stateSuffix = if (definitionStates == allStates) {
                    ""
                } else {
                    " (${matchingStates.sortedBy { code -> states.indexOfFirst { it.code == code } }.joinToString(", ") { code ->
                        states.first { it.code == code }.label
                    }})"
                }

                CalendarSpecialDate(
                    id = definition.id + matchingStates.sorted().joinToString(prefix = "_", separator = "_"),
                    name = definition.name + stateSuffix,
                    type = CalendarMarkerType.SCHOOL_BREAK,
                    startDate = startDate,
                    endDate = endDate
                )
            }
            .sortedWith(compareBy({ it.startDate }, { it.name }))
    }

    private fun loadDataset(): AustriaSchoolBreakDataset {
        val override = File(context.filesDir, OVERRIDE_FILE)
        return try {
            if (override.exists()) {
                val overrideDataset = JIO.getObjectMapper()
                    .readValue(override, AustriaSchoolBreakDataset::class.java)
                AustriaSchoolBreakDatasetValidator
                    .validate(overrideDataset, allStates)
                    .takeIf { it.isValid }
                    ?.let { overrideDataset }
                    ?: loadBundledDataset()
            } else {
                loadBundledDataset()
            }
        } catch (_: Exception) {
            loadBundledDataset()
        }
    }

    private fun loadBundledDataset(): AustriaSchoolBreakDataset {
        return try {
            context.assets.open(ASSET_PATH).use { input ->
                val bundled = JIO.getObjectMapper()
                    .readValue(input, AustriaSchoolBreakDataset::class.java)
                AustriaSchoolBreakDatasetValidator
                    .validate(bundled, allStates)
                    .takeIf { it.isValid }
                    ?.let { bundled }
                    ?: AustriaSchoolBreakDataset()
            }
        } catch (_: Exception) {
            AustriaSchoolBreakDataset()
        }
    }
}
