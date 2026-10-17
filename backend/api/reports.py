from fastapi import APIRouter, UploadFile, File
from models import ReportAnalysisResult

router = APIRouter(prefix="/api/reports", tags=["Report Parser Engine"])

@router.post("/parse", response_model=ReportAnalysisResult)
async def parse_lab_report(file: UploadFile = File(...)):
    # Simulated lab report parser extracting blood biomarkers
    return ReportAnalysisResult(
        file_name=file.filename or "lab_report.pdf",
        biomarkers={
            "Fasting Glucose": "90 mg/dL (Optimal)",
            "HbA1c": "5.3% (Normal)",
            "LDL Cholesterol": "110 mg/dL (Borderline)",
            "HDL Cholesterol": "65 mg/dL (Optimal)",
            "hs-CRP": "0.8 mg/L (Low Risk)",
            "Vitamin D": "48 ng/mL (Optimal)",
            "Triglycerides": "95 mg/dL (Optimal)"
        },
        confidence_score=98.7,
        risk_assessment="Low cardiovascular and metabolic risk profile. LDL cholesterol is slightly elevated; consider increasing soluble fiber intake.",
        recommendations=[
            "Increase omega-3 fatty acids intake.",
            "Incorporate 30 mins of aerobic exercise 4x weekly.",
            "Re-test lipid panel in 6 months."
        ]
    )
