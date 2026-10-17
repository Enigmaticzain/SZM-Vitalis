package com.example.humandigitaltwin.data.models

import kotlinx.serialization.Serializable

/**
 * Standardized biometric metric identifiers supported across all device providers.
 */
@Serializable
enum class StandardMetric(val displayName: String, val category: String) {
    HEART_RATE("Heart Rate", "Cardiovascular"),
    RESTING_HEART_RATE("Resting Heart Rate", "Cardiovascular"),
    HRV_SDNN("Heart Rate Variability (SDNN)", "Autonomic / Recovery"),
    HRV_RMSSD("Heart Rate Variability (RMSSD)", "Autonomic / Recovery"),
    BLOOD_GLUCOSE("Blood Glucose", "Metabolic / Endocrine"),
    SPO2("Oxygen Saturation", "Respiratory"),
    STEPS("Step Count", "Activity"),
    SLEEP_DURATION("Sleep Duration", "Sleep & Recovery"),
    SLEEP_DEEP("Deep Sleep Duration", "Sleep & Recovery"),
    SLEEP_REM("REM Sleep Duration", "Sleep & Recovery"),
    ACTIVE_CALORIES("Active Energy Burned", "Activity"),
    BODY_WEIGHT("Body Weight", "Body Composition"),
    BODY_FAT_PERCENTAGE("Body Fat Percentage", "Body Composition"),
    BLOOD_PRESSURE_SYSTOLIC("Systolic Blood Pressure", "Cardiovascular"),
    BLOOD_PRESSURE_DIASTOLIC("Diastolic Blood Pressure", "Cardiovascular"),
    RESPIRATORY_RATE("Respiratory Rate", "Respiratory"),
    BODY_TEMPERATURE("Body Temperature", "Thermoregulation"),
    STRESS_LEVEL("Autonomic Stress Score", "Neurological")
}

/**
 * Standard units of measurement with canonical normalization rules.
 */
@Serializable
enum class StandardUnit(val symbol: String) {
    BPM("bpm"),
    MILLISECONDS("ms"),
    MG_DL("mg/dL"),
    MMOL_L("mmol/L"),
    PERCENTAGE("%"),
    COUNT("steps"),
    HOURS("hrs"),
    MINUTES("min"),
    SECONDS("sec"),
    KCAL("kcal"),
    KILOGRAMS("kg"),
    POUNDS("lbs"),
    MMHG("mmHg"),
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    BREATHS_PER_MINUTE("br/min"),
    SCORE("score")
}

/**
 * Providers / Source Categories for health telemetry.
 */
@Serializable
enum class ProviderType(val label: String) {
    HEALTH_CONNECT("Android Health Connect"),
    BLE_GATT("Bluetooth LE Device"),
    DIRECT_WEARABLE("Wearable Cloud API"),
    CGM_MONITOR("Continuous Glucose Monitor"),
    SMART_SCALE("Smart Scale"),
    FILE_IMPORT("File Import (CSV/JSON)"),
    MANUAL_ENTRY("User Manual Entry"),
    SYNTHETIC_SIMULATOR("Internal Simulator")
}

/**
 * Acquisition method for auditing observation latency and cadence.
 */
@Serializable
enum class AcquisitionMethod(val label: String) {
    CONTINUOUS_STREAMING("Continuous Real-Time"),
    PERIODIC_SYNC("Periodic Background Sync"),
    EPISODIC_SPOT_CHECK("Episodic Spot Check"),
    MANUAL_LOG("Manual Entry"),
    FILE_BATCH("Batch File Ingestion")
}

/**
 * Data validation status after clinical range checking and noise filtering.
 */
@Serializable
enum class ValidationStatus(val label: String) {
    VALID("Clinically Plausible"),
    OUT_OF_PLAUSIBLE_RANGE("Out of Physiological Range"),
    SUSPECT_NOISE("Suspect Sensor Noise"),
    CONFLICTING("Conflicting Device Reading"),
    DUPLICATE("Duplicate Ingestion")
}

/**
 * Source hardware descriptor for auditing and provenance.
 */
@Serializable
data class DeviceDescriptor(
    val deviceId: String,
    val manufacturer: String,
    val model: String,
    val hardwareVersion: String? = null,
    val firmwareVersion: String? = null,
    val batteryPercent: Int? = null,
    val signalStrengthRssi: Int? = null
)

/**
 * Universal Normalized Health Observation.
 * Every sensor, wearable, lab report, or file import normalizes into this structure.
 */
