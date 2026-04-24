package com.alextos.thousand.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT

@Composable
fun GameRulesView() {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Правила игры",
            style = MaterialTheme.typography.titleLarge,
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Открытие игры", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$STARTING_LIMIT очков")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Первая бочка", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$BARREL_1")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Вторая бочка", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$BARREL_2")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Третья бочка", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$BARREL_3")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Штраф за 3 подряд нулевых хода", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$BOLT_FINE")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Штраф за обгон", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("$OVERTAKE_FINE")
            }
        }
    }
}
