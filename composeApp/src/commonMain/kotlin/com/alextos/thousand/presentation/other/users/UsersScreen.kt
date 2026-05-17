package com.alextos.thousand.presentation.other.users

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.EmptyStateView
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.person_24px

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UsersScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: UsersViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Пользователи",
        goBack = onGoBack,
    ) { modifier ->
        when {
            state.isLoading -> {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
            state.users.isEmpty() -> {
                EmptyStateView(
                    modifier = modifier,
                    icon = Res.drawable.person_24px,
                    text = "Пользователей пока нет",
                )
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.users,
                        key = { user -> user.id },
                    ) { user ->
                        UserItem(
                            user = user,
                            onRename = {
                                viewModel.onAction(UsersAction.StartRenameUser(user))
                            },
                            onDelete = {
                                viewModel.onAction(UsersAction.DeleteUser(user))
                            },
                        )
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (state.editingUser != null) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                viewModel.onAction(UsersAction.HideRenameUserSheet)
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Переименовать пользователя",
                    style = MaterialTheme.typography.titleMedium,
                )

                OutlinedTextField(
                    value = state.editingUserName,
                    onValueChange = { value ->
                        viewModel.onAction(UsersAction.UpdateEditingUserName(value))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Имя пользователя")
                    },
                    isError = state.editingUserNameError != null,
                    supportingText = state.editingUserNameError?.let { error ->
                        {
                            Text(error)
                        }
                    },
                    singleLine = true,
                )

                Button(
                    onClick = {
                        viewModel.onAction(UsersAction.SaveEditingUser)
                    },
                    enabled = state.canSaveEditingUser,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserItem(
    user: User,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        isMenuExpanded = true
                    },
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = user.kind.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = {
                isMenuExpanded = false
            },
        ) {
            DropdownMenuItem(
                text = {
                    Text("Переименовать")
                },
                onClick = {
                    isMenuExpanded = false
                    onRename()
                },
            )
            if (user.kind != UserKind.MainUser) {
                DropdownMenuItem(
                    text = {
                        Text("Удалить")
                    },
                    onClick = {
                        isMenuExpanded = false
                        onDelete()
                    },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error),
                )
            }
        }
    }
}

private val UserKind.title: String
    get() = when (this) {
        UserKind.LocalUser -> "Локальный игрок"
        UserKind.Bot -> "Бот"
        UserKind.MainUser -> "Основной игрок"
    }
