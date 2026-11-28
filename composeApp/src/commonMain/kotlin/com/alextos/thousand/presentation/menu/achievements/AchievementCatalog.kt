package com.alextos.thousand.presentation.menu.achievements

import androidx.compose.runtime.Composable
import com.alextos.thousand.domain.models.AchievementId
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.achievement_first_win_description
import thousand.composeapp.generated.resources.achievement_first_win_title

enum class AchievementVisibility {
    Public,
    Hidden,
}

data class AchievementDefinition(
    val id: AchievementId,
    val title: StringResource,
    val description: StringResource,
    val visibility: AchievementVisibility,
)

object AchievementCatalog {
    val all = listOf(
        AchievementDefinition(
            id = AchievementId.FirstWin,
            title = Res.string.achievement_first_win_title,
            description = Res.string.achievement_first_win_description,
            visibility = AchievementVisibility.Public,
        ),
    )

    @Composable
    fun title(id: AchievementId): String =
        stringResource(all.first { it.id == id }.title)
}
