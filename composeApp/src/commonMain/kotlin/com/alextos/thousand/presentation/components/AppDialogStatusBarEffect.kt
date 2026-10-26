package com.alextos.thousand.presentation.components

import androidx.compose.runtime.Composable

/** Android dialogs have their own windows and must also hide the status bar. */
@Composable
internal expect fun AppDialogStatusBarEffect()
