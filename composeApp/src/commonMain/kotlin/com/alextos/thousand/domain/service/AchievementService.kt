package com.alextos.thousand.domain.service

import com.alextos.thousand.domain.models.AchievementId
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.OnlineAchievementProgress
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.repository.AchievementStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

class AchievementService(
    private val store: AchievementStore,
) {
    private val notifications = Channel<AchievementId>(Channel.UNLIMITED)
    private val onlineProgressMutex = Mutex()

    val unlockNotifications: Flow<AchievementId> = notifications.receiveAsFlow()

    val unlockedAchievements: Flow<Map<AchievementId, Instant>> =
        store.observeUnlockedAchievements().map { achievements ->
            achievements.associate { it.id to it.unlockedAt }
        }

    suspend fun onLocalTurnSaved(
        game: Game,
        turn: Turn,
        loadTurns: suspend () -> List<Turn>,
    ): List<AchievementId> {
        val userId = game.players.firstOrNull { it.isMain() }?.user?.id ?: return emptyList()
        val eligible = buildSet {
            if (turn.player.user.id == userId) {
                addTurnScore(turn.total)
                turn.rolls.forEachIndexed { index, roll -> addRoll(roll, index + 1) }
            }
            turn.results.firstOrNull { it.player.user.id == userId }?.let { addGameScore(it.newScore) }
            if (turn.hasPitFall(userId) && loadTurns().count { it.hasPitFall(userId) } >= 2) {
                add(AchievementId.TwoPitFalls)
            }
        }
        return unlock(userId, eligible)
    }

    suspend fun onLocalGameFinished(game: Game, turns: List<Turn>): List<AchievementId> {
        val player = game.players.firstOrNull { it.isMain() } ?: return emptyList()
        val userId = player.user.id
        val personalTurns = turns.filter { it.player.user.id == userId }
        val eligible = buildSet {
            if (player.isWinner) {
                if (game.players.any { it.isBot() }) add(AchievementId.BotWin)
                if (turns.any { it.hasPitFall(userId) }) add(AchievementId.WinAfterPit)
            }
            addGameFinish(
                isWinner = player.isWinner,
                personalTurns = personalTurns.size,
                hadZeroTurn = personalTurns.any { it.total == 0 },
            )
        }
        return unlock(userId, eligible)
    }

    suspend fun onOnlineRollSaved(
        playerId: String,
        accountId: String,
        roll: DiceRoll,
        rollCount: Int,
    ) {
        if (playerId != accountId) return
        notifyUnlocked(unlock(accountId, buildSet { addRoll(roll, rollCount) }))
    }

    suspend fun onOnlineTurnSaved(game: RemoteGame, accountId: String) {
        val turn = game.achievementTurn ?: return
        val player = game.players.firstOrNull { it.user.id == accountId } ?: return
        if (turn.number == 0 || turn.playerId == null) return

        onlineProgressMutex.withLock {
            val previous = store.getOnlineProgress(accountId, turn.roundId)
            val isPersonalTurn = turn.playerId == accountId
            val fellIntoPit = accountId in turn.pitPlayerIds
            val progress = if (turn.number in previous.observedTurns) {
                previous
            } else {
                previous.copy(
                    observedTurns = previous.observedTurns + turn.number,
                    personalTurns = previous.personalTurns + if (isPersonalTurn) 1 else 0,
                    pitFalls = previous.pitFalls + if (fellIntoPit) 1 else 0,
                    hadZeroTurn = previous.hadZeroTurn || (isPersonalTurn && turn.total == 0),
                ).also { store.saveOnlineProgress(accountId, turn.roundId, it) }
            }
            val eligible = buildSet {
                if (isPersonalTurn) addTurnScore(turn.total)
                addGameScore(player.currentScore)
                if (progress.pitFalls >= 2) add(AchievementId.TwoPitFalls)
            }
            notifyUnlocked(unlock(accountId, eligible))
        }
    }

    suspend fun onOnlineGameFinished(game: RemoteGame, accountId: String) {
        val player = game.players.firstOrNull { it.user.id == accountId } ?: return
        val finalTurn = game.achievementTurn ?: return
        if (!game.isFinished() || game.finishedAt == null || finalTurn.nextPlayerIndex != -1) return

        // The final turn is part of the confirmed result and can be saved again on reconnect.
        onOnlineTurnSaved(game, accountId)
        onlineProgressMutex.withLock {
            val progress = store.getOnlineProgress(accountId, finalTurn.roundId)
            val eligible = buildSet {
                if (player.isWinner) {
                    add(AchievementId.MultiplayerWin)
                    if (progress.pitFalls > 0) add(AchievementId.WinAfterPit)
                }
                if (hasAllPersonalTurns(game, accountId, progress)) {
                    addGameFinish(player.isWinner, progress.personalTurns, progress.hadZeroTurn)
                }
            }
            notifyUnlocked(unlock(accountId, eligible))
        }
    }

    private fun hasAllPersonalTurns(
        game: RemoteGame,
        userId: String,
        progress: OnlineAchievementProgress,
    ): Boolean {
        val finalTurn = game.achievementTurn ?: return false
        if (!finalTurn.hasCompleteNumbering) return false
        val playerIndex = game.players.indexOfFirst { it.user.id == userId }
        val playerCount = game.players.size
        val firstPersonalTurn = playerIndex + 1
        if (playerIndex < 0 || firstPersonalTurn > finalTurn.number) return false

        // Turns rotate from seat zero. Missing an opponent's turn does not invalidate personal history.
        val expectedCount = (finalTurn.number - firstPersonalTurn) / playerCount + 1
        return progress.personalTurns == expectedCount &&
            progress.observedTurns.count {
                it in firstPersonalTurn..finalTurn.number && (it - firstPersonalTurn) % playerCount == 0
            } == expectedCount
    }

    fun notifyUnlocked(ids: List<AchievementId>) {
        ids.forEach { notifications.trySend(it) }
    }

    private suspend fun unlock(userId: String, eligible: Set<AchievementId>): List<AchievementId> =
        eligible.filter { store.unlockForMainUser(userId, it) }

    private fun MutableSet<AchievementId>.addRoll(roll: DiceRoll, rollCount: Int) {
        if (rollCount >= 5) add(AchievementId.RollChain5)
        if (rollCount >= 10) add(AchievementId.RollChain10)
        roll.dice.groupingBy { it.value }.eachCount().forEach { (value, count) ->
            if (count >= 5) add(diceAchievements[value.ordinal])
        }
    }

    private fun MutableSet<AchievementId>.addTurnScore(total: Int) {
        turnScoreAchievements.forEach { (threshold, achievement) ->
            if (total >= threshold) add(achievement)
        }
    }

    private fun MutableSet<AchievementId>.addGameScore(score: Int) {
        if (score <= -1000) add(AchievementId.GameScoreMinus1000)
        if (score >= 2000) add(AchievementId.GameScore2000)
    }

    private fun MutableSet<AchievementId>.addGameFinish(
        isWinner: Boolean,
        personalTurns: Int,
        hadZeroTurn: Boolean,
    ) {
        if (isWinner && personalTurns in 1..10) add(AchievementId.WinWithinTenTurns)
        if (isWinner && personalTurns == 1) add(AchievementId.WinOnFirstTurn)
        if (personalTurns > 0 && !hadZeroTurn) add(AchievementId.FinishWithoutZero)
    }

    private fun Turn.hasPitFall(userId: String): Boolean =
        effects.any { it.effect == Effect.PIT_FALL && it.affectedPlayer.user.id == userId }

    private companion object {
        val diceAchievements = listOf(
            AchievementId.FiveOnes,
            AchievementId.FiveTwos,
            AchievementId.FiveThrees,
            AchievementId.FiveFours,
            AchievementId.FiveFives,
            AchievementId.FiveSixes,
        )

        val turnScoreAchievements = listOf(
            200 to AchievementId.TurnScore200,
            300 to AchievementId.TurnScore300,
            400 to AchievementId.TurnScore400,
            500 to AchievementId.TurnScore500,
            1000 to AchievementId.TurnScore1000,
        )
    }
}
