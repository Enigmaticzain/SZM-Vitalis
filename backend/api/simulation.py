from fastapi import APIRouter
from models import SimulationRequest, SimulationResult
import math

router = APIRouter(prefix="/api/simulation", tags=["Simulation Engine"])

@router.post("/", response_model=SimulationResult)
def run_simulation(req: SimulationRequest):
    scenario = req.scenario_type
    intensity = max(0.1, min(1.0, req.intensity))
    duration = max(2, req.duration_hours)

    # Base resting vitals
    base_health = 88.5
    base_glucose = 92.0
    base_rr = 16.0
    base_hr = 64.0

    if scenario == "marathon_training":
        impact = 12.0 * intensity
        glucose_peak = 114.0
        recovery_time = 24.0 * intensity
        rr_peak = round(base_rr + 15.0 * intensity, 1) # up to 31.0 br/min
        hr_peak = round(base_hr + 92.0 * intensity, 1) # up to 156 bpm
        recommendations = [
            "Hydrate with 500-750ml electrolyte solution post-exercise.",
            "Replenish glycogen with a 4:1 carbohydrate-to-protein ratio snack within 45 mins.",
            "Maintain Zone 1 active recovery cooldown to accelerate lactate clearance."
        ]
    elif scenario == "sleep_deprivation":
        impact = -14.5 * intensity
        glucose_peak = round(base_glucose + 24.0 * intensity, 1)
        recovery_time = round(16.0 * intensity, 1)
        rr_peak = round(base_rr + 3.2 * intensity, 1) # autonomic sympathetic elevation
        hr_peak = round(base_hr + 16.0 * intensity, 1)
        recommendations = [
            "Incorporate a 20-30 minute restorative afternoon nap before 3:00 PM.",
            "Curtail caffeine consumption after 12:00 PM to avoid compounding sleep fragmentation.",
            "Engage 4-7-8 parasympathetic diaphragmatic breathing before bedtime."
        ]
    elif scenario == "caffeine_overload":
        impact = -6.0 * intensity
        glucose_peak = round(base_glucose + 18.0 * intensity, 1)
        recovery_time = round(8.0 * intensity, 1)
        rr_peak = round(base_rr + 5.5 * intensity, 1)
        hr_peak = round(base_hr + 28.0 * intensity, 1)
        recommendations = [
            "Drink 500ml water with magnesium or electrolytes to calm adrenergic tone.",
            "Perform slow resonant coherent breathing (5.5 breaths/min) for 10 minutes.",
            "Avoid additional stimulants for the next 8–10 hours."
        ]
    elif scenario == "high_carb_meal":
        impact = -5.0 * intensity
        glucose_peak = round(base_glucose + 68.0 * intensity, 1)
        recovery_time = round(5.0 * intensity, 1)
        rr_peak = round(base_rr + 1.8 * intensity, 1)
        hr_peak = round(base_hr + 11.0 * intensity, 1)
        recommendations = [
            "Engage in a 15-minute brisk walk immediately to stimulate non-insulin GLUT4 uptake.",
            "Pair future high-glycemic meals with soluble dietary fiber and healthy fats.",
            "Monitor postprandial glucose return to baseline over the next 2-3 hours."
        ]
    elif scenario == "altitude_exposure":
        impact = -9.2 * intensity
        glucose_peak = round(base_glucose + 12.0 * intensity, 1)
        recovery_time = round(32.0 * intensity, 1)
        rr_peak = round(base_rr + 10.5 * intensity, 1) # hypoxic ventilatory drive
        hr_peak = round(base_hr + 26.0 * intensity, 1)
        recommendations = [
            "Acclimatize gradually: allow 24-48 hours before undertaking strenuous exertion.",
            "Increase daily hydration by 1.0-1.5L to counter hyperventilation fluid loss.",
            "Keep peripheral capillary SpO2 monitored closely; descend if tachypnea persists."
        ]
    elif scenario == "intermittent_fasting":
        impact = 9.8 * intensity
        glucose_peak = 79.0
        recovery_time = 6.0
        rr_peak = 14.5
        hr_peak = 59.0
        recommendations = [
            "Maintain cellular hydration with mineral-rich water.",
            "Break fast gently with clean proteins and cruciferous greens.",
            "Leverage ketone elevation for high-focus cognitive workflows."
        ]
    else:
        impact = 5.0 * intensity
        glucose_peak = 105.0
        recovery_time = 12.0
        rr_peak = 16.0
        hr_peak = 64.0
        recommendations = [
            "Maintain balanced hydration and nutrition.",
            "Prioritize consistent circadian sleep windows."
        ]

    time_series = []
    for h in range(duration):
        progress = h / float(duration)
        # Pulse peak function for acute events (peaking around 25-35% into duration)
        peak_pos = 0.3
        sigma = 0.25
        bell_curve = math.exp(-((progress - peak_pos) ** 2) / (2 * (sigma ** 2)))

        # Health score curve: gradual impact and recovery
        health_delta = impact * (1 - progress) if impact < 0 else impact * progress
        sim_health = round(max(40.0, min(100.0, base_health + health_delta)), 1)

        # Glucose curve
        sim_glucose = round(max(65.0, min(240.0, base_glucose + (glucose_peak - base_glucose) * bell_curve)), 1)

        # Respiratory rate curve
        sim_rr = round(max(10.0, min(45.0, base_rr + (rr_peak - base_rr) * bell_curve)), 1)

        # Heart rate curve
        sim_hr = round(max(45.0, min(200.0, base_hr + (hr_peak - base_hr) * bell_curve)), 1)

        time_series.append({
            "hour": h,
            "simulated_health_score": sim_health,
            "simulated_glucose": sim_glucose,
            "simulated_respiratory_rate": sim_rr,
            "simulated_heart_rate": sim_hr
        })

    return SimulationResult(
        scenario=f"{scenario} (Intensity: {round(intensity, 1)})",
        predicted_health_score_impact=round(impact, 2),
        predicted_glucose_peak=round(glucose_peak, 2),
        predicted_recovery_time_hours=round(recovery_time, 2),
        recommendations=recommendations,
        time_series_projection=time_series,
        predicted_respiratory_peak=rr_peak,
        predicted_heart_rate_peak=hr_peak
    )
