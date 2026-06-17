package com.alextos.thousand.application

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.presentation.components.LogoView
import com.alextos.thousand.presentation.components.RollingDieView
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen(
    modifier: Modifier = Modifier,
) {
    var isLogoVisible by remember { mutableStateOf(false) }
    val dice = remember {
        List(LAUNCH_DICE_COUNT) { index ->
            Die(id = index.toLong(), value = DieValue.ONE)
        }
    }
    val density = LocalDensity.current
    val dieStepPx = with(density) {
        (LAUNCH_DIE_SIZE + LAUNCH_DIE_SPACING).toPx()
    }
    val logoInitialOffsetPx = with(density) {
        LAUNCH_LOGO_INITIAL_OFFSET.toPx()
    }
    val diceCollapseProgress by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
    )
    val diceAlpha by animateFloatAsState(
        targetValue = if (isLogoVisible) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
    )

    LaunchedEffect(Unit) {
        delay(LAUNCH_DICE_ROLL_DURATION_MILLIS)
        isLogoVisible = true
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.graphicsLayer {
                alpha = diceAlpha
            },
            horizontalArrangement = Arrangement.spacedBy(LAUNCH_DIE_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dice.forEachIndexed { index, die ->
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = (LAUNCH_DICE_CENTER_INDEX - index) *
                            dieStepPx *
                            diceCollapseProgress
                    },
                ) {
                    RollingDieView(
                        die = die,
                        delay = (index + 1) * LAUNCH_DICE_STOP_INTERVAL_MILLIS,
                    )
                }
            }
        }

        LogoView(
            modifier = Modifier
                .graphicsLayer {
                    alpha = logoAlpha
                    translationY = -logoInitialOffsetPx * (1f - logoAlpha)
                }
        )
    }
}

private const val LAUNCH_DICE_COUNT = 5
private const val LAUNCH_DICE_CENTER_INDEX = 2
private const val LAUNCH_DICE_STOP_INTERVAL_MILLIS = 200L
private const val LAUNCH_DICE_ROLL_DURATION_MILLIS = 1_000L
private val LAUNCH_DIE_SIZE: Dp = 48.dp
private val LAUNCH_DIE_SPACING: Dp = 12.dp
private val LAUNCH_LOGO_INITIAL_OFFSET: Dp = 48.dp
