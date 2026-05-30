package com.alextos.thousand.application.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberAppNavState(): AppNavState {
    val navController = rememberNavController()
    return AppNavState(navController)
}

@Stable
class AppNavState(
    val navController: NavHostController,
) {
    val currentTab: BottomTab
        @Composable get() {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val destination = backStackEntry?.destination

            return BottomTab.entries.firstOrNull { tab ->
                destination?.hierarchy?.any { it.route == tab.route } == true
            } ?: BottomTab.Game
        }

    fun navigateToTab(tab: BottomTab) {
        val currentTab = BottomTab.entries.firstOrNull { currentTab ->
            navController.currentDestination?.hierarchy?.any { it.route == currentTab.route } == true
        }
        if (currentTab == tab) return

        navController.navigate(tab.route) {
            currentTab?.let { tab ->
                popUpTo(tab.route) {
                    inclusive = true
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
