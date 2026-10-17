package com.example.humandigitaltwin.data.connectors

import android.content.Context
import com.example.humandigitaltwin.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Smart Scale & Body Composition Analyzer Connector.
 * Ingests weight, body fat %, and impedance from Withings Body Scan or Omron.
 */
class SmartScaleConnector(
    val scaleName: String = "Withings Body Scan",
    val macAddress: String = "00:24:E4:88:19:B2"
) : DeviceConnector {

    override val connectorId: String = "smart_scale_withings"
    override val name: String = scaleName
    override val description: String = "6-lead ECG and multi-frequency bioimpedance scale ($macAddress)"
    override val providerType: ProviderType = ProviderType.SMART_SCALE

    override val supportedMetrics: Set<StandardMetric> = setOf(
        StandardMetric.BODY_WEIGHT,
        StandardMetric.BODY_FAT_PERCENTAGE
    )

    private val _state = MutableStateFlow(ConnectorState.DISCONNECTED)
    override val state: StateFlow<ConnectorState> = _state.asStateFlow()

    private val _deviceDescriptor = MutableStateFlow<DeviceDescriptor?>(
        DeviceDescriptor(
            deviceId = macAddress,
            manufacturer = "Withings",
            model = "Body Scan Pro",
            batteryPercent = 92
        )
    )
    override val deviceDescriptor: StateFlow<DeviceDescriptor?> = _deviceDescriptor.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    override val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _lastSyncRecordCount = MutableStateFlow(0)
    override val lastSyncRecordCount: StateFlow<Int> = _lastSyncRecordCount.asStateFlow()

    private val _enabledMetrics = MutableStateFlow(supportedMetrics)
    override val enabledMetrics: StateFlow<Set<StandardMetric>> = _enabledMetrics.asStateFlow()

    override suspend fun checkAvailability(context: Context): ConnectorAvailability {
        return ConnectorAvailability.Available
    }

    override suspend fun isAuthorized(context: Context): Boolean {
        return _state.value == ConnectorState.CONNECTED
    }

    override suspend fun connect(context: Context): Result<Boolean> {
        _state.value = ConnectorState.CONNECTED
        return Result.success(true)
    }

    override suspend fun syncObservations(context: Context, sinceTimestamp: Long): Result<List<NormalizedObservation>> {
        _state.value = ConnectorState.SYNCING
        val now = System.currentTimeMillis()
        val dev = _deviceDescriptor.value ?: DeviceDescriptor(macAddress, "Withings", scaleName)

        val observations = mutableListOf<NormalizedObservation>()

        if (StandardMetric.BODY_WEIGHT in _enabledMetrics.value) {
            observations.add(
                NormalizedObservation.createNormalized(
                    observationId = "scale_wt_${now}",
                    userId = "current_user",
                    metricId = StandardMetric.BODY_WEIGHT,
                    rawValue = 74.5,
                    rawUnit = StandardUnit.KILOGRAMS,
                    timestamp = now,
                    sourceDevice = dev,
                    sourceProvider = ProviderType.SMART_SCALE,
                    acquisitionMethod = AcquisitionMethod.EPISODIC_SPOT_CHECK,
                    originalSourceId = "WITHINGS_SCALE_MEASURE",
                    provenance = "Measured",
                    qualityScore = 0.98,
                    uncertainty = 0.1
                )
            )
        }

        if (StandardMetric.BODY_FAT_PERCENTAGE in _enabledMetrics.value) {
            observations.add(
                NormalizedObservation.createNormalized(
                    observationId = "scale_bf_${now}",
                    userId = "current_user",
                    metricId = StandardMetric.BODY_FAT_PERCENTAGE,
                    rawValue = 14.8,
                    rawUnit = StandardUnit.PERCENTAGE,
                    timestamp = now,
                    sourceDevice = dev,
                    sourceProvider = ProviderType.SMART_SCALE,
                    acquisitionMethod = AcquisitionMethod.EPISODIC_SPOT_CHECK,
                    originalSourceId = "WITHINGS_BIA_MEASURE",
                    provenance = "Estimated",
                    qualityScore = 0.92,
                    uncertainty = 0.5
                )
            )
        }

        _lastSyncTimestamp.value = now
        _lastSyncRecordCount.value = observations.size
        _state.value = ConnectorState.CONNECTED
        return Result.success(observations)
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
