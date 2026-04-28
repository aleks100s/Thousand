package com.alextos.thousand.application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.navigation.AppNavHost
import com.alextos.thousand.application.navigation.BottomBar
import com.alextos.thousand.application.navigation.rememberAppNavState
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.presentation.onboarding.FirstUserScreen
import com.alextos.thousand.presentation.onboarding.FirstUserViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    ThousandTheme {
        val firstUserViewModel: FirstUserViewModel = koinViewModel()
        val firstUserState by firstUserViewModel.state.collectAsStateWithLifecycle()
        val appNavState = rememberAppNavState()

        when {
            firstUserState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            firstUserState.isFirstUserRequired -> {
                FirstUserScreen(
                    state = firstUserState,
                    onAction = firstUserViewModel::onAction,
                )
            }
            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            currentTab = appNavState.currentTab,
                            onTabSelected = appNavState::navigateToTab,
                        )
                    },
                ) { innerPadding ->
                    AppNavHost(
                        appNavState = appNavState,
                        contentAlignment = Alignment.Center,
                        contentPadding = innerPadding,
                    )
                }
            }
        }
    }
}
