from models import UserProfile, WearableDataPoint, TwinState
from datetime import datetime

default_user = UserProfile(
    user_id="user_001",
    name="Alex Mercer",
    age=32,
    gender="Male",
    weight_kg=78.5,
    height_cm=180.0,
    baseline_hr=60.0,
    baseline_hrv=78.0,
    baseline_glucose=88.0,
    baseline_respiratory_rate=16.0
)

initial_wearable = WearableDataPoint(
    timestamp=datetime.now().isoformat(),
    heart_rate=64.5,
    hrv=74.2,
    blood_glucose=92.5,
    steps=6420,
    sleep_hours=7.8,
    stress_level=28.0,
    energy_expenditure_kcal=2140.0,
    respiratory_rate=15.5,
    respiratory_rate_predicted=15.8,
    respiratory_rate_confidence=94.2,
    respiratory_rate_uncertainty=1.2,
    respiratory_rate_status="Within Reference Range",
    respiratory_rate_source="Predicted (Autonomic Coupling Model v1.0)"
)

initial_twin_state = TwinState(
    user_id="user_001",
    health_score=88.5,
    biological_age=29.2,
    metabolic_flexibility=84.0,
    recovery_index=91.0,
    current_status="Optimal recovery state. Heart rate variability is strong.",
    latest_metrics=initial_wearable,
    updated_at=datetime.now().isoformat()
)
