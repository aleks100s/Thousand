package com.alextos.thousand.domain.service

import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.AchievementStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Instant

class AchievementService(
    private val store: AchievementStore,
) {
    private val notifications = Channel<AchievementId>(Channel.BUFFERED)

    val unlockNotifications: Flow<AchievementId> = notifications.receiveAsFlow()

    val unlockedAchievements: Flow<Map<AchievementId, Instant>> =
        store.observeUnlockedAchievements().map { achievements ->
            achievements.associate { it.id to it.unlockedAt }
        }

    suspend fun onLocalTurnSaved(turn: Turn): List<AchievementId> =
        unlockFor(Trigger.TurnSaved, turn.player.user.id)

    suspend fun onLocalGameFinished(winnerId: String?): List<AchievementId> =
        unlockFor(Trigger.GameWon, winnerId)

    suspend fun onOnlineTurnSaved(playerId: String, accountId: String): List<AchievementId> {
        if (playerId != accountId) return emptyList()
        return unlockFor(Trigger.TurnSaved, playerId).also(::notifyUnlocked)
    }

    fun notifyUnlocked(ids: List<AchievementId>) {
        ids.forEach { notifications.trySend(it) }
    }

    suspend fun onOnlineGameFinished(
        winnerId: String,
        accountId: String,
        finishedAt: Instant?,
    ): List<AchievementId> {
        if (winnerId != accountId) return emptyList()
        if (finishedAt == null || finishedAt <= AchievementActivationTime) return emptyList()

        return unlockFor(Trigger.GameWon, winnerId).also(::notifyUnlocked)
    }

    private suspend fun unlockFor(trigger: Trigger, userId: String?): List<AchievementId> {
        if (userId.isNullOrBlank()) return emptyList()
        val eligible = when (trigger) {
            Trigger.TurnSaved -> emptyList()
            Trigger.GameWon -> listOf(AchievementId.FirstWin)
        }
        return eligible.filter { store.unlockForMainUser(userId, it) }
    }

    private enum class Trigger {
        TurnSaved,
        GameWon,
    }

    private companion object {
        val AchievementActivationTime: Instant = Instant.parse("2026-11-28T00:00:00Z")
    }
}
