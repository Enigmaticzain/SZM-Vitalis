from fastapi import APIRouter
from typing import List, Dict, Any

router = APIRouter(prefix="/api/correlation", tags=["Correlation Engine"])

@router.get("/", response_model=List[Dict[str, Any]])
def get_correlations():
    return [
        {"metric_a": "Sleep Duration", "metric_b": "Next Day HRV", "correlation_coefficient": 0.82, "strength": "Strong Positive"},
        {"metric_a": "Late Dinner", "metric_b": "Resting Heart Rate", "correlation_coefficient": 0.76, "strength": "Strong Positive"},
        {"metric_a": "Daily Step Count", "metric_b": "Stress Level", "correlation_coefficient": -0.68, "strength": "Moderate Negative"},
        {"metric_a": "Screen Time before Bed", "metric_b": "Sleep Latency", "correlation_coefficient": 0.71, "strength": "Strong Positive"}
    ]
