import unittest
import io
from fastapi.testclient import TestClient
import sys
import os

# Ensure backend root is in python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from main import app
from services.hospital_engine import hospital_engine

class AdvancedFeaturesTestCase(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(app)

    def test_simulation_multimetric(self):
        # 1. Test Marathon Training Scenario
        payload = {
            "scenario_type": "marathon_training",
            "intensity": 0.9,
            "duration_hours": 12
        }
        resp = self.client.post("/api/simulation/", json=payload)
        self.assertEqual(resp.status_code, 200)
        data = resp.json()
        self.assertIn("predicted_respiratory_peak", data)
        self.assertIn("predicted_heart_rate_peak", data)
        self.assertGreater(data["predicted_respiratory_peak"], 25.0)
        self.assertGreater(data["predicted_heart_rate_peak"], 130.0)

        # Check time series points
        ts = data["time_series_projection"]
        self.assertEqual(len(ts), 12)
        first_pt = ts[0]
        self.assertIn("simulated_respiratory_rate", first_pt)
        self.assertIn("simulated_heart_rate", first_pt)
        self.assertIn("simulated_health_score", first_pt)
        self.assertIn("simulated_glucose", first_pt)

        # 2. Test Altitude Exposure Scenario
        alt_payload = {
            "scenario_type": "altitude_exposure",
            "intensity": 0.8,
            "duration_hours": 24
        }
        alt_resp = self.client.post("/api/simulation/", json=alt_payload)
        self.assertEqual(alt_resp.status_code, 200)
        alt_data = alt_resp.json()
        self.assertGreater(alt_data["predicted_respiratory_peak"], 20.0)

    def test_reports_biomarker_extraction(self):
        # Simulated lab report text with known clinical biomarkers
        sample_lab_text = (
            "QUEST DIAGNOSTICS - CLINICAL COMPREHENSIVE PANEL\n"
            "Patient: Alex Mercer | DOB: 1992-04-12\n"
            "Fasting Glucose: 94 mg/dL [70-99]\n"
            "HbA1c: 5.2% [<5.7]\n"
            "Total Cholesterol: 185 mg/dL [<200]\n"
            "LDL Cholesterol: 92 mg/dL [<100]\n"
            "HDL Cholesterol: 68 mg/dL [>50]\n"
            "Triglycerides: 110 mg/dL [<150]\n"
            "hs-CRP: 0.6 mg/L [<1.0]\n"
            "Vitamin D: 52 ng/mL [30-80]\n"
            "Respiratory Rate: 15 br/min [12-20]\n"
            "SpO2: 99% [95-100]\n"
        )
        file_obj = io.BytesIO(sample_lab_text.encode("utf-8"))
        files = {"file": ("quest_panel.txt", file_obj, "text/plain")}

        resp = self.client.post("/api/reports/parse", files=files)
        self.assertEqual(resp.status_code, 200)
        data = resp.json()

        self.assertIn("biomarkers", data)
        biomarkers = data["biomarkers"]
        self.assertIn("Fasting Glucose", biomarkers)
        self.assertIn("94.0 mg/dL (Optimal)", biomarkers["Fasting Glucose"])
        self.assertIn("HbA1c", biomarkers)
        self.assertIn("5.2 % (Optimal)", biomarkers["HbA1c"])
        self.assertIn("Respiratory Rate", biomarkers)
        self.assertIn("15.0 br/min (Optimal)", biomarkers["Respiratory Rate"])
        self.assertIn("SpO2", biomarkers)
        self.assertIn("99.0 % (Optimal)", biomarkers["SpO2"])
        self.assertEqual(data["confidence_score"], 98.8)

    def test_hospital_news2_scoring(self):
        # 1. Normal Vitals -> Level 1 Routine Telemetry
        eval_normal = hospital_engine.evaluate_vitals(
            hr=64.0,
            glucose=92.0,
            stress=20.0,
            recovery=85.0,
            rr=16.0,
            spo2=98.0
        )
        self.assertEqual(eval_normal["escalation_level"], 1)
        self.assertEqual(eval_normal["news2_score"], 0)

        # 2. Critical Tachypnea (RR >= 25) -> Level 4 Emergency Escalation
        eval_tachypnea = hospital_engine.evaluate_vitals(
            hr=80.0,
            glucose=95.0,
            stress=30.0,
            recovery=70.0,
            rr=26.0,
            spo2=97.0
        )
        self.assertEqual(eval_tachypnea["escalation_level"], 4)
        self.assertEqual(eval_tachypnea["news2_breakdown"]["respiratory_rate"]["score"], 3)
        self.assertTrue(any("CRITICAL TACHYPNEA" in a for a in eval_tachypnea["active_alerts"]))

        # 3. Critical Bradypnea (RR <= 8) -> Level 4 Emergency Escalation
        eval_bradypnea = hospital_engine.evaluate_vitals(
            hr=72.0,
            glucose=90.0,
            stress=20.0,
            recovery=75.0,
            rr=7.5,
            spo2=96.0
        )
        self.assertEqual(eval_bradypnea["escalation_level"], 4)
        self.assertEqual(eval_bradypnea["news2_breakdown"]["respiratory_rate"]["score"], 3)

        # 4. Doctor Dashboard Patient Telemetry
        doc_resp = self.client.get("/api/doctor/patients")
        self.assertEqual(doc_resp.status_code, 200)
        patients = doc_resp.json()
        self.assertGreaterEqual(len(patients), 3)
        for p in patients:
            self.assertIn("latest_vitals", p)
            self.assertIn("rr", p["latest_vitals"])
            self.assertIn("spo2", p["latest_vitals"])

    def test_chat_emergency_and_respiratory_queries(self):
        # 1. Emergency guardrail trigger
        em_resp = self.client.post("/api/chat/", json={"message": "I feel crushing chest pain and shortness of breath"})
        self.assertEqual(em_resp.status_code, 200)
        em_msg = em_resp.json()
        self.assertIn("CRITICAL CLINICAL ALERT", em_msg["message"])
        self.assertIn("911", em_msg["message"])

        # 2. Respiration rate query
        rr_resp = self.client.post("/api/chat/", json={"message": "What is my current respiratory rate?"})
        self.assertEqual(rr_resp.status_code, 200)
        rr_msg = rr_resp.json()
        self.assertIn("breaths/minute", rr_msg["message"])
        self.assertIn("12–20", rr_msg["message"])

if __name__ == "__main__":
    unittest.main()
