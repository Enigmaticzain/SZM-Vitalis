from fastapi import APIRouter
from typing import Dict, Any

router = APIRouter(prefix="/api/prediction", tags=["Prediction Engine"])

@router.get("/trajectory", response_model=Dict[str, Any])
def get_health_trajectory():
    return {
        "user_id": "user_001",
        "current_biological_age": 29.2,
        "projected_age_in_5_years": 27.8,
        "risk_factors": [
            {"factor": "Late night screen time", "impact": "Moderate", "probability": 0.65},
            {"factor": "Inconsistent meal timing", "impact": "Low", "probability": 0.30}
        ],
        "7_day_forecast": [
            {"day": "Day 1", "predicted_score": 89.0},
            {"day": "Day 2", "predicted_score": 90.2},
            {"day": "Day 3", "predicted_score": 88.5},
            {"day": "Day 4", "predicted_score": 91.0},
            {"day": "Day 5", "predicted_score": 92.1},
            {"day": "Day 6", "predicted_score": 91.5},
            {"day": "Day 7", "predicted_score": 93.0}
        ]
    }
