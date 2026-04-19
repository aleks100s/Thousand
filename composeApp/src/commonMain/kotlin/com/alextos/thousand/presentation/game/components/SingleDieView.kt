package com.alextos.thousand.presentation.game.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.DieValue
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.dice_five
import thousand.composeapp.generated.resources.dice_four
import thousand.composeapp.generated.resources.dice_one
import thousand.composeapp.generated.resources.dice_six
import thousand.composeapp.generated.resources.dice_three
import thousand.composeapp.generated.resources.dice_two

@Composable
fun SingleDieView(
    modifier: Modifier = Modifier.size(32.dp),
    dieValue: DieValue
) {
    Icon(
        modifier = modifier,
        tint = Color.Unspecified,
        painter = painterResource(
            when(dieValue) {
                DieValue.ONE -> Res.drawable.dice_one
                DieValue.TWO -> Res.drawable.dice_two
                DieValue.THREE -> Res.drawable.dice_three
                DieValue.FOUR -> Res.drawable.dice_four
                DieValue.FIVE -> Res.drawable.dice_five
                DieValue.SIX -> Res.drawable.dice_six
            }
        ),
        contentDescription = dieValue.value.toString()
    )
}