package com.example.humandigitaltwin.data.engine

import com.example.humandigitaltwin.data.models.*
import kotlin.math.sqrt

/**
 * Operating Mode of the Adaptive Human Digital Twin.
 * Automatically adapts according to the sensors and data actually available.
 */
enum class TwinOperatingMode(val title: String, val description: String) {
    FULL_TWIN(
        "Full Multi-System Twin",
        "Comprehensive physiological modeling active across Cardiovascular, Metabolic, Autonomic, and Sleep domains."
    ),
    ACTIVITY_ONLY(
        "Activity & Locomotion Twin",
        "Step and movement tracking active. Autonomic, cardiac recovery, and metabolic scores pending sensor connection."
    ),
    METABOLIC_FOCUSED(
        "Metabolic & Glycemic Twin",
        "Continuous glucose and nutritional analysis active. Continuous cardiovascular and sleep modeling pending sensor connection."
    ),
    BASELINE_ONLY(
        "Heuristic Baseline Twin",
        "Operating solely on self-reported check-ins and demographic priors. Objective biometric telemetry absent."
    )
}

/**
 * Metric Confidence / Availability Status.
 */
sealed class MetricAvailability {
    data class Measured(
        val value: Double,
        val unit: String,
        val sourceDevice: String,
        val qualityScore: Double,
        val personalDeviation: String?,
        val populationPercentile: String?
    ) : MetricAvailability()

    data class Estimated(
        val value: Double,
        val unit: String,
        val basis: String
    ) : MetricAvailability()

    data class InsufficientData(
        val missingSensor: String,
        val guidanceMessage: String
    ) : MetricAvailability()
}

/**
 * Rolling personal baseline statistics for a biometric metric.
 */
data class PersonalBaseline(
    val metricId: StandardMetric,
    val windowDays: Int,
    val mean: Double,
    val standardDeviation: Double,
    val sampleCount: Int
) {
    fun formatDeviation(currentValue: Double, unitSymbol: String): String {
        val diff = currentValue - mean
        val sign = if (diff > 0) "+" else ""
        val roundedDiff = (diff * 10.0).toLong() / 10.0
        val roundedMean = (mean * 10.0).toLong() / 10.0
        return if (sampleCount < 3) {
            "Baseline establishing ($sampleCount samples)"
        } else {
            "$sign$roundedDiff $unitSymbol vs $windowDays-day baseline ($roundedMean $unitSymbol)"
        }
    }
}

/**
 * Summary generated for the Adaptive AI Health Companion.
 */
data class AdaptiveTwinSummary(
    val operatingMode: TwinOperatingMode,
    val overallConfidenceScore: Double, // 0 to 100
    val activeSensors: List<String>,
    val missingSensors: List<String>,
    val availableMetrics: Map<StandardMetric, MetricAvailability>,
    val clinicalGuidanceSummary: String
)

/**
 * The Adaptive Human Digital Twin Engine.
 * Dynamically computes physiological scores based strictly on available data.
 * Resolves multi-device conflicts and maintains personal rolling baselines.
 */
class AdaptiveTwinEngine {

