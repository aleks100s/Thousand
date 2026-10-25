@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.alextos.thousand.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberTextShare(): (String) -> Unit {
    val controller = LocalUIViewController.current
    return remember(controller) {
        { text ->
            val presenter = generateSequence(controller) { it.presentedViewController }.last()
            val sheet = UIActivityViewController(listOf(text), null)
            sheet.popoverPresentationController?.apply {
                sourceView = presenter.view
                sourceRect = presenter.view.bounds.useContents { CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0) }
            }
            presenter.presentViewController(sheet, animated = true, completion = null)
        }
    }
}
