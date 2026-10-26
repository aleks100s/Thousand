package com.alextos.thousand.presentation.menu.multiplayer.lobby

import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.presentation.components.appPanel
import thousand.composeapp.generated.resources.player_crown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.application.invites.LobbyLinks
import com.alextos.thousand.presentation.components.rememberTextShare
import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppButtonStyle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.content_copy_24px
import thousand.composeapp.generated.resources.person_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("DEPRECATION")
fun LobbyScreen(
    goBack: () -> Unit,
    openGame: (String) -> Unit,
) {
    val viewModel: LobbyViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LobbyEvent.Disconnect -> goBack()
                is LobbyEvent.StartGame -> openGame(event.gameID)
            }
        }
    }

    LobbyContent(state, goBack, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("DEPRECATION")
internal fun LobbyContent(state: LobbyState, goBack: () -> Unit, onAction: (LobbyAction) -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val shareText = rememberTextShare()

    if (state.error != null) {
        ModalBottomSheet(
            onDismissRequest = goBack
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error)

                AppButton(onClick = goBack) {
                    Text("Выйти в меню")
                }
            }
        }
    }

    Screen(
        modifier = Modifier,
        title = "Лобби ${state.lobbyId}",
        goBack = goBack,
        actions = {
            {
                TextButton(
                    enabled = state.lobbyId.isNotBlank(),
                    onClick = {
                        onAction(LobbyAction.LeaveGame)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                ) {
                    Text("Выйти из игры")
                }
            }
        },
    ) { modifier ->
        BoxWithConstraints(modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            Column(
                Modifier.align(Alignment.TopCenter)
                    .widthIn(max = if (landscape) 600.dp else Dp.Infinity)
                    .fillMaxSize().navigationBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    item(key = "lobby-code") {
                        Row(
                            Modifier.fillMaxWidth().appPanel().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Код лобби", color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.labelLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.lobbyId.ifBlank { "—" },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                    IconButton(
                                        enabled = state.lobbyId.isNotBlank(),
                                        onClick = { clipboardManager.setText(AnnotatedString(state.lobbyId)) },
                                    ) {
                                        Icon(painterResource(Res.drawable.content_copy_24px),
                                            "Скопировать код лобби", tint = AppColors.Accent)
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Подключены", color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.labelLarge)
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(state.players.size.toString(), color = AppColors.TextPrimary,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold)
                                    Icon(painterResource(Res.drawable.person_24px), null,
                                        tint = AppColors.Accent)
                                }
                            }
                        }
                    }
                    items(state.players, key = { it.id }, contentType = { "player" }) { player ->
                        LobbyPlayerRow(player, player.id == state.hostId)
                    }
                    if (state.players.isEmpty()) {
                        item(key = "connecting") {
                            Text("Подключение к лобби…", color = AppColors.TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
                if (landscape) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LobbyShareButton(Modifier.weight(1f), state.lobbyId, shareText)
                        LobbyStartButton(Modifier.weight(1f), state, onAction)
                    }
                } else {
                    LobbyShareButton(Modifier.fillMaxWidth(), state.lobbyId, shareText)
                    LobbyStartButton(Modifier.fillMaxWidth(), state, onAction)
                }
            }
        }
    }
}

@Composable
private fun LobbyPlayerRow(player: com.alextos.thousand.domain.models.User, isHost: Boolean) {
    Row(
        Modifier.fillMaxWidth().appPanel().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(AppColors.SurfaceRaised)
            .border(2.dp, if (isHost) AppColors.Highlight else AppColors.Accent, CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(painterResource(Res.drawable.person_24px), null,
                modifier = Modifier.size(28.dp), tint = AppColors.TextPrimary)
        }
        Column(Modifier.weight(1f)) {
            Text(player.name, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            if (isHost) Text("Хозяин лобби", color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall)
        }
        if (isHost) Icon(painterResource(Res.drawable.player_crown), "Хозяин лобби",
            modifier = Modifier.size(24.dp), tint = AppColors.Highlight)
        Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.Positive))
    }
}

@Composable
private fun LobbyShareButton(modifier: Modifier, lobbyId: String, shareText: (String) -> Unit) {
    AppButton(modifier, style = AppButtonStyle.Secondary, enabled = lobbyId.isNotBlank(),
        onClick = { shareText("Присоединяйся к игре «Тысяча»!\n${LobbyLinks.create(lobbyId)}") },
    ) { Text("Поделиться ссылкой") }
}

@Composable
private fun LobbyStartButton(modifier: Modifier, state: LobbyState, onAction: (LobbyAction) -> Unit) {
    AppButton(modifier, enabled = state.isStartButtonEnabled,
        onClick = { onAction(LobbyAction.StartGame) },
    ) { Text("Начать") }
}
@androidx.compose.ui.tooling.preview.Preview
@Composable
internal fun LobbyPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            LobbyContent(
                LobbyState(lobbyId = "1234", isHost = true, hostId = "preview-a", players = listOf(
                    com.alextos.thousand.domain.models.User(id = "preview-a", name = "Маша"),
                    com.alextos.thousand.domain.models.User(id = "preview-b", name = "Саша"),
                    com.alextos.thousand.domain.models.User(id = "preview-c", name = "Александр"),
                    com.alextos.thousand.domain.models.User(id = "preview-d", name = "Легендарный Хомяк"),
                    com.alextos.thousand.domain.models.User(id = "preview-e", name = "Олег"),
                    com.alextos.thousand.domain.models.User(id = "preview-f", name = "Екатерина"),
                )), {}, {},
            )
        }
    }
}
