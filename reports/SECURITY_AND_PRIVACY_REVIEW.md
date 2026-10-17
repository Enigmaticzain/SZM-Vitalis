# Security and Privacy Review — Beyond Horizon Android App

**Date**: 2026-10-14  
**Scope**: Native Android Application (com.example.humandigitaltwin)

---

## 1. Executive Summary
Beyond Horizon handles sensitive biological, physiological, and metabolic indicators. This review validates that the application adheres to local data sovereignty principles, defends against common mobile security risks, prevents clinical hallucination, and implements explicit user controls over data retention and destruction.

---

## 2. Security & Data Integrity Audit Findings

### A. Local Data Storage & Sovereignty
- **Room SQLite Sandbox**: User data (meals, conversation history, check-ins, cached twin metrics) is stored strictly in the application's private internal storage (`/data/data/com.example.humandigitaltwin/databases/twin_database`).
- **No External SD Card Leakage**: No unencrypted logs or database exports are placed in public shared storage.
- **User-Controlled Data Purge**: Implemented in `PrivacyScreen.kt` via `viewModel.purgeAllUserData()`, which synchronously deletes all Room records across `MealDao`, `ChatMessageDao`, `CheckInDao`, and `TwinCacheDao`.

### B. Network Transit & Host Gateway
- **Configurable Backend Host**: Communication with the FastAPI backend utilizes Retrofit with configurable base URLs (`http://192.168.240.1:8000/` for local Waydroid testing; domain endpoints for production).
- **Cleartext Traffic Policy**: `android:usesCleartextTraffic="true"` is scoped for local development/emulator testing. In production builds, network security config enforces HTTPS TLS 1.3 with certificate pinning.
- **Zero Third-Party Telemetry**: The codebase contains **zero** third-party ad networks, telemetry trackers, analytics SDKs, or third-party data brokers.

### C. Clinical Information Safety Guardrails
- **Emergency Triage Filter**: Implemented in `DigitalTwinRepository.kt`. All conversational queries are scanned for acute red-flag symptoms (chest pain, stroke, breathing difficulty, acute neuro symptoms). Detected emergencies immediately trigger hardcoded red banner warnings with explicit emergency guidance (Call 911 / 112).
- **No AI Hallucinated Diagnoses**: Companion engine explicitly rejects diagnosing medical conditions or recommending prescription medication alterations.
- **Provenance Transparency Badges**: Every visual metric is tagged with its provenance status:
  - `Measured` (physical sensor/lab report)
  - `Estimated` (calculated from primary metrics)
  - `Predicted` (forecasting model)
  - `Simulated` (synthetic scenario or baseline)
  - `User-Reported` (manual user entry)
- **Clinical Demo Disclaimers**: Doctor Console, Hospital Step-Down, and Remote Care screens prominently display persistent disclaimer cards stating they are demonstration prototypes and not tied to live hospital dispatch.
