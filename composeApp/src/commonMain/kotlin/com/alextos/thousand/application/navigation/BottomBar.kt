package com.alextos.thousand.application.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomBar(
    currentTab: BottomTab,
    hideMultiplayer: Boolean,
    onTabSelected: (BottomTab) -> Unit,
) {
    NavigationBar {
        BottomTab.entries
            .filter { tab -> tab != BottomTab.Multiplayer || hideMultiplayer.not() }
            .forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconResource),
                        contentDescription = tab.title
                    )
                },
                label = { Text(tab.title) },
                alwaysShowLabel = false
            )
        }
    }
}
