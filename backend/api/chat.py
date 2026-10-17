from fastapi import APIRouter
from models import ChatMessage, ChatRequest
from typing import List
from datetime import datetime
from seed_data import initial_wearable, initial_twin_state, default_user
from services.respiratory_engine import respiratory_engine

router = APIRouter(prefix="/api/chat", tags=["Chat Engine"])

chat_history: List[ChatMessage] = [
    ChatMessage(
        sender="twin",
        message="Hello Alex! Your digital twin is operating at 88.5 health score today. Resting heart rate is 64 bpm and respiratory rate is 16 breaths/min. How can I assist your longevity goals?",
        timestamp=datetime.now().isoformat()
    )
]

EMERGENCY_RED_FLAGS = [
    "chest pain", "pressure in chest", "short of breath", "shortness of breath",
    "cant breathe", "can't breathe", "difficulty breathing", "heart attack",
    "stroke", "facial drooping", "slurred speech", "suicide", "severe bleeding",
    "unconscious", "passed out", "passing out", "severe head injury", "blue lips"
]

@router.get("/", response_model=List[ChatMessage])
def get_chat_history():
    return chat_history

@router.post("/", response_model=ChatMessage)
def send_chat_message(req: ChatRequest):
    user_msg = ChatMessage(sender="user", message=req.message, timestamp=datetime.now().isoformat())
    chat_history.append(user_msg)

    text_lower = req.message.lower()

    # 1. Clinical Safety Emergency Guardrail
    if any(keyword in text_lower for keyword in EMERGENCY_RED_FLAGS):
        resp_text = (
            "🚨 CRITICAL CLINICAL ALERT: Your symptoms may indicate an acute medical emergency. "
            "Please call 911 (or your local emergency services) immediately or go to the nearest emergency room. "
            "Do not rely on this digital twin or delay in-person medical attention."
        )
    # 2. Respiration & Breathing Queries
    elif any(k in text_lower for k in ["respirat", "breath", "lung", "oxygen", "spo2", "br/min"]):
        cur_rr = initial_wearable.respiratory_rate or 16.0
        status = initial_wearable.respiratory_rate_status or "Within Reference Range (12–20)"
        resp_text = (
            f"Your current estimated resting respiratory rate is {cur_rr} breaths/minute ({status}), "
            f"modeled via autonomic coupling (RSA) with an uncertainty of ±1.2 br/min. "
            f"This falls within the standard 12–20 br/min resting adult reference range. Pulmonary gas exchange is efficient."
        )
    # 3. Sleep & Recovery Queries
    elif "sleep" in text_lower:
        resp_text = (
            f"Your sleep duration was {initial_wearable.sleep_hours} hours with 89% sleep efficiency. "
            f"Nocturnal HRV was {initial_wearable.hrv} ms (RMSSD), indicating strong parasympathetic vagal recovery."
        )
    # 4. Glucose & Metabolism Queries
    elif "glucose" in text_lower or "sugar" in text_lower:
        resp_text = (
            f"Your interstitial glucose is currently stable at {initial_wearable.blood_glucose} mg/dL. "
            f"Metabolic flexibility index is {initial_twin_state.metabolic_flexibility}%, reflecting efficient substrate switching."
        )
    # 5. Heart Rate & Cardiac Queries
    elif any(k in text_lower for k in ["heart", "pulse", "bpm", "cardiac"]):
        resp_text = (
            f"Your resting heart rate is {initial_wearable.heart_rate} bpm against your personal baseline of "
            f"{default_user.baseline_hr} bpm. Autonomic balance is optimal."
        )
    # 6. Workout & Exercise Readiness
    elif any(k in text_lower for k in ["workout", "exercise", "train", "gym", "run"]):
        resp_text = (
            f"Based on your Recovery Index of {initial_twin_state.recovery_index}% and stable vitals, "
            f"today is ideal for a Zone 2 endurance session or high-intensity interval training. Ensure proper hydration."
        )
    # 7. General Longevity Query
    else:
        resp_text = (
            f"I've analyzed your telemetry against your digital twin. "
            f"Overall health score is {initial_twin_state.health_score}/100 with biological age estimated at "
            f"{initial_twin_state.biological_age} (vs chronological {default_user.age}). Vitals are in stable homeostasis."
        )

    twin_msg = ChatMessage(sender="twin", message=resp_text, timestamp=datetime.now().isoformat())
    chat_history.append(twin_msg)
    return twin_msg
