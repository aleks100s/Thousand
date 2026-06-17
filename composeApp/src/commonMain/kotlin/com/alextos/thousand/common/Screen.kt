package com.alextos.thousand.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.arrow_back_24px
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    modifier: Modifier,
    title: String = "",
    titleView: @Composable (() -> Unit)? = null,
    useDynamicTitle: Boolean = false,
    color: Color? = null,
    goBack: (() -> Unit)? = null,
    backButtonIcon: Painter = painterResource(Res.drawable.arrow_back_24px),
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable (Color) -> @Composable RowScope.() -> Unit = { {} },
    bannerView: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isPhone = minOf(maxWidth, maxHeight) < 600.dp
        val isLandscape = maxWidth > maxHeight
        val isTopBarVisible = isPhone.not() || isLandscape.not()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (isTopBarVisible) {
                    TopAppBar(
                        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                        title = titleView ?: {
                            if (useDynamicTitle) {
                                val scale by animateFloatAsState(
                                    max(min(1.2f + scrollBehavior.state.contentOffset / 150, 1.2f), 1f)
                                )
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = TextUnit(
                                        value = MaterialTheme.typography.headlineSmall.fontSize.value * scale,
                                        type = MaterialTheme.typography.headlineSmall.fontSize.type
                                    )
                                )
                            } else {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = color?.let {
                            TopAppBarDefaults.topAppBarColors().copy(containerColor = it)
                        } ?: TopAppBarDefaults.topAppBarColors(),
                        actions = actions(MaterialTheme.colorScheme.onSurface),
                        navigationIcon = {
                            if (goBack != null) {
                                IconButton(
                                    onClick = {
                                        goBack()
                                    }
                                ) {
                                    Icon(
                                        backButtonIcon,
                                        "Назад",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            bottomBar = {
                if (bannerView != null) {
                    bannerView()
                }
            }
        ) { innerPaddings ->
            val topPadding = if (isTopBarVisible) {
                innerPaddings.calculateTopPadding()
            } else {
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            }

            content(
                Modifier.padding(
                    top = topPadding,
                    bottom = if (bannerView != null) innerPaddings.calculateBottomPadding() else 0.dp
                )
            )
        }
    }
}
