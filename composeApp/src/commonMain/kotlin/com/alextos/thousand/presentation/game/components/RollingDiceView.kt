package com.alextos.thousand.presentation.game.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import kotlinx.coroutines.delay

@Composable
fun RollingDiceView(die: Die, delay: Long) {
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

    SingleDieView(dieValue = currentDie)
}