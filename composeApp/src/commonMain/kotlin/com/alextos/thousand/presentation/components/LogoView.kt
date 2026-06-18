package com.alextos.thousand.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.logo

@Composable
fun LogoView(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.width(200.dp),
        painter = painterResource(Res.drawable.logo),
        contentDescription = "Логотип Thousand",
    )
}
