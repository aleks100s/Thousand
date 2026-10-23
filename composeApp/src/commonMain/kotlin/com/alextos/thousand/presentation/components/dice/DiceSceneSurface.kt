package com.alextos.thousand.presentation.components.dice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alextos.thousand.domain.service.DiceHapticsService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import thousand.composeapp.generated.resources.Res

/** Resource, lifecycle, timeout and haptic effects are isolated from the game layout. */
@Composable
fun DiceSceneSurface(
    coordinator: DiceSceneCoordinator,
    state: DicePresentationState,
    configuration: DiceSceneConfiguration,
    modifier: Modifier = Modifier,
) {
    var html by remember { mutableStateOf<String?>(null) }
    val haptics = koinInject<DiceHapticsService>()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, coordinator) {
        val observer = LifecycleEventObserver { _, _ ->
            coordinator.setActive(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        }
        lifecycle.addObserver(observer)
        coordinator.setActive(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose {
            lifecycle.removeObserver(observer)
            coordinator.setActive(false)
            haptics.cancel()
        }
    }
    LaunchedEffect(coordinator) {
        try {
            html = Res.readBytes("files/dice-scene/index.html").decodeToString()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            coordinator.fail()
        }
    }
    LaunchedEffect(state.ready, state.phase == DiceScenePhase.Fallback, state.active) {
        if (state.active && !state.ready && state.phase != DiceScenePhase.Fallback) {
            delay(5_000)
            coordinator.fail()
        }
    }
    LaunchedEffect(state.request?.presentationId, state.ready, state.completed, state.active) {
        if (state.ready && !state.completed && state.active) {
            delay(3_000)
            coordinator.fail()
        }
    }
    LaunchedEffect(state.phase, state.request?.presentationId, state.active) {
        try {
            if (state.phase == DiceScenePhase.Animating && state.active && state.request?.animate == true) {
                haptics.playDiceRollSequence()
                awaitCancellation()
            }
        } finally {
            haptics.cancel()
        }
    }
    if (state.phase != DiceScenePhase.Fallback) {
        html?.let { DiceSceneView(it, configuration, coordinator::onEvent, modifier) }
    }
}
