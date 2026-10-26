package com.alextos.thousand.common

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.nav_back

@Composable
fun Screen(
    modifier: Modifier,
    title: String = "",
    titleView: @Composable (() -> Unit)? = null,
    goBack: (() -> Unit)? = null,
    backButtonIcon: Painter = painterResource(Res.drawable.nav_back),
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable (Color) -> @Composable RowScope.() -> Unit = { {} },
    bannerView: @Composable (() -> Unit)? = null,
    constrainListWidth: Boolean = false,
    hideNavigationInLandscape: Boolean = false,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val showNavigation = !hideNavigationInLandscape || maxWidth <= maxHeight
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            ),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                if (showNavigation) AppNavigationBar(title, titleView, goBack, backButtonIcon, actions)
            },
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            bottomBar = { bannerView?.invoke() },
        ) { innerPaddings ->
            val contentModifier = Modifier.padding(
                top = innerPaddings.calculateTopPadding(),
                bottom = if (bannerView != null) innerPaddings.calculateBottomPadding() else 0.dp,
            )
            if (constrainListWidth) {
                ListScreenContent { content(contentModifier) }
            } else {
                content(contentModifier)
            }
        }
    }
}
