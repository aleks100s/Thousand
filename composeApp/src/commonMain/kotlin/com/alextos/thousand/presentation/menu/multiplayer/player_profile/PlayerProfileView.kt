package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.theme.AppBackground
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.AchievementService
import com.alextos.thousand.presentation.menu.achievements.AchievementList
import com.alextos.thousand.presentation.components.PlayerAvatarView
import com.alextos.thousand.presentation.components.AppPanel
import org.koin.compose.koinInject

@Composable
fun PlayerProfileView(
    modifier: Modifier = Modifier,
    user: User? = null,
) {
    val achievementService: AchievementService = koinInject()
    val unlocked by achievementService.unlockedAchievements.collectAsStateWithLifecycle(emptyMap())
    val profile = user ?: User(id = "local-profile", name = "Игрок", kind = UserKind.MainUser)
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "profile") {
            ProfileIdentityCard(profile)
        }
        item(key = "achievements-title") {
            Text("Достижения", color = AppColors.TextPrimary, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
        }
        item(key = "achievements") {
            AchievementList(unlocked)
        }
    }
}

@Composable
private fun ProfileIdentityCard(user: User) {
    AppPanel(modifier = Modifier.fillMaxWidth(), padding = 20.dp) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlayerAvatarView(user, size = 96.dp)
            Text(user.name, color = AppColors.TextPrimary, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
internal fun PlayerProfilePreview() {
    ThousandTheme {
        AppBackground {
            com.alextos.thousand.common.Screen(Modifier, title = "Профиль игрока", goBack = {}, constrainListWidth = true) {
                PlayerProfileView(modifier = it,
                    user = User("profile-preview", "Маша", UserKind.MainUser,
                        avatar = PlayerAvatar.Female))
            }
        }
    }
}
