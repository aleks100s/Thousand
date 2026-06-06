package com.alextos.thousand.presentation.multiplayer.player_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerProfileScreen(
    goBack: () -> Unit,
) {
    val viewModel: PlayerProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            goBack()
        }
    }

    Screen(
        modifier = Modifier,
        title = "Профиль игрока",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        viewModel.onAction(PlayerProfileAction.ShowLogoutDialog)
                    },
                ) {
                    Text("Выйти")
                }
            }
        },
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Профиль игрока",
                style = MaterialTheme.typography.titleMedium,
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.username.firstOrNull()?.uppercase().orEmpty(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = state.username,
                style = MaterialTheme.typography.titleLarge,
            )
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
