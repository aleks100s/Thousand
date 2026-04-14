package com.alextos.thousand.presentation.statistics

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val STATISTICS_ROUTE = "statistics"

@Composable
fun StatisticsTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = STATISTICS_ROUTE,
    ) {
        composable(STATISTICS_ROUTE) {
            StatisticsScreen()
        }
    }
}
