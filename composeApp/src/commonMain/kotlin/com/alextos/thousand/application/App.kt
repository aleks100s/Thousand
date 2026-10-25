package com.alextos.thousand.application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.application.navigation.AppNavHost
import com.alextos.thousand.application.theme.AppBackground
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.presentation.onboarding.OnboardingScreen
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    ThousandTheme {
        val viewModel: AppViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            delay(2_000)
            viewModel.onAction(AppAction.LaunchFinished)
        }

        AppBackground {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                when {
                    state.isLoading || !state.isLaunchFinished -> {
                        LaunchScreen()
                    }
                    state.isOnboardingRequired -> {
                        OnboardingScreen()
                    }
                    else -> {
                        AppNavHost(hideMultiplayer = state.hideMultiplayer)
                    }
                }
            }
        }
    }
}
