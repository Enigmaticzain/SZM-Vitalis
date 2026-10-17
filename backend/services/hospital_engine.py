import random
from datetime import datetime

class HospitalEngine:
    def __init__(self):
        self.escalation_level = 1  # 1: Observation, 2: Nurse Review, 3: Doctor Notification, 4: Emergency Escalation
        self.active_alerts = []
        self.observation_log = []

    def evaluate_vitals(
        self,
        hr: float,
        glucose: float,
        stress: float,
        recovery: float,
        rr: float = 16.0,
        spo2: float = 98.0
    ) -> dict:
        alerts = []
        news2_score = 0
        breakdown = {}

        # 1. Respiration Rate (Royal College of Physicians NEWS2)
        if rr <= 8.0:
            rr_score = 3
            alerts.append(f"CRITICAL BRADYPEA: Respiratory rate is {rr} br/min (<=8). Imminent hypoventilation risk.")
        elif rr <= 11.0:
            rr_score = 1
            alerts.append(f"NOTICE: Respiratory rate {rr} br/min is slightly below resting reference range (12–20).")
        elif rr <= 20.0:
            rr_score = 0
        elif rr <= 24.0:
            rr_score = 2
            alerts.append(f"WARNING TACHYPNEA: Respiratory rate elevated to {rr} br/min (21–24). Autonomic or respiratory distress.")
        else:
            rr_score = 3
            alerts.append(f"CRITICAL TACHYPNEA: Severe hyperventilation at {rr} br/min (>=25). Rapid clinical evaluation warranted.")
        news2_score += rr_score
        breakdown["respiratory_rate"] = {"value": rr, "score": rr_score}

        # 2. Heart Rate
        if hr <= 40.0:
            hr_score = 3
            alerts.append(f"CRITICAL BRADYCARDIA: Heart rate is {hr} bpm (<=40).")
        elif hr <= 50.0:
            hr_score = 1
        elif hr <= 90.0:
            hr_score = 0
        elif hr <= 110.0:
            hr_score = 1
            alerts.append(f"NOTICE: Mild sinus tachycardia at {hr} bpm.")
        elif hr <= 130.0:
            hr_score = 2
            alerts.append(f"WARNING: Marked tachycardia at {hr} bpm (111–130).")
        else:
            hr_score = 3
            alerts.append(f"CRITICAL TACHYCARDIA: Severe cardiac overdrive at {hr} bpm (>130).")
        news2_score += hr_score
        breakdown["heart_rate"] = {"value": hr, "score": hr_score}

        # 3. Oxygen Saturation (SpO2)
        if spo2 <= 91.0:
            spo2_score = 3
            alerts.append(f"CRITICAL HYPOXEMIA: SpO2 dropped to {spo2}% (<=91%). Supplemental O2 required.")
        elif spo2 <= 93.0:
            spo2_score = 2
            alerts.append(f"WARNING: Peripheral oxygen saturation at {spo2}% (92–93%).")
        elif spo2 <= 95.0:
            spo2_score = 1
        else:
            spo2_score = 0
        news2_score += spo2_score
        breakdown["spo2"] = {"value": spo2, "score": spo2_score}

        # 4. Metabolic / Glycemic and Stress Flags
        if glucose > 220.0 or glucose < 55.0:
            news2_score += 3
            alerts.append(f"CRITICAL GLYCEMIC DEREGULATION: Blood glucose is {glucose} mg/dL.")
        elif glucose > 180.0 or glucose < 70.0:
            news2_score += 1
            alerts.append(f"WARNING: Blood glucose excursion at {glucose} mg/dL.")

        if stress > 85.0 or recovery < 25.0:
            news2_score += 1

        # Determine clinical escalation level from NEWS2
        if news2_score >= 7 or rr_score == 3 or hr_score == 3 or spo2_score == 3:
            level = 4
        elif news2_score >= 5 or rr_score == 2 or hr_score == 2:
            level = 3
        elif news2_score >= 2:
            level = 2
        else:
            level = 1

        if not alerts:
            alerts.append("INFO: Patient vitals stable within normal homeostatic parameters.")

        self.escalation_level = level
        self.active_alerts = alerts

        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "level": level,
            "news2_score": news2_score,
            "hr": hr,
            "rr": rr,
            "spo2": spo2,
            "glucose": glucose,
            "stress": stress,
            "status_summary": alerts[0]
        }
        self.observation_log.insert(0, log_entry)
        if len(self.observation_log) > 20:
            self.observation_log.pop()

        return {
            "escalation_level": level,
            "level_name": self._get_level_name(level),
            "news2_score": news2_score,
            "news2_breakdown": breakdown,
            "active_alerts": alerts,
            "observation_log": self.observation_log
        }

    def _get_level_name(self, level: int) -> str:
        mapping = {
            1: "Level 1: Routine Telemetry Observation",
            2: "Level 2: Digital Nurse Review",
            3: "Level 3: Attending Physician Notification",
            4: "Level 4: Emergency Escalation (MET Dispatched)"
        }
        return mapping.get(level, "Level 1: Routine Telemetry Observation")

hospital_engine = HospitalEngine()
