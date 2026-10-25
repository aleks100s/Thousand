package com.alextos.thousand.presentation.menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import com.alextos.thousand.presentation.components.AppIconBadge
import com.alextos.thousand.presentation.components.appPanel
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppDimens
import com.alextos.thousand.presentation.components.AppButtonStyle
import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.LogoView
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.book_2_24px
import thousand.composeapp.generated.resources.language_24px
import thousand.composeapp.generated.resources.mobile_24px
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

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        pendingFirstLaunchDestination = null
                        viewModel.onAction(MenuAction.CompleteFirstLaunch)
                        openTutorial()
                    },
                ) {
                    Text("Да, хочу научиться")
                }

                AppButton(style = AppButtonStyle.Secondary,
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
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = AppShapes.Panel,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().appPanel(),
    ) {
        if (tile.size == MenuTileSize.Large) {
            Row(Modifier.fillMaxWidth().heightIn(min = 144.dp).padding(AppDimens.CardPadding),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap)) {
                AppIconBadge(tile.action.icon(), large = true)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tile.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Text(tile.description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().heightIn(min = 132.dp).padding(AppDimens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconBadge(tile.action.icon(), compact = true)
                    Text(tile.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                }
                Text(tile.description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        }
    }
}

private fun MenuTileAction.icon(): DrawableResource =
    when (this) {
        MenuTileAction.LocalGame -> Res.drawable.mobile_24px
        MenuTileAction.Multiplayer -> Res.drawable.language_24px
        MenuTileAction.Rules -> Res.drawable.book_2_24px
        MenuTileAction.Tutorial -> Res.drawable.school_24px
    }
