package com.alextos.thousand

import android.os.Bundle
import android.content.Intent
import com.alextos.thousand.application.invites.LobbyInvitations
import com.alextos.thousand.application.invites.LobbyLinks
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        hideStatusBar()

        val invitationUrl = if (savedInstanceState == null) intent?.dataString else savedInstanceState.getString("pendingLobbyUrl")
        invitationUrl?.let { LobbyInvitations.open(it) }
        intent?.data = null
        setContent {
            App()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBar()
    }

    private fun hideStatusBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
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