@Serializable
data class NormalizedObservation(
    val observationId: String,
    val userId: String,
    val metricId: StandardMetric,
    val metricName: String = metricId.displayName,
    val originalValue: Double,
    val originalUnit: StandardUnit,
    val normalizedValue: Double,
    val normalizedUnit: StandardUnit,
    val timestamp: Long, // Epoch millis
    val timeZoneOffsetMinutes: Int = 0,
    val sourceDevice: DeviceDescriptor,
    val sourceProvider: ProviderType,
    val acquisitionMethod: AcquisitionMethod,
    val originalSourceId: String,
    val provenance: String, // "Measured", "User-Reported", "Estimated", "Predicted", "Simulated"
    val qualityScore: Double, // 0.0 to 1.0 based on sensor fidelity and signal-to-noise
    val validationStatus: ValidationStatus = ValidationStatus.VALID,
    val uncertainty: Double? = null,
    val userConsented: Boolean = true,
    val syncedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Normalizes any raw observation into standard canonical units:
         * - Glucose: mg/dL
         * - Weight: kg
         * - Temperature: °C
         * - Duration: hours
         */
        fun createNormalized(
            observationId: String,
            userId: String,
            metricId: StandardMetric,
            rawValue: Double,
            rawUnit: StandardUnit,
            timestamp: Long,
            sourceDevice: DeviceDescriptor,
            sourceProvider: ProviderType,
            acquisitionMethod: AcquisitionMethod,
            originalSourceId: String,
            provenance: String = "Measured",
            qualityScore: Double = 1.0,
            uncertainty: Double? = null,
            userConsented: Boolean = true
        ): NormalizedObservation {
            val (normVal, normUnit) = normalizeUnitAndValue(metricId, rawValue, rawUnit)
            val validation = validatePlausibleRange(metricId, normVal, normUnit)

            return NormalizedObservation(
                observationId = observationId,
                userId = userId,
                metricId = metricId,
                metricName = metricId.displayName,
                originalValue = rawValue,
                originalUnit = rawUnit,
                normalizedValue = normVal,
                normalizedUnit = normUnit,
                timestamp = timestamp,
                sourceDevice = sourceDevice,
                sourceProvider = sourceProvider,
                acquisitionMethod = acquisitionMethod,
                originalSourceId = originalSourceId,
                provenance = provenance,
                qualityScore = if (validation == ValidationStatus.VALID) qualityScore.coerceIn(0.0, 1.0) else (qualityScore * 0.4).coerceIn(0.0, 1.0),
                validationStatus = validation,
                uncertainty = uncertainty,
                userConsented = userConsented
            )
        }

        fun normalizeUnitAndValue(metric: StandardMetric, value: Double, unit: StandardUnit): Pair<Double, StandardUnit> {
            return when (unit) {
                StandardUnit.MMOL_L -> {
                    // Convert mmol/L to mg/dL for blood glucose
                    val converted = (value * 18.0182 * 10.0).toLong() / 10.0
                    converted to StandardUnit.MG_DL
                }
                StandardUnit.POUNDS -> {
                    // Convert lbs to kg
                    val converted = (value * 0.45359237 * 100.0).toLong() / 100.0
                    converted to StandardUnit.KILOGRAMS
                }
                StandardUnit.FAHRENHEIT -> {
                    // Convert °F to °C
                    val converted = (((value - 32.0) * 5.0 / 9.0) * 10.0).toLong() / 10.0
                    converted to StandardUnit.CELSIUS
                }
                StandardUnit.MINUTES -> {
                    if (metric == StandardMetric.SLEEP_DURATION || metric == StandardMetric.SLEEP_DEEP || metric == StandardMetric.SLEEP_REM) {
                        val hours = (value / 60.0 * 100.0).toLong() / 100.0
                        hours to StandardUnit.HOURS
                    } else {
                        value to unit
                    }
                }
                StandardUnit.SECONDS -> {
                    if (metric == StandardMetric.SLEEP_DURATION || metric == StandardMetric.SLEEP_DEEP || metric == StandardMetric.SLEEP_REM) {
                        val hours = (value / 3600.0 * 100.0).toLong() / 100.0
                        hours to StandardUnit.HOURS
                    } else {
                        value to unit
                    }
                }
                else -> value to unit
            }
        }

        fun validatePlausibleRange(metric: StandardMetric, value: Double, unit: StandardUnit): ValidationStatus {
            return when (metric) {
                StandardMetric.HEART_RATE, StandardMetric.RESTING_HEART_RATE -> {
                    if (value in 28.0..240.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.HRV_SDNN, StandardMetric.HRV_RMSSD -> {
                    if (value in 4.0..300.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.BLOOD_GLUCOSE -> {
                    if (unit == StandardUnit.MG_DL) {
                        if (value in 25.0..550.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                    } else {
                        if (value in 1.4..30.5) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                    }
                }
                StandardMetric.SPO2 -> {
                    if (value in 60.0..100.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.STEPS -> {
                    if (value in 0.0..150000.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.SLEEP_DURATION -> {
                    if (value in 0.0..24.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.BODY_WEIGHT -> {
                    if (value in 20.0..350.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.BLOOD_PRESSURE_SYSTOLIC -> {
                    if (value in 60.0..260.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.BLOOD_PRESSURE_DIASTOLIC -> {
                    if (value in 35.0..160.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.BODY_TEMPERATURE -> {
                    if (value in 32.0..43.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                StandardMetric.RESPIRATORY_RATE -> {
                    if (value in 6.0..60.0) ValidationStatus.VALID else ValidationStatus.OUT_OF_PLAUSIBLE_RANGE
                }
                else -> ValidationStatus.VALID
            }
        }
    }
}

/**
 * Connector Availability status.
 */
sealed class ConnectorAvailability {
    data object Available : ConnectorAvailability()
    data class Unavailable(val reason: String) : ConnectorAvailability()
    data class ProviderUpdateRequired(val providerPackage: String) : ConnectorAvailability()
}

/**
 * Connection lifecycle states.
 */
enum class ConnectorState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    PERMISSION_REQUIRED,
    UNSUPPORTED,
    ERROR
}
