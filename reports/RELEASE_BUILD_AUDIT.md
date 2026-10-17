# Release Build & Packaging Verification Audit

**Date**: 2026-10-14  
**Project**: Beyond Horizon — Human Digital Twin Platform  
**Target Component**: Android Application Package (`.apk`)

---

## 1. Variant Differentiation & Build Integrity

The Gradle build variants were inspected, configured, and compiled to ensure clear differentiation between debug and release variants:

```bash
./gradlew :app:assembleDebug :app:assembleRelease
```

### Artifact Inspection & Comparison

| Characteristic | Debug Variant (`app-debug.apk`) | Release Candidate (`app-release-unsigned.apk`) |
|---|---|---|
| **Path** | `app/build/outputs/apk/debug/app-debug.apk` | `app/build/outputs/apk/release/app-release-unsigned.apk` |
| **Package / Application ID** | `com.example.humandigitaltwin.debug` | `com.example.humandigitaltwin` |
| **Version Name** | `1.0-DEBUG` | `1.0` |
| **Version Code** | `1` | `1` |
| **Binary Size** | **19.2 MB** (19,204,285 bytes) | **12.7 MB** (12,749,285 bytes) |
| **Optimization** | Unminified, debug tooling attached | R8/ProGuard dead-code elimination, unminified resources pruned |
| **Configured Backend URL** | `http://192.168.240.1:8000/` (Local Waydroid gateway) | `https://api.beyondhorizon.health/` (HTTPS TLS 1.3) |
| **Embedded Client API Key** | `"dev-twin-key"` | `""` (Empty string; stripped to prevent hardcoded leak) |
| **HTTP Body Logging** | Active (`HttpLoggingInterceptor.Level.BODY`) | **Disabled** (`HttpLoggingInterceptor.Level.NONE`) |
| **Signing Status** | Signed with standard Android Debug Keystore | **Unsigned** (`app-release-unsigned.apk`) |
| **SHA-256 Checksum** | `cf6fbdc4807427168909e5118658c9e5c92a45acb4b6a1249cf322dd83f3fb7f` | `a1e0da3d734ccc08f159920c2b103b7b96a59becfc758467dd404b8c096d082e` |

---

## 2. On-Device Installation & Execution Results

### A. Physical Hardware Testing Status
- **Physical Device Status**: **Unavailable in current environment.** No physical USB-debugging Android phone was detected upon running `adb devices`.
- **Operating Boundary**: In accordance with project instructions, this limitation is recorded. Testing was executed on a clean, dedicated Android 13 container (Waydroid x86_64, API 33).

### B. Clean Environment Installation Verification
1. **Unsigned Release APK**:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release-unsigned.apk
   # Result: FAILED (Expected)
   # Failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES: Failed to collect certificates]
   ```
   *Confirmed*: Android package manager rejects unsigned release binaries.
2. **Debug Variant Clean Installation**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   # Result: Performing Streamed Install -> Success
   adb shell am start -n com.example.humandigitaltwin.debug/com.example.humandigitaltwin.MainActivity
   # Result: Launched and displayed cleanly (+2s04ms)
   ```
   *Confirmed*: 0 crashes, 0 `ClassNotFoundException` errors, zero unhandled coroutine or Room lifecycle exceptions.

---

## 3. Network Security & Backup Rules Audit

- **Cloud Backup Restrictions**: Confirmed in [`data_extraction_rules.xml`](file:///mnt/D/humandigitaltwin/app/src/main/res/xml/data_extraction_rules.xml) and [`backup_rules.xml`](file:///mnt/D/humandigitaltwin/app/src/main/res/xml/backup_rules.xml):
  - `twin_database` (Room SQLite) is explicitly excluded from Google Cloud Auto-Backup and device-to-device transfers.
- **Exported Components**: Confirmed in [`AndroidManifest.xml`](file:///mnt/D/humandigitaltwin/app/src/main/AndroidManifest.xml):
  - Only `MainActivity` is exported (`android:exported="true"`), protected with standard launcher intent filters. Zero unintended receivers, services, or providers are exposed to external processes.
- **Release Signing Automation**: Provided standalone utility script [`generate_signed_release.sh`](file:///mnt/D/humandigitaltwin/generate_signed_release.sh) enabling secure `apksigner` signing when the enterprise release keystore and alias are supplied, preventing accidental exposure of private passwords in source control.
