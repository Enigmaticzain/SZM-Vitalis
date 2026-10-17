package com.example.humandigitaltwin.data.repository

import android.content.Context
import com.example.humandigitaltwin.BuildConfig
import com.example.humandigitaltwin.data.api.ApiKeyInterceptor
import com.example.humandigitaltwin.data.api.DigitalTwinApiService
import com.example.humandigitaltwin.data.local.TwinDatabase
import com.example.humandigitaltwin.data.local.entity.ChatMessageEntity
import com.example.humandigitaltwin.data.local.entity.CheckInEntity
import com.example.humandigitaltwin.data.local.entity.MealEntity
import com.example.humandigitaltwin.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

class DigitalTwinRepository(private val context: Context? = null) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: DigitalTwinApiService = retrofit.create(DigitalTwinApiService::class.java)

    private val database: TwinDatabase? by lazy {
        context?.let { TwinDatabase.getInstance(it) }
    }

    val deviceHub: com.example.humandigitaltwin.data.connectors.DeviceHubManager by lazy {
        com.example.humandigitaltwin.data.connectors.DeviceHubManager()
    }

    private var localEscalationLevel = 0

    // Emergency symptom keywords for AI safety guardrails
    private val emergencyKeywords = listOf(
        "chest pain", "pressure in chest", "short of breath", "shortness of breath",
        "cant breathe", "can't breathe", "difficulty breathing", "heart attack",
        "stroke", "facial drooping", "slurred speech", "suicide", "severe bleeding",
        "unconscious", "passed out", "passing out", "severe head injury", "blue lips"
    )

    suspend fun getTwinState(): TwinState {
        return try {
            val remote = api.getTwinState()
            remote
        } catch (e: Exception) {
            TwinState(
                user_id = "patient_alex_2026",
                health_score = 92.4,
                biological_age = 29.5,
                metabolic_flexibility = 8.8,
                recovery_index = 89.2,
                current_status = "Optimal & Stable (Offline Demo Mode)",
                latest_metrics = getCurrentWearable(),
                updated_at = Instant.now().toString()
            )
        }
    }

    suspend fun getCurrentWearable(): WearableDataPoint {
        return try {
            api.getCurrentWearable()
        } catch (e: Exception) {
            WearableDataPoint(
                timestamp = Instant.now().toString(),
                heart_rate = 68.0,
                hrv = 75.0,
                blood_glucose = 92.0,
                steps = 6420,
                sleep_hours = 7.8,
                stress_level = 22.0,
                energy_expenditure_kcal = 2150.0
            )
        }
    }

    suspend fun simulateWearable(spike: Boolean): WearableDataPoint {
        return try {
            api.simulateWearable(spike)
        } catch (e: Exception) {
            WearableDataPoint(
                timestamp = Instant.now().toString(),
                heart_rate = if (spike) 135.0 else 68.0,
                hrv = if (spike) 35.0 else 75.0,
                blood_glucose = if (spike) 148.0 else 92.0,
                steps = if (spike) 7200 else 6500,
                sleep_hours = 7.8,
                stress_level = if (spike) 78.0 else 22.0,
                energy_expenditure_kcal = if (spike) 2450.0 else 2150.0
            )
        }
    }

    suspend fun runSimulation(req: SimulationRequest): SimulationResult {
        return try {
            api.runSimulation(req)
        } catch (e: Exception) {
            val baseScore = 92.4
            val delta = when (req.scenario_type) {
                "sleep_deprivation" -> -5.5 * req.intensity
                "high_caffeine" -> -2.0 * req.intensity
                "carb_surge" -> -3.8 * req.intensity
                else -> -3.0 * req.intensity
            }
            val peakGlucose = when (req.scenario_type) {
                "carb_surge" -> 165.0 * req.intensity
                "high_caffeine" -> 118.0 * req.intensity
                else -> 128.0 * req.intensity
            }
            SimulationResult(
                scenario = req.scenario_type,
                predicted_health_score_impact = delta,
                predicted_glucose_peak = peakGlucose,
                predicted_recovery_time_hours = 3.5 * req.intensity + 1.0,
                recommendations = listOf(
                    "Projected impact modeled via metabolic simulator (Educational Estimate).",
                    "Hydrate with 500mL water to mitigate autonomic stress.",
                    "Engage in 20 minutes Zone 1 light mobility to accelerate recovery."
                ),
                time_series_projection = listOf(
                    TimeSeriesPoint(0, baseScore, 92.0),
                    TimeSeriesPoint(1, baseScore + delta * 0.4, peakGlucose * 0.8),
                    TimeSeriesPoint(2, baseScore + delta, peakGlucose),
                    TimeSeriesPoint(4, baseScore + delta * 0.7, 110.0),
                    TimeSeriesPoint(6, baseScore + delta * 0.2, 98.0),
                    TimeSeriesPoint(8, baseScore, 92.0)
                )
            )
        }
    }

    // --- NUTRITION (Room Persisted) ---

    suspend fun getNutritionLogs(): List<NutritionLog> {
        val db = database
        if (db != null) {
            val local = db.mealDao().getAllMeals()
            if (local.isNotEmpty()) {
                return local.map { it.toDomain() }
            } else {
                // Seed initial meals into Room
                val defaults = listOf(
                    NutritionLog("Avocado Toast & Poached Eggs", 35.0, 22.0, 18.0, 390.0, 112.0),
                    NutritionLog("Grilled Salmon with Quinoa & Asparagus", 42.0, 38.0, 14.0, 446.0, 108.0),
                    NutritionLog("Greek Yogurt with Berries & Walnuts", 24.0, 20.0, 12.0, 284.0, 98.0)
                )
                defaults.forEach { db.mealDao().insertMeal(MealEntity.fromDomain(it)) }
                return defaults
            }
        }
        return try {
            api.getNutritionLogs()
        } catch (e: Exception) {
            listOf(
                NutritionLog("Avocado Toast & Poached Eggs", 35.0, 22.0, 18.0, 390.0, 112.0),
                NutritionLog("Grilled Salmon with Quinoa & Asparagus", 42.0, 38.0, 14.0, 446.0, 108.0)
            )
        }
    }

    suspend fun addNutritionLog(log: NutritionLog): NutritionLog {
        database?.mealDao()?.insertMeal(MealEntity.fromDomain(log))
        try {
            api.addNutritionLog(log)
        } catch (e: Exception) {
            // Offline resilience
        }
        return log
    }

    suspend fun deleteNutritionLog(id: Long) {
        database?.mealDao()?.deleteMealById(id)
    }

    suspend fun deleteNutritionLogByName(name: String) {
        database?.mealDao()?.deleteMealByName(name)
    }

    suspend fun clearNutritionLogs() {
        database?.mealDao()?.deleteAllMeals()
    }

    // --- AI HEALTH COMPANION (Room Persisted with Triage Guardrails) ---

    suspend fun getChatHistory(): List<ChatMessage> {
        val db = database
        if (db != null) {
            val local = db.chatMessageDao().getAllMessages()
            if (local.isNotEmpty()) {
                return local.map { it.toDomain() }
            } else {
                val welcome = ChatMessage(
                    sender = "Digital Nurse",
                    message = "Hello Alex! I am your AI Health Companion. I can help analyze your digital twin baselines, nutritional glycemic impact, and recovery trends. Note: For acute symptoms or medical emergencies, please contact emergency services immediately.",
                    timestamp = "08:00"
                )
                db.chatMessageDao().insertMessage(ChatMessageEntity.fromDomain(welcome))
                return listOf(welcome)
            }
        }
        return try {
            api.getChatHistory()
        } catch (e: Exception) {
            listOf(
                ChatMessage("Digital Nurse", "Running in offline demo mode. What physiological metrics would you like to review?", "08:00")
            )
        }
    }

    suspend fun sendChatMessage(userText: String): ChatMessage {
        val now = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val userMsg = ChatMessage("Alex", userText, now)
        database?.chatMessageDao()?.insertMessage(ChatMessageEntity.fromDomain(userMsg))

        // Safety Guardrail: Check for red-flag emergency symptoms
        val isEmergency = emergencyKeywords.any { userText.lowercase().contains(it) }
        val botReply = if (isEmergency) {
            ChatMessage(
                sender = "Digital Nurse (URGENT)",
                message = "CRITICAL ALERT: Your query mentions potential emergency symptoms ('$userText'). This app is an educational prototype and CANNOT treat or diagnose emergencies. Please call 911 (or your local emergency dispatch) immediately or go to the nearest emergency room.",
                timestamp = now
            )
        } else {
            try {
                api.sendChatMessage(ChatRequest(userText))
            } catch (e: Exception) {
                // Adaptive AI Companion referencing actual connected devices and sensor gaps
                val summary = deviceHub.twinSummary.value
                val missing = summary.missingSensors
                val mode = summary.operatingMode

                ChatMessage(
                    sender = "Digital Nurse",
                    message = when {
                        userText.contains("device", ignoreCase = true) || userText.contains("connect", ignoreCase = true) || userText.contains("sensor", ignoreCase = true) ->
                            "Device Status: ${summary.operatingMode.title} (Confidence: ${summary.overallConfidenceScore}%). Active sensors: ${if (summary.activeSensors.isEmpty()) "None (Offline Baseline Demo)" else summary.activeSensors.joinToString(", ")}. Missing: ${if (missing.isEmpty()) "All primary sensors active" else missing.joinToString(", ")}."
                        (userText.contains("hrv", ignoreCase = true) || userText.contains("heart", ignoreCase = true)) && missing.any { it.contains("Heart", ignoreCase = true) } ->
                            "Notice: No heart rate monitor is currently connected. To track real-time heart rate and HRV autonomic tone, please connect a BLE monitor or Health Connect in the Device Hub."
                        userText.contains("sleep", ignoreCase = true) && missing.any { it.contains("Sleep", ignoreCase = true) } ->
                            "Notice: Sleep tracking sensor is not yet connected. Connect Android Health Connect or an Oura/Whoop device to capture restorative sleep architecture."
                        userText.contains("glucose", ignoreCase = true) && missing.any { it.contains("Glucose", ignoreCase = true) } ->
                            "Notice: Continuous Glucose Monitor (CGM) is not linked. Connect Dexcom or FreeStyle Libre in Device Hub to track live glycemic variability."
                        userText.contains("metabolic", ignoreCase = true) ->
                            "Your metabolic flexibility index is ${if (mode == com.example.humandigitaltwin.data.engine.TwinOperatingMode.BASELINE_ONLY) "8.8/10 (Heuristic Baseline)" else "monitored live"}. Fasting glycemic excursions remain within targeted euglycemic boundaries (70-135 mg/dL)."
                        userText.contains("dinner", ignoreCase = true) || userText.contains("food", ignoreCase = true) ->
                            "A balanced meal with 30-40g protein (e.g. wild salmon or tofu), cruciferous vegetables, and complex low-GI carbs (quinoa or lentils) will minimize nighttime glucose spikes."
                        userText.contains("hrv", ignoreCase = true) || userText.contains("heart", ignoreCase = true) ->
                            "Your HRV baseline is 75 ms (RMSSD). Recovery index is 89.2%, showing healthy parasympathetic autonomic balance."
                        userText.contains("sleep", ignoreCase = true) ->
                            "You logged 7.8 hours with 89% sleep efficiency. Deep sleep phases supported optimal musculoskeletal repair."
                        else ->
                            "Received: '$userText'. Active model: ${summary.operatingMode.title} (Confidence: ${summary.overallConfidenceScore}%). For best accuracy, ensure all compatible smart devices are synced via the Device Hub."
                    },
                    timestamp = now
                )
            }
        }

        database?.chatMessageDao()?.insertMessage(ChatMessageEntity.fromDomain(botReply, isEmergency = isEmergency))
        return botReply
    }

    suspend fun clearChatHistory() {
        database?.chatMessageDao()?.deleteAllMessages()
    }

    // --- LAB REPORT PARSER ---

    suspend fun parseReport(file: okhttp3.MultipartBody.Part): ReportAnalysisResult {
        return try {
            api.parseReport(file)
        } catch (e: Exception) {
            ReportAnalysisResult(
                file_name = "Comprehensive_Blood_Panel_Q1_2026.pdf",
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
                risk_assessment = "Low cardiovascular and metabolic risk profile. LDL cholesterol is borderline (110 mg/dL); consider increasing soluble fiber and omega-3s.",
                recommendations = listOf(
                    "Incorporate 30 minutes of aerobic exercise 4x weekly.",
                    "Increase dietary soluble fiber (oats, chia seeds, legumes).",
                    "Repeat lipid panel check-in in 6 months."
                )
            )
        }
    }

    // --- HOSPITAL & DOCTOR WORKFLOWS ---

    suspend fun getHospitalStatus(): HospitalStatusResponse {
        return try {
            api.getHospitalStatus()
        } catch (e: Exception) {
            val alerts = if (localEscalationLevel > 0) listOf("Elevated telemetry anomaly simulated (Educational Demo)") else emptyList()
            HospitalStatusResponse(
                unit = "ICU Telemetry Step-Down Unit (Demonstration)",
                patient_id = "ICU-8842",
                patient_name = "Alex Mercer",
                escalation_level = localEscalationLevel,
                level_name = when(localEscalationLevel) {
                    1 -> "Level 1: Clinical Observation"
                    2 -> "Level 2: Digital Nurse Review"
                    3 -> "Level 3: Attending Physician Notification"
                    4 -> "Level 4: Rapid Response / Code Alert"
                    else -> "Stable Telemetry Baseline"
                },
                active_alerts = alerts,
                observation_log = listOf(
                    ObservationLogEntry(
                        Instant.now().toString(),
                        localEscalationLevel,
                        if (localEscalationLevel > 0) 125.0 else 68.0,
                        if (localEscalationLevel > 0) 155.0 else 92.0,
                        if (localEscalationLevel > 0) 75.0 else 20.0,
                        "Telemetry simulated event log."
                    )
                )
            )
        }
    }

    suspend fun forceHospitalEscalation(level: Int): HospitalStatusResponse {
        localEscalationLevel = level
        return try {
            api.forceHospitalEscalation(mapOf("forced_level" to level))
        } catch (e: Exception) {
            getHospitalStatus()
        }
    }

    suspend fun resolveHospitalAlerts(): HospitalStatusResponse {
        localEscalationLevel = 0
        return try {
            api.resolveHospitalAlerts()
        } catch (e: Exception) {
            getHospitalStatus()
        }
    }

    suspend fun getDoctorPatients(): List<DoctorPatient> {
        return try {
            api.getDoctorPatients()
        } catch (e: Exception) {
            listOf(
                DoctorPatient(
                    patient_id = "PAT-101",
                    name = "Alex Mercer",
                    age = 29,
                    risk_status = "Low Risk",
                    health_score = 92.4,
                    recovery_index = 89.2,
                    latest_vitals = mapOf("HR" to "68 bpm", "Glucose" to "92 mg/dL", "SpO2" to "99%"),
                    trajectory = listOf(90.0, 91.2, 91.8, 92.4),
                    clinical_notes = "Adherent to cardiac rehab exercise regimen. Metabolic twin baseline stable."
                ),
                DoctorPatient(
                    patient_id = "PAT-102",
                    name = "Sarah Jenkins",
                    age = 45,
                    risk_status = "Moderate Monitoring",
                    health_score = 78.5,
                    recovery_index = 74.0,
                    latest_vitals = mapOf("HR" to "82 bpm", "Glucose" to "125 mg/dL", "SpO2" to "97%"),
                    trajectory = listOf(75.0, 76.2, 77.0, 78.5),
                    clinical_notes = "Slight glycemic elevation. Recommended Mediterranean diet adjustments."
                )
            )
        }
    }

    suspend fun getDoctorAlerts(): List<DoctorAlert> {
        return try {
            api.getDoctorAlerts()
        } catch (e: Exception) {
            listOf(
                DoctorAlert("ALT-001", "PAT-102", "Sarah Jenkins", "Moderate", "Fasting glucose exceeded 120 mg/dL threshold.", "08:30")
            )
        }
    }

    suspend fun submitDoctorReview(patientId: String, notes: String, actionItem: String) {
        try {
            api.submitDoctorReview(DoctorReviewRequest(patientId, notes, actionItem))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- REMOTE POST-DISCHARGE CARE (Room Persisted Check-Ins) ---

    suspend fun getRemoteCarePlan(): CarePlan {
        return try {
            api.getRemoteCarePlan()
        } catch (e: Exception) {
            CarePlan(
                patient_id = "PAT-101",
                discharge_date = "2026-03-15",
                diagnosis = "Post-Operative Cardiac Rehabilitation & Metabolic Optimization",
                attending_physician = "Dr. Robert Vance, MD",
                daily_goals = listOf(
                    CareGoal("Morning Walk", "30 minutes Zone 2 aerobic activity", true),
                    CareGoal("Hydration Target", "2.5 Liters filtered water", true),
                    CareGoal("Medication Adherence", "Take prescribed omega-3 & CoQ10", true)
                ),
                medications = listOf(
                    Medication("Omega-3 Fatty Acids", "1000 mg", "Once daily with breakfast"),
                    Medication("CoQ10", "200 mg", "Once daily with lunch")
                ),
                recovery_milestones = listOf(
                    Milestone("Surgical Incision Healing", "Complete"),
                    Milestone("Cardiovascular Baseline Reset", "Achieved"),
                    Milestone("Metabolic Flexibility Goal", "In Progress")
                )
            )
        }
    }

    suspend fun submitRemoteCheckIn(symptomScore: Int, painLevel: Int, temp: Double, notes: String) {
        val entry = CheckInEntry(Instant.now().toString(), symptomScore, painLevel, temp, notes)
        database?.checkInDao()?.insertCheckIn(CheckInEntity.fromDomain(entry))
        try {
            api.submitRemoteCheckIn(CheckInRequest(symptomScore, painLevel, temp, notes))
        } catch (e: Exception) {
            // Offline resilience
        }
    }

    suspend fun getRemoteCheckIns(): List<CheckInEntry> {
        val db = database
        if (db != null) {
            val local = db.checkInDao().getAllCheckIns()
            if (local.isNotEmpty()) {
                return local.map { it.toDomain() }
            }
        }
        return try {
            api.getRemoteCheckIns()
        } catch (e: Exception) {
            listOf(
                CheckInEntry("2026-03-30 08:00", 1, 0, 98.6, "Feeling energetic and well-rested.")
            )
        }
    }

    // --- CONFIDENCE & PROVENANCE ---

    suspend fun getConfidenceBreakdown(): ConfidenceResponse {
        return try {
            api.getConfidenceBreakdown()
        } catch (e: Exception) {
            ConfidenceResponse(
                overall_confidence = 94.5,
                data_quality_index = 96.2,
                domains = listOf(
                    PhysiologicalDomainConfidence("Cardiovascular", 96.8, 98.1, 14, "High Reliability", listOf("ECG", "PPG Optical Heart Rate", "Blood Pressure Monitor"), "1.2%"),
                    PhysiologicalDomainConfidence("Respiratory", 95.2, 97.4, 8, "High Reliability", listOf("SpO2 Pulse Oximeter", "Chest Impedance"), "1.8%"),
                    PhysiologicalDomainConfidence("Endocrine", 92.4, 94.0, 6, "Moderate Reliability", listOf("Continuous Glucose Monitor (CGM)", "Salivary Cortisol"), "3.5%"),
                    PhysiologicalDomainConfidence("Nutrition", 90.1, 91.5, 10, "Moderate Reliability", listOf("Manual Meal Logging", "Photo AI Macro Analyzer"), "4.8%"),
                    PhysiologicalDomainConfidence("Metabolic", 94.0, 95.6, 12, "High Reliability", listOf("Indirect Calorimetry", "Wearable Energy Expenditure"), "2.1%"),
                    PhysiologicalDomainConfidence("Neurological & Sleep", 93.8, 96.0, 9, "High Reliability", listOf("EEG Headband", "Actigraphy Accelerometer"), "2.4%")
                ),
                recommendations = listOf(
                    "Calibrate CGM sensor against capillary blood test to improve Endocrine domain confidence above 95%.",
                    "Log water intake consistently to enhance Nutrition domain data quality index."
                )
            )
        }
    }

    suspend fun getModelPerformance(): ModelPerformanceResponse {
        return try {
            api.getModelPerformance()
        } catch (e: Exception) {
            ModelPerformanceResponse(
                models = listOf(
                    ModelMetricItem("Cardiovascular Event Risk Predictor", "v2.4.1", 0.952, 0.921, 0.905, 0.932, 0.918, 0.054, "Validated & Active"),
                    ModelMetricItem("Glycemic Spike Forecaster", "v3.1.0", 0.941, 0.908, 0.889, 0.920, 0.904, 0.061, "Validated & Active"),
                    ModelMetricItem("Sepsis & ICU Escalation Risk", "v1.8.5", 0.965, 0.940, 0.924, 0.945, 0.934, 0.045, "Validated & Active"),
                    ModelMetricItem("Metabolic Flexibility & Recovery", "v2.0.2", 0.938, 0.895, 0.880, 0.915, 0.897, 0.068, "Validated & Active")
                ),
                provenance = listOf(
                    DataProvenanceItem("Synthea Patient Population", "Synthetic EHR & Clinical Trials", "1,250,000 synthetic patient profiles", "45%", "Tier 1 (Synthetic Benchmark)", "Generates realistic synthetic patient lifelines including condition histories, medications, and lab encounters."),
                    DataProvenanceItem("MIMIC-IV Critical Care Database", "ICU Telemetry & EHR", "450,000 ICU admissions", "25%", "Tier 1 (Clinical Gold Standard)", "De-identified critical care data from Beth Israel Deaconess Medical Center for severe physiological modeling."),
                    DataProvenanceItem("PhysioNet Physiological Signals", "ECG, PPG & Signal Waveforms", "850,000 multi-lead recordings", "20%", "Tier 1 (Research Benchmark)", "Open-access physiological waveform database used for calibrating autonomic nervous system and cardiac anomaly models."),
                    DataProvenanceItem("Live Wearable Sensor Stream", "Consumer IoT Telemetry", "Real-time continuous streaming", "10%", "Tier 2 (Direct Telemetry)", "High-frequency PPG, accelerometer, temperature, and CGM data streams from user's connected wearable devices.")
                )
            )
        }
    }

    // --- PRIVACY & DATA GOVERNANCE ---

    suspend fun purgeAllUserData() {
        database?.let { db ->
            db.mealDao().deleteAllMeals()
            db.chatMessageDao().deleteAllMessages()
            db.checkInDao().deleteAllCheckIns()
            db.twinCacheDao().clearAllCache()
        }
    }
}
