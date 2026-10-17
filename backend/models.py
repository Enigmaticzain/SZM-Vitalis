from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

class UserProfile(BaseModel):
    user_id: str
    name: str
    age: int
    gender: str
    weight_kg: float
    height_cm: float
    baseline_hr: float = 62.0
    baseline_hrv: float = 75.0
    baseline_glucose: float = 90.0
    baseline_respiratory_rate: float = 16.0

class WearableDataPoint(BaseModel):
    timestamp: str
    heart_rate: float
    hrv: float
    blood_glucose: float
    steps: int
    sleep_hours: float
    stress_level: float # 0-100
    energy_expenditure_kcal: float
    respiratory_rate: Optional[float] = 16.0
    respiratory_rate_predicted: Optional[float] = 15.8
    respiratory_rate_confidence: Optional[float] = 94.2
    respiratory_rate_uncertainty: Optional[float] = 1.2
    respiratory_rate_status: Optional[str] = "Within Reference Range"
    respiratory_rate_source: Optional[str] = "Predicted (Model v1.0)"

class RespiratoryPredictionRequest(BaseModel):
    heart_rate: float
    hrv: float
    stress_level: Optional[float] = 25.0
    steps_recent: Optional[int] = 0
    age: Optional[int] = 32

class RespiratoryRecord(BaseModel):
    timestamp: str
    respiratory_rate: float
    is_predicted: bool = True
    confidence: float = 94.2
    uncertainty: float = 1.2
    status: str
    reference_range: str = "12–20 breaths/min (Resting Adult)"
    context: str = "Resting"
    model_version: str = "v1.0.0-autonomic-regressor"
    provenance_source: str = "PhysioNet BIDMC / CapnoBase Benchmark Calibration"

class RespiratoryHistoryResponse(BaseModel):
    current: RespiratoryRecord
    baseline_resting_rate: float
    history_24h: List[Dict[str, Any]]
    clinical_disclaimer: str = (
        "General reference range (12–20 breaths/min) is intended for resting adults. "
        "Context such as exercise, sleep, anxiety, or acute illness dynamically modulates respiration. "
        "Respiratory rate alone must not be used to diagnose any disease."
    )

class TwinState(BaseModel):
    user_id: str
    health_score: float # 0-100
    biological_age: float
    metabolic_flexibility: float # 0-100
    recovery_index: float # 0-100
    current_status: str
    latest_metrics: WearableDataPoint
    updated_at: str

class SimulationRequest(BaseModel):
    scenario_type: str # e.g., "sleep_deprivation", "marathon_training", "caffeine_overload", "high_carb_meal"
    intensity: float # 0.0 to 1.0
    duration_hours: int

class SimulationResult(BaseModel):
    scenario: str
    predicted_health_score_impact: float
    predicted_glucose_peak: float
    predicted_recovery_time_hours: float
    recommendations: List[str]
    time_series_projection: List[Dict[str, Any]]

class NutritionLog(BaseModel):
    meal_name: str
    carbs_g: float
    protein_g: float
    fat_g: float
    calories: float
    predicted_glucose_spike: float

class ChatMessage(BaseModel):
    sender: str # "user" or "twin"
    message: str
    timestamp: str

class ChatRequest(BaseModel):
    message: str

class ReportAnalysisResult(BaseModel):
    file_name: str
    biomarkers: Dict[str, Any]
    confidence_score: float = 98.7
    risk_assessment: str
    recommendations: List[str]
