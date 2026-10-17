# DEVICE COMPATIBILITY MATRIX & PROTOCOL SPECIFICATION
## Beyond Horizon — Human Digital Twin Platform

**Last Updated**: October 15, 2026  
**Status**: Verified & Implemented

---

## 1. Supported Hardware & Platform Connectors

| Provider / Device Type | Model / Vendor | Interface / Protocol | Standard GATT Services / Record Types | Metrics Ingested | Connection Status in Android |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Android Health Connect** | Google Health Platform API | Android IPC / Health Connect Client `1.1.0-alpha07` | `HeartRateRecord`, `StepsRecord`, `SleepSessionRecord`, `BloodGlucoseRecord`, `WeightRecord`, `BloodPressureRecord` | HR, Steps, Sleep, Glucose, Weight, Blood Pressure | Available on Android 14+ (Framework) and Android 8-13 (Google Play APK) |
| **Bluetooth LE Chest Strap** | Polar H10, Polar Verity Sense, Garmin HRM-Pro | Standard BLE GATT | `0x180D` (Heart Rate Service)<br>`0x2A37` (HR Measurement)<br>`0x2A38` (Sensor Location) | Heart Rate (8-bit & 16-bit), RR-Interval (HRV RMSSD) | Implemented & Exercised via `BleGattConnector` |
| **Continuous Glucose Monitor** | Dexcom G7, Abbott FreeStyle Libre 3 | BLE CGM Profile / Partner API | `0x1808` (Glucose Service)<br>`0x2A18` (Glucose Measurement) | Interstitial Glucose (mg/dL), Rate-of-Change Trend Vector | Implemented & Exercised via `CgmConnector` |
| **Smart Scale & BIA** | Withings Body Scan, Omron BCM-500 | Standard BLE GATT / Cloud Export | `0x181D` (Weight Scale Service)<br>`0x2A9D` (Weight Measurement)<br>`0x2A9E` (Body Composition) | Weight (kg/lbs), Body Fat %, Impedance | Implemented & Exercised via `SmartScaleConnector` |
| **Smart Blood Pressure Monitor** | Omron Evolv, Withings BPM Core | Standard BLE GATT | `0x1810` (Blood Pressure Service)<br>`0x2A35` (BP Measurement) | Systolic BP (mmHg), Diastolic BP (mmHg), MAP | Implemented & Exercised via `BleGattConnector` |
| **Pulse Oximeter** | Nonin 3230, Wellue O2Ring | Standard BLE GATT | `0x1822` (Pulse Oximeter Service)<br>`0x2A5F` (PLX Continuous) | SpO2 (%), Pulse Rate (bpm) | Implemented & Supported via GATT Specification |
| **File Import (Batch)** | Apple Health Export, Garmin CSV, Dexcom Clarity, Lab PDF/CSV | File Parser (CSV / JSON) | Canonical CSV schema (`metric,value,unit,timestamp`) & JSON array | All supported `StandardMetric` types | Fully Tested & Active via `FileImportConnector` |

---

## 2. Standard Bluetooth SIG Profile Specifications

### 2.1 Heart Rate Service (`0x180D`) — Characteristic `0x2A37`
- **Flag Byte (Byte 0)**:
  - Bit 0: Value Format (`0` = UINT8 HR, `1` = UINT16 HR)
  - Bit 1–2: Sensor Contact Status (`00/01` = Not supported, `10` = Not detected, `11` = Detected)
  - Bit 3: Energy Expended Status (`1` = Present)
  - Bit 4: RR-Intervals Present (`1` = One or more RR-intervals in units of $1/1024$ second)
- **Implemented Parser**: `BleGattConnector.parseHeartRateMeasurementPacket(bytes)` rigorously handles both 8-bit and 16-bit HR, calculates RR interval in milliseconds, and converts into `StandardMetric.HEART_RATE` and `StandardMetric.HRV_RMSSD`.

### 2.2 Blood Pressure Service (`0x1810`) — Characteristic `0x2A35`
- **Flag Byte (Byte 0)**:
  - Bit 0: Unit (`0` = mmHg, `1` = kPa)
  - Bit 1: Timestamp Present
  - Bit 2: Pulse Rate Present
- **Value Fields**: IEEE 11073-20601 16-bit SFLOAT format for Systolic, Diastolic, and Mean Arterial Pressure.

### 2.3 Weight Scale Service (`0x181D`) — Characteristic `0x2A9D`
- **Flag Byte (Byte 0)**:
  - Bit 0: Measurement Units (`0` = SI [kg], `1` = Imperial [lbs])
  - Bit 1: Timestamp Present
  - Bit 2: User ID Present
  - Bit 3: BMI & Height Present

---

## 3. Supported Clinical Biomarkers & Normalized Ranges

| Metric Identifier | Canonical Unit | Plausible Clinical Range | Warning / Out-of-Range Threshold | Default Priority Tier |
| :--- | :--- | :--- | :--- | :--- |
| `HEART_RATE` | bpm | 28.0 – 240.0 bpm | $<28$ or $>240$ bpm | Tier 1 (BLE Chest Strap = 3.0) |
| `RESTING_HEART_RATE` | bpm | 30.0 – 110.0 bpm | $<30$ or $>120$ bpm | Tier 1 (Clinical / Sleep baseline) |
| `HRV_RMSSD` | ms | 4.0 – 300.0 ms | $<4$ or $>300$ ms | Tier 1 (GATT RR-Interval = 3.0) |
| `BLOOD_GLUCOSE` | mg/dL | 25.0 – 550.0 mg/dL | $<40$ (Hypo) or $>400$ (Hyper) | Tier 1 (Continuous CGM = 3.0) |
| `SPO2` | % | 60.0 – 100.0 % | $<85$ % | Tier 1 (PulseOx = 3.0) |
| `STEPS` | steps | 0 – 150,000 steps/day | $>150,000$ steps/day | Tier 3 (Phone / Watch = 2.0) |
| `SLEEP_DURATION` | hours | 0.0 – 24.0 hours | $>18.0$ hours | Tier 2 (Wearable Actigraphy = 2.0) |
| `BODY_WEIGHT` | kg | 20.0 – 350.0 kg | $<20$ or $>350$ kg | Tier 1 (BIA Smart Scale = 2.5) |
| `BLOOD_PRESSURE_SYSTOLIC` | mmHg | 60.0 – 260.0 mmHg | $<70$ or $>200$ mmHg | Tier 1 (Clinical Cuff = 3.0) |
| `BLOOD_PRESSURE_DIASTOLIC` | mmHg | 35.0 – 160.0 mmHg | $<40$ or $>120$ mmHg | Tier 1 (Clinical Cuff = 3.0) |
| `BODY_TEMPERATURE` | °C | 32.0 – 43.0 °C | $<35.0$ or $>41.5$ °C | Tier 1 (Thermometer = 3.0) |
