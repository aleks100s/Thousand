package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.alextos.thousand.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.more_horiz_24px

@Composable
fun PlayerProfileScreen(
    goBack: () -> Unit,
) {
    val viewModel: PlayerProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isMenuExpanded by remember { mutableStateOf(false) }

    Screen(
        modifier = Modifier,
        title = "Профиль игрока",
        constrainListWidth = true,
        goBack = goBack,
        actions = {
            {
                IconButton(
                    onClick = {
                        isMenuExpanded = true
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.more_horiz_24px),
                        modifier = Modifier.rotate(90f),
                        contentDescription = "Меню",
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = {
                        isMenuExpanded = false
                    },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Удалить аккаунт")
                        },
                        enabled = state.isDeleteInProgress.not(),
                        colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            isMenuExpanded = false
                            viewModel.onAction(PlayerProfileAction.ShowDeleteAccountDialog)
                        },
                    )
                }
            }
        },
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize(),
        ) {
            PlayerProfileView(
                user = state.user,
            )

            if (state.isDeleteInProgress) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (state.isDeleteAccountDialogVisible) {
        DeleteAccountDialog(
            error = state.deleteError,
            isDeleting = state.isDeleteInProgress,
            onDeleteAccount = {
                viewModel.onAction(PlayerProfileAction.DeleteAccount)
            },
            onDismiss = {
                viewModel.onAction(PlayerProfileAction.HideDeleteAccountDialog)
            },
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    error: String?,
    isDeleting: Boolean,
    onDeleteAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Удалить аккаунт?")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Будут удалены аккаунт и все локальные данные приложения, включая игры, настройки и достижения. После удаления приложение запустится заново с онбординга. Это действие нельзя отменить.")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                onClick = onDeleteAccount,
                enabled = !isDeleting,
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Отмена")
            }
        },
    )
}
