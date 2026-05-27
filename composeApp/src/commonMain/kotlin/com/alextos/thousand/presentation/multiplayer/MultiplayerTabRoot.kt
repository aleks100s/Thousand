package com.alextos.thousand.presentation.multiplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.multiplayer.create_lobby.CreateLobbyScreen
import com.alextos.thousand.presentation.multiplayer.lobby.LobbyScreen
import com.alextos.thousand.presentation.multiplayer.multiplayer_game.MultiplayerGameScreen

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
                openGame = { gameId ->
                    navController.navigate(MultiplayerRoute.MultiplayerGame(gameId))
                }
            )
        }
        horizontalTransition<MultiplayerRoute.CreateLobby> {
            CreateLobbyScreen(
                goBack = {
                    navController.popBackStack()
                },
                openLobby = { lobbyId ->
                    navController.navigate(MultiplayerRoute.Lobby(lobbyId)) {
                        popUpTo(MultiplayerRoute.Multiplayer)
                    }
                },
            )
        }
        horizontalTransition<MultiplayerRoute.Lobby> {
            LobbyScreen(
                goBack = {
                    navController.popBackStack()
                },
                openGame = { lobbyId ->
                    navController.navigate(MultiplayerRoute.MultiplayerGame(lobbyId)) {
                        popUpTo(MultiplayerRoute.Multiplayer)
                    }
                },
            )
        }
        horizontalTransition<MultiplayerRoute.MultiplayerGame> {
            MultiplayerGameScreen(
                goBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
