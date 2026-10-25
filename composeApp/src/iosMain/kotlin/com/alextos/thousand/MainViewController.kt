package com.alextos.thousand

import androidx.compose.ui.window.ComposeUIViewController
import com.alextos.thousand.application.App

fun MainViewController() = ComposeUIViewController { App() }
fun openLobbyInvitation(url: String) {
    com.alextos.thousand.application.invites.LobbyInvitations.open(url)
}
