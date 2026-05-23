package com.alextos.thousand.presentation.multiplayer.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LobbyScreen(
    goBack: () -> Unit,
) {
    val viewModel: LobbyViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Лобби",
        goBack = goBack,
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("ID лобби: ${state.lobbyId}")
        }
    }
}
