package com.alextos.thousand.presentation.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import com.alextos.thousand.application.theme.AppColors

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.TextPrimary, unfocusedTextColor = AppColors.TextPrimary,
    focusedBorderColor = AppColors.Accent, unfocusedBorderColor = AppColors.Outline,
    focusedLabelColor = AppColors.Accent, unfocusedLabelColor = AppColors.TextSecondary,
    cursorColor = AppColors.Accent, errorTextColor = AppColors.TextPrimary, errorBorderColor = AppColors.Danger,
    errorLabelColor = AppColors.Danger, errorSupportingTextColor = AppColors.Danger,
    focusedContainerColor = AppColors.SurfaceInset, unfocusedContainerColor = AppColors.SurfaceInset,
)
