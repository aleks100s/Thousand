package com.alextos.thousand.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.LogoView
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.presentation.components.AppButton
import com.alextos.thousand.presentation.components.AppButtonStyle
import com.alextos.thousand.presentation.components.AppBottomSheet as ModalBottomSheet
import com.alextos.thousand.presentation.components.appPanel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight
        Screen(Modifier, titleView = { LogoView() }, hideNavigationInLandscape = true) { modifier ->
            MenuContent(
                modifier = modifier.consumeWindowInsets(WindowInsets.statusBars),
                tiles = tiles,
                isLandscape = landscape,
            ) { action ->
                when (action) {
                    MenuTileAction.Tutorial -> openTutorial()
                    MenuTileAction.Rules -> openRules()
                    MenuTileAction.Multiplayer, MenuTileAction.LocalGame -> {
                        if (state.isFirstLaunch) pendingFirstLaunchDestination = action
                        else openTile(action)
                    }
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

/** Measures the menu first so the illustration uses only the remaining space. */
@Composable
internal fun MenuContent(
    modifier: Modifier = Modifier,
    tiles: List<MenuTile> = MenuState().tiles,
    isLandscape: Boolean? = null,
    onClick: (MenuTileAction) -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize().safeDrawingPadding()) {
        val landscape = isLandscape ?: (maxWidth > maxHeight)
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                MenuHero(Modifier.fillMaxSize())
                Column(
                    Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    tiles.filter { it.size == MenuTileSize.Large }.forEach { tile ->
                        MenuPlayButton(tile) { onClick(tile.action) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        tiles.filter { it.size == MenuTileSize.Default }.forEach { tile ->
                            MenuSectionCard(Modifier.weight(1f), tile) { onClick(tile.action) }
                        }
                    }
                }
            },
        ) { children, constraints ->
            val menuWidth = if (landscape) constraints.maxWidth / 2 else constraints.maxWidth
            val menu = children[1].measure(Constraints(
                minWidth = menuWidth, maxWidth = menuWidth, maxHeight = constraints.maxHeight,
            ))
            val heroWidth = if (landscape) constraints.maxWidth - menuWidth else constraints.maxWidth
            val heroHeight = if (landscape) constraints.maxHeight else (constraints.maxHeight - menu.height).coerceAtLeast(0)
            val hero = children[0].measure(Constraints.fixed(heroWidth, heroHeight))
            layout(constraints.maxWidth, constraints.maxHeight) {
                hero.placeRelative(0, 0)
                menu.placeRelative(if (landscape) heroWidth else 0, constraints.maxHeight - menu.height)
            }
        }
    }
}

@Composable
private fun MenuPlayButton(tile: MenuTile, onClick: () -> Unit) {
    AppButton(
        Modifier.fillMaxWidth().heightIn(min = 56.dp),
        style = if (tile.action == MenuTileAction.LocalGame) AppButtonStyle.Primary else AppButtonStyle.Secondary,
        secondaryColor = AppColors.Positive,
        onClick = onClick,
    ) {
        Icon(painterResource(tile.action.icon()), null, Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(tile.title, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun MenuSectionCard(modifier: Modifier, tile: MenuTile, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.appPanel(outline = AppColors.OutlineSubtle),
        shape = AppShapes.Panel,
        color = Color.Transparent,
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 128.dp).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            Icon(painterResource(tile.action.icon()), null, Modifier.size(44.dp), tint = AppColors.Highlight)
            Text(tile.title, color = AppColors.TextPrimary, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun MenuTileAction.icon(): DrawableResource = when (this) {
    MenuTileAction.LocalGame -> Res.drawable.mobile_24px
    MenuTileAction.Multiplayer -> Res.drawable.language_24px
    MenuTileAction.Rules -> Res.drawable.rules_description
    MenuTileAction.Tutorial -> Res.drawable.book_2_24px
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun MenuPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground { MenuContent() }
    }
}
