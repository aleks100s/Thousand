package com.alextos.thousand.presentation.menu.create_game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.AppButton
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

private val EditorCream = Color(0xFFF5F0DF)
private val EditorBody = Color(0xFFAACBDD)
private val EditorGold = Color(0xFFFFC56A)
private val EditorCyan = Color(0xFF0785A4)
private val EditorRed = Color(0xFFFF7778)

@Composable
private fun EditorPlayerAvatar(user: User, modifier: Modifier = Modifier) {
    val main = user.kind == UserKind.MainUser
    val palette = listOf(Color(0xFF226E8F), Color(0xFF8C3556), Color(0xFF477C45), Color(0xFF66459C), Color(0xFF935526))
    val color = if (main) Color(0xFF54451E) else palette[(user.id.hashCode() and Int.MAX_VALUE) % palette.size]
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.size(58.dp).clip(shape).background(Brush.verticalGradient(listOf(color, Color(0xFF09232E))))
        .border(1.dp, if (main) EditorGold else color, shape), contentAlignment = Alignment.Center) {
        Icon(painterResource(when {
            main -> Res.drawable.player_crown
            user.kind == UserKind.Bot -> Res.drawable.robot_24px
            else -> Res.drawable.rules_casino
        }), contentDescription = null, modifier = Modifier.size(34.dp), tint = if (main) EditorGold else EditorCream)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerEditorSheet(state: CreateGameState, onAction: (CreateGameAction) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { if (!state.isPlayerOperationRunning) onAction(CreateGameAction.HidePlayerEditor) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = !state.isPlayerOperationRunning,
        containerColor = Color(0xFF031F2B), contentColor = EditorCream,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = EditorCyan) },
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
                shape = RoundedCornerShape(14.dp),
                isError = state.playerNameError != null,
                supportingText = state.playerNameError?.let { error -> { Text(error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EditorCream, unfocusedTextColor = EditorCream,
                    focusedBorderColor = EditorGold, unfocusedBorderColor = EditorCyan,
                    focusedLabelColor = EditorGold, unfocusedLabelColor = EditorBody,
                    cursorColor = EditorGold, errorTextColor = EditorCream, errorBorderColor = EditorRed,
                    errorLabelColor = EditorRed, errorSupportingTextColor = EditorRed,
                    focusedContainerColor = Color(0xFF062B38), unfocusedContainerColor = Color(0xFF062B38),
                ),
            )
            state.playerError?.let { Text(it, color = EditorRed, style = MaterialTheme.typography.bodyMedium) }
            AppButton(Modifier.fillMaxWidth(), enabled = state.canSavePlayer, onClick = { onAction(CreateGameAction.SavePlayer) }) {
                if (state.isPlayerOperationRunning) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Сохранить", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
