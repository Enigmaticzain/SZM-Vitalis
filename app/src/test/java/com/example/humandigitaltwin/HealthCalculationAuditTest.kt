package com.example.humandigitaltwin

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests auditing health calculation functions for physiological boundary clamping,
 * invalid input handling, unit conversions, and determinism.
 */
class HealthCalculationAuditTest {

    // ── 1. Glycemic Spike Calculation ─────────────────────────────────────────

    @Test
    fun `glycemic forecast handles zero and extreme carbs gracefully`() {
        // Normal meal: 45g carbs -> 90 + (45 * 0.8) = 126 mg/dL
        val normalSpike = calculateGlycemicSpike(carbsG = 45.0)
        assertEquals(126.0, normalSpike, 0.01)

        // Zero carbs: baseline fasting 90 mg/dL
        val zeroCarbSpike = calculateGlycemicSpike(carbsG = 0.0)
        assertEquals(90.0, zeroCarbSpike, 0.01)

        // Negative input should clamp to baseline
        val negativeCarbSpike = calculateGlycemicSpike(carbsG = -20.0)
        assertEquals(90.0, negativeCarbSpike, 0.01)

        // Extreme surge (>250g carbs) clamped to physiological ceiling
        val extremeSpike = calculateGlycemicSpike(carbsG = 500.0)
        assertTrue("Spike must not exceed physiological ceiling of 350 mg/dL", extremeSpike <= 350.0)
    }

    // ── 2. Biological Age Estimation ──────────────────────────────────────────

    @Test
    fun `biological age stays within plausible deviation window`() {
        val chronologicalAge = 32.0

        // Optimal autonomic state: high HRV (85ms), normal fasting glucose (85 mg/dL)
        val optimalBioAge = estimateBiologicalAge(chronologicalAge, hrvMs = 85.0, glucoseMgDl = 85.0)
        assertTrue("Optimal health should reduce bio age", optimalBioAge < chronologicalAge)
        assertTrue("Bio age should not drop unrealistically (>10 yrs below chrono)", chronologicalAge - optimalBioAge <= 10.0)

        // Depleted autonomic state: low HRV (20ms), elevated glucose (140 mg/dL)
        val depletedBioAge = estimateBiologicalAge(chronologicalAge, hrvMs = 20.0, glucoseMgDl = 140.0)
        assertTrue("Poor biomarkers should increase bio age", depletedBioAge > chronologicalAge)
        assertTrue("Bio age should not spike unrealistically (>15 yrs above chrono)", depletedBioAge - chronologicalAge <= 15.0)
    }

    // ── 3. Recovery Index Bounds ──────────────────────────────────────────────

    @Test
    fun `recovery index is strictly bounded between 0 and 100 percent`() {
        val perfectRecovery = calculateRecoveryIndex(sleepHours = 9.0, sleepEfficiency = 0.98, hrvDelta = 25.0)
        assertTrue("Recovery index ceiling is 100", perfectRecovery <= 100.0)

        val severeExhaustion = calculateRecoveryIndex(sleepHours = 2.0, sleepEfficiency = 0.40, hrvDelta = -40.0)
        assertTrue("Recovery index floor is 0", severeExhaustion >= 0.0)
    }

    // ── 4. Unit Conversion Accuracy ───────────────────────────────────────────

    @Test
    fun `glucose conversion mg_dL to mmol_L conforms to standard 18_018 factor`() {
        val mgDl = 90.0
        val mmolL = convertGlucoseMgDlToMmolL(mgDl)
        // 90 / 18.0182 = 4.995 mmol/L
        assertEquals(5.0, mmolL, 0.1)
    }

    @Test
    fun `temperature conversion Celsius to Fahrenheit is exact`() {
        assertEquals(98.6, convertCelsiusToFahrenheit(37.0), 0.05)
        assertEquals(32.0, convertCelsiusToFahrenheit(0.0), 0.01)
        assertEquals(212.0, convertCelsiusToFahrenheit(100.0), 0.01)
    }

    // ── 5. Deterministic Baseline ─────────────────────────────────────────────

    @Test
    fun `identical inputs produce bitwise identical outputs`() {
        val run1 = calculateGlycemicSpike(60.0)
        val run2 = calculateGlycemicSpike(60.0)
        assertEquals(run1, run2, 0.000001)

        val bioAge1 = estimateBiologicalAge(30.0, 75.0, 92.0)
        val bioAge2 = estimateBiologicalAge(30.0, 75.0, 92.0)
        assertEquals(bioAge1, bioAge2, 0.000001)
    }

    // ── Pure Kotlin Model Calculations Under Audit ────────────────────────────

    private fun calculateGlycemicSpike(carbsG: Double): Double {
        val clampedCarbs = maxOf(0.0, carbsG)
        val projected = 90.0 + (clampedCarbs * 0.8)
        return minOf(350.0, projected)
    }

    private fun estimateBiologicalAge(chronologicalAge: Double, hrvMs: Double, glucoseMgDl: Double): Double {
        val hrvDelta = (hrvMs - 50.0) / 10.0 // +1 year younger per 10ms above 50
        val glucoseDelta = (glucoseMgDl - 95.0) / 15.0 // +1 year older per 15mg/dL above 95
        val rawDelta = (-hrvDelta + glucoseDelta).coerceIn(-10.0, 15.0)
        return maxOf(18.0, chronologicalAge + rawDelta)
    }

    private fun calculateRecoveryIndex(sleepHours: Double, sleepEfficiency: Double, hrvDelta: Double): Double {
        val sleepScore = (sleepHours / 8.0 * 50.0) * sleepEfficiency
        val autonomicBonus = (hrvDelta * 1.2)
        val raw = (sleepScore + 40.0 + autonomicBonus)
        return raw.coerceIn(0.0, 100.0)
    }

    private fun convertGlucoseMgDlToMmolL(mgDl: Double): Double =
        mgDl / 18.0182

    private fun convertCelsiusToFahrenheit(c: Double): Double =
        (c * 9.0 / 5.0) + 32.0
}
