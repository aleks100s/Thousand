package com.alextos.thousand.presentation.onboarding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.LogoView
import com.alextos.thousand.presentation.components.PlayerAvatarPicker
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Screen(
        Modifier,
        titleView = {
            LogoView()
        }
    ) {
        Box(
            modifier = it
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = if (isKeyboardVisible) 0.dp else 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = if (isKeyboardVisible) 0.dp else 56.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Как вас зовут?",
                    color = MaterialTheme.colorScheme.onBackground,
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
                PlayerAvatarPicker(
                    state.avatar, { viewModel.onAction(OnboardingAction.SelectAvatar(it)) }, !state.isSaving,
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
        }
    }
}
