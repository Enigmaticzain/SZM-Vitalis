package com.example.humandigitaltwin.data.models

import kotlinx.serialization.Serializable

@Serializable
data class WearableDataPoint(
    val timestamp: String,
    val heart_rate: Double,
    val hrv: Double,
    val blood_glucose: Double,
    val steps: Int,
    val sleep_hours: Double,
    val stress_level: Double,
    val energy_expenditure_kcal: Double,
    val respiratory_rate: Double = 16.0,
    val respiratory_rate_predicted: Double = 15.8,
    val respiratory_rate_confidence: Double = 94.2,
    val respiratory_rate_uncertainty: Double = 1.2,
    val respiratory_rate_status: String = "Within Reference Range (12–20)"
)

@Serializable
data class TwinState(
    val user_id: String,
    val health_score: Double,
    val biological_age: Double,
    val metabolic_flexibility: Double,
    val recovery_index: Double,
    val current_status: String,
    val latest_metrics: WearableDataPoint,
    val updated_at: String
)

@Serializable
data class SimulationRequest(
    val scenario_type: String,
    val intensity: Double,
    val duration_hours: Int
)

@Serializable
data class SimulationResult(
    val scenario: String,
    val predicted_health_score_impact: Double,
    val predicted_glucose_peak: Double,
    val predicted_recovery_time_hours: Double,
    val recommendations: List<String>,
    val time_series_projection: List<TimeSeriesPoint>
)

@Serializable
data class TimeSeriesPoint(
    val hour: Int,
    val simulated_health_score: Double,
    val simulated_glucose: Double
)

@Serializable
data class NutritionLog(
    val meal_name: String,
    val carbs_g: Double,
    val protein_g: Double,
    val fat_g: Double,
    val calories: Double,
    val predicted_glucose_spike: Double
)

@Serializable
data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String
)

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ReportAnalysisResult(
    val file_name: String,
    val biomarkers: Map<String, String>,
    val confidence_score: Double = 98.7,
    val risk_assessment: String,
    val recommendations: List<String>
)

@Serializable
data class HospitalStatusResponse(
    val unit: String,
    val patient_id: String,
    val patient_name: String,
    val escalation_level: Int,
    val level_name: String,
    val active_alerts: List<String>,
    val observation_log: List<ObservationLogEntry>
)

@Serializable
data class ObservationLogEntry(
    val timestamp: String,
    val level: Int,
    val hr: Double,
    val glucose: Double,
    val stress: Double,
    val status_summary: String
)

@Serializable
data class DoctorPatient(
    val patient_id: String,
    val name: String,
    val age: Int,
    val risk_status: String,
    val health_score: Double,
    val recovery_index: Double,
    val latest_vitals: Map<String, String>,
    val trajectory: List<Double>,
    val clinical_notes: String
)

@Serializable
data class DoctorAlert(
    val alert_id: String,
    val patient_id: String,
    val patient_name: String,
    val severity: String,
    val message: String,
    val timestamp: String
)

@Serializable
data class DoctorReviewRequest(
    val patient_id: String,
    val notes: String,
    val action_item: String
)

@Serializable
data class CarePlan(
    val patient_id: String,
    val discharge_date: String,
    val diagnosis: String,
    val attending_physician: String,
    val daily_goals: List<CareGoal>,
    val medications: List<Medication>,
    val recovery_milestones: List<Milestone>
)

@Serializable
data class CareGoal(
    val goal: String,
    val target: String,
    val completed: Boolean
)

@Serializable
data class Medication(
    val name: String,
    val dosage: String,
    val schedule: String
)

@Serializable
data class Milestone(
    val milestone: String,
    val status: String
)

@Serializable
data class CheckInRequest(
    val symptom_score: Int,
    val pain_level: Int,
    val temperature_f: Double,
    val notes: String
)

@Serializable
data class CheckInEntry(
    val timestamp: String,
    val symptom_score: Int,
    val pain_level: Int,
    val temperature_f: Double,
    val notes: String
)

@Serializable
data class ConfidenceResponse(
    val overall_confidence: Double,
    val data_quality_index: Double,
    val domains: List<PhysiologicalDomainConfidence>,
    val recommendations: List<String>
)

@Serializable
data class PhysiologicalDomainConfidence(
    val domain: String,
    val confidence_score: Double,
    val data_quality: Double,
    val metrics_count: Int,
    val status: String,
    val sensors: List<String>,
    val noise_ratio: String
)

@Serializable
data class ModelPerformanceResponse(
    val models: List<ModelMetricItem>,
    val provenance: List<DataProvenanceItem>
)

@Serializable
data class ModelMetricItem(
    val model_name: String,
    val version: String,
    val auroc: Double,
    val auprc: Double,
    val sensitivity: Double,
    val specificity: Double,
    val f1_score: Double,
    val brier_score: Double,
    val status: String
)

@Serializable
data class DataProvenanceItem(
    val source_name: String,
    val category: String,
    val records_count: String,
    val provenance_weight: String,
    val reliability_tier: String,
    val description: String
)
