package com.alextos.thousand.application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.navigation.AppNavHost
import com.alextos.thousand.application.navigation.BottomBar
import com.alextos.thousand.application.navigation.rememberAppNavState
import com.alextos.thousand.application.theme.ThousandTheme

@Composable
@Preview
fun App() {
    ThousandTheme {
        val appNavState = rememberAppNavState()

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
