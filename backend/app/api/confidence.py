from fastapi import APIRouter
from app.services.confidence_engine import confidence_engine

router = APIRouter(prefix="/api/confidence", tags=["Twin Confidence"])

@router.get("")
def get_confidence_breakdown():
    return confidence_engine.get_confidence_breakdown()
