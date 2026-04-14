package com.alextos.thousand.application.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
) {
    NavigationBar {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Text(tab.iconLabel) },
                label = { Text(tab.title) },
            )
        }
    }
}
