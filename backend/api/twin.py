from fastapi import APIRouter
from models import TwinState
from seed_data import initial_twin_state, initial_wearable
from datetime import datetime

router = APIRouter(prefix="/api/twin", tags=["Twin Engine"])

current_twin = initial_twin_state

@router.get("/", response_model=TwinState)
def get_twin_state():
    current_twin.latest_metrics = initial_wearable
    current_twin.updated_at = datetime.now().isoformat()
    return current_twin
