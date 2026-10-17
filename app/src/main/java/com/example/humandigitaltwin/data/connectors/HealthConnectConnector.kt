package com.example.humandigitaltwin.data.connectors

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.humandigitaltwin.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

/**
 * Android Health Connect Platform Connector.
 * Ingests multi-sensor observations from Google Health Connect framework (Android 14)
 * and Health Connect APK (Android 8-13).
 */
class HealthConnectConnector : DeviceConnector {

    override val connectorId: String = "android_health_connect"
    override val name: String = "Android Health Connect"
    override val description: String = "Universal hub for Google Fit, Samsung Health, Whoop, Oura, and Pixel Watch"
    override val providerType: ProviderType = ProviderType.HEALTH_CONNECT

    override val supportedMetrics: Set<StandardMetric> = setOf(
        StandardMetric.HEART_RATE,
        StandardMetric.STEPS,
        StandardMetric.SLEEP_DURATION,
        StandardMetric.BLOOD_GLUCOSE,
        StandardMetric.SPO2,
        StandardMetric.ACTIVE_CALORIES,
        StandardMetric.BODY_WEIGHT,
        StandardMetric.BLOOD_PRESSURE_SYSTOLIC,
        StandardMetric.BLOOD_PRESSURE_DIASTOLIC
    )

    private val _state = MutableStateFlow(ConnectorState.DISCONNECTED)
    override val state: StateFlow<ConnectorState> = _state.asStateFlow()

    private val _deviceDescriptor = MutableStateFlow<DeviceDescriptor?>(
        DeviceDescriptor(
            deviceId = "health_connect_system",
            manufacturer = "Google / Android OS",
            model = "Health Connect Platform API"
        )
    )
    override val deviceDescriptor: StateFlow<DeviceDescriptor?> = _deviceDescriptor.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    override val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _lastSyncRecordCount = MutableStateFlow(0)
    override val lastSyncRecordCount: StateFlow<Int> = _lastSyncRecordCount.asStateFlow()

