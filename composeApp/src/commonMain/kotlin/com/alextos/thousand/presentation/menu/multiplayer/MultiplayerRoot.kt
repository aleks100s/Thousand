package com.alextos.thousand.presentation.menu.multiplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.menu.multiplayer.create_lobby.CreateLobbyScreen
import com.alextos.thousand.presentation.menu.multiplayer.lobby.LobbyScreen
import com.alextos.thousand.presentation.menu.multiplayer.multiplayer_game.MultiplayerGameScreen
import com.alextos.thousand.presentation.menu.multiplayer.player_profile.PlayerProfileScreen

@Composable
fun MultiplayerRoot(
    goBack: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MultiplayerRoute.Multiplayer,
    ) {
        horizontalTransition<MultiplayerRoute.Multiplayer> {
            MultiplayerScreen(
                goBack = goBack,
                openCreateLobby = {
                    navController.navigate(MultiplayerRoute.CreateLobby)
                },
                openLobby = { lobbyId ->
                    navController.navigate(
                        MultiplayerRoute.Lobby(
                            lobbyId
                        )
                    )
                },
                openGame = { gameId ->
                    navController.navigate(
                        MultiplayerRoute.MultiplayerGame(
                            gameId
                        )
                    )
                },
                openPlayerProfile = {
                    navController.navigate(MultiplayerRoute.PlayerProfile)
                },
            )
        }
        horizontalTransition<MultiplayerRoute.PlayerProfile> {
            PlayerProfileScreen(
                goBack = {
                    navController.popBackStack()
                },
            )
        }
        horizontalTransition<MultiplayerRoute.CreateLobby> {
            CreateLobbyScreen(
                goBack = {
                    navController.popBackStack()
                },
                openLobby = { lobbyId ->
                    navController.navigate(
                        MultiplayerRoute.Lobby(
                            lobbyId
                        )
                    ) {
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
                    navController.navigate(
                        MultiplayerRoute.MultiplayerGame(
                            lobbyId
                        )
                    ) {
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
