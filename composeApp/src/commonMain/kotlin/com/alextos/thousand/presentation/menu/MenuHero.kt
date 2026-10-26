package com.alextos.thousand.presentation.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

private data class FloatingDie(
    val image: DrawableResource,
    val x: Float,
    val y: Float,
    val size: Float,
    val amplitude: Float,
    val durationMillis: Int,
    val phaseMillis: Int,
)

private val floatingDice = listOf(
    FloatingDie(Res.drawable.main_die_1, .17f, .48f, .19f, .17f, 1650, 0),
    FloatingDie(Res.drawable.main_die_2, .35f, .36f, .135f, .23f, 2300, 760),
    FloatingDie(Res.drawable.main_die_3, .52f, .48f, .17f, .21f, 1950, 1200),
    FloatingDie(Res.drawable.main_die_4, .70f, .33f, .12f, .24f, 2850, 430),
    FloatingDie(Res.drawable.main_die_5, .84f, .52f, .15f, .19f, 3250, 1900),
)

/** Decorative artwork only: cached images move in the draw phase, without relayout per frame. */
@Composable
internal fun MenuHero(modifier: Modifier = Modifier) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    BoxWithConstraints(modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        if (maxHeight < 48.dp || maxWidth < 48.dp) return@BoxWithConstraints
        val width = minOf(maxWidth - 24.dp, maxHeight * (360f / 280f))
        val height = width * (280f / 360f)
        val transition = if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
            rememberInfiniteTransition(label = "Floating menu dice")
        } else null
        Box(Modifier.size(width, height)) {
            Image(
                painterResource(Res.drawable.table), contentDescription = null,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().aspectRatio(1199f / 401f),
                contentScale = ContentScale.Fit,
            )
            floatingDice.forEach { die ->
                key(die.image) {
                    val movement = transition?.animateFloat(
                        initialValue = -1f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(die.durationMillis, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(die.phaseMillis, StartOffsetType.FastForward),
                        ),
                        label = "Die ${die.durationMillis}",
                    )
                    val side = width * die.size
                    Image(
                        painterResource(die.image), contentDescription = null,
                        modifier = Modifier.offset(width * die.x - side / 2, height * die.y - side / 2)
                            .size(side).graphicsLayer {
                                translationY = (movement?.value ?: 0f) * (height * die.amplitude).toPx()
                            },
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}
