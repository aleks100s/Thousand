package com.alextos.thousand.presentation.menu.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.service.AchievementService
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.achievement_unlocked_capsule
import thousand.composeapp.generated.resources.statistics_emoji_events

@Stable
private class AchievementCapsuleState {
    var achievement by mutableStateOf<AchievementId?>(null)
    var hostedInResultDialog by mutableStateOf(false)
}

private val LocalAchievementCapsule = staticCompositionLocalOf<AchievementCapsuleState?> { null }

@Composable
internal fun AchievementCapsuleLayer(content: @Composable () -> Unit) {
    val service: AchievementService = koinInject()
    val state = remember { AchievementCapsuleState() }

    LaunchedEffect(service) {
        service.unlockNotifications.collect { achievement ->
            state.achievement = achievement
            delay(4_000)
            state.achievement = null
        }
    }

    CompositionLocalProvider(LocalAchievementCapsule provides state) {
        content()
        if (!state.hostedInResultDialog) {
            state.achievement?.let { AchievementUnlockCapsule(it) }
        }
    }
}

@Composable
internal fun AchievementCapsuleInResultDialog() {
    val state = LocalAchievementCapsule.current ?: return
    DisposableEffect(state) {
        state.hostedInResultDialog = true
        onDispose { state.hostedInResultDialog = false }
    }
    state.achievement?.let { AchievementUnlockCapsule(it) }
}

@Composable
private fun AchievementUnlockCapsule(achievement: AchievementId) {
    val density = LocalDensity.current
    val top = WindowInsets.safeDrawing.getTop(density) + with(density) { 12.dp.roundToPx() }
    val position = remember(top) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = IntOffset((windowSize.width - popupContentSize.width) / 2, top)
        }
    }
    Popup(
        popupPositionProvider = position,
        properties = PopupProperties(focusable = false),
    ) {
        AchievementUnlockCapsuleContent(achievement)
    }
}

@Composable
private fun AchievementUnlockCapsuleContent(achievement: AchievementId) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .background(AppColors.SurfaceRaised, RoundedCornerShape(28.dp))
            .border(1.dp, AppColors.Highlight.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.statistics_emoji_events),
            contentDescription = null,
            tint = AppColors.Highlight,
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(Res.string.achievement_unlocked_capsule),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary,
            )
            Text(
                text = AchievementCatalog.title(achievement),
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.TextPrimary,
            )
        }
    }
}
