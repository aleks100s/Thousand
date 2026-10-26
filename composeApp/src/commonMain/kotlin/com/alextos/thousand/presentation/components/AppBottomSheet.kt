package com.alextos.thousand.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.alextos.thousand.application.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetGesturesEnabled: Boolean = true,
    shape: Shape = AppShapes.Sheet,
    containerColor: Color = AppColors.Sheet,
    contentColor: Color = AppColors.TextPrimary,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle(color = AppColors.Outline) },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState,
        sheetGesturesEnabled = sheetGesturesEnabled, shape = shape, containerColor = containerColor,
        contentColor = contentColor, dragHandle = dragHandle) {
        AppDialogStatusBarEffect()
        content()
    }
}
