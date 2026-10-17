# PRIVACY, DATA CONSENT & REGULATORY AUDIT
## Beyond Horizon — Phase 6 Governance Review

**Date**: October 15, 2026  
**Status**: Compliant with Android Health Connect & Medical Device Data Governance Principles

---

## 1. Android Health Connect Compliance

### 1.1 Declared Permissions
The application explicitly limits its manifest declarations to essential read permissions:
```xml
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_BLOOD_GLUCOSE" />
<uses-permission android:name="android.permission.health.READ_OXYGEN_SATURATION" />
<uses-permission android:name="android.permission.health.READ_TOTAL_CALORIES_BURNED" />
<uses-permission android:name="android.permission.health.READ_WEIGHT" />
<uses-permission android:name="android.permission.health.READ_BLOOD_PRESSURE" />
```
Write permissions (`WRITE_*`) are intentionally **not requested**, ensuring the application operates strictly as a consumer of health data and cannot inadvertently mutate external records in Google Fit or Samsung Health.

### 1.2 Permissions Rationale & Usage Intent
An `<activity-alias>` targeting `MainActivity` is registered for:
- `android.intent.action.VIEW_PERMISSION_USAGE` with category `android.intent.category.HEALTH_PERMISSIONS`
- Protected by permission `android.permission.START_VIEW_PERMISSION_USAGE`

When users review health data access in Android System Settings, the application correctly directs them to the in-app Privacy Center and Device Hub.

---

## 2. Granular Data-Type Consent & Privacy Controls

Users retain per-metric granular control over all connected hardware:
1. **Interactive Signal Toggles**: On each connector card in the Device Hub, users can toggle individual metrics (e.g. share Heart Rate, but disable Sleep Duration or Blood Glucose).
2. **Immediate Suppression**: Toggling off a metric immediately halts ingestion and eliminates the signal from subsequent twin model iterations.
3. **Local Encryption & In-Memory Isolation**: Telemetry streams are processed in-memory or persisted locally within encrypted Room database partitions. No raw telemetry is dispatched to unauthenticated external third-party tracking services.

---

## 3. Disconnection & Complete Data Purge Verification

1. **Connector Disconnect**:
   - Tapping "Disconnect" on any device tears down active sessions, clears temporary buffers, and resets record counters to zero.
2. **Right to Be Forgotten / Complete Purge**:
   - In `PrivacyScreen.kt` and `DigitalTwinRepository.purgeAllUserData()`, the user can trigger an irreversible purge:
     - Deletes all local meals (`meals` table)
     - Deletes all AI chat history (`chat_messages` table)
     - Deletes all clinical check-ins (`check_ins` table)
     - Flushes digital twin cache (`twin_cache` table)
     - Disconnects all active hardware connectors and purges all buffered normalized observations
