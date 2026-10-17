import random
from datetime import datetime

class HospitalEngine:
    def __init__(self):
        self.escalation_level = 1  # 1: Observation, 2: Nurse Review, 3: Doctor Notification, 4: Emergency Escalation
        self.active_alerts = []
        self.observation_log = []

    def evaluate_vitals(self, hr: float, glucose: float, stress: float, recovery: float) -> dict:
        alerts = []
        level = 1

        if hr > 130 or glucose > 220 or stress > 85 or recovery < 30:
            level = 4
            alerts.append("CRITICAL: Severe vitals deviation detected. Code Blue / Emergency Escalation dispatched.")
        elif hr > 115 or glucose > 180 or stress > 70 or recovery < 45:
            level = 3
            alerts.append("WARNING: Significant vitals anomaly. Attending physician notified.")
        elif hr > 100 or glucose > 140 or stress > 55:
            level = 2
            alerts.append("NOTICE: Elevated metrics detected. Digital nurse flagged for clinical review.")
        else:
            level = 1
            alerts.append("INFO: Patient vitals stable within observation parameters.")

        self.escalation_level = level
        self.active_alerts = alerts

        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "level": level,
            "hr": hr,
            "glucose": glucose,
            "stress": stress,
            "status_summary": alerts[0] if alerts else "Stable"
        }
        self.observation_log.insert(0, log_entry)
        if len(self.observation_log) > 20:
            self.observation_log.pop()

        return {
            "escalation_level": level,
            "level_name": self._get_level_name(level),
            "active_alerts": alerts,
            "observation_log": self.observation_log
        }

    def _get_level_name(self, level: int) -> str:
        mapping = {
            1: "Level 1: Routine Observation",
            2: "Level 2: Digital Nurse Review",
            3: "Level 3: Attending Doctor Notification",
            4: "Level 4: Emergency Escalation"
        }
        return mapping.get(level, "Level 1: Routine Observation")

hospital_engine = HospitalEngine()
