from fastapi import APIRouter
from typing import List, Dict, Any

router = APIRouter(prefix="/api/pattern", tags=["Pattern Engine"])

@router.get("/", response_model=List[Dict[str, Any]])
def get_identified_patterns():
    return [
        {
            "pattern_id": "p_01",
            "title": "Caffeine Sensitivity Window",
            "description": "Caffeine consumed after 2:00 PM reduces deep sleep duration by 28% and elevates resting heart rate by 4.5 bpm.",
            "confidence": 0.91,
            "category": "Sleep & Recovery"
        },
        {
            "pattern_id": "p_02",
            "title": "Glucose Stability Post-Walk",
            "description": "A 15-minute walk within 30 minutes post-meal blunts glucose spikes by 35%.",
            "confidence": 0.95,
            "category": "Metabolism"
        },
        {
            "pattern_id": "p_03",
            "title": "Morning HRV Peak",
            "description": "Highest autonomic nervous system recovery occurs between 6:00 AM and 8:00 AM.",
            "confidence": 0.88,
            "category": "Autonomic Function"
        }
    ]
