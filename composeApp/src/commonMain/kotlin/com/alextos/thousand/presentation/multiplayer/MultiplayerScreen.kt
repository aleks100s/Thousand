package com.alextos.thousand.presentation.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.InfoCardView
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.diversity_3_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen() {
    val viewModel: MultiplayerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Мультиплеер",
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MultiplayerHero(
                state = state,
                onAction = viewModel::onAction,
            )
        }
    }

    if (state.isLoginSheetVisible) {
        LoginSheet(
            state = state,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun MultiplayerHero(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
    InfoCardView(
        icon = Res.drawable.diversity_3_24px,
        title = "Играйте с друзьями",
        text = "Мультиплеер поможет собрать игроков в одну партию: один создает игру, остальные подключаются, а приложение синхронизирует ход, броски, эффекты и итоговый счет.",
    ) {
        MultiplayerActions(
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun MultiplayerActions(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
    if (state.isAuthorized) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text("Подключиться")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text("Создать игру")
            }
        }
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAction(MultiplayerAction.ShowLoginSheet)
            },
        ) {
            Text("Авторизоваться")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginSheet(
    state: MultiplayerState,
    onAction: (MultiplayerAction) -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = {
            onAction(MultiplayerAction.HideLoginSheet)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Вход в мультиплеер",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { value ->
                    onAction(MultiplayerAction.UpdateEmail(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Email")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { value ->
                    onAction(MultiplayerAction.UpdatePassword(value))
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Пароль")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canLogIn && state.isLoginInProgress.not(),
                onClick = {
                    onAction(MultiplayerAction.LogIn)
                },
            ) {
                if (state.isLoginInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Войти")
                }
            }
        }
    }
}
