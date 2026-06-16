package com.alextos.thousand.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isFinalizingInProgress) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Как вас зовут?",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChange = { value ->
                        viewModel.onAction(OnboardingAction.UpdateName(value))
                    },
                    label = {
                        Text("Имя")
                    },
                    singleLine = true,
                    enabled = state.isSaving.not(),
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.name.isNotBlank() && state.isSaving.not(),
                    onClick = {
                        viewModel.onAction(OnboardingAction.SaveUser)
                    },
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text("Продолжить")
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Уже зарегистрированы?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    TextButton(
                        enabled = state.isSaving.not(),
                        onClick = {
                            viewModel.onAction(OnboardingAction.ShowLoginSheet)
                        },
                    ) {
                        Text("Войти")
                    }
                }
            }
        }

        if (state.isLoginSheetVisible) {
            LoginSheet(
                state = state,
                onAction = viewModel::onAction,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginSheet(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val canSubmit = email.trim().isNotBlank() && password.isNotBlank()
    val buttonEnabled = state.isLoginInProgress.not() && canSubmit

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = {
            onAction(OnboardingAction.HideLoginSheet)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Вход",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = email,
                onValueChange = { value ->
                    email = value
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Email")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { value ->
                    password = value
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Пароль")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            state.loginError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonEnabled,
                onClick = {
                    onAction(
                        OnboardingAction.LogIn(
                            email = email,
                            password = password,
                        )
                    )
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
