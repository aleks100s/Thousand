package com.alextos.thousand.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.application.navigation.AppNavHost
import com.alextos.thousand.application.navigation.BottomBar
import com.alextos.thousand.application.navigation.BottomTab
import com.alextos.thousand.application.navigation.rememberAppNavState
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.presentation.onboarding.FirstUserScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    ThousandTheme {
        val appNavState = rememberAppNavState()
        val viewModel: AppViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val currentTab = appNavState.currentTab

        LaunchedEffect(state.hideMultiplayer, currentTab) {
            if (state.hideMultiplayer && currentTab == BottomTab.Multiplayer) {
                appNavState.navigateToTab(BottomTab.Game)
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.isFirstUserRequired -> {
                FirstUserScreen()
            }
            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            currentTab = currentTab,
                            hideMultiplayer = state.hideMultiplayer,
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
