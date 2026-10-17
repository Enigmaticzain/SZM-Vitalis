# Health Data Validation, Metric Calculation & Calculation Audit

**Date**: 2026-10-14  
**Project**: Beyond Horizon — Human Digital Twin Platform  
**Audit Scope**: All 7 Primary Physiological Health Metrics & Statistical Performance Models

---

## 1. Metric Audit & Provenance Verification

| Metric | UI Screen | Model / Formulation | Inputs & Units | Assumptions | Uncertainty / Limitations | Validation Status |
|---|---|---|---|---|---|---|
| **Health Score** | Dashboard, Anatomy | Multi-domain weighted composite: $\sum (w_i \cdot \text{normalized\_metric}_i)$ | HRV RMSSD (ms), Resting HR (bpm), Fasting glucose (mg/dL), Sleep (hrs) | Linear weighting across domains; additive model | Does not account for acute illness or medication | **Estimated (Educational)** |
| **Biological Age** | Dashboard | Chronological age shifted by autonomic & metabolic deviation | Chronological age (years), RMSSD (ms), Fasting glucose (mg/dL) | Population medians: RMSSD 50ms, Glucose 95 mg/dL | Clamped to $\pm 10$ yrs deviation window | **Estimated (Heuristic)** |
| **Recovery Index** | Dashboard, Vitals | Parasympathetic rebound score ($0-100\%$) | Sleep hours, sleep efficiency, overnight HRV nadir | Quality sleep directly yields autonomic recovery | Sensitive to sensor fitment and motion artifacts | **Estimated** |
| **Glycemic Spike Forecast** | Nutrition | Postprandial excursion: $90 + (\text{Carbs} \times 0.8)$ | Carbohydrate intake (grams) | Standard mixed-meal digestion; baseline fasting 90 mg/dL | Clamped at 350 mg/dL ceiling; not continuous CGM | **Predicted (Heuristic)** |
| **Atwater Energy** | Nutrition | $(\text{C} \times 4) + (\text{P} \times 4) + (\text{F} \times 9)$ | User-reported macronutrients in grams | Standard physiological metabolizable energy coefficients | $\pm 10\%$ portion size estimation uncertainty | **Calculated** |
| **Organ System Integrity** | Anatomy | 5-system heuristic scores (Cardio, Metabolic, Nervous, Musculo, Immune) | Biomarker clusters (hs-CRP, RMSSD, fasting glucose) | Multi-organ baseline correlation | Educational visualization; not clinical pathology | **Simulated / Baseline** |
| **Academic Benchmarks** | Performance | AUROC, AUPRC, F1, Brier score statistical evaluation | Retrospective open-access cohorts (MIMIC-IV, Synthea, PhysioNet) | Historical synthetic/ICU population data | Explicitly labeled as academic research benchmarks | **Offline Benchmark (Not FDA SaMD)** |

---

## 2. Invariant Safety Guardrail Verification

1. **Non-Destructive Simulation Invariant**:
   - Confirmed: Perturbation simulations in `SimulationScreen.kt` (Sleep Debt, Caffeine Spike, Carb Surge) are rendered with a `Projected` badge and do **not** write to or overwrite measured Room database observations.
2. **Emergency Precedence Invariant**:
   - Confirmed: In `DigitalTwinRepository.kt`, when red-flag acute symptoms are identified in conversational input (e.g. "chest pain", "shortness of breath", "stroke"), reassuring routine scores are suppressed and an immediate emergency response is returned instructing the user to contact local emergency services (911 / 112).
3. **Automated Calculation Test Suite**:
   - Verified via [`HealthCalculationAuditTest.kt`](file:///mnt/D/humandigitaltwin/app/src/test/java/com/example/humandigitaltwin/HealthCalculationAuditTest.kt):
     - Zero/extreme carbohydrate inputs handled safely ($90 \text{ mg/dL} \le \text{Spike} \le 350 \text{ mg/dL}$).
     - Biological age deviation clamped within plausible bounds ($18 \text{ yrs} \le \text{BioAge} \le \text{ChronoAge} + 15$).
     - Recovery Index strictly bounded between $0\%$ and $100\%$.
     - Accurate glucose ($mg/dL \leftrightarrow mmol/L$) and temperature ($^\circ C \leftrightarrow ^\circ F$) unit conversions.
     - Bitwise mathematical determinism verified.
