package com.alextos.thousand.presentation.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.InfoCardView
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.diversity_3_24px

@Composable
fun MultiplayerScreen() {
    val viewModel: MultiplayerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Мультиплеер",
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MultiplayerHero(state)
        }
    }
}

@Composable
private fun MultiplayerHero(
    state: MultiplayerState,
) {
    InfoCardView(
        icon = Res.drawable.diversity_3_24px,
        title = "Играйте с друзьями",
        text = "Мультиплеер поможет собрать игроков в одну партию: один создает игру, остальные подключаются, а приложение синхронизирует ход, броски, эффекты и итоговый счет.",
    ) {
        MultiplayerActions(state)
    }
}

@Composable
private fun MultiplayerActions(
    state: MultiplayerState,
) {
    if (state.isAuthorized) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text("Подключиться")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text("Создать игру")
            }
        }
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
        ) {
            Text("Авторизоваться")
        }
    }
}
