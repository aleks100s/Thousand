package com.alextos.thousand.presentation.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberTextShare(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }, "Пригласить в игру"))
        }
    }
}
