package de.nulide.shiftcal.data.calendar

import android.content.Context
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.factory.JIO
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AustriaSchoolBreakUpdateManager(private val context: Context) {

    data class DatasetStatus(
        val sourceType: SourceType,
        val datasetVersion: String,
        val schoolYear: String,
        val updatedAt: String,
        val sourceLabel: String
    )

    data class ImportResult(
        val success: Boolean,
        val errorMessage: String? = null,
        val datasetStatus: DatasetStatus? = null
    )

    data class UpdateCheckResult(
        val state: UpdateState,
        val currentDatasetStatus: DatasetStatus,
        val remoteDatasetStatus: DatasetStatus? = null,
        val errorMessage: String? = null,
        val validationErrors: List<String> = emptyList()
    )

    enum class SourceType {
        BUNDLED,
        OVERRIDE
    }

    enum class UpdateState {
        DISABLED,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        INVALID_REMOTE_DATASET,
        NETWORK_ERROR
    }

    companion object {
        const val REMOTE_DATA_URL =
            "https://raw.githubusercontent.com/leohoppergit/ShiftRounds/main/calendar/at_school_breaks_latest.json"
        private const val ASSET_PATH = "calendar/at_school_breaks_2025_2026.json"
        private const val OVERRIDE_RELATIVE_PATH = "calendar/at_school_breaks_latest.json"
        private const val CONNECT_TIMEOUT_MS = 5000
        private const val READ_TIMEOUT_MS = 5000
        private const val ONLINE_UPDATES_ENABLED = false
    }

    fun getRemoteDataUrl(): String = REMOTE_DATA_URL

    fun getOverrideFile(): File = File(context.filesDir, OVERRIDE_RELATIVE_PATH)

    fun hasLocalOverride(): Boolean = getOverrideFile().exists()

    fun isOnlineUpdatesEnabled(): Boolean = ONLINE_UPDATES_ENABLED

    fun getActiveDatasetStatus(): DatasetStatus {
        val overrideStatus = getOverrideDatasetStatus()
        return overrideStatus ?: getBundledDatasetStatus()
    }

    fun getDataSourceSummary(): String {
        val status = getActiveDatasetStatus()
        return context.getString(
            R.string.settings_school_break_data_source_summary,
            getSourceTypeLabel(status.sourceType),
            status.datasetVersion,
            status.schoolYear,
            status.updatedAt
        )
    }

    fun getPreparedUpdateSummary(): String {
        val status = getActiveDatasetStatus()
        return context.getString(
            R.string.settings_school_break_update_prepared_message,
            status.datasetVersion,
            status.schoolYear,
            status.updatedAt,
            getSourceTypeLabel(status.sourceType),
            REMOTE_DATA_URL
        )
    }

    fun checkForRemoteUpdate(): UpdateCheckResult {
        val currentStatus = getActiveDatasetStatus()
        if (!ONLINE_UPDATES_ENABLED) {
            return UpdateCheckResult(
                state = UpdateState.DISABLED,
                currentDatasetStatus = currentStatus
            )
        }

        return try {
            val remoteJson = downloadRemoteJson()
            val validation = validateJson(remoteJson)
            if (!validation.isValid) {
                UpdateCheckResult(
                    state = UpdateState.INVALID_REMOTE_DATASET,
                    currentDatasetStatus = currentStatus,
                    validationErrors = validation.errors
                )
            } else {
                val remoteDataset = JIO.fromJSON(remoteJson, AustriaSchoolBreakDataset::class.java)
                val remoteStatus = DatasetStatus(
                    sourceType = SourceType.OVERRIDE,
                    datasetVersion = remoteDataset.datasetVersion,
                    schoolYear = remoteDataset.schoolYear,
                    updatedAt = remoteDataset.updatedAt,
                    sourceLabel = remoteDataset.source
                )
                val hasNewerVersion = compareDatasetVersions(
                    currentStatus.datasetVersion,
                    remoteDataset.datasetVersion
                ) < 0

                UpdateCheckResult(
                    state = if (hasNewerVersion) UpdateState.UPDATE_AVAILABLE else UpdateState.UP_TO_DATE,
                    currentDatasetStatus = currentStatus,
                    remoteDatasetStatus = remoteStatus
                )
            }
        } catch (exception: Exception) {
            UpdateCheckResult(
                state = UpdateState.NETWORK_ERROR,
                currentDatasetStatus = currentStatus,
                errorMessage = exception.message
            )
        }
    }

    fun validateJson(json: String): AustriaSchoolBreakDatasetValidator.ValidationResult {
        return try {
            val dataset = JIO.fromJSON(json, AustriaSchoolBreakDataset::class.java)
            AustriaSchoolBreakDatasetValidator.validate(
                dataset,
                AustriaSchoolBreakProvider.states.map { it.code }.toSet()
            )
        } catch (exception: Exception) {
            AustriaSchoolBreakDatasetValidator.ValidationResult(
                isValid = false,
                errors = listOfNotNull(exception.message ?: "JSON could not be parsed")
            )
        }
    }

    fun importValidatedJson(json: String): ImportResult {
        val validation = validateJson(json)
        if (!validation.isValid) {
            return ImportResult(
                success = false,
                errorMessage = validation.errors.joinToString(separator = "\n")
            )
        }

        return try {
            val dataset = JIO.fromJSON(json, AustriaSchoolBreakDataset::class.java)
            val overrideFile = getOverrideFile()
            overrideFile.parentFile?.mkdirs()
            JIO.saveAsJSON(dataset, overrideFile)

            ImportResult(
                success = true,
                datasetStatus = DatasetStatus(
                    sourceType = SourceType.OVERRIDE,
                    datasetVersion = dataset.datasetVersion,
                    schoolYear = dataset.schoolYear,
                    updatedAt = dataset.updatedAt,
                    sourceLabel = dataset.source
                )
            )
        } catch (exception: Exception) {
            ImportResult(success = false, errorMessage = exception.message)
        }
    }

    private fun getBundledDatasetStatus(): DatasetStatus {
        return try {
            val dataset = context.assets.open(ASSET_PATH).use { input ->
                JIO.getObjectMapper().readValue(input, AustriaSchoolBreakDataset::class.java)
            }
            DatasetStatus(
                sourceType = SourceType.BUNDLED,
                datasetVersion = dataset.datasetVersion.ifBlank { "unknown" },
                schoolYear = dataset.schoolYear.ifBlank { "-" },
                updatedAt = dataset.updatedAt.ifBlank { "-" },
                sourceLabel = dataset.source.ifBlank { "Bundled dataset" }
            )
        } catch (_: Exception) {
            DatasetStatus(
                sourceType = SourceType.BUNDLED,
                datasetVersion = "unknown",
                schoolYear = "-",
                updatedAt = "-",
                sourceLabel = "Bundled dataset"
            )
        }
    }

    private fun getOverrideDatasetStatus(): DatasetStatus? {
        val overrideFile = getOverrideFile()
        if (!overrideFile.exists()) return null

        return try {
            val dataset = JIO.getObjectMapper()
                .readValue(overrideFile, AustriaSchoolBreakDataset::class.java)
            val validation = AustriaSchoolBreakDatasetValidator.validate(
                dataset,
                AustriaSchoolBreakProvider.states.map { it.code }.toSet()
            )
            if (!validation.isValid) return null

            DatasetStatus(
                sourceType = SourceType.OVERRIDE,
                datasetVersion = dataset.datasetVersion,
                schoolYear = dataset.schoolYear,
                updatedAt = dataset.updatedAt,
                sourceLabel = dataset.source
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun getSourceTypeLabel(sourceType: SourceType): String {
        return when (sourceType) {
            SourceType.BUNDLED -> context.getString(R.string.settings_school_break_data_source_type_bundled)
            SourceType.OVERRIDE -> context.getString(R.string.settings_school_break_data_source_type_override)
        }
    }

    private fun downloadRemoteJson(): String {
        val connection = (URL(REMOTE_DATA_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "GET"
            doInput = true
        }

        return try {
            connection.connect()
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun compareDatasetVersions(currentVersion: String, remoteVersion: String): Int {
        val currentParts = currentVersion.split('.', '-', '_')
        val remoteParts = remoteVersion.split('.', '-', '_')
        val maxSize = maxOf(currentParts.size, remoteParts.size)

        repeat(maxSize) { index ->
            val currentPart = currentParts.getOrNull(index).orEmpty()
            val remotePart = remoteParts.getOrNull(index).orEmpty()

            val currentInt = currentPart.toIntOrNull()
            val remoteInt = remotePart.toIntOrNull()

            val comparison = when {
                currentInt != null && remoteInt != null -> currentInt.compareTo(remoteInt)
                else -> currentPart.compareTo(remotePart)
            }

            if (comparison != 0) {
                return comparison
            }
        }

        return 0
    }
}