    private val _enabledMetrics = MutableStateFlow(supportedMetrics)
    override val enabledMetrics: StateFlow<Set<StandardMetric>> = _enabledMetrics.asStateFlow()

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class)
    )

    override suspend fun checkAvailability(context: Context): ConnectorAvailability {
        return try {
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    ConnectorAvailability.Available
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    _state.value = ConnectorState.UNSUPPORTED
                    ConnectorAvailability.ProviderUpdateRequired("com.google.android.apps.healthdata")
                }
                else -> {
                    _state.value = ConnectorState.UNSUPPORTED
                    ConnectorAvailability.Unavailable("Health Connect is not supported or not installed on this Android environment.")
                }
            }
        } catch (e: Exception) {
            _state.value = ConnectorState.UNSUPPORTED
            ConnectorAvailability.Unavailable(e.message ?: "Health Connect query failed")
        }
    }

    override suspend fun isAuthorized(context: Context): Boolean {
        return try {
            if (checkAvailability(context) !is ConnectorAvailability.Available) return false
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            permissions.any { it in granted }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun connect(context: Context): Result<Boolean> {
        val avail = checkAvailability(context)
        if (avail !is ConnectorAvailability.Available) {
            _state.value = ConnectorState.UNSUPPORTED
            return Result.failure(IllegalStateException("Health Connect is unavailable on this device."))
        }
        return try {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            val hasAny = permissions.any { it in granted }
            if (hasAny) {
                _state.value = ConnectorState.CONNECTED
                Result.success(true)
            } else {
                _state.value = ConnectorState.PERMISSION_REQUIRED
                Result.success(false)
            }
        } catch (e: Exception) {
            _state.value = ConnectorState.ERROR
            Result.failure(e)
        }
    }

    override suspend fun syncObservations(context: Context, sinceTimestamp: Long): Result<List<NormalizedObservation>> {
        val avail = checkAvailability(context)
        if (avail !is ConnectorAvailability.Available) {
            return Result.failure(IllegalStateException("Health Connect is not available."))
        }

        _state.value = ConnectorState.SYNCING
        val results = mutableListOf<NormalizedObservation>()
        val startTime = if (sinceTimestamp > 0) Instant.ofEpochMilli(sinceTimestamp) else Instant.now().minusSeconds(86400 * 7)
        val timeFilter = TimeRangeFilter.after(startTime)

        return try {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            val dev = _deviceDescriptor.value ?: DeviceDescriptor("health_connect", "Android", "Health Connect")

            // 1. Steps
            if (StandardMetric.STEPS in _enabledMetrics.value && HealthPermission.getReadPermission(StepsRecord::class) in granted) {
                val stepRecords = client.readRecords(ReadRecordsRequest(StepsRecord::class, timeRangeFilter = timeFilter)).records
                for (rec in stepRecords) {
                    results.add(
                        NormalizedObservation.createNormalized(
                            observationId = "hc_step_${rec.metadata.id}",
                            userId = "current_user",
                            metricId = StandardMetric.STEPS,
                            rawValue = rec.count.toDouble(),
                            rawUnit = StandardUnit.COUNT,
                            timestamp = rec.startTime.toEpochMilli(),
                            sourceDevice = dev,
                            sourceProvider = ProviderType.HEALTH_CONNECT,
                            acquisitionMethod = AcquisitionMethod.PERIODIC_SYNC,
                            originalSourceId = rec.metadata.id,
                            provenance = "Measured",
                            qualityScore = 0.95
                        )
                    )
                }
            }

            // 2. Heart Rate
            if (StandardMetric.HEART_RATE in _enabledMetrics.value && HealthPermission.getReadPermission(HeartRateRecord::class) in granted) {
                val hrRecords = client.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter = timeFilter)).records
                for (rec in hrRecords) {
                    for (sample in rec.samples) {
                        results.add(
                            NormalizedObservation.createNormalized(
                                observationId = "hc_hr_${rec.metadata.id}_${sample.time.toEpochMilli()}",
                                userId = "current_user",
                                metricId = StandardMetric.HEART_RATE,
                                rawValue = sample.beatsPerMinute.toDouble(),
                                rawUnit = StandardUnit.BPM,
                                timestamp = sample.time.toEpochMilli(),
                                sourceDevice = dev,
                                sourceProvider = ProviderType.HEALTH_CONNECT,
                                acquisitionMethod = AcquisitionMethod.CONTINUOUS_STREAMING,
                                originalSourceId = rec.metadata.id,
                                provenance = "Measured",
                                qualityScore = 0.98
                            )
                        )
                    }
                }
            }

            // 3. Sleep Session
            if (StandardMetric.SLEEP_DURATION in _enabledMetrics.value && HealthPermission.getReadPermission(SleepSessionRecord::class) in granted) {
                val sleepRecords = client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter = timeFilter)).records
                for (rec in sleepRecords) {
                    val durationHours = java.time.Duration.between(rec.startTime, rec.endTime).toMinutes() / 60.0
                    results.add(
                        NormalizedObservation.createNormalized(
                            observationId = "hc_sleep_${rec.metadata.id}",
                            userId = "current_user",
                            metricId = StandardMetric.SLEEP_DURATION,
                            rawValue = durationHours,
                            rawUnit = StandardUnit.HOURS,
                            timestamp = rec.endTime.toEpochMilli(),
                            sourceDevice = dev,
                            sourceProvider = ProviderType.HEALTH_CONNECT,
                            acquisitionMethod = AcquisitionMethod.PERIODIC_SYNC,
                            originalSourceId = rec.metadata.id,
                            provenance = "Measured",
                            qualityScore = 0.94
                        )
                    )
                }
            }

            // 4. Blood Glucose
            if (StandardMetric.BLOOD_GLUCOSE in _enabledMetrics.value && HealthPermission.getReadPermission(BloodGlucoseRecord::class) in granted) {
                val glucoseRecords = client.readRecords(ReadRecordsRequest(BloodGlucoseRecord::class, timeRangeFilter = timeFilter)).records
                for (rec in glucoseRecords) {
                    // BloodGlucoseRecord level is in MillimolesPerLiter
                    val mmol = rec.level.inMillimolesPerLiter
                    results.add(
                        NormalizedObservation.createNormalized(
                            observationId = "hc_glucose_${rec.metadata.id}",
                            userId = "current_user",
                            metricId = StandardMetric.BLOOD_GLUCOSE,
                            rawValue = mmol,
                            rawUnit = StandardUnit.MMOL_L,
                            timestamp = rec.time.toEpochMilli(),
                            sourceDevice = dev,
                            sourceProvider = ProviderType.HEALTH_CONNECT,
                            acquisitionMethod = AcquisitionMethod.EPISODIC_SPOT_CHECK,
                            originalSourceId = rec.metadata.id,
                            provenance = "Measured",
                            qualityScore = 0.97
                        )
                    )
                }
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _lastSyncRecordCount.value = results.size
            _state.value = ConnectorState.CONNECTED
            Result.success(results)
        } catch (e: Exception) {
            _state.value = ConnectorState.ERROR
            Result.failure(e)
        }
    }

    override suspend fun disconnect(context: Context): Result<Unit> {
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
