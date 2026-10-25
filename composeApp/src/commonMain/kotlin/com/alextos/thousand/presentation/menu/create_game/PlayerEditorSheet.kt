package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.presentation.components.AppIconBadge
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.appTextFieldColors
import com.alextos.thousand.presentation.components.AppButton
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

private val EditorCream = AppColors.TextPrimary
private val EditorBody = AppColors.TextSecondary
private val EditorGold = AppColors.Highlight
private val EditorCyan = AppColors.Outline
private val EditorRed = AppColors.Danger

@Composable
private fun EditorPlayerAvatar(user: User, modifier: Modifier = Modifier) {
    AppIconBadge(when (user.kind) {
        UserKind.MainUser -> Res.drawable.player_crown
        UserKind.Bot -> Res.drawable.robot_24px
        else -> Res.drawable.rules_casino
    }, modifier = modifier, large = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerEditorSheet(state: CreateGameState, onAction: (CreateGameAction) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { if (!state.isPlayerOperationRunning) onAction(CreateGameAction.HidePlayerEditor) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = !state.isPlayerOperationRunning,
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorPlayerAvatar(state.editingUser ?: User("new", "", state.playerKind))
                Text(if (state.editingUser != null) "Редактирование игрока" else if (state.playerKind == UserKind.Bot) "Новый бот" else "Новый игрок", modifier = Modifier.weight(1f),
                    color = EditorCream, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(modifier = Modifier.align(Alignment.Top), enabled = !state.isPlayerOperationRunning, onClick = { onAction(CreateGameAction.HidePlayerEditor) }) {
                    Icon(painterResource(Res.drawable.close_24px), "Закрыть", tint = EditorBody)
                }
            }
            OutlinedTextField(
                value = state.playerName,
                onValueChange = { onAction(CreateGameAction.UpdatePlayerName(it)) },
                modifier = Modifier.fillMaxWidth(), enabled = !state.isPlayerOperationRunning,
                label = { Text("Имя пользователя") }, singleLine = true,
                shape = AppShapes.Row,
                isError = state.playerNameError != null,
                supportingText = state.playerNameError?.let { error -> { Text(error) } },
                colors = appTextFieldColors(),
            )
            state.playerError?.let { Text(it, color = EditorRed, style = MaterialTheme.typography.bodyMedium) }
            AppButton(Modifier.fillMaxWidth(), enabled = state.canSavePlayer, onClick = { onAction(CreateGameAction.SavePlayer) }) {
                if (state.isPlayerOperationRunning) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Сохранить", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