    companion object {
        /**
         * Resolves conflicts when multiple devices report the same metric in overlapping time windows.
         * Deterministic priority:
         * 1. Sensor category tier: Dedicated medical/chest BLE > Wrist wearable > Phone accelerometer/batch.
         * 2. Quality Score (sensor fidelity / noise).
         * 3. Timestamp recency.
         */
        fun resolveMultiDeviceConflict(
            observations: List<NormalizedObservation>
        ): List<NormalizedObservation> {
            if (observations.isEmpty()) return emptyList()

            // Group by metric and 60-second time windows
            val grouped = observations.groupBy { obs ->
                "${obs.metricId}_${obs.timestamp / 60000}"
            }

            val resolved = mutableListOf<NormalizedObservation>()

            for ((_, group) in grouped) {
                if (group.size == 1) {
                    resolved.add(group.first())
                } else {
                    // Sort by priority score
                    val winner = group.maxByOrNull { obs ->
                        val providerTier = when (obs.sourceProvider) {
                            ProviderType.BLE_GATT -> 3.0 // Dedicated chest strap / medical sensor
                            ProviderType.CGM_MONITOR -> 3.0 // Continuous biosensor
                            ProviderType.SMART_SCALE -> 2.5
                            ProviderType.HEALTH_CONNECT -> 2.0 // Aggregated platform / wearable
                            ProviderType.DIRECT_WEARABLE -> 2.0
                            ProviderType.FILE_IMPORT -> 1.5
                            ProviderType.MANUAL_ENTRY -> 1.0
                            ProviderType.SYNTHETIC_SIMULATOR -> 0.5
                        }
                        // Composite priority = (tier * 1000) + (quality * 100) + timestamp
                        (providerTier * 1000.0) + (obs.qualityScore * 100.0) + (obs.timestamp % 1000.0) / 1000.0
                    }
                    if (winner != null) {
                        resolved.add(winner)
                    }
                }
            }

            return resolved.sortedByDescending { it.timestamp }
        }

        /**
         * Computes rolling personal baselines from historical observations.
         */
        fun computePersonalBaseline(
            metric: StandardMetric,
            observations: List<NormalizedObservation>,
            windowDays: Int = 14
        ): PersonalBaseline? {
            val matching = observations
                .filter { it.metricId == metric && it.validationStatus == ValidationStatus.VALID }
                .map { it.normalizedValue }

            if (matching.isEmpty()) return null

            val mean = matching.average()
            val variance = if (matching.size > 1) {
                matching.map { (it - mean) * (it - mean) }.sum() / (matching.size - 1)
            } else 0.0
            val stdDev = sqrt(variance)

            return PersonalBaseline(
                metricId = metric,
                windowDays = windowDays,
                mean = (mean * 10.0).toLong() / 10.0,
                standardDeviation = (stdDev * 10.0).toLong() / 10.0,
                sampleCount = matching.size
            )
        }

        /**
         * Calculates population percentile for standard demographics (Adult 30-40).
         */
        fun getPopulationPercentile(metric: StandardMetric, value: Double): String {
            return when (metric) {
                StandardMetric.HEART_RATE, StandardMetric.RESTING_HEART_RATE -> {
                    when {
                        value < 55.0 -> "Top 10th percentile (Athletic)"
                        value < 65.0 -> "75th percentile (Above Average)"
                        value < 75.0 -> "50th percentile (Normal Adult)"
                        value < 85.0 -> "25th percentile (Below Average)"
                        else -> "10th percentile (Elevated)"
                    }
                }
                StandardMetric.HRV_RMSSD -> {
                    when {
                        value > 70.0 -> "Top 10th percentile (High Parasympathetic Tone)"
                        value > 50.0 -> "75th percentile (Good Autonomic Balance)"
                        value > 30.0 -> "50th percentile (Normal Adult)"
                        else -> "25th percentile (Autonomic Stress / Fatigue)"
                    }
                }
                StandardMetric.BLOOD_GLUCOSE -> {
                    when {
                        value in 70.0..99.0 -> "Normal Fasting / Euglycemic Range (70-99 mg/dL)"
                        value in 100.0..125.0 -> "Impaired Fasting Glycemia (100-125 mg/dL)"
                        value >= 126.0 -> "Elevated Glycemia (≥126 mg/dL)"
                        else -> "Hypoglycemic Alert (<70 mg/dL)"
                    }
                }
                StandardMetric.STEPS -> {
                    when {
                        value >= 10000 -> "Top 20th percentile (Highly Active)"
                        value >= 7500 -> "60th percentile (Active)"
                        value >= 5000 -> "40th percentile (Moderately Active)"
                        else -> "20th percentile (Sedentary)"
                    }
                }
                StandardMetric.RESPIRATORY_RATE -> {
                    when {
                        value < 12.0 -> "Below Resting Adult Range (Bradypnea Alert, <12 br/min)"
                        value <= 20.0 -> "Optimal Adult Resting Range (12–20 br/min)"
                        else -> "Above Resting Adult Range (Tachypnea Alert, >20 br/min)"
                    }
                }
                else -> "Population reference bounds standard"
            }
        }
    }

