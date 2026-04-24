package com.alextos.thousand.presentation.game.play_game.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.presentation.game.components.SingleDieView

@Composable
fun ManualDiceInputView(
    count: Int,
    onSubmit: (List<Die>) -> Unit
) {
    val dice = remember { mutableStateListOf<Die>() }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Результат:")

            dice.forEach {
                SingleDieView(dieValue = it.value)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            DieValue.entries.forEach { dieValue ->
                SingleDieView(
                    modifier = Modifier
                        .clickable(
                            enabled = dice.count() < count,
                            onClick = {
                                dice.add(Die(value = dieValue))
                            }
                        )
                        .size(40.dp),
                    dieValue = dieValue
                )
            }
        }

        Button(
            onClick = {
                onSubmit(dice)
            },
            enabled = dice.count() == count
        ) {
            Text("Готово")
        }
    }
}