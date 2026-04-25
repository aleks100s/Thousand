package com.alextos.thousand.data.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import kotlin.math.roundToInt

class AndroidDiceHapticsService(
    context: Context,
) : DiceHapticsService {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
        vibrator.cancel()
    }

    private suspend fun playPhase(
        phase: DiceHapticPhase,
        hasNextPhase: Boolean,
    ) {
        repeat(phase.pulses) { index ->
            currentCoroutineContext().ensureActive()

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    phase.activeMs,
                    phase.intensity.toAmplitude(),
                )
            )
            delay(phase.activeMs)
            if (index < phase.pulses - 1 || hasNextPhase) {
                delay(phase.pauseMs)
            }
        }
    }

    private fun Float.toAmplitude(): Int {
        return (coerceIn(0f, 1f) * MAX_AMPLITUDE)
            .roundToInt()
            .coerceIn(MIN_AMPLITUDE, MAX_AMPLITUDE)
    }

    private companion object {
        const val MIN_AMPLITUDE = 1
        const val MAX_AMPLITUDE = 255
    }
}
