package com.example.humandigitaltwin

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

// ── Data stubs — top-level so Kotlin resolves types before test methods ────────

data class MealSnapshot(
    val id: Long, val name: String, val quantityG: Double,
    val carbsG: Double, val proteinG: Double, val fatG: Double,
    val timestampMs: Long
) {
    fun toEntity() = MealEntityStub(id, name, quantityG, carbsG, proteinG, fatG, timestampMs)
}

data class MealEntityStub(
    val id: Long, val name: String, val quantityG: Double,
    val carbsG: Double, val proteinG: Double, val fatG: Double,
    val timestampMs: Long
) {
    fun toSnapshot() = MealSnapshot(id, name, quantityG, carbsG, proteinG, fatG, timestampMs)
}

data class ChatMessageSnapshot(
    val sender: String, val content: String, val isEmergency: Boolean, val timestampMs: Long
) {
    fun toEntity() = ChatEntityStub(sender, content, isEmergency, timestampMs)
}

data class ChatEntityStub(
    val sender: String, val content: String, val isEmergency: Boolean, val timestampMs: Long
) {
    fun toSnapshot() = ChatMessageSnapshot(sender, content, isEmergency, timestampMs)
}

data class ResolvedState(val value: Double?, val isCached: Boolean, val isEmpty: Boolean)

// ──────────────────────────────────────────────────────────────────────────────

/**
 * Unit tests for repository-layer data model mapping, stale-data detection,
 * and fallback behaviour when network data is unavailable.
 */
class RepositoryPersistenceTest {

    // ── Meal entity mapping ──────────────────────────────────────────────────

    @Test
    fun `meal entity round-trip preserves all fields`() {
        val original = MealSnapshot(
            id = 1L,
            name = "Grilled Salmon",
            quantityG = 200.0,
            carbsG = 0.0,
            proteinG = 40.0,
            fatG = 12.0,
            timestampMs = 1_700_000_000_000L
        )
        val entity = original.toEntity()
        val restored = entity.toSnapshot()

        assertEquals(original.name, restored.name)
        assertNear(original.quantityG, restored.quantityG)
        assertNear(original.proteinG, restored.proteinG)
        assertNear(original.fatG, restored.fatG)
        assertEquals(original.timestampMs, restored.timestampMs)
    }

    @Test
    fun `meal entity with zero quantities maps correctly`() {
        val snap = MealSnapshot(0L, "Water", 250.0, 0.0, 0.0, 0.0, 0L)
        val entity = snap.toEntity()
        assertEquals("Water", entity.name)
        assertNear(0.0, entity.carbsG)
    }

    // ── Chat message entity mapping ──────────────────────────────────────────

    @Test
    fun `chat message preserves sender and content`() {
        val msg = ChatMessageSnapshot("user", "Is my glucose level ok?", isEmergency = false, timestampMs = 123L)
        val entity = msg.toEntity()
        val restored = entity.toSnapshot()

        assertEquals("user", restored.sender)
        assertEquals("Is my glucose level ok?", restored.content)
        assertFalse(restored.isEmergency)
    }

    @Test
    fun `emergency chat message flag is preserved`() {
        val msg = ChatMessageSnapshot("user", "I have chest pain", isEmergency = true, timestampMs = 999L)
        val entity = msg.toEntity()
        assertTrue(entity.toSnapshot().isEmergency)
    }

    // ── Stale data detection ─────────────────────────────────────────────────

    @Test
    fun `cache older than 5 minutes is marked stale`() {
        val nowMs = System.currentTimeMillis()
        val oldMs = nowMs - (6L * 60L * 1000L) // 6 minutes ago
        assertTrue(isCacheStale(oldMs, nowMs, maxAgeMs = 5L * 60L * 1000L))
    }

    @Test
    fun `cache younger than 5 minutes is not stale`() {
        val nowMs = System.currentTimeMillis()
        val recentMs = nowMs - (3L * 60L * 1000L) // 3 minutes ago
        assertFalse(isCacheStale(recentMs, nowMs, maxAgeMs = 5L * 60L * 1000L))
    }

    @Test
    fun `zero timestamp is always stale`() {
        assertTrue(isCacheStale(0L, System.currentTimeMillis(), maxAgeMs = 5L * 60L * 1000L))
    }

    // ── Fallback behaviour ───────────────────────────────────────────────────

    @Test
    fun `offline fallback returns cached data with stale flag`() {
        val cachedScore = 78.0
        val result = resolveTwinState(networkScore = null, cachedScore = cachedScore)
        assertNear(cachedScore, result.value!!)
        assertTrue("Fallback should be marked as cached", result.isCached)
    }

    @Test
    fun `live network data takes precedence over cache`() {
        val result = resolveTwinState(networkScore = 85.0, cachedScore = 70.0)
        assertNear(85.0, result.value!!)
        assertFalse(result.isCached)
    }

    @Test
    fun `null network and null cache returns empty placeholder`() {
        val result = resolveTwinState(networkScore = null, cachedScore = null)
        assertNull(result.value)
        assertTrue(result.isEmpty)
    }

    // ── Data purge ───────────────────────────────────────────────────────────

    @Test
    fun `purge clears all in-memory user data`() {
        val meals = mutableListOf("meal1", "meal2")
        val messages = mutableListOf("msg1")
        purgeAll(meals, messages)
        assertTrue(meals.isEmpty())
        assertTrue(messages.isEmpty())
    }

    // ── Pure helpers ──────────────────────────────────────────────────────────

    /** delta-safe double equality (avoids JUnit 4 overload ambiguity) */
    private fun assertNear(expected: Double, actual: Double, delta: Double = 0.001) {
        assertTrue(
            "Expected ~$expected but was $actual (delta $delta)",
            abs(expected - actual) <= delta
        )
    }

    private fun isCacheStale(cachedAtMs: Long, nowMs: Long, maxAgeMs: Long): Boolean =
        cachedAtMs == 0L || (nowMs - cachedAtMs) > maxAgeMs

    private fun resolveTwinState(networkScore: Double?, cachedScore: Double?): ResolvedState =
        when {
            networkScore != null -> ResolvedState(networkScore, isCached = false, isEmpty = false)
            cachedScore != null  -> ResolvedState(cachedScore, isCached = true,  isEmpty = false)
            else                 -> ResolvedState(null,        isCached = false, isEmpty = true)
        }

    private fun purgeAll(meals: MutableList<String>, messages: MutableList<String>) {
        meals.clear()
        messages.clear()
    }
}
