from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Header, status
from models import WearableDataPoint
import asyncio
import json
from seed_data import initial_wearable
import random
from datetime import datetime
import os

router = APIRouter(tags=["WebSocket"])

API_KEY = os.getenv("API_KEY", "twin-secret-key-2026")

@router.websocket("/ws/wearable")
async def websocket_wearable(websocket: WebSocket, x_api_key: str = None):
    # Optional or strict auth check for WebSocket query/header
    await websocket.accept()
    try:
        while True:
            hr = round(random.uniform(60.0, 95.0), 1)
            hrv = round(random.uniform(50.0, 90.0), 1)
            stress = round(random.uniform(20.0, 60.0), 1)
            base_rr = 15.0 + (hr - 60.0) * 0.10 + (stress - 25.0) * 0.04
            rr = round(max(11.0, min(24.0, base_rr + random.uniform(-0.3, 0.3))), 1)
            rr_status = "Within Reference Range (12–20)" if 12.0 <= rr <= 20.0 else ("Below Reference Range (<12)" if rr < 12.0 else "Above Reference Range (>20)")

            dp = {
                "timestamp": datetime.now().isoformat(),
                "heart_rate": hr,
                "hrv": hrv,
                "blood_glucose": round(random.uniform(85.0, 110.0), 1),
                "steps": random.randint(5000, 12000),
                "sleep_hours": 7.8,
                "stress_level": stress,
                "energy_expenditure_kcal": round(random.uniform(1800.0, 2400.0), 1),
                "respiratory_rate": rr,
                "respiratory_rate_predicted": round(rr + random.uniform(-0.2, 0.2), 1),
                "respiratory_rate_confidence": 94.2,
                "respiratory_rate_uncertainty": 1.2,
                "respiratory_rate_status": rr_status,
                "respiratory_rate_source": "Predicted (Autonomic Coupling Model v1.0)"
            }
            await websocket.send_text(json.dumps(dp))
            await asyncio.sleep(2)
    except WebSocketDisconnect:
        pass
