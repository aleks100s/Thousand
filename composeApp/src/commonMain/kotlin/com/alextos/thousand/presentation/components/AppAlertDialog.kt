package com.alextos.thousand.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Keeps native dialog windows consistent with the app's fullscreen status bar. */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    containerColor: Color = AlertDialogDefaults.containerColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            AppDialogStatusBarEffect()
            confirmButton()
        },
        dismissButton = dismissButton,
        title = title,
        text = text,
        containerColor = containerColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
    )
}
