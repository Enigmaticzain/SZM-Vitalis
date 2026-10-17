package com.example.humandigitaltwin

import com.example.humandigitaltwin.data.connectors.BleGattConnector
import com.example.humandigitaltwin.data.connectors.FileImportConnector
import com.example.humandigitaltwin.data.engine.AdaptiveTwinEngine
import com.example.humandigitaltwin.data.engine.MetricAvailability
import com.example.humandigitaltwin.data.engine.TwinOperatingMode
import com.example.humandigitaltwin.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 6 Verification Test Suite:
 * Universal Health Data Normalization, Adaptive Twin Engine, and Connector Framework.
 */
class HealthDataNormalizationTest {

    private val testDevice = DeviceDescriptor(
        deviceId = "TEST-DEV-001",
        manufacturer = "Test Biosystems",
        model = "Model X1"
    )

    // --- 1. UNIT CONVERSIONS & CANONICAL NORMALIZATION ---

    @Test
    fun testGlucoseUnitConversion_mmolL_to_mgdL() {
        // 5.5 mmol/L * 18.0182 = ~99.1 mg/dL
        val (normVal, normUnit) = NormalizedObservation.normalizeUnitAndValue(
            StandardMetric.BLOOD_GLUCOSE,
            5.5,
            StandardUnit.MMOL_L
        )
        assertEquals(StandardUnit.MG_DL, normUnit)
        assertEquals(99.1, normVal, 0.2)
    }

    @Test
    fun testWeightUnitConversion_lbs_to_kg() {
        // 165 lbs * 0.45359237 = ~74.84 kg
        val (normVal, normUnit) = NormalizedObservation.normalizeUnitAndValue(
            StandardMetric.BODY_WEIGHT,
            165.0,
            StandardUnit.POUNDS
        )
        assertEquals(StandardUnit.KILOGRAMS, normUnit)
        assertEquals(74.84, normVal, 0.1)
    }

    @Test
    fun testTemperatureConversion_F_to_C() {
        // 98.6°F -> 37.0°C
        val (normVal, normUnit) = NormalizedObservation.normalizeUnitAndValue(
            StandardMetric.BODY_TEMPERATURE,
            98.6,
            StandardUnit.FAHRENHEIT
        )
        assertEquals(StandardUnit.CELSIUS, normUnit)
        assertEquals(37.0, normVal, 0.1)
    }

    @Test
    fun testSleepDurationConversion_minutes_to_hours() {
        // 450 minutes -> 7.5 hours
        val (normVal, normUnit) = NormalizedObservation.normalizeUnitAndValue(
            StandardMetric.SLEEP_DURATION,
            450.0,
            StandardUnit.MINUTES
        )
        assertEquals(StandardUnit.HOURS, normUnit)
        assertEquals(7.5, normVal, 0.01)
    }

    // --- 2. PHYSIOLOGICAL PLAUSIBILITY VALIDATION ---

    @Test
    fun testValidPhysiologicalBoundsAccepted() {
        val obs = NormalizedObservation.createNormalized(
            observationId = "obs-1",
            userId = "user-1",
            metricId = StandardMetric.HEART_RATE,
            rawValue = 72.0,
            rawUnit = StandardUnit.BPM,
            timestamp = 1000L,
            sourceDevice = testDevice,
            sourceProvider = ProviderType.BLE_GATT,
            acquisitionMethod = AcquisitionMethod.CONTINUOUS_STREAMING,
            originalSourceId = "test"
        )
        assertEquals(ValidationStatus.VALID, obs.validationStatus)
        assertEquals(1.0, obs.qualityScore, 0.01)
    }

    @Test
    fun testExtremeUnphysiologicalValuesFlaggedAndPenalized() {
        // Heart rate 350 bpm is physiologically impossible for sustained human vitals
        val extremeHr = NormalizedObservation.createNormalized(
            observationId = "obs-extreme",
            userId = "user-1",
            metricId = StandardMetric.HEART_RATE,
            rawValue = 350.0,
            rawUnit = StandardUnit.BPM,
            timestamp = 1000L,
            sourceDevice = testDevice,
            sourceProvider = ProviderType.BLE_GATT,
            acquisitionMethod = AcquisitionMethod.CONTINUOUS_STREAMING,
            originalSourceId = "test"
        )
        assertEquals(ValidationStatus.OUT_OF_PLAUSIBLE_RANGE, extremeHr.validationStatus)
        assertTrue(extremeHr.qualityScore < 0.5)

        // Negative step count
        val negativeSteps = NormalizedObservation.createNormalized(
            observationId = "obs-steps",
            userId = "user-1",
            metricId = StandardMetric.STEPS,
            rawValue = -500.0,
            rawUnit = StandardUnit.COUNT,
            timestamp = 1000L,
            sourceDevice = testDevice,
            sourceProvider = ProviderType.HEALTH_CONNECT,
            acquisitionMethod = AcquisitionMethod.PERIODIC_SYNC,
            originalSourceId = "test"
        )
        assertEquals(ValidationStatus.OUT_OF_PLAUSIBLE_RANGE, negativeSteps.validationStatus)
    }

