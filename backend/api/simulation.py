from fastapi import APIRouter
from models import SimulationRequest, SimulationResult
import random

router = APIRouter(prefix="/api/simulation", tags=["Simulation Engine"])

@router.post("/", response_model=SimulationResult)
def run_simulation(req: SimulationRequest):
    impact = -12.5 * req.intensity if req.scenario_type in ["sleep_deprivation", "caffeine_overload"] else 8.2 * req.intensity
    glucose_peak = 145.0 if req.scenario_type == "high_carb_meal" else 105.0
    recovery_time = 24.0 * req.intensity if req.scenario_type == "marathon_training" else 12.0

    time_series = []
    for h in range(req.duration_hours):
        time_series.append({
            "hour": h,
            "simulated_health_score": max(40.0, min(100.0, 88.5 + (impact * (h / req.duration_hours)))),
            "simulated_glucose": max(70.0, min(200.0, 92.0 + (glucose_peak - 92.0) * max(0, 1 - abs(h - 2)/4)))
        })

    recommendations = [
        "Hydrate with 500ml of water immediately post-activity.",
        "Ensure wind-down period 1 hour prior to resting.",
        "Monitor blood glucose response over the next 3 hours."
    ]

    return SimulationResult(
        scenario=f"{req.scenario_type} (Intensity: {req.intensity})",
        predicted_health_score_impact=round(impact, 2),
        predicted_glucose_peak=round(glucose_peak, 2),
        predicted_recovery_time_hours=round(recovery_time, 2),
        recommendations=recommendations,
        time_series_projection=time_series
    )
