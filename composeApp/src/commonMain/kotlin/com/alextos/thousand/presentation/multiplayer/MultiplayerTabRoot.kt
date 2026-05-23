package com.alextos.thousand.presentation.multiplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.multiplayer.create_lobby.CreateLobbyScreen
import com.alextos.thousand.presentation.multiplayer.lobby.LobbyScreen

@Composable
fun MultiplayerTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MultiplayerRoute.Multiplayer,
    ) {
        horizontalTransition<MultiplayerRoute.Multiplayer> {
            MultiplayerScreen(
                openCreateLobby = {
                    navController.navigate(MultiplayerRoute.CreateLobby)
                },
                openLobby = { lobbyId ->
                    navController.navigate(MultiplayerRoute.Lobby(lobbyId))
                },
            )
        }
        horizontalTransition<MultiplayerRoute.CreateLobby> {
            CreateLobbyScreen(
                goBack = {
                    navController.popBackStack()
                },
                openLobby = { lobbyId ->
                    navController.navigate(MultiplayerRoute.Lobby(lobbyId))
                },
            )
        }
        horizontalTransition<MultiplayerRoute.Lobby> {
            LobbyScreen(
                goBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
