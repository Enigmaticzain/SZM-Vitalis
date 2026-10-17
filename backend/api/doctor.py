from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime

router = APIRouter(prefix="/api/doctor", tags=["Doctor Dashboard"])

class DoctorReview(BaseModel):
    patient_id: str
    notes: str
    action_item: str

patients_db = [
    {
        "patient_id": "patient_001",
        "name": "Alex Mercer",
        "age": 32,
        "risk_status": "Stable",
        "health_score": 88.5,
        "recovery_index": 82.0,
        "latest_vitals": {"hr": 64.0, "glucose": 92.0, "bp": "120/80"},
        "trajectory": [85.0, 86.2, 87.0, 88.5],
        "clinical_notes": "Recovering well post-monitoring. Vitals stable."
    },
    {
        "patient_id": "patient_002",
        "name": "Sarah Jenkins",
        "age": 45,
        "risk_status": "Warning",
        "health_score": 68.0,
        "recovery_index": 54.0,
        "latest_vitals": {"hr": 98.0, "glucose": 155.0, "bp": "138/88"},
        "trajectory": [75.0, 72.0, 70.0, 68.0],
        "clinical_notes": "Elevated glucose and heart rate observed during afternoon hours. Nurse review flagged."
    },
    {
        "patient_id": "patient_003",
        "name": "Marcus Vance",
        "age": 58,
        "risk_status": "Critical",
        "health_score": 42.5,
        "recovery_index": 31.0,
        "latest_vitals": {"hr": 128.0, "glucose": 210.0, "bp": "155/98"},
        "trajectory": [60.0, 52.0, 48.0, 42.5],
        "clinical_notes": "Level 4 Emergency Escalation triggered due to acute tachycardia and hyperglycemia."
    }
]

@router.get("/patients")
def get_patients():
    return patients_db

@router.get("/alerts")
def get_doctor_alerts():
    return [
        {
            "alert_id": "alt_001",
            "patient_id": "patient_003",
            "patient_name": "Marcus Vance",
            "severity": "CRITICAL",
            "message": "Heart rate sustained > 120 bpm, glucose spike to 210 mg/dL.",
            "timestamp": datetime.now().isoformat()
        },
        {
            "alert_id": "alt_002",
            "patient_id": "patient_002",
            "patient_name": "Sarah Jenkins",
            "severity": "WARNING",
            "message": "Elevated stress level and resting HR anomaly.",
            "timestamp": datetime.now().isoformat()
        }
    ]

@router.post("/review")
def submit_review(review: DoctorReview):
    for p in patients_db:
        if p["patient_id"] == review.patient_id:
            p["clinical_notes"] = f"Dr. Review ({datetime.now().strftime('%m-%d %H:%M')}): {review.notes} | Action: {review.action_item}"
            return {"status": "success", "patient": p}
    return {"status": "error", "message": "Patient not found"}
