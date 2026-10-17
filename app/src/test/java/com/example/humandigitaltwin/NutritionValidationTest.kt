package com.example.humandigitaltwin

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for nutrition input validation and macro calculation logic.
 * These tests operate on pure Kotlin logic without Android dependencies.
 */
class NutritionValidationTest {

    // ── Calorie calculation ──────────────────────────────────────────────────

    @Test
    fun `calories from macros calculated correctly`() {
        val carbs = 60.0   // g
        val protein = 30.0 // g
        val fat = 20.0     // g
        // Atwater: carbs × 4, protein × 4, fat × 9
        val expected = (carbs * 4 + protein * 4 + fat * 9).toInt()
        val actual = calcCaloriesFromMacros(carbs, protein, fat)
        assertEquals(expected, actual)
    }

    @Test
    fun `zero macros yields zero calories`() {
        assertEquals(0, calcCaloriesFromMacros(0.0, 0.0, 0.0))
    }

    @Test
    fun `negative macro values are clamped to zero`() {
        val calories = calcCaloriesFromMacros(-10.0, 30.0, 10.0)
        // Negative carbs should be treated as 0
        val expected = calcCaloriesFromMacros(0.0, 30.0, 10.0)
        assertEquals(expected, calories)
    }

    // ── Quantity validation ──────────────────────────────────────────────────

    @Test
    fun `valid quantity passes validation`() {
        assertTrue(isValidQuantity("150"))
    }

    @Test
    fun `zero quantity fails validation`() {
        assertFalse(isValidQuantity("0"))
    }

    @Test
    fun `negative quantity fails validation`() {
        assertFalse(isValidQuantity("-50"))
    }

    @Test
    fun `blank quantity fails validation`() {
        assertFalse(isValidQuantity(""))
    }

    @Test
    fun `non-numeric quantity fails validation`() {
        assertFalse(isValidQuantity("abc"))
    }

    @Test
    fun `quantity above maximum fails validation`() {
        // Single food item > 5 kg (5000 g) is unrealistic
        assertFalse(isValidQuantity("5001"))
    }

    // ── Macro range validation ───────────────────────────────────────────────

    @Test
    fun `macro within normal range passes`() {
        assertTrue(isValidMacroGrams("45"))
    }

    @Test
    fun `macro of zero is valid for fat-free food`() {
        assertTrue(isValidMacroGrams("0"))
    }

    @Test
    fun `macro above 999g fails`() {
        assertFalse(isValidMacroGrams("1000"))
    }

    @Test
    fun `negative macro fails`() {
        assertFalse(isValidMacroGrams("-1"))
    }

    // ── Glycemic estimate ────────────────────────────────────────────────────

    @Test
    fun `high-carb meal produces high glycemic estimate tag`() {
        val tag = glycemicImpactLabel(carbsG = 80.0, fiberG = 2.0)
        assertEquals("High", tag)
    }

    @Test
    fun `low-carb meal produces low glycemic estimate tag`() {
        val tag = glycemicImpactLabel(carbsG = 10.0, fiberG = 5.0)
        assertEquals("Low", tag)
    }

    @Test
    fun `medium-carb meal produces moderate glycemic estimate tag`() {
        val tag = glycemicImpactLabel(carbsG = 35.0, fiberG = 4.0)
        assertEquals("Moderate", tag)
    }

    // ── Helpers under test (pure Kotlin, no Android) ─────────────────────────

    private fun calcCaloriesFromMacros(carbs: Double, protein: Double, fat: Double): Int {
        val c = maxOf(carbs, 0.0)
        val p = maxOf(protein, 0.0)
        val f = maxOf(fat, 0.0)
        return ((c * 4) + (p * 4) + (f * 9)).toInt()
    }

    private fun isValidQuantity(input: String): Boolean {
        val v = input.toDoubleOrNull() ?: return false
        return v > 0 && v <= 5000
    }

    private fun isValidMacroGrams(input: String): Boolean {
        val v = input.toDoubleOrNull() ?: return false
        return v >= 0 && v < 1000
    }

    /** Heuristic glycemic impact label: net carbs = carbs - fiber */
    private fun glycemicImpactLabel(carbsG: Double, fiberG: Double): String {
        val netCarbs = maxOf(carbsG - fiberG, 0.0)
        return when {
            netCarbs >= 60 -> "High"
            netCarbs >= 25 -> "Moderate"
            else -> "Low"
        }
    }
}
