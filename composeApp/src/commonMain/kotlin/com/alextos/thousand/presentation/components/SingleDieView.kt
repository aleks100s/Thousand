package com.alextos.thousand.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.DieValue
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.die_5
import thousand.composeapp.generated.resources.die_4
import thousand.composeapp.generated.resources.die_1
import thousand.composeapp.generated.resources.die_6
import thousand.composeapp.generated.resources.die_3
import thousand.composeapp.generated.resources.die_2

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
                DieValue.ONE -> Res.drawable.die_1
                DieValue.TWO -> Res.drawable.die_2
                DieValue.THREE -> Res.drawable.die_3
                DieValue.FOUR -> Res.drawable.die_4
                DieValue.FIVE -> Res.drawable.die_5
                DieValue.SIX -> Res.drawable.die_6
            }
        ),
        contentDescription = dieValue.value.toString()
    )
}