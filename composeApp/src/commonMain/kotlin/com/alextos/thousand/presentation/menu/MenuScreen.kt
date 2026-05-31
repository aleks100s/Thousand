package com.alextos.thousand.presentation.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onCreateGame: () -> Unit,
    openGamesHistory: () -> Unit,
    openRules: () -> Unit,
    openTutorial: () -> Unit,
    openStatistics: () -> Unit,
    openUsers: () -> Unit,
) {
    val viewModel: MenuViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var isTutorialSheetVisible by remember { mutableStateOf(false) }

    Screen(
        modifier = Modifier,
        title = "Меню",
    ) { modifier ->
        BoxWithConstraints(modifier = modifier.fillMaxSize(),) {
            val columns = if (maxWidth > maxHeight) 3 else 2

            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = state.tiles,
                    span = { tile ->
                        GridItemSpan(
                            if (tile.size == MenuTileSize.Large) maxLineSpan else 1
                        )
                    },
                ) { tile ->
                    MenuTileView(
                        tile = tile,
                        onClick = {
                            when (tile.action) {
                                MenuTileAction.Tutorial -> openTutorial()
                                MenuTileAction.Rules -> openRules()
                                MenuTileAction.GamesHistory -> openGamesHistory()
                                MenuTileAction.Statistics -> openStatistics()
                                MenuTileAction.Users -> openUsers()
                                MenuTileAction.NewGame -> {
                                    if (state.isFirstLaunch) {
                                        isTutorialSheetVisible = true
                                    } else {
                                        onCreateGame()
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (isTutorialSheetVisible) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isTutorialSheetVisible = false
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
                        isTutorialSheetVisible = false
                        viewModel.onAction(MenuAction.CompleteFirstLaunch)
                        openTutorial()
                    },
                ) {
                    Text("Да, хочу научиться")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isTutorialSheetVisible = false
                        viewModel.onAction(MenuAction.CompleteFirstLaunch)
                        onCreateGame()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (tile.size == MenuTileSize.Large) 168.dp else 112.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