    // --- 3. MULTI-DEVICE CONFLICT RESOLUTION ---

    @Test
    fun testMultiDeviceConflictPrioritizesDedicatedMedicalSensorOverPhone() {
        val timestamp = 1773500000000L

        // Provider A: Phone accelerometer / file import (75 bpm, quality 0.8)
        val phoneReading = NormalizedObservation.createNormalized(
            observationId = "phone_hr",
            userId = "user-1",
            metricId = StandardMetric.HEART_RATE,
            rawValue = 75.0,
            rawUnit = StandardUnit.BPM,
            timestamp = timestamp,
            sourceDevice = DeviceDescriptor("phone", "OEM", "Smartphone"),
            sourceProvider = ProviderType.FILE_IMPORT,
            acquisitionMethod = AcquisitionMethod.FILE_BATCH,
            originalSourceId = "phone_accel",
            qualityScore = 0.80
        )

        // Provider B: Dedicated BLE Chest Strap (68 bpm, quality 0.99)
        val chestStrapReading = NormalizedObservation.createNormalized(
            observationId = "polar_hr",
            userId = "user-1",
            metricId = StandardMetric.HEART_RATE,
            rawValue = 68.0,
            rawUnit = StandardUnit.BPM,
            timestamp = timestamp + 5000L, // 5 seconds later in same 60s window
            sourceDevice = DeviceDescriptor("polar_h10", "Polar", "H10 Chest Strap"),
            sourceProvider = ProviderType.BLE_GATT,
            acquisitionMethod = AcquisitionMethod.CONTINUOUS_STREAMING,
            originalSourceId = "GATT_2A37",
            qualityScore = 0.99
        )

        val resolved = AdaptiveTwinEngine.resolveMultiDeviceConflict(listOf(phoneReading, chestStrapReading))

        // Only 1 reading retained for this window, and it MUST be the Polar chest strap
        assertEquals(1, resolved.size)
        assertEquals("polar_hr", resolved.first().observationId)
        assertEquals(68.0, resolved.first().normalizedValue, 0.01)
    }

    // --- 4. BLE GATT BINARY PACKET PARSER ---

    @Test
    fun testBleGattHeartRatePacketParsing_8bit_and_16bit() {
        // 8-bit HR (65 bpm, no RR interval)
        val packet8bit = byteArrayOf(0x00.toByte(), 65.toByte())
        val (hr1, rr1) = BleGattConnector.parseHeartRateMeasurementPacket(packet8bit)
        assertEquals(65.0, hr1, 0.01)
        assertNull(rr1)

        // 16-bit HR (125 bpm = 0x007D, Flag bit 0 = 1, Flag bit 4 = 1 for RR interval)
        // Flag: 0x11 (17) -> 16-bit HR + RR present
        // HR: 0x7D, 0x00 = 125
        // RR: 0x00, 0x04 = 1024 (1024/1024 s = 1000 ms)
        val packet16bit = byteArrayOf(0x11.toByte(), 0x7D.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte())
        val (hr2, rr2) = BleGattConnector.parseHeartRateMeasurementPacket(packet16bit)
        assertEquals(125.0, hr2, 0.01)
        assertNotNull(rr2)
        assertEquals(1000.0, rr2!!, 1.0)
    }

    // --- 5. ADAPTIVE TWIN ENGINE & MISSING SENSOR HANDLING ---

    @Test
    fun testBaselineTwinWhenNoSensorsConnected() {
        val engine = AdaptiveTwinEngine()
        val summary = engine.evaluateTwin(emptyList(), checkInsCount = 1)

        assertEquals(TwinOperatingMode.BASELINE_ONLY, summary.operatingMode)
        assertTrue("Confidence must be low when no sensors connected", summary.overallConfidenceScore <= 35.0)
        assertEquals(5, summary.missingSensors.size)

        // Vitals must report InsufficientData, NEVER invented high scores
        val hrStatus = summary.availableMetrics[StandardMetric.HEART_RATE]
        assertTrue(hrStatus is MetricAvailability.InsufficientData)
        val bgStatus = summary.availableMetrics[StandardMetric.BLOOD_GLUCOSE]
        assertTrue(bgStatus is MetricAvailability.InsufficientData)
    }

