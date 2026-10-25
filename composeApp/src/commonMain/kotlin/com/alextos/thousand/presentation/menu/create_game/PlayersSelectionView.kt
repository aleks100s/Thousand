package com.alextos.thousand.presentation.menu.create_game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.AppButton
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.add_24px
import thousand.composeapp.generated.resources.robot_24px

private val PlayerCream = Color(0xFFF5F0DF)
private val PlayerMuted = Color(0xFFAFC6D6)
private val PlayerMint = Color(0xFF89F1D1)
private val PlayerCardShape = RoundedCornerShape(14.dp)

@Composable
internal fun PlayersSelectionView(
    modifier: Modifier = Modifier,
    users: List<User>,
    selectedUsers: Set<User>,
    onAddUser: () -> Unit,
    onAddBot: () -> Unit,
    onToggleUser: (User) -> Unit,
    onContinue: () -> Unit,
) {
    Column(modifier.navigationBarsPadding()) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val compact = maxWidth > maxHeight
            val columns = if (compact) 4 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(users, key = { it.id }, contentType = { "player" }) { user ->
                    PlayerSelectionCard(user, user in selectedUsers, compact) { onToggleUser(user) }
                }
                item(key = "add-player", contentType = "add") { AddPlayerCard(false, compact, onAddUser) }
                item(key = "add-bot", contentType = "add") { AddPlayerCard(true, compact, onAddBot) }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppButton(Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = selectedUsers.size >= 2, onClick = onContinue) {
                Text("Далее", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerSelectionCard(user: User, selected: Boolean, compact: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = if (compact) 132.dp else 172.dp).clip(PlayerCardShape)
            .background(Brush.verticalGradient(listOf(Color(0xE5223D4C), Color(0xE50D2431))))
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF789EAF) else Color(0xFF365465), PlayerCardShape)
            .toggleable(selected, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(if (compact) 8.dp else 12.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = if (compact) 0.dp else 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        ) {
            PlayerAvatar(user, compact)
            Text(user.name, color = PlayerCream, style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (user.kind == UserKind.MainUser) {
                Text("Это вы", color = PlayerMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (selected) {
            Box(Modifier.align(Alignment.TopEnd).size(26.dp).background(PlayerMint, CircleShape), contentAlignment = Alignment.Center) {
                // A drawn check keeps the selection decoration passive; the whole card is one accessible control.
                Box(Modifier.size(15.dp).drawBehind {
                    val stroke = 2.5.dp.toPx()
                    drawLine(Color(0xFF06262D), Offset(size.width * .15f, size.height * .5f), Offset(size.width * .4f, size.height * .75f), stroke)
                    drawLine(Color(0xFF06262D), Offset(size.width * .4f, size.height * .75f), Offset(size.width * .87f, size.height * .2f), stroke)
                })
            }
        }
    }
}

@Composable
private fun PlayerAvatar(user: User, compact: Boolean) {
    val bot = user.kind == UserKind.Bot
    val colors = if (bot) listOf(Color(0xFFA38EE7), Color(0xFF584C91))
        else if (user.kind == UserKind.MainUser) listOf(Color(0xFF72CBE6), Color(0xFF285875))
        else listOf(Color(0xFFD497C7), Color(0xFF70456F))
    Box(
        Modifier.size(if (compact) 56.dp else 80.dp).clip(CircleShape).background(Brush.verticalGradient(colors))
            .border(2.dp, Color(0xFFBBD3E0), CircleShape).padding(5.dp)
            .border(1.dp, Color(0x667CD6E4), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (bot) Icon(painterResource(Res.drawable.robot_24px), contentDescription = null,
            modifier = Modifier.size(if (compact) 28.dp else 40.dp), tint = PlayerCream)
        else Text(user.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge, color = PlayerCream, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddPlayerCard(bot: Boolean, compact: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = if (compact) 132.dp else 172.dp).clip(PlayerCardShape)
            .background(Color(0x80061924)).drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(Color(0xFF496371), topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke), cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))))
            }.clickable(role = Role.Button, onClickLabel = if (bot) "Добавить бота" else "Добавить игрока", onClick = onClick)
            .padding(if (compact) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(if (compact) 56.dp else 72.dp).background(Color(0xFF1B3342), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(if (bot) Res.drawable.robot_24px else Res.drawable.add_24px),
                contentDescription = null, modifier = Modifier.size(34.dp), tint = PlayerCream)
        }
        Text(if (bot) "Добавить бота" else "Игрок", color = PlayerMuted,
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}


@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PlayersSelectionPreview() {
    val users = listOf(User("1", "Александр", UserKind.MainUser), User("2", "Маша"), User("3", "Бот", UserKind.Bot))
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            PlayersSelectionView(Modifier.fillMaxSize(), users, users.toSet(), {}, {}, {}, {})
        }
    }
}
