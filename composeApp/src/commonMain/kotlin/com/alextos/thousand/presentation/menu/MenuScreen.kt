package com.alextos.thousand.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.LogoView
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.book_2_24px
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.language_24px
import thousand.composeapp.generated.resources.school_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    hideMultiplayer: Boolean,
    openLocalGame: (hasLocalGames: Boolean) -> Unit,
    openMultiplayer: () -> Unit,
    openRules: () -> Unit,
    openTutorial: () -> Unit,
) {
    val viewModel: MenuViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var pendingFirstLaunchDestination by remember {
        mutableStateOf<MenuTileAction?>(null)
    }
    val tiles = remember(state.tiles, hideMultiplayer) {
        state.tiles.mapNotNull { tile ->
            when (tile.action) {
                MenuTileAction.Multiplayer if hideMultiplayer -> null
                else -> tile
            }
        }
    }

    fun openTile(tileAction: MenuTileAction) {
        when (tileAction) {
            MenuTileAction.Tutorial -> openTutorial()
            MenuTileAction.Rules -> openRules()
            MenuTileAction.Multiplayer -> openMultiplayer()
            MenuTileAction.LocalGame -> openLocalGame(state.hasLocalGames)
        }
    }

    Screen(
        modifier = Modifier,
        titleView = {
            LogoView()
        }
    ) { modifier ->
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val columns = if (isLandscape) 6 else 2
            val largeTileCount = tiles.count { tile -> tile.size == MenuTileSize.Large }

            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = tiles,
                    span = { tile ->
                        GridItemSpan(
                            when {
                                tile.size == MenuTileSize.Large && isLandscape && largeTileCount > 1 -> 3
                                tile.size == MenuTileSize.Large -> maxLineSpan
                                isLandscape -> 2
                                else -> 1
                            }
                        )
                    },
                ) { tile ->
                    MenuTileView(
                        tile = tile,
                        onClick = {
                            when (tile.action) {
                                MenuTileAction.Tutorial -> openTutorial()
                                MenuTileAction.Rules -> openRules()
                                MenuTileAction.Multiplayer,
                                MenuTileAction.LocalGame -> {
                                    if (state.isFirstLaunch) {
                                        pendingFirstLaunchDestination = tile.action
                                    } else {
                                        openTile(tile.action)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (pendingFirstLaunchDestination != null) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                pendingFirstLaunchDestination = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Хотите пройти обучение и сыграть тестовую игру?",
                    style = MaterialTheme.typography.titleMedium,
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        pendingFirstLaunchDestination = null
                        viewModel.onAction(MenuAction.CompleteFirstLaunch)
                        openTutorial()
                    },
                ) {
                    Text("Да, хочу научиться")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val destination = pendingFirstLaunchDestination
                        pendingFirstLaunchDestination = null
                        viewModel.onAction(MenuAction.CompleteFirstLaunch)
                        destination?.let { openTile(it) }
                    },
                ) {
                    Text("Нет, я умею играть")
                }
            }
        }
    }
}

@Composable
private fun MenuTileView(
    tile: MenuTile,
    onClick: () -> Unit,
) {
    val gradient = tile.action.gradient()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (tile.size == MenuTileSize.Large) 144.dp else 100.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
        ) {
            Icon(
                modifier = Modifier
                    .offset(x = 8.dp, y = 8.dp)
                    .align(Alignment.BottomEnd)
                    .size(if (tile.size == MenuTileSize.Large) 88.dp else 48.dp),
                painter = painterResource(tile.action.icon()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = tile.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun MenuTileAction.gradient(): Brush =
    Brush.linearGradient(
        colors = when (this) {
            MenuTileAction.LocalGame -> listOf(Color(0x77FFE3E0), Color(0x77E57373))
            MenuTileAction.Multiplayer -> listOf(Color(0x77DCEBFF), Color(0x7764A6F7))
            MenuTileAction.Rules -> listOf(Color(0x77FFF2BF), Color(0x77FFB74D))
            MenuTileAction.Tutorial -> listOf(Color(0x77DFF7E8), Color(0x7766BB6A))
        },
        start = Offset.Zero,
        end = Offset.Infinite,
    )

private fun MenuTileAction.icon(): DrawableResource =
    when (this) {
        MenuTileAction.LocalGame -> Res.drawable.casino_24px
        MenuTileAction.Multiplayer -> Res.drawable.language_24px
        MenuTileAction.Rules -> Res.drawable.book_2_24px
        MenuTileAction.Tutorial -> Res.drawable.school_24px
    }
