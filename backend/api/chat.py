from fastapi import APIRouter
from models import ChatMessage, ChatRequest
from typing import List
from datetime import datetime

router = APIRouter(prefix="/api/chat", tags=["Chat Engine"])

chat_history: List[ChatMessage] = [
    ChatMessage(sender="twin", message="Hello Alex! Your digital twin is operating at 88.5 health score today. How can I assist your longevity goals?", timestamp=datetime.now().isoformat())
]

@router.get("/", response_model=List[ChatMessage])
def get_chat_history():
    return chat_history

@router.post("/", response_model=ChatMessage)
def send_chat_message(req: ChatRequest):
    user_msg = ChatMessage(sender="user", message=req.message, timestamp=datetime.now().isoformat())
    chat_history.append(user_msg)

    # Generate twin response based on query
    text_lower = req.message.lower()
    if "sleep" in text_lower:
        resp_text = "Your sleep efficiency last night was 89%. To improve deep sleep tonight, avoid screens 45 minutes before bed and keep room temperature at 67°F."
    elif "glucose" in text_lower:
        resp_text = "Your blood glucose is currently stable at 92.5 mg/dL. Your metabolic flexibility index is 84%, which is excellent."
    elif "workout" in text_lower or "exercise" in text_lower:
        resp_text = "Based on your high recovery index (91%), today is optimal for a Zone 2 endurance session or high-intensity interval training."
    else:
        resp_text = f"I've analyzed your query regarding '{req.message}'. Your digital twin indicates stable physiological rhythms across all telemetry vectors."

    twin_msg = ChatMessage(sender="twin", message=resp_text, timestamp=datetime.now().isoformat())
    chat_history.append(twin_msg)
    return twin_msg
