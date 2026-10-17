package com.example.humandigitaltwin.data.connectors

import android.content.Context
import com.example.humandigitaltwin.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Summary of a batch file import operation.
 */
data class BatchImportSummary(
    val totalRows: Int,
    val acceptedObservations: List<NormalizedObservation>,
    val rejectedRows: List<String>,
    val duplicateCount: Int,
    val warnings: List<String>
)

/**
 * Connector for ingesting historical or episodic health data
 * from CSV and JSON exports (Apple Health, Garmin, Dexcom Clarity, Lab Panels).
 */
class FileImportConnector : DeviceConnector {

    override val connectorId: String = "file_import_hub"
    override val name: String = "Health Data File Ingestion"
    override val description: String = "Batch import historical biometric records from CSV or JSON exports"
    override val providerType: ProviderType = ProviderType.FILE_IMPORT

    override val supportedMetrics: Set<StandardMetric> = setOf(
        StandardMetric.HEART_RATE,
        StandardMetric.BLOOD_GLUCOSE,
        StandardMetric.STEPS,
        StandardMetric.BODY_WEIGHT,
        StandardMetric.SPO2,
        StandardMetric.SLEEP_DURATION,
        StandardMetric.RESPIRATORY_RATE
    )

    private val _state = MutableStateFlow(ConnectorState.DISCONNECTED)
    override val state: StateFlow<ConnectorState> = _state.asStateFlow()

    private val _deviceDescriptor = MutableStateFlow<DeviceDescriptor?>(
        DeviceDescriptor(
            deviceId = "file_import_agent",
            manufacturer = "File Parser / Exporter",
            model = "Universal CSV/JSON Ingestion"
        )
    )
    override val deviceDescriptor: StateFlow<DeviceDescriptor?> = _deviceDescriptor.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    override val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _lastSyncRecordCount = MutableStateFlow(0)
    override val lastSyncRecordCount: StateFlow<Int> = _lastSyncRecordCount.asStateFlow()

    private val _enabledMetrics = MutableStateFlow(supportedMetrics)
    override val enabledMetrics: StateFlow<Set<StandardMetric>> = _enabledMetrics.asStateFlow()

    private val pendingImportedObservations = mutableListOf<NormalizedObservation>()

    fun parseCsv(csvText: String, userId: String = "current_user"): BatchImportSummary {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return BatchImportSummary(0, emptyList(), emptyList(), 0, listOf("Empty CSV file"))
        }

        val header = lines.first().lowercase()
        val dataLines = lines.drop(1)
        val accepted = mutableListOf<NormalizedObservation>()
        val rejected = mutableListOf<String>()
        val seenSignatures = mutableSetOf<String>()
        var duplicates = 0

        val dev = _deviceDescriptor.value ?: DeviceDescriptor("file_import", "Export", "CSV")

        for ((idx, line) in dataLines.withIndex()) {
            val tokens = line.split(",").map { it.trim() }
            if (tokens.size < 3) {
                rejected.add("Line ${idx + 2}: Insufficient columns ('$line')")
                continue
            }

            try {
                // Expected format: metric, value, unit, timestamp_epoch_or_iso
                val metricStr = tokens[0].uppercase()
                val valueStr = tokens[1]
                val unitStr = tokens[2].uppercase()
                val timestamp = if (tokens.size > 3) {
                    tokens[3].toLongOrNull() ?: System.currentTimeMillis()
                } else {
                    System.currentTimeMillis()
                }

                val metric = parseMetric(metricStr)
                if (metric == null) {
                    rejected.add("Line ${idx + 2}: Unknown metric identifier '$metricStr'")
                    continue
                }

                val rawValue = valueStr.toDoubleOrNull()
                if (rawValue == null) {
                    rejected.add("Line ${idx + 2}: Non-numeric value '$valueStr'")
                    continue
                }

                val unit = parseUnit(unitStr)

                val signature = "${metric.name}_$timestamp"
                if (seenSignatures.contains(signature)) {
                    duplicates++
                    continue
                }
                seenSignatures.add(signature)

                val obs = NormalizedObservation.createNormalized(
                    observationId = "csv_${System.currentTimeMillis()}_$idx",
                    userId = userId,
                    metricId = metric,
                    rawValue = rawValue,
                    rawUnit = unit,
                    timestamp = timestamp,
                    sourceDevice = dev,
                    sourceProvider = ProviderType.FILE_IMPORT,
                    acquisitionMethod = AcquisitionMethod.FILE_BATCH,
                    originalSourceId = "CSV_ROW_${idx + 2}",
                    provenance = "Measured",
                    qualityScore = 0.90
                )

                if (obs.validationStatus == ValidationStatus.OUT_OF_PLAUSIBLE_RANGE) {
                    rejected.add("Line ${idx + 2}: Plausibility check failed for $metric: $rawValue ${unit.symbol}")
                } else {
                    accepted.add(obs)
                }
            } catch (e: Exception) {
                rejected.add("Line ${idx + 2}: Parsing error - ${e.message}")
            }
        }

