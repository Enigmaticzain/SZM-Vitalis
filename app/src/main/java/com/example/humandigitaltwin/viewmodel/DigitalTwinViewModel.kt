package com.example.humandigitaltwin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.humandigitaltwin.DigitalTwinApplication
import com.example.humandigitaltwin.data.api.WearableWebSocketClient
import com.example.humandigitaltwin.data.models.*
import com.example.humandigitaltwin.data.repository.DigitalTwinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class DigitalTwinViewModel : ViewModel() {
    private val repository: DigitalTwinRepository = try {
        DigitalTwinApplication.repository
    } catch (e: Exception) {
        DigitalTwinRepository()
    }

    private val _twinState = MutableStateFlow<TwinState?>(null)
    val twinState: StateFlow<TwinState?> = _twinState.asStateFlow()

    private val _wearableData = MutableStateFlow<WearableDataPoint?>(null)
    val wearableData: StateFlow<WearableDataPoint?> = _wearableData.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _nutritionLogs = MutableStateFlow<List<NutritionLog>>(emptyList())
    val nutritionLogs: StateFlow<List<NutritionLog>> = _nutritionLogs.asStateFlow()

    private val _simulationResult = MutableStateFlow<SimulationResult?>(null)
    val simulationResult: StateFlow<SimulationResult?> = _simulationResult.asStateFlow()

    private val _reportResult = MutableStateFlow<ReportAnalysisResult?>(null)
    val reportResult: StateFlow<ReportAnalysisResult?> = _reportResult.asStateFlow()

    private val _isParsingReport = MutableStateFlow(false)
    val isParsingReport: StateFlow<Boolean> = _isParsingReport.asStateFlow()

    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOfflineDemo = MutableStateFlow(true)
    val isOfflineDemo: StateFlow<Boolean> = _isOfflineDemo.asStateFlow()

    private val _lastUpdatedTime = MutableStateFlow("Just now")
    val lastUpdatedTime: StateFlow<String> = _lastUpdatedTime.asStateFlow()

    private val _confidenceBreakdown = MutableStateFlow<ConfidenceResponse?>(null)
    val confidenceBreakdown: StateFlow<ConfidenceResponse?> = _confidenceBreakdown.asStateFlow()

    private val _modelPerformance = MutableStateFlow<ModelPerformanceResponse?>(null)
    val modelPerformance: StateFlow<ModelPerformanceResponse?> = _modelPerformance.asStateFlow()

    val deviceHub: com.example.humandigitaltwin.data.connectors.DeviceHubManager get() = repository.deviceHub
    val adaptiveTwinSummary: StateFlow<com.example.humandigitaltwin.data.engine.AdaptiveTwinSummary> = repository.deviceHub.twinSummary
    val deviceObservations: StateFlow<List<NormalizedObservation>> = repository.deviceHub.allObservations
    val isDeviceSyncing: StateFlow<Boolean> = repository.deviceHub.isGlobalSyncing

    private val webSocketClient = WearableWebSocketClient(
        onDataReceived = { dp ->
            _wearableData.value = dp
            val current = _twinState.value
            if (current != null) {
                _twinState.value = current.copy(latest_metrics = dp)
            }
        },
        onConnectionChanged = { connected ->
            _isWebSocketConnected.value = connected
        }
    )

    init {
        refreshAll()
        startWebSocket()
    }

    fun startWebSocket() {
        webSocketClient.connect()
    }

    fun stopWebSocket() {
        webSocketClient.disconnect()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val state = repository.getTwinState()
                _twinState.value = state
                _isOfflineDemo.value = state.current_status.contains("Offline", ignoreCase = true)
                _wearableData.value = repository.getCurrentWearable()
                _chatMessages.value = repository.getChatHistory()
                _nutritionLogs.value = repository.getNutritionLogs()
                _confidenceBreakdown.value = repository.getConfidenceBreakdown()
                _modelPerformance.value = repository.getModelPerformance()
                _lastUpdatedTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            } catch (e: Exception) {
                e.printStackTrace()
                _isOfflineDemo.value = true
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun simulateWearable(spike: Boolean) {
        viewModelScope.launch {
            try {
                val updated = repository.simulateWearable(spike)
                _wearableData.value = updated
                val current = _twinState.value
                if (current != null) {
                    _twinState.value = current.copy(latest_metrics = updated)
                }
                _lastUpdatedTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun runSimulation(scenario: String, intensity: Double, duration: Int) {
        viewModelScope.launch {
            try {
                _simulationResult.value = repository.runSimulation(SimulationRequest(scenario, intensity, duration))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetSimulation() {
        _simulationResult.value = null
    }

    fun addNutritionLog(meal: String, carbs: Double, protein: Double, fat: Double) {
        viewModelScope.launch {
            try {
                val calories = carbs * 4.0 + protein * 4.0 + fat * 9.0
                val predictedSpike = 90.0 + (carbs * 0.8)
                val newLog = NutritionLog(meal, carbs, protein, fat, calories, predictedSpike)
                repository.addNutritionLog(newLog)
                _nutritionLogs.value = repository.getNutritionLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNutritionLog(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNutritionLog(id)
                _nutritionLogs.value = repository.getNutritionLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNutritionLogByName(name: String) {
        viewModelScope.launch {
            try {
                repository.deleteNutritionLogByName(name)
                _nutritionLogs.value = repository.getNutritionLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearNutritionLogs() {
        viewModelScope.launch {
            try {
                repository.clearNutritionLogs()
                _nutritionLogs.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                repository.sendChatMessage(text)
                _chatMessages.value = repository.getChatHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                repository.clearChatHistory()
                _chatMessages.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun purgeAllUserData() {
        viewModelScope.launch {
            try {
                repository.purgeAllUserData()
                _chatMessages.value = emptyList()
                _nutritionLogs.value = emptyList()
                refreshAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importMockReport(reportName: String = "Q1 2026 Comprehensive Blood Panel") {
        viewModelScope.launch {
            _isParsingReport.value = true
            try {
                val dummyBody = "mock pdf bytes".toRequestBody("application/pdf".toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("file", "$reportName.pdf", dummyBody)
                _reportResult.value = repository.parseReport(part)
            } catch (e: Exception) {
                e.printStackTrace()
                _reportResult.value = ReportAnalysisResult(
                    file_name = "$reportName.pdf",
                    biomarkers = mapOf(
                        "Fasting Glucose" to "90 mg/dL (Optimal)",
                        "HbA1c" to "5.3% (Normal)",
                        "LDL Cholesterol" to "110 mg/dL (Borderline)",
                        "HDL Cholesterol" to "65 mg/dL (Optimal)",
                        "hs-CRP" to "0.8 mg/L (Low Risk)",
                        "Triglycerides" to "95 mg/dL (Optimal)",
                        "Vitamin D" to "48 ng/mL (Optimal)"
                    ),
                    confidence_score = 98.7,
                    risk_assessment = "Low metabolic risk profile. Regular monitoring recommended.",
                    recommendations = listOf("Maintain balanced diet", "Regular cardiovascular exercise")
                )
            } finally {
                _isParsingReport.value = false
            }
        }
    }

    fun syncDevice(connector: com.example.humandigitaltwin.data.connectors.DeviceConnector, context: android.content.Context) {
        viewModelScope.launch {
            repository.deviceHub.syncConnector(connector, context)
            _lastUpdatedTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        }
    }

    fun syncAllDevices(context: android.content.Context) {
        viewModelScope.launch {
            repository.deviceHub.syncAll(context)
            _lastUpdatedTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        }
    }

    fun importCsvData(csvText: String): com.example.humandigitaltwin.data.connectors.BatchImportSummary {
        val summary = repository.deviceHub.fileImport.parseCsv(csvText)
        repository.deviceHub.addObservations(summary.acceptedObservations)
        return summary
    }

    fun importJsonData(jsonText: String): com.example.humandigitaltwin.data.connectors.BatchImportSummary {
        val summary = repository.deviceHub.fileImport.parseJson(jsonText)
        repository.deviceHub.addObservations(summary.acceptedObservations)
        return summary
    }

    override fun onCleared() {
        super.onCleared()
        stopWebSocket()
    }
}
