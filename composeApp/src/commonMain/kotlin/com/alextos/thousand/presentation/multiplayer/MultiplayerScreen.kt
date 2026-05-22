package com.alextos.thousand.presentation.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.diversity_3_24px
import thousand.composeapp.generated.resources.person_24px

@Composable
fun MultiplayerScreen() {
    val viewModel: MultiplayerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        modifier = Modifier,
        title = "Мультиплеер",
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MultiplayerHero(state)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrincipleCard(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.diversity_3_24px,
                    title = "Играйте вместе",
                    description = "Создавайте партию и приглашайте друзей, чтобы следить за счетом в одном общем пространстве.",
                )
                PrincipleCard(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.casino_24px,
                    title = "Единые правила",
                    description = "Бочки, болты, яма и обгоны считаются одинаково для всех участников партии.",
                )
            }

            MultiplayerStepsCard()
            MultiplayerActions(state)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MultiplayerHero(
    state: MultiplayerState,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        ),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxSize(),
                        painter = painterResource(Res.drawable.diversity_3_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = "Тысяча без споров о счете",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Text(
                    text = "Мультиплеер поможет собрать игроков в одну партию: один создает игру, остальные подключаются, а приложение синхронизирует ход, броски, эффекты и итоговый счет.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.84f),
                )

                StatusPill(state)
            }
        }
    }
}

@Composable
private fun StatusPill(
    state: MultiplayerState,
) {
    val text = state.userName?.let { userName ->
        "Вы вошли как $userName"
    } ?: "Войдите, чтобы играть онлайн"

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(Res.drawable.person_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PrincipleCard(
    modifier: Modifier,
    icon: DrawableResource,
    title: String,
    description: String,
) {
    Card(
        modifier = modifier.height(184.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(10.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MultiplayerStepsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Как это будет работать",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            MultiplayerStep("1", "Создатель выбирает правила и открывает онлайн-партию.")
            MultiplayerStep("2", "Игроки подключаются к столу и видят общий список участников.")
            MultiplayerStep("3", "Каждый ход синхронизируется: броски, эффекты и счет остаются прозрачными для всех.")
        }
    }
}

@Composable
private fun MultiplayerStep(
    number: String,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = number,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MultiplayerActions(
    state: MultiplayerState,
) {
    if (state.isAuthorized) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {},
            ) {
                Text("Подключиться")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {},
            ) {
                Text("Создать игру")
            }
        }
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
        ) {
            Text("Авторизоваться")
        }
    }
}