        pendingImportedObservations.addAll(accepted)
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _lastSyncRecordCount.value = accepted.size
        _state.value = ConnectorState.CONNECTED

        return BatchImportSummary(
            totalRows = dataLines.size,
            acceptedObservations = accepted,
            rejectedRows = rejected,
            duplicateCount = duplicates,
            warnings = if (rejected.isNotEmpty()) listOf("${rejected.size} rows skipped due to invalid formats or physiological boundaries.") else emptyList()
        )
    }

    fun parseJson(jsonString: String, userId: String = "current_user"): BatchImportSummary {
        val accepted = mutableListOf<NormalizedObservation>()
        val rejected = mutableListOf<String>()
        val seenSignatures = mutableSetOf<String>()
        var duplicates = 0
        val dev = _deviceDescriptor.value ?: DeviceDescriptor("file_import", "Export", "JSON")

        try {
            val objectRegex = Regex("""\{([^}]+)\}""")
            val matches = objectRegex.findAll(jsonString).toList()

            if (matches.isEmpty()) {
                return BatchImportSummary(0, emptyList(), listOf("No JSON objects found"), 0, emptyList())
            }

            for ((i, match) in matches.withIndex()) {
                val block = match.groupValues[1]

                val metricStr = Regex(""""(?:metric|metric_id)"\s*:\s*"([^"]+)"""").find(block)?.groupValues?.get(1)
                if (metricStr == null) {
                    rejected.add("Index $i: Missing metric identifier")
                    continue
                }

                val metric = parseMetric(metricStr)
                if (metric == null) {
                    rejected.add("Index $i: Unknown metric '$metricStr'")
                    continue
                }

                val rawValue = Regex(""""value"\s*:\s*([0-9.]+)""").find(block)?.groupValues?.get(1)?.toDoubleOrNull()
                if (rawValue == null) {
                    rejected.add("Index $i: Missing or non-numeric value")
                    continue
                }

                val unitStr = Regex(""""unit"\s*:\s*"([^"]+)"""").find(block)?.groupValues?.get(1) ?: ""
                val unit = parseUnit(unitStr)

                val timestamp = Regex(""""timestamp"\s*:\s*([0-9]+)""").find(block)?.groupValues?.get(1)?.toLongOrNull()
                    ?: System.currentTimeMillis()

                val signature = "${metric.name}_$timestamp"
                if (seenSignatures.contains(signature)) {
                    duplicates++
                    continue
                }
                seenSignatures.add(signature)

                val obs = NormalizedObservation.createNormalized(
                    observationId = "json_${System.currentTimeMillis()}_$i",
                    userId = userId,
                    metricId = metric,
                    rawValue = rawValue,
                    rawUnit = unit,
                    timestamp = timestamp,
                    sourceDevice = dev,
                    sourceProvider = ProviderType.FILE_IMPORT,
                    acquisitionMethod = AcquisitionMethod.FILE_BATCH,
                    originalSourceId = "JSON_INDEX_$i",
                    provenance = "Measured",
                    qualityScore = 0.92
                )

                if (obs.validationStatus == ValidationStatus.OUT_OF_PLAUSIBLE_RANGE) {
                    rejected.add("Index $i: Out of physiological range: $rawValue ${unit.symbol}")
                } else {
                    accepted.add(obs)
                }
            }
        } catch (e: Exception) {
            return BatchImportSummary(0, emptyList(), listOf("Invalid JSON syntax: ${e.message}"), 0, emptyList())
        }

        pendingImportedObservations.addAll(accepted)
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _lastSyncRecordCount.value = accepted.size
        _state.value = ConnectorState.CONNECTED

        return BatchImportSummary(
            totalRows = accepted.size + rejected.size + duplicates,
            acceptedObservations = accepted,
            rejectedRows = rejected,
            duplicateCount = duplicates,
            warnings = if (rejected.isNotEmpty()) listOf("${rejected.size} entries skipped.") else emptyList()
        )
    }

    private fun parseMetric(str: String): StandardMetric? {
        val s = str.uppercase().replace(" ", "_").replace("-", "_")
        return when {
            s.contains("HEART_RATE") || s == "HR" -> StandardMetric.HEART_RATE
            s.contains("RESTING_HR") || s.contains("RHR") -> StandardMetric.RESTING_HEART_RATE
            s.contains("GLUCOSE") || s == "BG" -> StandardMetric.BLOOD_GLUCOSE
            s.contains("STEP") -> StandardMetric.STEPS
            s.contains("WEIGHT") || s == "WT" -> StandardMetric.BODY_WEIGHT
            s.contains("SPO2") || s.contains("OXYGEN") -> StandardMetric.SPO2
            s.contains("SLEEP") -> StandardMetric.SLEEP_DURATION
            s.contains("CALORIE") || s.contains("KCAL") -> StandardMetric.ACTIVE_CALORIES
            s.contains("HRV") -> StandardMetric.HRV_RMSSD
            s.contains("RESPIRAT") || s == "RR" || s.contains("BREATH") -> StandardMetric.RESPIRATORY_RATE
            else -> try { StandardMetric.valueOf(s) } catch (e: Exception) { null }
        }
    }

    private fun parseUnit(str: String): StandardUnit {
        val s = str.uppercase().replace("/", "_").replace("°", "")
        return when {
            s.contains("BREATH") || s.contains("BR_MIN") || s.contains("BRMIN") || s.contains("RPM") -> StandardUnit.BREATHS_PER_MINUTE
            s.contains("BPM") -> StandardUnit.BPM
            s.contains("MMOL") -> StandardUnit.MMOL_L
            s.contains("MG_DL") || s.contains("MGDL") -> StandardUnit.MG_DL
            s.contains("LBS") || s.contains("POUND") -> StandardUnit.POUNDS
            s.contains("KG") || s.contains("KILO") -> StandardUnit.KILOGRAMS
            s.contains("STEP") || s.contains("COUNT") -> StandardUnit.COUNT
            s.contains("PERCENT") || s.contains("%") -> StandardUnit.PERCENTAGE
            s.contains("HOUR") || s.contains("HR") -> StandardUnit.HOURS
            s.contains("MIN") -> StandardUnit.MINUTES
            s.contains("SEC") -> StandardUnit.SECONDS
            s.contains("MS") -> StandardUnit.MILLISECONDS
            s.contains("MMHG") -> StandardUnit.MMHG
            s.contains("C") -> StandardUnit.CELSIUS
            s.contains("F") -> StandardUnit.FAHRENHEIT
            else -> StandardUnit.SCORE
        }
    }

    override suspend fun checkAvailability(context: Context): ConnectorAvailability = ConnectorAvailability.Available
    override suspend fun isAuthorized(context: Context): Boolean = true
    override suspend fun connect(context: Context): Result<Boolean> {
        _state.value = ConnectorState.CONNECTED
        return Result.success(true)
    }

    override suspend fun syncObservations(context: Context, sinceTimestamp: Long): Result<List<NormalizedObservation>> {
        val copy = pendingImportedObservations.toList()
        return Result.success(copy)
    }

    override suspend fun disconnect(context: Context): Result<Unit> {
        pendingImportedObservations.clear()
        _state.value = ConnectorState.DISCONNECTED
        _lastSyncRecordCount.value = 0
        return Result.success(Unit)
    }

    override fun setMetricEnabled(metric: StandardMetric, enabled: Boolean) {
        val current = _enabledMetrics.value.toMutableSet()
        if (enabled) current.add(metric) else current.remove(metric)
        _enabledMetrics.value = current
    }
}
