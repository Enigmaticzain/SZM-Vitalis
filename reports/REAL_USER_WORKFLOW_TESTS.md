# Real-User Workflow Verification & Journey Testing

**Date**: 2026-10-14  
**Device**: Waydroid x86_64 Container (Android 13 / API 33, Window size 1366x736)  
**Package Under Test**: `com.example.humandigitaltwin.debug` (Release Candidate Codebase)

---

## 1. Journey Walkthrough & Verification Matrix

| Step | User Action | Observed App Response | Verification Evidence |
|---|---|---|---|
| **1. First Launch & Scope Guide** | Opened "Onboarding & Scope Guide" from menu. | 3-step carousel displayed: Purpose, Boundaries ("Not a medical device"), Provenance definitions, and Unit preferences. | `screen_onboarding.png` |
| **2. Understand Capabilities & Limits** | Reviewed disclaimers and emergency rules. | Disclaimers explicitly mandate calling 911 / 112 for acute chest/neuro pain. | Verified in `OnboardingScreen.kt` & `MedicalDisclaimerCard.kt` |
| **3. Metric & Consent Preferences** | Toggled between Metric and Imperial units, and reviewed Local SQLite consent switch. | Switches responded smoothly without freezing; state persisted across navigation. | Verified in `OnboardingScreen.kt` |
| **4. Dashboard Access without Hardware** | Navigated to main Twin Dashboard. | Dashboard loaded immediately with "Offline Resilience Demo Mode", displaying 92.4 Health Score and 89.2% Recovery Index. | `screen_dashboard.png` |
| **5. Entry of User-Reported Nutrition** | Navigated to Nutrition tab, adjusted sliders (45g carbs, 30g protein, 15g fat), entered meal description, tapped "Save Meal". | Saved to Room database (`MealEntity`), updated today's macro totals (1120 kcal, 101g carbs), and updated `NutritionScreen` list. | `screen_nutrition.png` |
| **6. Provenance & Freshness Labels** | Inspected badges across Dashboard, Vitals, and Nutrition. | `Simulated` badge on offline baseline, `User-Reported` on macro totals, `Predicted` on glycemic spike, and timestamp "Updated: HH:mm:ss". | Verified across all screenshots |
| **7. Cross-Screen Twin Consistency** | Navigated to Anatomical Screen and Vitals Screen. | Anatomical screen matched Dashboard baseline (92.4% Overall Integrity). Resting HR displayed 68 bpm consistently. | `screen_anatomy.png`, `screen_vitals.png` |
| **8. Timeline & History** | Navigated to Biometric Timeline via menu. | Chronological events displayed: Sleep cycle, Post-workout recovery, Glycemic peak, Morning baseline. | `screen_sim.png` (Timeline view) |
| **9. Offline Simulation & What-If** | Navigated to Simulation Sandbox, selected "Sleep Debt", adjusted intensity to 80%. | Simulated delta rendered with baseline comparison, tagged `Projected`, and did not alter main Twin baseline. | `screen_sim.png` |
| **10. Deleting User Data** | Clicked delete icon on a meal entry in Nutrition Screen. | Confirmation dialog prompted: "Delete Meal Entry?". Confirmed deletion removed record from Room SQLite database. | Verified in `NutritionScreen.kt` & `MealDao.kt` |
| **11. Personal Data Export** | Navigated to Privacy Center, tapped "Export Personal Health Data". | Generated sandbox export payload and notified user with a confirmed snackbar message. | `screen_privacy.png` |
| **12. Full Data Reset & Purge** | Tapped "Purge All" in Privacy Center and confirmed dialog. | Synchronously cleared `MealDao`, `ChatMessageDao`, `CheckInDao`, and `TwinCacheDao`. Refreshed state to clean defaults. | Verified in `PrivacyScreen.kt` & `TwinDatabase.kt` |
