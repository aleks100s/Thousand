package com.alextos.thousand.presentation.menu.users

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppButtonStyle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

internal val UsersCream = Color(0xFFF5F0DF)
internal val UsersBody = Color(0xFFAACBDD)
internal val UsersGold = Color(0xFFFFC56A)
internal val UsersCyan = Color(0xFF0785A4)
internal val UsersRed = Color(0xFFFF7778)

@Composable
fun UsersScreen(onGoBack: () -> Unit) {
    val viewModel: UsersViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    UsersContent(state, viewModel::onAction, onGoBack)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun UsersContent(state: UsersState, onAction: (UsersAction) -> Unit, onGoBack: () -> Unit) {
    Screen(
        modifier = Modifier,
        constrainListWidth = true,
        title = "Локальные игроки",
        goBack = onGoBack,
        actions = { { UserActionButton(Res.drawable.add_24px, "Добавить игрока") { onAction(UsersAction.StartAddUser) } } },
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingIndicator() }
        } else {
            Column(modifier.fillMaxSize().navigationBarsPadding()) {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (state.users.isEmpty()) {
                        item(key = "empty", contentType = "empty") {
                            Text("Пользователей пока нет", color = UsersBody, modifier = Modifier.padding(16.dp))
                        }
                    }
                    items(state.users, key = { it.id }, contentType = { "user" }) { user ->
                        LocalUserCard(user, state.statistics[user.id] ?: UserStatisticsUi(),
                            onRename = { onAction(UsersAction.StartRenameUser(user)) },
                            onDelete = { onAction(UsersAction.RequestDeleteUser(user)) })
                    }
                }
                AppButton(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp),
                    onClick = { onAction(UsersAction.StartAddUser) },
                ) {
                    Icon(painterResource(Res.drawable.add_24px), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить игрока", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (state.isEditorVisible) UserEditorSheet(state, onAction)
    state.pendingDeleteUser?.let { user ->
        AlertDialog(
            onDismissRequest = { onAction(UsersAction.CancelDeleteUser) },
            containerColor = Color(0xFF062431), titleContentColor = UsersCream, textContentColor = UsersBody,
            title = { Text("Удалить игрока?") },
            text = { Text("Игрок «${user.name}» будет удалён из списка локальных игроков.") },
            confirmButton = { TextButton(onClick = { onAction(UsersAction.DeleteUser(user)) }) { Text("Удалить", color = UsersRed) } },
            dismissButton = { TextButton(onClick = { onAction(UsersAction.CancelDeleteUser) }) { Text("Отмена", color = UsersBody) } },
        )
    }
}

@Composable
private fun LocalUserCard(user: User, statistics: UserStatisticsUi, onRename: () -> Unit, onDelete: () -> Unit) {
    val main = user.kind == UserKind.MainUser
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onRename,
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().border(1.5.dp, if (main) UsersGold else UsersCyan, shape),
    ) {
        BoxWithConstraints {
            val inlineBadge = maxWidth >= 440.dp
            Row(
                Modifier.fillMaxWidth().background(Brush.horizontalGradient(if (main) listOf(Color(0xD0363422), Color(0xC0082029))
                    else listOf(Color(0xD0042B39), Color(0xC0001A25)))).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LocalUserAvatar(user)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(user.name, color = UsersCream, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(painterResource(Res.drawable.rules_casino), null, Modifier.size(18.dp), tint = UsersBody)
                        Text("Побед: ${statistics.wins} из ${statistics.games}", color = UsersBody, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (main && !inlineBadge) CurrentPlayerBadge()
                }
                if (main && inlineBadge) CurrentPlayerBadge()
                if (!main) {
                    UserActionButton(Res.drawable.edit_24px, "Переименовать ${user.name}", onClick = onRename)
                    UserActionButton(Res.drawable.delete_24px, "Удалить ${user.name}", destructive = true, onClick = onDelete)
                }
            }
        }
    }
}

@Composable
private fun CurrentPlayerBadge() {
    Text("Текущий игрок", color = UsersGold, fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(Color(0x663E3415)).border(1.dp, Color(0xFF9E813C), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp))
}

@Composable
internal fun LocalUserAvatar(user: User, modifier: Modifier = Modifier) {
    val main = user.kind == UserKind.MainUser
    val palette = listOf(Color(0xFF226E8F), Color(0xFF8C3556), Color(0xFF477C45), Color(0xFF66459C), Color(0xFF935526))
    val color = if (main) Color(0xFF54451E) else palette[(user.id.hashCode() and Int.MAX_VALUE) % palette.size]
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.size(58.dp).clip(shape).background(Brush.verticalGradient(listOf(color, Color(0xFF09232E))))
        .border(1.dp, if (main) UsersGold else color, shape), contentAlignment = Alignment.Center) {
        Icon(painterResource(when {
            main -> Res.drawable.player_crown
            user.kind == UserKind.Bot -> Res.drawable.robot_24px
            else -> Res.drawable.rules_casino
        }), contentDescription = null, modifier = Modifier.size(34.dp), tint = if (main) UsersGold else UsersCream)
    }
}

@Composable
private fun UserActionButton(icon: DrawableResource, label: String, destructive: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp).clip(shape)
        .background(if (destructive) Color(0x662F1721) else Color(0x99012838))
        .border(1.dp, if (destructive) Color(0xFF9E3A43) else Color(0xFF17647C), shape)) {
        Icon(painterResource(icon), contentDescription = label, tint = if (destructive) UsersRed else UsersBody, modifier = Modifier.size(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserEditorSheet(state: UsersState, onAction: (UsersAction) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { if (!state.isSaving) onAction(UsersAction.HideRenameUserSheet) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF031F2B), contentColor = UsersCream,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = UsersCyan) },
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalUserAvatar(state.editingUser ?: User("new", "", UserKind.LocalUser))
                Text(if (state.isAddingUser) "Новый игрок" else "Редактирование игрока", modifier = Modifier.weight(1f),
                    color = UsersCream, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = state.editingUserName,
                onValueChange = { onAction(UsersAction.UpdateEditingUserName(it)) },
                modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving,
                label = { Text("Имя пользователя") }, singleLine = true,
                shape = RoundedCornerShape(14.dp),
                isError = state.editingUserNameError != null,
                supportingText = state.editingUserNameError?.let { error -> { Text(error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = UsersCream, unfocusedTextColor = UsersCream,
                    focusedBorderColor = UsersGold, unfocusedBorderColor = UsersCyan,
                    focusedLabelColor = UsersGold, unfocusedLabelColor = UsersBody,
                    cursorColor = UsersGold, errorTextColor = UsersCream, errorBorderColor = UsersRed,
                    errorLabelColor = UsersRed, errorSupportingTextColor = UsersRed,
                    focusedContainerColor = Color(0xFF062B38), unfocusedContainerColor = Color(0xFF062B38),
                ),
            )
            state.saveError?.let { Text(it, color = UsersRed, style = MaterialTheme.typography.bodyMedium) }
            AppButton(Modifier.fillMaxWidth(), enabled = state.canSaveEditingUser, onClick = { onAction(UsersAction.SaveEditingUser) }) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Сохранить", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            AppButton(Modifier.fillMaxWidth(), style = AppButtonStyle.Secondary, enabled = !state.isSaving,
                onClick = { onAction(UsersAction.HideRenameUserSheet) }) { Text("Отмена") }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UsersPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            UsersContent(UsersState(isLoading = false, users = listOf(User("1", "DiceQA", UserKind.MainUser), User("2", "Александр"))), {}, {})
        }
    }
}
