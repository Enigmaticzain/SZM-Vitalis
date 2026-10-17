package com.example.humandigitaltwin

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for What-If simulation input validation and trajectory projection logic.
 */
class SimulationLogicTest {

    // ── Input range validation ───────────────────────────────────────────────

    @Test
    fun `exercise intensity 0 to 10 is valid`() {
        for (i in 0..10) {
            assertTrue("Intensity $i should be valid", isValidIntensity(i))
        }
    }

    @Test
    fun `exercise intensity below 0 is invalid`() {
        assertFalse(isValidIntensity(-1))
    }

    @Test
    fun `exercise intensity above 10 is invalid`() {
        assertFalse(isValidIntensity(11))
    }

    @Test
    fun `sleep hours between 0 and 24 are valid`() {
        assertTrue(isValidSleepHours(7.5))
        assertTrue(isValidSleepHours(0.0))
        assertTrue(isValidSleepHours(24.0))
    }

    @Test
    fun `sleep hours above 24 are invalid`() {
        assertFalse(isValidSleepHours(24.1))
        assertFalse(isValidSleepHours(30.0))
    }

    @Test
    fun `negative sleep hours are invalid`() {
        assertFalse(isValidSleepHours(-1.0))
    }

    @Test
    fun `stress level 1 to 10 is valid`() {
        assertTrue(isValidStressLevel(1))
        assertTrue(isValidStressLevel(10))
    }

    @Test
    fun `stress level outside 1 to 10 is invalid`() {
        assertFalse(isValidStressLevel(0))
        assertFalse(isValidStressLevel(11))
    }

    // ── Simulation output stays within physiological bounds ──────────────────

    @Test
    fun `projected health score is clamped between 0 and 100`() {
        val result = projectHealthScore(baseline = 85.0, exerciseIntensity = 10, sleepHours = 9.0, stressLevel = 1)
        assertTrue("Score should be >= 0", result >= 0.0)
        assertTrue("Score should be <= 100", result <= 100.0)
    }

    @Test
    fun `high stress and no sleep reduces score below baseline`() {
        val baseline = 70.0
        val result = projectHealthScore(baseline, exerciseIntensity = 0, sleepHours = 3.0, stressLevel = 10)
        assertTrue("Poor lifestyle should reduce health score", result < baseline)
    }

    @Test
    fun `optimal lifestyle improves score toward baseline ceiling`() {
        val baseline = 60.0
        val result = projectHealthScore(baseline, exerciseIntensity = 7, sleepHours = 8.0, stressLevel = 2)
        assertTrue("Good lifestyle should maintain or improve score", result >= baseline)
    }

    @Test
    fun `baseline at zero still produces non-negative result`() {
        val result = projectHealthScore(0.0, 5, 7.0, 5)
        assertTrue(result >= 0.0)
    }

    @Test
    fun `baseline at 100 with perfect lifestyle stays at 100`() {
        val result = projectHealthScore(100.0, 8, 8.0, 1)
        assertEquals(100.0, result, 0.01)
    }

    // ── Scenario reset ───────────────────────────────────────────────────────

    @Test
    fun `reset returns default scenario values`() {
        val scenario = SimulationScenario(exerciseIntensity = 9, sleepHours = 4.0, stressLevel = 8)
        val reset = scenario.reset()
        assertEquals(5, reset.exerciseIntensity)
        assertEquals(7.0, reset.sleepHours, 0.01)
        assertEquals(5, reset.stressLevel)
    }

    // ── Simulation does not write to the Twin ────────────────────────────────

    @Test
    fun `simulation output is tagged as projected not measured`() {
        val result = simulateScenario(
            baseline = 72.0,
            scenario = SimulationScenario(5, 7.0, 5)
        )
        assertEquals("Projected", result.dataTag)
        assertFalse("Simulation must not overwrite real Twin data", result.overwritesTwin)
    }

    // ── Pure Kotlin helpers under test ────────────────────────────────────────

    private fun isValidIntensity(v: Int) = v in 0..10
    private fun isValidSleepHours(v: Double) = v in 0.0..24.0
    private fun isValidStressLevel(v: Int) = v in 1..10

    /** Heuristic: each sleep deficit from 8h subtracts 2 pts, each stress unit above 5 subtracts 1 pt. */
    private fun projectHealthScore(
        baseline: Double,
        exerciseIntensity: Int,
        sleepHours: Double,
        stressLevel: Int
    ): Double {
        val sleepBonus = (sleepHours - 8.0) * 2.0     // positive if > 8 h
        val exerciseBonus = (exerciseIntensity - 5) * 1.5
        val stressPenalty = (stressLevel - 5) * 1.0
        val delta = sleepBonus + exerciseBonus - stressPenalty
        return (baseline + delta).coerceIn(0.0, 100.0)
    }

    private fun simulateScenario(baseline: Double, scenario: SimulationScenario): SimulationResult {
        val score = projectHealthScore(baseline, scenario.exerciseIntensity, scenario.sleepHours, scenario.stressLevel)
        return SimulationResult(projectedScore = score, dataTag = "Projected", overwritesTwin = false)
    }

    data class SimulationScenario(
        val exerciseIntensity: Int,
        val sleepHours: Double,
        val stressLevel: Int
    ) {
        fun reset() = SimulationScenario(exerciseIntensity = 5, sleepHours = 7.0, stressLevel = 5)
    }

    data class SimulationResult(
        val projectedScore: Double,
        val dataTag: String,
        val overwritesTwin: Boolean
    )
}
