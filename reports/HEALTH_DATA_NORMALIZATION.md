# HEALTH DATA NORMALIZATION & VALIDATION ARCHITECTURE
## Beyond Horizon — Universal Ingestion Engine

**Date**: October 15, 2026  
**Status**: Fully Specified, Tested, and Implemented

---

## 1. Mathematical Normalization Functions

The platform enforces strict unit conversion rules into canonical metric representations before any analysis or digital twin state updates occur:

### 1.1 Blood Glucose (mmol/L -> mg/dL)
$$\text{Value}_{\text{mg/dL}} = \text{round}\Big(\text{Value}_{\text{mmol/L}} \times 18.0182,\, 1\Big)$$
*Example*: $5.5\text{ mmol/L} \times 18.0182 = 99.1\text{ mg/dL}$.

### 1.2 Body Weight (lbs -> kg)
$$\text{Value}_{\text{kg}} = \text{round}\Big(\text{Value}_{\text{lbs}} \times 0.45359237,\, 2\Big)$$
*Example*: $165.0\text{ lbs} \times 0.45359237 = 74.84\text{ kg}$.

### 1.3 Body Temperature (°F -> °C)
$$\text{Value}_{\text{°C}} = \text{round}\Big((\text{Value}_{\text{°F}} - 32.0) \times \frac{5.0}{9.0},\, 1\Big)$$
*Example*: $98.6\text{ °F} \rightarrow 37.0\text{ °C}$.

### 1.4 Temporal Durations (Minutes & Seconds -> Hours)
$$\text{Duration}_{\text{hours}} = \text{round}\Big(\frac{\text{Duration}_{\text{minutes}}}{60.0},\, 2\Big) = \text{round}\Big(\frac{\text{Duration}_{\text{seconds}}}{3600.0},\, 2\Big)$$
*Example*: $450\text{ minutes} \rightarrow 7.50\text{ hours}$.

---

## 2. Plausibility Range Verification & Outlier Suppression

Every ingested reading is audited by `NormalizedObservation.validatePlausibleRange`:
1. If within canonical physiological limits, the observation is tagged as `ValidationStatus.VALID` and receives its full device quality score ($0.80 - 1.0$).
2. If outside plausible bounds (e.g. Heart Rate $= 350\text{ bpm}$, Glucose $= 1200\text{ mg/dL}$, Steps $< 0$), the observation is flagged as `ValidationStatus.OUT_OF_PLAUSIBLE_RANGE`.
3. Out-of-bounds readings are **never discarded silently**; they are retained in the audit log for clinical review, but their influence on the digital twin score is attenuated ($\text{Quality Score} \times 0.4$) to prevent corrupted advice.

---

## 3. Deduplication & Chronological Alignment

### 3.1 Signature Hashing
Identical records ingested across duplicate Bluetooth packets or repeated file imports are identified via composite signature:
$$\text{Signature} = \text{MetricName} \mathbin{\Vert} \text{EpochTimestamp}$$
Within a given synchronization window, identical signatures increment the `duplicateCount` and are suppressed from downstream model calculation.

### 3.2 Out-of-Order Packet Handling
Observations can arrive out of order during background synchronization or batch uploads. Ingestion pipelines execute chronological sort:
$$\text{SortedObservations} = \text{Observations.sortedByDescending } \{\, \text{it.timestamp} \,\}$$
Ensuring the Twin state always derives from the freshest valid physiological snapshot.

---

## 4. Multi-Device Conflict Resolution

When multiple sensors measure the same physiological parameter in an overlapping window (defined as a $60\text{-second}$ bin), deterministic resolution is applied:

$$\text{Priority Score} = (\text{TierWeight} \times 1000) + (\text{QualityScore} \times 100) + \text{FractionalSeconds}$$

### Provider Tier Weights:
- **Tier 1 (Weight 3.0)**: Dedicated BLE Medical Sensors (Polar ECG chest strap, Continuous Glucose Monitor, Clinical Blood Pressure Cuff).
- **Tier 2 (Weight 2.5)**: Smart Scales & Bioimpedance Analyzers.
- **Tier 3 (Weight 2.0)**: Platform Hubs & Wrist Wearables (Android Health Connect, Apple Health, Garmin Watch).
- **Tier 4 (Weight 1.5)**: Batch File Ingestion (CSV / JSON imports).
- **Tier 5 (Weight 1.0)**: User Manual Self-Reported Logs.
- **Tier 6 (Weight 0.5)**: Synthetic Demo / Simulation Generator.

The observation with the highest composite priority score is designated as the active measurement for the physiological domain; all competing observations are preserved in the audit history.
