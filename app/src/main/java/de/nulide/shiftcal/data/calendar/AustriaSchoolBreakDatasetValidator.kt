package de.nulide.shiftcal.data.calendar

import java.time.LocalDate

object AustriaSchoolBreakDatasetValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validate(
        dataset: AustriaSchoolBreakDataset,
        knownStateCodes: Set<String>
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (dataset.schemaVersion <= 0) {
            errors.add("schemaVersion must be greater than 0")
        }
        if (dataset.datasetVersion.isBlank()) {
            errors.add("datasetVersion must not be blank")
        }
        if (dataset.schoolYear.isBlank()) {
            errors.add("schoolYear must not be blank")
        }
        if (dataset.source.isBlank()) {
            errors.add("source must not be blank")
        }
        if (dataset.updatedAt.isBlank()) {
            errors.add("updatedAt must not be blank")
        } else if (runCatching { LocalDate.parse(dataset.updatedAt) }.isFailure) {
            errors.add("updatedAt must be an ISO date")
        }
        if (dataset.entries.isEmpty()) {
            errors.add("entries must not be empty")
        }

        val ids = mutableSetOf<String>()
        dataset.entries.forEachIndexed { index, entry ->
            val prefix = "entries[$index]"

            if (entry.id.isBlank()) {
                errors.add("$prefix.id must not be blank")
            } else if (!ids.add(entry.id)) {
                errors.add("$prefix.id must be unique")
            }

            if (entry.name.isBlank()) {
                errors.add("$prefix.name must not be blank")
            }
            if (entry.start.isBlank() || entry.end.isBlank()) {
                errors.add("$prefix start and end must not be blank")
            }

            val start = runCatching { LocalDate.parse(entry.start) }.getOrNull()
            val end = runCatching { LocalDate.parse(entry.end) }.getOrNull()

            if (start == null) {
                errors.add("$prefix.start must be an ISO date")
            }
            if (end == null) {
                errors.add("$prefix.end must be an ISO date")
            }
            if (start != null && end != null && end.isBefore(start)) {
                errors.add("$prefix.end must not be before start")
            }

            if (entry.states.isEmpty()) {
                errors.add("$prefix.states must not be empty")
            }
            val unknownStates = entry.states.filterNot { knownStateCodes.contains(it) }
            if (unknownStates.isNotEmpty()) {
                errors.add("$prefix.states contains unknown codes: ${unknownStates.joinToString()}")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}
