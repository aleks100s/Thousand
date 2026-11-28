package com.alextos.thousand.presentation.menu.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.application.theme.AppBackground
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.service.AchievementService
import com.alextos.thousand.presentation.components.appPanel
import com.alextos.thousand.presentation.models.formatForUi
import kotlin.time.Instant
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.achievements_title
import thousand.composeapp.generated.resources.achievement_hidden
import thousand.composeapp.generated.resources.achievement_hidden_condition
import thousand.composeapp.generated.resources.achievement_unlocked_at
import thousand.composeapp.generated.resources.lock_24px

@Composable
fun AchievementsScreen(onGoBack: () -> Unit) {
    val service: AchievementService = koinInject()
    val unlocked by service.unlockedAchievements.collectAsStateWithLifecycle(emptyMap())

    Screen(
        modifier = Modifier,
        title = stringResource(Res.string.achievements_title),
        goBack = onGoBack,
        constrainListWidth = true,
    ) { modifier ->
        AchievementsContent(
            modifier = modifier,
            unlocked = unlocked,
        )
    }
}

@Composable
private fun AchievementsContent(
    modifier: Modifier,
    unlocked: Map<AchievementId, Instant>,
) {
    val achievements = remember(unlocked) {
        AchievementCatalog.all.sortedBy { achievement ->
            when {
                achievement.id in unlocked -> 0
                achievement.visibility == AchievementVisibility.Public -> 1
                else -> 2
            }
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(achievements, key = { it.id.storageId }) { achievement ->
            val unlockedAt = unlocked[achievement.id]
            AchievementCard(achievement, unlockedAt)
        }
    }
}

@Composable
internal fun AchievementCard(
    achievement: AchievementDefinition,
    unlockedAt: Instant?,
) {
    val isHidden = achievement.visibility == AchievementVisibility.Hidden && unlockedAt == null
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Panel,
        color = Color.Transparent,
        contentColor = AppColors.TextPrimary,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .appPanel(outline = AppColors.OutlineSubtle)
                .drawWithContent {
                    drawContent()
                    if (unlockedAt == null) drawRect(Color.Black.copy(alpha = 0.30f))
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(achievement.id.image.resource),
                contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (isHidden) {
                        stringResource(Res.string.achievement_hidden)
                    } else {
                        stringResource(achievement.title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                if (isHidden) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.lock_24px),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.TextSecondary,
                        )
                        Text(
                            text = stringResource(Res.string.achievement_hidden_condition),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(achievement.description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                    unlockedAt?.let {
                        Text(
                            text = stringResource(
                                Res.string.achievement_unlocked_at,
                                it.formatForUi(),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.Highlight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AchievementList(unlocked: Map<AchievementId, Instant>) {
    val achievements = remember(unlocked) {
        AchievementCatalog.all.sortedBy { achievement ->
            when {
                achievement.id in unlocked -> 0
                achievement.visibility == AchievementVisibility.Public -> 1
                else -> 2
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        achievements.forEach { achievement ->
            AchievementCard(achievement, unlocked[achievement.id])
        }
    }
}

@Preview
@Composable
private fun AchievementsPreview() {
    ThousandTheme {
        AppBackground {
            AchievementsContent(
                modifier = Modifier,
                unlocked = emptyMap(),
            )
        }
    }
}

@Preview
@Composable
private fun HiddenAchievementPreview() {
    ThousandTheme {
        AppBackground {
            AchievementCard(
                achievement = AchievementCatalog.all.first { it.visibility == AchievementVisibility.Hidden },
                unlockedAt = null,
            )
        }
    }
}
