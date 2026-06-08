package com.alextos.thousand.presentation.menu.multiplayer.player_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PlayerProfileEvent.GoBack -> goBack()
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Профиль игрока",
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
                            Text("Выйти")
                        },
                        onClick = {
                            isMenuExpanded = false
                            viewModel.onAction(PlayerProfileAction.ShowLogoutDialog)
                        },
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Удалить аккаунт")
                        },
                        enabled = state.isDeleteInProgress.not(),
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                        ),
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
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            PlayerProfileView(
                username = state.username,
                gameCount = state.userInfo?.gameCount ?: 0,
                winCount = state.userInfo?.winCount ?: 0,
                rating = state.userInfo?.rating ?: 0,
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

    if (state.isLogoutDialogVisible) {
        LogoutDialog(
            onSignOut = {
                viewModel.onAction(PlayerProfileAction.SignOut)
            },
            onDismiss = {
                viewModel.onAction(PlayerProfileAction.HideLogoutDialog)
            },
        )
    }

    if (state.isDeleteAccountDialogVisible) {
        DeleteAccountDialog(
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
private fun LogoutDialog(
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text("Вы действительно хотите выйти из аккаунта?")
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                onClick = onSignOut,
            ) {
                Text("Выйти")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
private fun DeleteAccountDialog(
    onDeleteAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Удалить аккаунт?")
        },
        text = {
            Text("Будут удалены профиль игрока, а также все игры и лобби, где вы являетесь хостом. Это действие нельзя отменить.")
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                onClick = onDeleteAccount,
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
