package com.alextos.thousand

import android.os.Bundle
import android.content.Intent
import com.alextos.thousand.application.invites.LobbyInvitations
import com.alextos.thousand.application.invites.LobbyLinks
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val invitationUrl = if (savedInstanceState == null) intent?.dataString else savedInstanceState.getString("pendingLobbyUrl")
        invitationUrl?.let { LobbyInvitations.open(it) }
        intent?.data = null
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { LobbyInvitations.open(it) }
        intent.data = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LobbyInvitations.pending.value?.let { outState.putString("pendingLobbyUrl", LobbyLinks.create(it.code)) }
        super.onSaveInstanceState(outState)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}