    @Test
    fun testActivityOnlyTwinWhenOnlyStepsPresent() {
        val engine = AdaptiveTwinEngine()
        val stepObs = NormalizedObservation.createNormalized(
            observationId = "step_1",
            userId = "user-1",
            metricId = StandardMetric.STEPS,
            rawValue = 8500.0,
            rawUnit = StandardUnit.COUNT,
            timestamp = System.currentTimeMillis(),
            sourceDevice = testDevice,
            sourceProvider = ProviderType.HEALTH_CONNECT,
            acquisitionMethod = AcquisitionMethod.PERIODIC_SYNC,
            originalSourceId = "step_source"
        )

        val summary = engine.evaluateTwin(listOf(stepObs))
        assertEquals(TwinOperatingMode.ACTIVITY_ONLY, summary.operatingMode)
        assertTrue(summary.availableMetrics[StandardMetric.STEPS] is MetricAvailability.Measured)
        assertTrue(summary.availableMetrics[StandardMetric.HEART_RATE] is MetricAvailability.InsufficientData)
    }

    @Test
    fun testFullMultiSystemTwinWhenAllSignalsConnected() {
        val engine = AdaptiveTwinEngine()
        val now = System.currentTimeMillis()

        val observations = listOf(
            NormalizedObservation.createNormalized("hr", "u1", StandardMetric.HEART_RATE, 64.0, StandardUnit.BPM, now, testDevice, ProviderType.BLE_GATT, AcquisitionMethod.CONTINUOUS_STREAMING, "1"),
            NormalizedObservation.createNormalized("hrv", "u1", StandardMetric.HRV_RMSSD, 78.0, StandardUnit.MILLISECONDS, now, testDevice, ProviderType.BLE_GATT, AcquisitionMethod.CONTINUOUS_STREAMING, "2"),
            NormalizedObservation.createNormalized("bg", "u1", StandardMetric.BLOOD_GLUCOSE, 92.0, StandardUnit.MG_DL, now, testDevice, ProviderType.CGM_MONITOR, AcquisitionMethod.CONTINUOUS_STREAMING, "3"),
            NormalizedObservation.createNormalized("steps", "u1", StandardMetric.STEPS, 10450.0, StandardUnit.COUNT, now, testDevice, ProviderType.HEALTH_CONNECT, AcquisitionMethod.PERIODIC_SYNC, "4"),
            NormalizedObservation.createNormalized("sleep", "u1", StandardMetric.SLEEP_DURATION, 8.1, StandardUnit.HOURS, now, testDevice, ProviderType.HEALTH_CONNECT, AcquisitionMethod.PERIODIC_SYNC, "5")
        )

        val summary = engine.evaluateTwin(observations)
        assertEquals(TwinOperatingMode.FULL_TWIN, summary.operatingMode)
        assertTrue("Confidence must be high with full telemetry", summary.overallConfidenceScore >= 85.0)
        assertEquals(0, summary.missingSensors.size)
    }

    // --- 6. ROLLING PERSONAL BASELINE VS POPULATION PERCENTILE ---

    @Test
    fun testPersonalBaselineCalculationAndDeviation() {
        val rhrValues = listOf(60.0, 62.0, 58.0, 64.0, 61.0, 59.0, 60.0) // Mean = 60.57
        val observations = rhrValues.mapIndexed { idx, v ->
            NormalizedObservation.createNormalized(
                observationId = "rhr_$idx",
                userId = "u1",
                metricId = StandardMetric.RESTING_HEART_RATE,
                rawValue = v,
                rawUnit = StandardUnit.BPM,
                timestamp = System.currentTimeMillis() - (idx * 86400000L),
                sourceDevice = testDevice,
                sourceProvider = ProviderType.BLE_GATT,
                acquisitionMethod = AcquisitionMethod.CONTINUOUS_STREAMING,
                originalSourceId = "src"
            )
        }

        val baseline = AdaptiveTwinEngine.computePersonalBaseline(StandardMetric.RESTING_HEART_RATE, observations, 14)
        assertNotNull(baseline)
        assertEquals(60.5, baseline!!.mean, 0.2)
        assertTrue(baseline.sampleCount == 7)

        val deviation = baseline.formatDeviation(65.0, "bpm")
        assertTrue(deviation.contains("+4.4 bpm vs 14-day baseline") || deviation.contains("+4.5 bpm vs 14-day baseline"))

        val percentile = AdaptiveTwinEngine.getPopulationPercentile(StandardMetric.RESTING_HEART_RATE, 60.0)
        assertTrue(percentile.contains("75th percentile") || percentile.contains("Above Average"))
    }

