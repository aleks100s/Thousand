package com.alextos.thousand.domain.service

interface DiceHapticsService {
    suspend fun playDiceRollSequence(count: Int)

    fun cancel()
}

data class DiceHapticPhase(
    val activeMs: Long,
    val pauseMs: Long,
    val pulses: Int,
    val intensity: Float,
)

object DiceHapticPattern {
    val phases = listOf(
        DiceHapticPhase(activeMs = 18, pauseMs = 32, pulses = 5, intensity = 0.35f),
        DiceHapticPhase(activeMs = 22, pauseMs = 40, pulses = 4, intensity = 0.47f),
        DiceHapticPhase(activeMs = 30, pauseMs = 53, pulses = 3, intensity = 0.63f),
        DiceHapticPhase(activeMs = 45, pauseMs = 80, pulses = 2, intensity = 0.78f),
        DiceHapticPhase(activeMs = 70, pauseMs = 180, pulses = 1, intensity = 1.00f),
    )

    fun phasesForDiceCount(count: Int): List<DiceHapticPhase> {
        val phaseCount = count.coerceIn(0, phases.size)
        if (phaseCount == 0) return emptyList()
        return phases.takeLast(phaseCount)
    }
}
