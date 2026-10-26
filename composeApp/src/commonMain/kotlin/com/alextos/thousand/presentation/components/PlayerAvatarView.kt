package com.alextos.thousand.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.domain.models.*
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

val User.appearanceColor: Color get() = Color(0xFF000000 or color.hex.removePrefix("#").toLong(16))

@Composable
fun PlayerAvatarView(user: User, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Image(
        painterResource(if (user.kind == UserKind.Bot) Res.drawable.avatar_robot else when (user.avatar) {
            PlayerAvatar.Neutral -> Res.drawable.avatar_neutral
            PlayerAvatar.Male -> Res.drawable.avatar_male
            PlayerAvatar.Female -> Res.drawable.avatar_female
        }),
        contentDescription = null,
        modifier = modifier.size(size).clip(CircleShape).background(user.appearanceColor)
            .border(1.dp, user.appearanceColor, CircleShape).padding(4.dp),
    )
}

@Composable
fun PlayerAvatarPicker(
    selected: PlayerAvatar,
    onSelect: (PlayerAvatar) -> Unit,
    enabled: Boolean = true,
    showLabels: Boolean = true,
) {
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
        PlayerAvatar.entries.forEach { avatar ->
            val label = when (avatar) { PlayerAvatar.Neutral -> "Нейтральный"; PlayerAvatar.Male -> "Мужской"; PlayerAvatar.Female -> "Женский" }
            Column(Modifier.weight(1f).clip(com.alextos.thousand.application.theme.AppShapes.Row)
                .selectable(selected = selected == avatar, enabled = enabled, role = Role.RadioButton) { onSelect(avatar) }
                .then(if (showLabels) Modifier else Modifier.semantics { contentDescription = label })
                .padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerAvatarView(User("avatar-choice", "", UserKind.MainUser, avatar = avatar),
                    Modifier.border(if (selected == avatar) 3.dp else 0.dp,
                        if (selected == avatar) AppColors.TextPrimary else Color.Transparent, CircleShape), size = 56.dp)
                if (showLabels) Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextPrimary)
            }
        }
    }
}
