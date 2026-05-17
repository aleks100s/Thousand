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
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserItem(
    user: User,
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

private val UserKind.title: String
    get() = when (this) {
        UserKind.LocalUser -> "Локальный игрок"
        UserKind.Bot -> "Бот"
        UserKind.MainUser -> "Основной игрок"
    }
