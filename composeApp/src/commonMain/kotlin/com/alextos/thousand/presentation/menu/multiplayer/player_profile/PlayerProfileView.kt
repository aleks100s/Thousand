package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.theme.*
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.PlayerAvatar
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.AppIconBadge
import com.alextos.thousand.presentation.components.AppPanel
import com.alextos.thousand.presentation.components.PlayerAvatarView
import com.alextos.thousand.presentation.components.appPanel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

@Composable
fun PlayerProfileView(
    username: String,
    gameCount: Int? = null,
    winCount: Int? = null,
    rating: Int? = null,
    modifier: Modifier = Modifier,
    user: User? = null,
) {
    LazyColumn(
        modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "profile") {
            ProfileIdentityCard(user ?: User(username, username, UserKind.Remote), gameCount, winCount, rating)
        }
        item(key = "achievements-title") {
            Text("Достижения", color = AppColors.TextPrimary, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
        }
        item(key = "achievements") {
            AppPanel(padding = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppIconBadge(Res.drawable.statistics_emoji_events, large = true)
                    Text("В разработке", color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun ProfileIdentityCard(user: User, gameCount: Int?, winCount: Int?, rating: Int?) {
    Box(
        Modifier.fillMaxWidth().appPanel().background(Brush.linearGradient(
            listOf(AppColors.AvatarMainBottom.copy(alpha = .45f), AppColors.SurfaceInset, AppColors.Background))),
    ) {
        Icon(painterResource(Res.drawable.casino_24px), null,
            Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 8.dp).size(144.dp).rotate(-18f),
            tint = AppColors.Accent.copy(alpha = .08f))
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlayerAvatarView(user, size = 96.dp)
            Text(user.name, color = AppColors.TextPrimary, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Row(
                Modifier.fillMaxWidth().clip(AppShapes.Row).background(AppColors.Background.copy(alpha = .6f))
                    .border(AppDimens.BorderSubtle, AppColors.OutlineSubtle, AppShapes.Row).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileMetric(Modifier.weight(1f), gameCount, "Игр", Res.drawable.sports_esports_24px, AppColors.Accent)
                VerticalDivider(Modifier.height(56.dp), color = AppColors.OutlineSubtle)
                ProfileMetric(Modifier.weight(1f), winCount, "Побед", Res.drawable.statistics_emoji_events, AppColors.Purple)
                VerticalDivider(Modifier.height(56.dp), color = AppColors.OutlineSubtle)
                ProfileMetric(Modifier.weight(1f), rating, "Рейтинг", Res.drawable.statistics_bar_chart, AppColors.Positive)
            }
        }
    }
}

@Composable
private fun ProfileMetric(modifier: Modifier, value: Int?, title: String, icon: DrawableResource, accent: Color) {
    BoxWithConstraints(modifier) {
        if (maxWidth >= 130.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppIconBadge(icon, tint = accent, compact = true)
                ProfileMetricValue(value, title)
            }
        } else {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconBadge(icon, tint = accent, compact = true)
                ProfileMetricValue(value, title, centered = true)
            }
        }
    }
}

@Composable
private fun ProfileMetricValue(value: Int?, title: String, centered: Boolean = false) {
    Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
        Text(value?.toString() ?: "—", color = AppColors.TextPrimary,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(title, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
internal fun PlayerProfilePreview() {
    ThousandTheme {
        AppBackground {
            com.alextos.thousand.common.Screen(Modifier, title = "Профиль игрока", goBack = {}, constrainListWidth = true) {
                PlayerProfileView("Маша", gameCount = 24, winCount = 12, rating = 860, modifier = it,
                    user = User("profile-preview", "Маша", UserKind.MainUser,
                        avatar = PlayerAvatar.Female))
            }
        }
    }
}
