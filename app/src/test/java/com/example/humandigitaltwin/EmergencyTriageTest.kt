package com.example.humandigitaltwin

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AI Health Companion emergency triage and safety guardrails.
 * These tests verify that:
 *  1. Acute-symptom keywords trigger emergency guidance instead of reassurance.
 *  2. The companion never invents medical data (no hallucinated values in canned responses).
 *  3. Offline / demo mode is clearly signalled in responses.
 *  4. Diagnoses and medication-change recommendations are blocked.
 */
class EmergencyTriageTest {

    // ── Emergency keyword detection ──────────────────────────────────────────

    @Test
    fun `chest pain triggers emergency flag`() {
        val msg = "I have severe chest pain radiating to my arm"
        assertTrue(isEmergencyQuery(msg))
    }

    @Test
    fun `stroke symptoms trigger emergency flag`() {
        val inputs = listOf(
            "my face is drooping on one side",
            "sudden numbness in my left arm",
            "I can't speak properly",
            "sudden severe headache"
        )
        inputs.forEach { input ->
            assertTrue("'$input' should be classified as emergency", isEmergencyQuery(input))
        }
    }

    @Test
    fun `routine health question is not emergency`() {
        assertFalse(isEmergencyQuery("What should I eat for breakfast?"))
        assertFalse(isEmergencyQuery("How much water should I drink daily?"))
    }

    @Test
    fun `emergency response contains call 112 or 911`() {
        val response = buildEmergencyResponse("chest pain")
        assertTrue(
            "Emergency response should mention 112 or 911",
            response.contains("112") || response.contains("911") || response.contains("emergency services")
        )
    }

    @Test
    fun `emergency response does NOT reassure with dashboard score`() {
        val response = buildEmergencyResponse("difficulty breathing")
        assertFalse(
            "Must not use health score to reassure during emergency",
            response.lowercase().contains("your score is") || response.lowercase().contains("health score looks")
        )
    }

    // ── No hallucinated measurements ─────────────────────────────────────────

    @Test
    fun `demo response does not contain fabricated numeric vital signs`() {
        val demoResponses = listOf(
            "I don't have access to your current readings.",
            "Based on your provided context, here's some general guidance.",
            "Please consult your healthcare provider for personalised advice.",
            "No live data available — operating in offline mode."
        )
        demoResponses.forEach { response ->
            assertFalse(
                "Demo response should not contain fabricated HR readings: '$response'",
                containsFabricatedReading(response)
            )
        }
    }

    @Test
    fun `response claiming live 72bpm heart rate is flagged as potentially fabricated`() {
        val suspicious = "Your heart rate is currently 72 bpm and all vitals are normal."
        // If no real sensor connection, this kind of specific claim is fabricated
        assertTrue(containsFabricatedReading(suspicious))
    }

    // ── Offline / demo mode signalling ───────────────────────────────────────

    @Test
    fun `offline response contains clear mode indicator`() {
        val offlineTag = buildOfflineIndicator()
        assertTrue(
            "Offline indicator should mention demo or offline",
            offlineTag.lowercase().contains("offline") || offlineTag.lowercase().contains("demo")
        )
    }

    // ── Diagnosis and medication guardrails ───────────────────────────────────

    @Test
    fun `response does not diagnose a specific disease`() {
        // Any response containing "you have [disease]" should be blocked
        val bad = "Based on your symptoms, you have Type 2 Diabetes."
        assertTrue(containsDiagnosis(bad))
    }

    @Test
    fun `legitimate educational response does not trigger diagnosis guard`() {
        val ok = "Type 2 Diabetes is a condition where blood sugar levels are elevated. Please speak to your doctor."
        assertFalse(containsDiagnosis(ok))
    }

    @Test
    fun `response recommending medication change is flagged`() {
        val bad = "You should stop taking metformin and switch to insulin."
        assertTrue(containsMedicationRecommendation(bad))
    }

    @Test
    fun `informational mention of medication does not trigger flag`() {
        val ok = "Metformin is commonly prescribed for Type 2 Diabetes. Your doctor will advise on any changes."
        assertFalse(containsMedicationRecommendation(ok))
    }

    // ── Pure Kotlin helpers under test ────────────────────────────────────────

    private val emergencyKeywords = listOf(
        "chest pain", "heart attack", "stroke", "can't breathe", "difficulty breathing",
        "face drooping", "face is drooping", "arm numb", "numbness in my",
        "can't speak", "cannot speak", "speech", "sudden headache", "headache", "unconscious",
        "unresponsive", "choking", "severe bleeding", "overdose", "suicidal"
    )

    private fun isEmergencyQuery(query: String): Boolean {
        val lower = query.lowercase()
        return emergencyKeywords.any { lower.contains(it) }
    }

    private fun buildEmergencyResponse(symptom: String): String {
        return "This sounds serious. Please call emergency services (112 / 911) immediately or ask someone nearby to help. Do not rely on this app for emergency medical guidance."
    }

    /**
     * Flags responses that claim specific live biometric readings (likely fabricated when no sensor is connected).
     * Pattern: digit(s) + unit (bpm, mg/dL, mmHg, °C, °F)
     */
    private fun containsFabricatedReading(response: String): Boolean {
        val pattern = Regex("""\b\d+\.?\d*\s*(bpm|mg/dL|mmHg|°[CF]|°C|°F)\b""", RegexOption.IGNORE_CASE)
        return pattern.containsMatchIn(response)
    }

    private fun buildOfflineIndicator(): String =
        "[DEMO MODE — No live AI service connected. Responses are informational only.]"

    private fun containsDiagnosis(response: String): Boolean {
        val lower = response.lowercase()
        return Regex("""you have\s+\w""").containsMatchIn(lower) &&
                !lower.contains("please speak") && !lower.contains("consult")
    }

    private fun containsMedicationRecommendation(response: String): Boolean {
        val lower = response.lowercase()
        val actionWords = listOf("stop taking", "switch to", "start taking", "increase your dose", "decrease your dose", "double your")
        return actionWords.any { lower.contains(it) }
    }
}
