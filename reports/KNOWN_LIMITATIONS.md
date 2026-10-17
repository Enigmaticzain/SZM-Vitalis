# Known Limitations & Operating Boundaries (Phase 5)

**System**: Beyond Horizon Human Digital Twin Android MVP  
**Date**: 2026-10-14

---

### 1. Physical Hardware & Wearable BLE Connectivity
- **Physical Device Status**: No physical USB-debugging Android handset was connected to the host during testing (`adb devices` yielded exclusively the Waydroid container at `192.168.240.112:5555`).
- **GATT Sensor Protocol**: Physical Bluetooth Low Energy GATT client pairing with commercial HR straps (Polar H10, Garmin) is not implemented in this phase. The application operates in **Offline Resilience Demo Mode**, displaying mathematical baseline equations rather than falsely claiming a physical sensor link.

### 2. Backend Cloud Endpoint Availability
- **Production URL**: The configured production domain `api.beyondhorizon.health` currently does not resolve to an active DNS entry.
- **Local Host Gateway**: No active FastAPI backend process was detected on `192.168.240.1:8000`.
- **Mitigation**: The app relies on its offline Room SQLite persistence and deterministic baseline generators. When a backend is unreachable, it surfaces transparent connection status indicators without freezing or throwing unhandled network exceptions.

### 3. Release Signing Keystore
- **Current State**: The compiled release candidate artifact [`app-release-unsigned.apk`](file:///mnt/D/humandigitaltwin/app/build/outputs/apk/release/app-release-unsigned.apk) is intentionally unsigned.
- **Requirement**: In compliance with security standards, production private keystores and passwords are not stored in source control. A signing helper script [`generate_signed_release.sh`](file:///mnt/D/humandigitaltwin/generate_signed_release.sh) is provided for signing prior to distribution.

### 4. Regulatory & Clinical Boundary
- **Classification**: Beyond Horizon is an investigational software prototype and educational tool. It is **not** an FDA-cleared Software as a Medical Device (SaMD), does not diagnose pathology, and must never replace certified emergency medical services.
