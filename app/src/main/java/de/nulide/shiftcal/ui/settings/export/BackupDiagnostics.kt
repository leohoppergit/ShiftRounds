package de.nulide.shiftcal.ui.settings.export

import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.data.repository.wrapper.FullBackupImportParser

data class BackupDiagnosticsReport(
    val compatible: Boolean,
    val summary: String,
    val details: String
)

object BackupDiagnostics {

    fun inspect(json: String): BackupDiagnosticsReport {
        return try {
            val root = JIO.getObjectMapper().readTree(json)
                ?: return incompatible("Backup JSON konnte nicht gelesen werden.")
            if (!root.isObject) {
                return incompatible("Die Datei enthält kein gültiges JSON-Objekt.")
            }

            val parsedBackup = FullBackupImportParser.parse(json)
                ?: return incompatible("Die Datei entspricht nicht dem erwarteten ShiftRounds-Backup-Format.")

            val rawShiftCount = root.path("shifts").takeIf { it.isArray }?.size() ?: 0
            val rawWorkDayCount = root.path("workDays").takeIf { it.isArray }?.size() ?: 0
            val rawMonthNoteCount = root.path("monthNotes").takeIf { it.isArray }?.size() ?: 0
            val rawShiftBlockCount = root.path("shiftBlocks").takeIf { it.isArray }?.size() ?: 0

            val shiftIds = parsedBackup.shifts.map { it.id }
            val duplicateShiftIds = shiftIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.sorted()
            val duplicateSortOrders = parsedBackup.shifts.groupingBy { it.sortOrder }.eachCount().filterValues { it > 1 }.keys.sorted()
            val emptyShortNames = parsedBackup.shifts.filter { it.shortName.isBlank() }.map { it.name }
            val missingWorkDayShiftRefs = parsedBackup.workDays
                .map { it.shiftId }
                .filter { it !in shiftIds }
                .distinct()
                .sorted()
            val parsedOutWorkDays = rawWorkDayCount - parsedBackup.workDays.size
            val parsedOutShifts = rawShiftCount - parsedBackup.shifts.size

            val issues = mutableListOf<String>()
            if (parsedBackup.backupVersion <= 0) issues += "backupVersion ist ungültig."
            if (parsedBackup.appName.isBlank()) issues += "appName fehlt."
            if (parsedOutShifts > 0) issues += "$parsedOutShifts Schichten konnten nicht geparst werden."
            if (parsedOutWorkDays > 0) issues += "$parsedOutWorkDays Kalendereinträge konnten nicht geparst werden."
            if (duplicateShiftIds.isNotEmpty()) issues += "Doppelte Shift-IDs: ${duplicateShiftIds.joinToString(", ")}."
            if (duplicateSortOrders.isNotEmpty()) issues += "Doppelte Sortierreihenfolgen: ${duplicateSortOrders.joinToString(", ")}."
            if (emptyShortNames.isNotEmpty()) issues += "Leere Kürzel bei: ${emptyShortNames.joinToString(", ")}."
            if (missingWorkDayShiftRefs.isNotEmpty()) issues += "Kalendereinträge referenzieren fehlende Shift-IDs: ${missingWorkDayShiftRefs.joinToString(", ")}."

            val summary = if (issues.isEmpty()) {
                "Kompatible ShiftRounds-Sicherung erkannt."
            } else {
                "Sicherung erkannt, aber mit Auffälligkeiten."
            }

            val details = buildString {
                appendLine(summary)
                appendLine()
                appendLine("App: ${parsedBackup.appName}")
                appendLine("Backup-Version: ${parsedBackup.backupVersion}")
                appendLine("Exportiert am: ${parsedBackup.exportedAt.ifBlank { "unbekannt" }}")
                appendLine("Nutzername: ${parsedBackup.userName.ifBlank { "leer" }}")
                appendLine()
                appendLine("Inhalt:")
                appendLine("- Schichten: ${parsedBackup.shifts.size} (roh: $rawShiftCount)")
                appendLine("- Kalendereinträge: ${parsedBackup.workDays.size} (roh: $rawWorkDayCount)")
                appendLine("- Monatsnotizen: ${parsedBackup.monthNotes.size} (roh: $rawMonthNoteCount)")
                appendLine("- Schichtblöcke: ${parsedBackup.shiftBlocks.size} (roh: $rawShiftBlockCount)")
                appendLine()
                if (issues.isEmpty()) {
                    appendLine("Keine strukturellen Auffälligkeiten erkannt.")
                } else {
                    appendLine("Auffälligkeiten:")
                    issues.forEach { appendLine("- $it") }
                }
            }.trim()

            BackupDiagnosticsReport(
                compatible = issues.isEmpty(),
                summary = summary,
                details = details
            )
        } catch (exception: Exception) {
            incompatible("Diagnose fehlgeschlagen: ${exception.message ?: exception::class.java.simpleName}")
        }
    }

    private fun incompatible(message: String): BackupDiagnosticsReport {
        return BackupDiagnosticsReport(
            compatible = false,
            summary = "Keine kompatible ShiftRounds-Sicherung erkannt.",
            details = "Keine kompatible ShiftRounds-Sicherung erkannt.\n\n$message"
        )
    }
}

object BackupRestoreDebugState {
    @Volatile
    private var lastRestoreError: String? = null

    fun setError(message: String) {
        lastRestoreError = message
    }

    fun setException(exception: Throwable) {
        val lines = mutableListOf<String>()
        var current: Throwable? = exception
        while (current != null) {
            val message = current.message?.takeIf { it.isNotBlank() }
            lines += if (message != null) {
                "${current::class.java.simpleName}: $message"
            } else {
                current::class.java.simpleName
            }
            current = current.cause
        }
        lastRestoreError = lines.joinToString(separator = "\n")
    }

    fun getError(): String? = lastRestoreError

    fun clear() {
        lastRestoreError = null
    }
}
