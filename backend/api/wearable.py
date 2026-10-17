from fastapi import APIRouter
from models import WearableDataPoint
from seed_data import initial_wearable
import random
from datetime import datetime

router = APIRouter(prefix="/api/wearable", tags=["Wearable Simulator"])

current_stream = initial_wearable

@router.get("/current", response_model=WearableDataPoint)
def get_current_wearable():
    return current_stream

@router.post("/simulate", response_model=WearableDataPoint)
def simulate_telemetry_tick(activity_spike: bool = False):
    global current_stream
    hr_delta = random.uniform(-2, 2) if not activity_spike else random.uniform(15, 30)
    hrv_delta = random.uniform(-3, 3) if not activity_spike else random.uniform(-10, -5)
    glucose_delta = random.uniform(-1, 1)

    new_hr = max(50.0, min(160.0, current_stream.heart_rate + hr_delta))
    new_hrv = max(20.0, min(120.0, current_stream.hrv + hrv_delta))
    new_stress = max(10.0, min(95.0, current_stream.stress_level + random.uniform(-5, 5)))
    
    # Physiological coupling: Respiratory rate scales with metabolic demand / heart rate / stress
    base_rr = 15.0 + (new_hr - 60.0) * 0.12 + (new_stress - 25.0) * 0.05
    new_rr = round(max(10.0, min(36.0, base_rr + random.uniform(-0.4, 0.4))), 1)
    
    if new_rr < 12.0:
        rr_status = "Below Reference Range (<12)"
    elif new_rr <= 20.0:
        rr_status = "Within Reference Range (12–20)"
    else:
        rr_status = "Above Reference Range (>20)"

    current_stream = WearableDataPoint(
        timestamp=datetime.now().isoformat(),
        heart_rate=round(new_hr, 1),
        hrv=round(new_hrv, 1),
        blood_glucose=round(max(70.0, min(180.0, current_stream.blood_glucose + glucose_delta)), 1),
        steps=current_stream.steps + (150 if activity_spike else 12),
        sleep_hours=current_stream.sleep_hours,
        stress_level=round(new_stress, 1),
        energy_expenditure_kcal=round(current_stream.energy_expenditure_kcal + (25.0 if activity_spike else 3.5), 1),
        respiratory_rate=new_rr,
        respiratory_rate_predicted=round(new_rr + random.uniform(-0.3, 0.3), 1),
        respiratory_rate_confidence=94.2 if not activity_spike else 88.5,
        respiratory_rate_uncertainty=1.2 if not activity_spike else 1.8,
        respiratory_rate_status=rr_status,
        respiratory_rate_source="Predicted (Autonomic Coupling Model v1.0)"
    )
    return current_stream
