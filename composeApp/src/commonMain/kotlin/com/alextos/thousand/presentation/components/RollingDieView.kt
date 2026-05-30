package com.alextos.thousand.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import kotlinx.coroutines.delay

@Composable
fun RollingDieView(die: Die, delay: Long) {
    var currentDie by remember { mutableStateOf(DieValue.entries.random()) }
    var currentDelay by remember { mutableStateOf(0L) }

    LaunchedEffect(die) {
        currentDie = DieValue.entries.random()
        currentDelay = 0L
        while (currentDelay < delay) {
            delay(50L)
            currentDelay += 50
            currentDie = DieValue.entries.random()
        }

        currentDie = die.value
    }

    SingleDieView(dieValue = currentDie, modifier = Modifier.clip(RoundedCornerShape(4.dp)).size(48.dp))
}