package com.example.humandigitaltwin.data.connectors

import android.content.Context
import com.example.humandigitaltwin.data.engine.AdaptiveTwinEngine
import com.example.humandigitaltwin.data.engine.AdaptiveTwinSummary
import com.example.humandigitaltwin.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central orchestrator for all device connectors and data normalization.
 */
class DeviceHubManager(
    val healthConnect: HealthConnectConnector = HealthConnectConnector(),
    val bleGatt: BleGattConnector = BleGattConnector(),
    val cgm: CgmConnector = CgmConnector(),
    val smartScale: SmartScaleConnector = SmartScaleConnector(),
    val fileImport: FileImportConnector = FileImportConnector()
) {
    val connectors: List<DeviceConnector> = listOf(
        healthConnect,
        bleGatt,
        cgm,
        smartScale,
        fileImport
    )

    private val engine = AdaptiveTwinEngine()

    private val _allObservations = MutableStateFlow<List<NormalizedObservation>>(emptyList())
    val allObservations: StateFlow<List<NormalizedObservation>> = _allObservations.asStateFlow()

    private val _twinSummary = MutableStateFlow(engine.evaluateTwin(emptyList()))
    val twinSummary: StateFlow<AdaptiveTwinSummary> = _twinSummary.asStateFlow()

    private val _isGlobalSyncing = MutableStateFlow(false)
    val isGlobalSyncing: StateFlow<Boolean> = _isGlobalSyncing.asStateFlow()

    suspend fun syncConnector(connector: DeviceConnector, context: Context): Result<List<NormalizedObservation>> {
        val result = connector.syncObservations(context)
        if (result.isSuccess) {
            val newObs = result.getOrNull().orEmpty()
            addObservations(newObs)
        }
        return result
    }

    suspend fun syncAll(context: Context): Map<String, Result<List<NormalizedObservation>>> {
        _isGlobalSyncing.value = true
        val results = mutableMapOf<String, Result<List<NormalizedObservation>>>()
        try {
            for (connector in connectors) {
                if (connector.state.value == ConnectorState.CONNECTED) {
                    val res = connector.syncObservations(context)
                    results[connector.connectorId] = res
                    if (res.isSuccess) {
                        addObservations(res.getOrNull().orEmpty())
                    }
                }
            }
        } finally {
            _isGlobalSyncing.value = false
        }
        return results
    }

    fun addObservations(observations: List<NormalizedObservation>) {
        if (observations.isEmpty()) return
        val current = _allObservations.value.toMutableList()
        current.addAll(observations)
        val deduplicated = AdaptiveTwinEngine.resolveMultiDeviceConflict(current)
        _allObservations.value = deduplicated
        _twinSummary.value = engine.evaluateTwin(deduplicated)
    }

    suspend fun disconnectAll(context: Context) {
        for (connector in connectors) {
            connector.disconnect(context)
        }
        _allObservations.value = emptyList()
        _twinSummary.value = engine.evaluateTwin(emptyList())
    }
}
