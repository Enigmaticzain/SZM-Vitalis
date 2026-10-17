package com.example.humandigitaltwin.data.connectors

import android.content.Context
import com.example.humandigitaltwin.data.models.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal interface for all health device and platform connectors.
 * Connectors isolate device-specific protocol details, authentication,
 * and parsing, and output normalized observations.
 */
interface DeviceConnector {
    val connectorId: String
    val name: String
    val description: String
    val providerType: ProviderType
    val supportedMetrics: Set<StandardMetric>

    /**
     * Observable lifecycle state of the connector.
     */
    val state: StateFlow<ConnectorState>

    /**
     * Currently active or bonded device details (battery, RSSI, manufacturer).
     */
    val deviceDescriptor: StateFlow<DeviceDescriptor?>

    /**
     * Last synchronization timestamp in epoch millis.
     */
    val lastSyncTimestamp: StateFlow<Long?>

    /**
     * Total number of observations ingested during the last sync.
     */
    val lastSyncRecordCount: StateFlow<Int>

    /**
     * Metric filters enabled by the user for this device.
     */
    val enabledMetrics: StateFlow<Set<StandardMetric>>

    /**
     * Check if hardware or platform services are available on the current Android environment.
     */
    suspend fun checkAvailability(context: Context): ConnectorAvailability

    /**
     * Check if necessary runtime permissions or platform consent have been granted.
     */
    suspend fun isAuthorized(context: Context): Boolean

    /**
     * Prompt user authorization or establish connection.
     */
    suspend fun connect(context: Context): Result<Boolean>

    /**
     * Pull or stream latest observations since given epoch millisecond timestamp.
     */
    suspend fun syncObservations(context: Context, sinceTimestamp: Long = 0L): Result<List<NormalizedObservation>>

    /**
     * Revoke authorizations or disconnect active hardware session.
     */
    suspend fun disconnect(context: Context): Result<Unit>

    /**
     * Toggle individual metric sharing for granular privacy control.
     */
    fun setMetricEnabled(metric: StandardMetric, enabled: Boolean)
}
