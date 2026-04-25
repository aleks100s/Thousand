package com.alextos.thousand.data.service

import com.alextos.thousand.domain.service.DiceHapticPattern
import com.alextos.thousand.domain.service.DiceHapticPhase
import com.alextos.thousand.domain.service.DiceHapticsService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class IosDiceHapticsService : DiceHapticsService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentJob: Job? = null

    override suspend fun playDiceRollSequence(count: Int) {
        cancel()
        val job = scope.launch(start = CoroutineStart.LAZY) {
            val phases = DiceHapticPattern.phasesForDiceCount(count)
            phases.forEachIndexed { index, phase ->
                playPhase(
                    phase = phase,
                    hasNextPhase = index < phases.lastIndex,
                )
            }
        }
        currentJob = job
        job.invokeOnCompletion {
            if (currentJob == job) {
                currentJob = null
            }
        }
        job.start()
    }

    override fun cancel() {
        currentJob?.cancel()
        currentJob = null
    }

    private suspend fun playPhase(
        phase: DiceHapticPhase,
        hasNextPhase: Boolean,
    ) {
        val generator = UIImpactFeedbackGenerator(style = phase.intensity.toImpactStyle())

        repeat(phase.pulses) { index ->
            currentCoroutineContext().ensureActive()

            generator.prepare()
            generator.impactOccurredWithIntensity(phase.intensity.coerceIn(0f, 1f).toDouble())
            delay(phase.activeMs)
            if (index < phase.pulses - 1 || hasNextPhase) {
                delay(phase.pauseMs)
            }
        }
    }

    private fun Float.toImpactStyle(): UIImpactFeedbackStyle {
        return when {
            this < 0.45f -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
            this < 0.75f -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
            else -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
        }
    }
}
