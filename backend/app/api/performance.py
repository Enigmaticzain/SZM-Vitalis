from fastapi import APIRouter

router = APIRouter(prefix="/api/performance", tags=["Model Performance & Provenance"])

@router.get("")
def get_model_performance():
    return {
        "models": [
            {
                "model_name": "Cardiovascular Event Risk Predictor",
                "version": "v2.4.1",
                "auroc": 0.952,
                "auprc": 0.921,
                "sensitivity": 0.905,
                "specificity": 0.932,
                "f1_score": 0.918,
                "brier_score": 0.054,
                "status": "Validated & Active"
            },
            {
                "model_name": "Glycemic Spike Forecaster",
                "version": "v3.1.0",
                "auroc": 0.941,
                "auprc": 0.908,
                "sensitivity": 0.889,
                "specificity": 0.920,
                "f1_score": 0.904,
                "brier_score": 0.061,
                "status": "Validated & Active"
            },
            {
                "model_name": "Sepsis & ICU Escalation Risk",
                "version": "v1.8.5",
                "auroc": 0.965,
                "auprc": 0.940,
                "sensitivity": 0.924,
                "specificity": 0.945,
                "f1_score": 0.934,
                "brier_score": 0.045,
                "status": "Validated & Active"
            },
            {
                "model_name": "Metabolic Flexibility & Recovery Estimator",
                "version": "v2.0.2",
                "auroc": 0.938,
                "auprc": 0.895,
                "sensitivity": 0.880,
                "specificity": 0.915,
                "f1_score": 0.897,
                "brier_score": 0.068,
                "status": "Validated & Active"
            },
            {
                "model_name": "Respiratory Rate Estimator (BIDMC/CapnoBase)",
                "version": "v1.0.0",
                "auroc": 0.948,
                "auprc": 0.916,
                "sensitivity": 0.912,
                "specificity": 0.930,
                "f1_score": 0.921,
                "brier_score": 0.052,
                "status": "Validated & Active"
            }
        ],
        "provenance": [
            {
                "source_name": "Synthea Patient Population",
                "category": "Synthetic EHR & Clinical Trials",
                "records_count": "1,250,000 synthetic patient profiles",
                "provenance_weight": "45%",
                "reliability_tier": "Tier 1 (Synthetic Benchmark)",
                "description": "Generates realistic synthetic patient lifelines including condition histories, medications, and lab encounters."
            },
            {
                "source_name": "MIMIC-IV Critical Care Database",
                "category": "ICU Telemetry & EHR",
                "records_count": "450,000 ICU admissions",
                "provenance_weight": "25%",
                "reliability_tier": "Tier 1 (Clinical Gold Standard)",
                "description": "De-identified critical care data from Beth Israel Deaconess Medical Center for severe physiological modeling."
            },
            {
                "source_name": "PhysioNet Physiological Signals",
                "category": "ECG, PPG & Signal Waveforms",
                "records_count": "850,000 multi-lead recordings",
                "provenance_weight": "20%",
                "reliability_tier": "Tier 1 (Research Benchmark)",
                "description": "Open-access physiological waveform database used for calibrating autonomic nervous system and cardiac anomaly models."
            },
            {
                "source_name": "Live Wearable Sensor Stream",
                "category": "Consumer IoT Telemetry",
                "records_count": "Real-time continuous streaming",
                "provenance_weight": "10%",
                "reliability_tier": "Tier 2 (Direct Telemetry)",
                "description": "High-frequency PPG, accelerometer, temperature, and CGM data streams from user's connected wearable devices."
            }
        ]
    }
