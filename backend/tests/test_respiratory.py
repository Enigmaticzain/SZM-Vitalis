import unittest
from fastapi.testclient import TestClient
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from main import app
from services.respiratory_engine import respiratory_engine
from models import RespiratoryPredictionRequest

class RespiratoryIntegrationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)
        cls.headers = {"X-API-Key": "twin-secret-key-2026"}

    def test_read_root(self):
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["status"], "online")

    def test_get_respiratory_telemetry(self):
        response = self.client.get("/api/respiratory", headers=self.headers)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("current", data)
        self.assertIn("history_24h", data)
        self.assertEqual(len(data["history_24h"]), 24)
        self.assertEqual(data["baseline_resting_rate"], 16.0)
        
        current = data["current"]
        self.assertIn("respiratory_rate", current)
        self.assertTrue(current["is_predicted"])
        self.assertGreater(current["confidence"], 80.0)
        self.assertEqual(current["uncertainty"], 1.2)
        self.assertIn("12–20", current["reference_range"])

    def test_respiratory_status_classification(self):
        # Below resting reference range (<12)
        self.assertIn("Below", respiratory_engine.classify_status(10.5, "Resting"))
        # Within resting reference range (12-20)
        self.assertIn("Within", respiratory_engine.classify_status(15.5, "Resting"))
        # Above resting reference range (>20)
        self.assertIn("Above", respiratory_engine.classify_status(22.0, "Resting"))

    def test_respiratory_prediction_endpoint_valid(self):
        payload = {
            "heart_rate": 65.0,
            "hrv": 75.0,
            "stress_level": 20.0,
            "steps_recent": 0,
            "age": 32
        }
        response = self.client.post("/api/respiratory/predict", json=payload, headers=self.headers)
        self.assertEqual(response.status_code, 200)
        res = response.json()
        self.assertTrue(12.0 <= res["respiratory_rate"] <= 20.0)
        self.assertTrue(res["is_predicted"])
        self.assertEqual(res["status"], "Within Reference Range (12–20)")

    def test_respiratory_prediction_invalid_input(self):
        # Out of plausible physiological range for heart rate
        payload = {
            "heart_rate": 350.0,
            "hrv": 75.0
        }
        response = self.client.post("/api/respiratory/predict", json=payload, headers=self.headers)
        self.assertEqual(response.status_code, 422)

    def test_respiratory_model_card_and_benchmarks(self):
        response = self.client.get("/api/respiratory/model-card", headers=self.headers)
        self.assertEqual(response.status_code, 200)
        card = response.json()
        self.assertEqual(card["mae_breaths_per_min"], 1.42)
        self.assertEqual(card["rmse_breaths_per_min"], 1.86)
        self.assertIn("BIDMC", card["dataset"])

    def test_wearable_current_includes_respiratory_rate(self):
        response = self.client.get("/api/wearable/current", headers=self.headers)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("respiratory_rate", data)
        self.assertIn("respiratory_rate_predicted", data)
        self.assertIn("respiratory_rate_uncertainty", data)
        self.assertIsNotNone(data["respiratory_rate_status"])

    def test_wearable_simulate_tick(self):
        response = self.client.post("/api/wearable/simulate?activity_spike=true", headers=self.headers)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertGreater(data["respiratory_rate"], 0)
        self.assertGreater(data["respiratory_rate_confidence"], 80.0)

if __name__ == "__main__":
    unittest.main()
