from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any
from services.hospital_engine import hospital_engine
from seed_data import initial_wearable

router = APIRouter(prefix="/api/hospital", tags=["Hospital Monitoring"])

class EscalationUpdate(BaseModel):
    forced_level: int

@router.get("/status")
def get_hospital_status():
    vitals = initial_wearable
    evaluation = hospital_engine.evaluate_vitals(
        hr=vitals.heart_rate,
        glucose=vitals.blood_glucose,
        stress=vitals.stress_level,
        recovery=75.0,
        rr=vitals.respiratory_rate or 16.0,
        spo2=98.0
    )
    return {
        "unit": "ICU / Telemetry Unit 4B",
        "patient_id": "patient_001",
        "patient_name": "Alex Mercer",
        **evaluation
    }

@router.post("/escalate")
def force_escalation(update: EscalationUpdate):
    hospital_engine.escalation_level = update.forced_level
    name = hospital_engine._get_level_name(update.forced_level)
    alert = f"Manual escalation updated to {name}"
    hospital_engine.active_alerts = [alert]
    return {
        "escalation_level": update.forced_level,
        "level_name": name,
        "active_alerts": hospital_engine.active_alerts,
        "observation_log": hospital_engine.observation_log
    }

@router.post("/resolve")
def resolve_alerts():
    hospital_engine.escalation_level = 1
    hospital_engine.active_alerts = ["All clinical alerts resolved. Patient returned to routine observation."]
    return {
        "escalation_level": 1,
        "level_name": hospital_engine._get_level_name(1),
        "active_alerts": hospital_engine.active_alerts,
        "observation_log": hospital_engine.observation_log
    }
