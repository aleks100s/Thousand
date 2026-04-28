package com.alextos.thousand.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.User

@Composable
fun FirstUserScreen(
    state: FirstUserState,
    onAction: (FirstUserAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Как вас зовут?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = { value ->
                    onAction(FirstUserAction.UpdateName(value))
                },
                label = {
                    Text("Имя")
                },
                singleLine = true,
                enabled = state.isSaving.not(),
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.name.isNotBlank() && state.isSaving.not(),
                onClick = {
                    onAction(FirstUserAction.SaveUser)
                },
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Продолжить")
                }
            }

            if (state.localUsers.isNotEmpty()) {
                ExistingUsersSection(
                    users = state.localUsers,
                    isEnabled = state.isSaving.not(),
                    onUserClick = { user ->
                        onAction(FirstUserAction.SelectExistingUser(user))
                    },
                )
            }
        }
    }
}

@Composable
private fun ExistingUsersSection(
    users: List<User>,
    isEnabled: Boolean,
    onUserClick: (User) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Выбрать существующего игрока",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(users) { user ->
            ExistingUserItem(
                user = user,
                isEnabled = isEnabled,
                onClick = {
                    onUserClick(user)
                },
            )
        }
    }
}

@Composable
private fun ExistingUserItem(
    user: User,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEnabled,
                onClick = onClick,
            ),
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = user.name,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
