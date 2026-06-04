package com.alextos.thousand.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
    }
}
