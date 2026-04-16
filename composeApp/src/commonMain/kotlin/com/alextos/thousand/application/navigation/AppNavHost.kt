package com.alextos.thousand.application.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alextos.thousand.presentation.game.GameTabRoot
import com.alextos.thousand.presentation.statistics.StatisticsTabRoot

@Composable
fun AppNavHost(
    appNavState: AppNavState,
    contentAlignment: Alignment,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = appNavState.navController,
        startDestination = BottomTab.Game.route,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
    ) {
        composable(BottomTab.Game.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = contentAlignment,
            ) {
                GameTabRoot()
            }
        }
        composable(BottomTab.Statistics.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = contentAlignment,
            ) {
                StatisticsTabRoot()
            }
        }
    }
}