    // --- 7. FILE INGESTION (CSV & JSON) ---

    @Test
    fun testCsvFileImportIngestionAndValidation() {
        val connector = FileImportConnector()
        val csv = """
metric,value,unit,timestamp
HEART_RATE,68,bpm,1773500000000
BLOOD_GLUCOSE,5.4,mmol/L,1773500000000
BODY_WEIGHT,165,lbs,1773500000000
INVALID_METRIC,999,bpm,1773500000000
HEART_RATE,500,bpm,1773500060000
        """.trimIndent()

        val summary = connector.parseCsv(csv)
        assertEquals(5, summary.totalRows)
        // 3 valid rows accepted: HR 68, Glucose 5.4 mmol/L (converted to mg/dL), Weight 165 lbs (converted to kg)
        // 2 rejected rows: INVALID_METRIC and HR 500 (out of range)
        assertEquals(3, summary.acceptedObservations.size)
        assertEquals(2, summary.rejectedRows.size)

        // Verify glucose was normalized from mmol/L to mg/dL
        val glucoseObs = summary.acceptedObservations.first { it.metricId == StandardMetric.BLOOD_GLUCOSE }
        assertEquals(StandardUnit.MG_DL, glucoseObs.normalizedUnit)
        assertEquals(97.2, glucoseObs.normalizedValue, 0.5)

        // Verify weight was normalized from lbs to kg
        val weightObs = summary.acceptedObservations.first { it.metricId == StandardMetric.BODY_WEIGHT }
        assertEquals(StandardUnit.KILOGRAMS, weightObs.normalizedUnit)
        assertEquals(74.84, weightObs.normalizedValue, 0.2)
    }

    @Test
    fun testJsonFileImportIngestion() {
        val connector = FileImportConnector()
        val json = """
[
  {"metric": "STEPS", "value": 7500, "unit": "steps"},
  {"metric": "BLOOD_GLUCOSE", "value": 95.0, "unit": "mg/dL"}
]
        """.trimIndent()

        val summary = connector.parseJson(json)
        assertEquals(2, summary.acceptedObservations.size)
        assertEquals(0, summary.rejectedRows.size)
    }

    // --- 8. RESPIRATORY RATE VALIDATION & PERCENTILES ---

    @Test
    fun testRespiratoryRatePlausibleRangeValidation() {
        val validRR = NormalizedObservation.validatePlausibleRange(
            StandardMetric.RESPIRATORY_RATE,
            16.0,
            StandardUnit.BREATHS_PER_MINUTE
        )
        assertEquals(ValidationStatus.VALID, validRR)

        val lowOutlier = NormalizedObservation.validatePlausibleRange(
            StandardMetric.RESPIRATORY_RATE,
            3.0,
            StandardUnit.BREATHS_PER_MINUTE
        )
        assertEquals(ValidationStatus.OUT_OF_PLAUSIBLE_RANGE, lowOutlier)

        val highOutlier = NormalizedObservation.validatePlausibleRange(
            StandardMetric.RESPIRATORY_RATE,
            95.0,
            StandardUnit.BREATHS_PER_MINUTE
        )
        assertEquals(ValidationStatus.OUT_OF_PLAUSIBLE_RANGE, highOutlier)
    }

    @Test
    fun testRespiratoryRatePopulationPercentile() {
        val optimal = AdaptiveTwinEngine.getPopulationPercentile(StandardMetric.RESPIRATORY_RATE, 16.0)
        assertTrue(optimal.contains("Optimal Adult Resting Range") || optimal.contains("12–20"))

        val bradypnea = AdaptiveTwinEngine.getPopulationPercentile(StandardMetric.RESPIRATORY_RATE, 10.0)
        assertTrue(bradypnea.contains("Bradypnea Alert") || bradypnea.contains("<12"))

        val tachypnea = AdaptiveTwinEngine.getPopulationPercentile(StandardMetric.RESPIRATORY_RATE, 25.0)
        assertTrue(tachypnea.contains("Tachypnea Alert") || tachypnea.contains(">20"))
    }

    @Test
    fun testRespiratoryRateFileIngestionCsvAndJson() {
        val connector = FileImportConnector()
        val csv = """
metric,value,unit,timestamp
RR,15.5,br/min,1773000000000
RESPIRATORY_RATE,17.0,breaths/min,1773000060000
        """.trimIndent()

        val summary = connector.parseCsv(csv)
        assertEquals(2, summary.acceptedObservations.size)
        assertEquals(StandardMetric.RESPIRATORY_RATE, summary.acceptedObservations[0].metricId)
        assertEquals(15.5, summary.acceptedObservations[0].normalizedValue, 0.1)
    }
}