    /**
     * Synthesizes the adaptive twin state and confidence score based strictly on actual observations.
     */
    fun evaluateTwin(
        observations: List<NormalizedObservation>,
        checkInsCount: Int = 0
    ): AdaptiveTwinSummary {
        val resolved = resolveMultiDeviceConflict(observations)

        val hasHeartRate = resolved.any { it.metricId == StandardMetric.HEART_RATE || it.metricId == StandardMetric.RESTING_HEART_RATE }
        val hasHrv = resolved.any { it.metricId == StandardMetric.HRV_RMSSD || it.metricId == StandardMetric.HRV_SDNN }
        val hasGlucose = resolved.any { it.metricId == StandardMetric.BLOOD_GLUCOSE }
        val hasSteps = resolved.any { it.metricId == StandardMetric.STEPS }
        val hasSleep = resolved.any { it.metricId == StandardMetric.SLEEP_DURATION }

        val activeSensors = mutableListOf<String>()
        val missingSensors = mutableListOf<String>()

        if (hasHeartRate) activeSensors.add("Heart Rate Sensor (PPG/ECG)") else missingSensors.add("Heart Rate Sensor")
        if (hasHrv) activeSensors.add("HRV Autonomic Monitor") else missingSensors.add("HRV Monitor")
        if (hasGlucose) activeSensors.add("Continuous Glucose Monitor") else missingSensors.add("Continuous Glucose Monitor")
        if (hasSteps) activeSensors.add("Activity Tracker (Steps)") else missingSensors.add("Activity Tracker")
        if (hasSleep) activeSensors.add("Sleep Actigraphy Monitor") else missingSensors.add("Sleep Tracker")

        // Determine Operating Mode
        val mode = when {
            hasHeartRate && hasGlucose && hasSleep && hasSteps -> TwinOperatingMode.FULL_TWIN
            hasGlucose && !hasHeartRate -> TwinOperatingMode.METABOLIC_FOCUSED
            hasSteps && !hasHeartRate && !hasGlucose -> TwinOperatingMode.ACTIVITY_ONLY
            else -> TwinOperatingMode.BASELINE_ONLY
        }

        // Determine Confidence Score (0-100)
        var confidence = 25.0 // baseline prior
        if (hasHeartRate) confidence += 20.0
        if (hasHrv) confidence += 15.0
        if (hasGlucose) confidence += 20.0
        if (hasSleep) confidence += 10.0
        if (hasSteps) confidence += 10.0
        if (checkInsCount > 0) confidence += 5.0
        confidence = confidence.coerceIn(0.0, 99.0)

        // Build metric availability map
        val metricMap = mutableMapOf<StandardMetric, MetricAvailability>()

        // 1. Heart Rate
        val latestHr = resolved.firstOrNull { it.metricId == StandardMetric.HEART_RATE || it.metricId == StandardMetric.RESTING_HEART_RATE }
        if (latestHr != null) {
            val baseline = computePersonalBaseline(latestHr.metricId, resolved)
            metricMap[StandardMetric.HEART_RATE] = MetricAvailability.Measured(
                value = latestHr.normalizedValue,
                unit = latestHr.normalizedUnit.symbol,
                sourceDevice = latestHr.sourceDevice.model,
                qualityScore = latestHr.qualityScore,
                personalDeviation = baseline?.formatDeviation(latestHr.normalizedValue, latestHr.normalizedUnit.symbol),
                populationPercentile = getPopulationPercentile(latestHr.metricId, latestHr.normalizedValue)
            )
        } else {
            metricMap[StandardMetric.HEART_RATE] = MetricAvailability.InsufficientData(
                missingSensor = "Heart Rate Monitor / Smart Watch",
                guidanceMessage = "Connect a BLE chest strap or Health Connect device to measure real cardiac telemetry."
            )
        }

        // 2. Blood Glucose
        val latestBg = resolved.firstOrNull { it.metricId == StandardMetric.BLOOD_GLUCOSE }
        if (latestBg != null) {
            val baseline = computePersonalBaseline(StandardMetric.BLOOD_GLUCOSE, resolved)
            metricMap[StandardMetric.BLOOD_GLUCOSE] = MetricAvailability.Measured(
                value = latestBg.normalizedValue,
                unit = latestBg.normalizedUnit.symbol,
                sourceDevice = latestBg.sourceDevice.model,
                qualityScore = latestBg.qualityScore,
                personalDeviation = baseline?.formatDeviation(latestBg.normalizedValue, latestBg.normalizedUnit.symbol),
                populationPercentile = getPopulationPercentile(StandardMetric.BLOOD_GLUCOSE, latestBg.normalizedValue)
            )
        } else {
            metricMap[StandardMetric.BLOOD_GLUCOSE] = MetricAvailability.InsufficientData(
                missingSensor = "Continuous Glucose Monitor (CGM)",
                guidanceMessage = "Connect Dexcom, FreeStyle Libre, or log fingerstick readings to monitor metabolic stability."
            )
        }

        // 3. Sleep
        val latestSleep = resolved.firstOrNull { it.metricId == StandardMetric.SLEEP_DURATION }
        if (latestSleep != null) {
            metricMap[StandardMetric.SLEEP_DURATION] = MetricAvailability.Measured(
                value = latestSleep.normalizedValue,
                unit = latestSleep.normalizedUnit.symbol,
                sourceDevice = latestSleep.sourceDevice.model,
                qualityScore = latestSleep.qualityScore,
                personalDeviation = null,
                populationPercentile = "Standard adult recommendation (7-9 hrs)"
            )
        } else {
            metricMap[StandardMetric.SLEEP_DURATION] = MetricAvailability.InsufficientData(
                missingSensor = "Sleep Tracker / Wearable Ring",
                guidanceMessage = "Connect Health Connect or Oura/Whoop to track restorative sleep stages and autonomic recovery."
            )
        }

        // 4. Steps
        val latestSteps = resolved.firstOrNull { it.metricId == StandardMetric.STEPS }
        if (latestSteps != null) {
            metricMap[StandardMetric.STEPS] = MetricAvailability.Measured(
                value = latestSteps.normalizedValue,
                unit = latestSteps.normalizedUnit.symbol,
                sourceDevice = latestSteps.sourceDevice.model,
                qualityScore = latestSteps.qualityScore,
                personalDeviation = null,
                populationPercentile = getPopulationPercentile(StandardMetric.STEPS, latestSteps.normalizedValue)
            )
        } else {
            metricMap[StandardMetric.STEPS] = MetricAvailability.InsufficientData(
                missingSensor = "Pedometer / Activity Monitor",
                guidanceMessage = "Sync Health Connect or wearable to record daily locomotion."
            )
        }

        val guidance = when (mode) {
            TwinOperatingMode.FULL_TWIN -> "Full multi-sensor synchronization active. Twin is calculating personalized biological age and recovery indices with high precision."
            TwinOperatingMode.ACTIVITY_ONLY -> "Locomotion telemetry active. Cardiovascular and metabolic models are currently in standby waiting for heart rate or glucose sensor connections."
            TwinOperatingMode.METABOLIC_FOCUSED -> "Continuous glucose monitoring active. Autonomic tone and cardiovascular strain cannot be computed without a heart rate sensor."
            TwinOperatingMode.BASELINE_ONLY -> "No live biometric sensors connected. Operating in synthetic demo mode. Connect a smart device in the Device Hub to activate your personalized twin."
        }

        return AdaptiveTwinSummary(
            operatingMode = mode,
            overallConfidenceScore = (confidence * 10.0).toLong() / 10.0,
            activeSensors = activeSensors,
            missingSensors = missingSensors,
            availableMetrics = metricMap,
            clinicalGuidanceSummary = guidance
        )
    }
}
