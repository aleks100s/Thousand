package com.alextos.thousand.presentation.multiplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition

@Composable
fun MultiplayerTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MultiplayerRoute.Multiplayer,
    ) {
        horizontalTransition<MultiplayerRoute.Multiplayer> {
            MultiplayerScreen()
        }
    }
}
