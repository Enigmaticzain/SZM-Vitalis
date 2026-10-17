from fastapi import APIRouter, HTTPException
from models import RespiratoryPredictionRequest, RespiratoryRecord, RespiratoryHistoryResponse
from services.respiratory_engine import respiratory_engine
from typing import Dict, Any

router = APIRouter(prefix="/api/respiratory", tags=["Respiratory Rate Engine"])

@router.get("", response_model=RespiratoryHistoryResponse)
@router.get("/", response_model=RespiratoryHistoryResponse)
def get_respiratory_telemetry():
    """
    Returns latest current respiratory rate prediction, calibrated uncertainty,
    resting adult reference range (12-20 br/min), and 24-hour historical trend.
    """
    return respiratory_engine.get_full_response()

@router.get("/current", response_model=RespiratoryRecord)
def get_current_respiratory_rate():
    """
    Returns the real-time resting respiratory rate prediction.
    Explicitly tags the reading as 'is_predicted=True' with model provenance.
    """
    full = respiratory_engine.get_full_response()
    return full.current

@router.get("/history")
def get_respiratory_history():
    """
    Returns chronological 24-hour historical respiratory trend with upper/lower bounds.
    """
    return {
        "baseline_resting_rate": respiratory_engine.baseline_resting_rate,
        "history": respiratory_engine.get_24h_history(),
        "reference_range": {
            "min_resting": 12.0,
            "max_resting": 20.0,
            "unit": "breaths/minute",
            "target_population": "Resting Adult"
        }
    }

@router.post("/predict", response_model=RespiratoryRecord)
def predict_respiratory_rate(req: RespiratoryPredictionRequest):
    """
    Predicts respiratory rate given incoming physiological parameters (HR, HRV, Stress).
    Validates plausible ranges and handles missing/invalid parameters safely.
    """
    try:
        return respiratory_engine.predict(req)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))

@router.get("/model-card")
def get_model_card() -> Dict[str, Any]:
    """
    Returns dataset audit, model validation metrics (MAE, RMSE, R2, LoA),
    and provenance information.
    """
    return respiratory_engine.MODEL_METRICS
