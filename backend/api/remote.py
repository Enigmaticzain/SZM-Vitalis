from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime

router = APIRouter(prefix="/api/remote", tags=["Remote Post-Discharge Care"])

class CheckInRequest(BaseModel):
    symptom_score: int # 1-10
    pain_level: int # 0-10
    temperature_f: float
    notes: str

check_in_history = [
    {
        "timestamp": datetime.now().isoformat(),
        "symptom_score": 2,
        "pain_level": 1,
        "temperature_f": 98.4,
        "notes": "Feeling good, walked 2000 steps."
    }
]

@router.get("/care-plan")
def get_care_plan():
    return {
        "patient_id": "patient_001",
        "discharge_date": "2026-03-01",
        "diagnosis": "Post-Cardiac Monitoring & Metabolic Optimization",
        "attending_physician": "Dr. Elizabeth Cole, MD",
        "daily_goals": [
            {"goal": "Morning Walk", "target": "2500 steps", "completed": True},
            {"goal": "Hydration", "target": "2000 ml", "completed": True},
            {"goal": "Blood Pressure Check", "target": "2x daily", "completed": False},
            {"goal": "Medication Adherence", "target": "CardioShield 10mg", "completed": True}
        ],
        "medications": [
            {"name": "CardioShield", "dosage": "10mg", "schedule": "Morning with food"},
            {"name": "MetabolicBalance", "dosage": "500mg", "schedule": "Evening"}
        ],
        "recovery_milestones": [
            {"milestone": "Discharge to Home", "status": "Completed"},
            {"milestone": "Remove Telemetry Patch", "status": "In Progress (Day 5 of 14)"},
            {"milestone": "First Follow-up Telehealth", "status": "Scheduled in 4 days"}
        ]
    }

@router.post("/checkin")
def submit_checkin(req: CheckInRequest):
    entry = {
        "timestamp": datetime.now().isoformat(),
        "symptom_score": req.symptom_score,
        "pain_level": req.pain_level,
        "temperature_f": req.temperature_f,
        "notes": req.notes
    }
    check_in_history.insert(0, entry)
    return {"status": "success", "entry": entry, "history": check_in_history}

@router.get("/checkins")
def get_checkins():
    return check_in_history
