package com.alextos.thousand.data.mappers

import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.DieEntity
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieResult
import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnEffect
import com.alextos.thousand.domain.models.User

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
)

fun GameEntity.toDomain(
    players: List<Player> = emptyList(),
    turns: List<Turn> = emptyList(),
): Game = Game(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    players = players,
    turns = turns,
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    userId = userID,
    gameId = gameID,
    currentScore = currentScore,
    isWinner = isWinner,
)

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    userID = userId,
    gameID = gameId,
    currentScore = currentScore,
    isWinner = isWinner,
)

fun Turn.toEntity(): TurnEntity = TurnEntity(
    id = id,
    playerId = playerID,
    gameId = gameID,
    order = order,
    total = total,
)

fun TurnEntity.toDomain(
    rolls: List<DiceRoll> = emptyList(),
    effects: List<Effect> = emptyList(),
): Turn = Turn(
    id = id,
    playerID = playerId,
    gameID = gameId,
    order = order,
    rolls = rolls,
    total = total,
    effects = effects,
)

fun DiceRoll.toEntity(): DiceRollEntity = DiceRollEntity(
    id = id,
    playerId = playerID,
    turnId = turnID,
    order = order,
    total = total,
)

fun DiceRollEntity.toDomain(
    dice: List<DieResult> = emptyList(),
): DiceRoll = DiceRoll(
    id = id,
    playerID = playerId,
    turnID = turnId,
    order = order,
    dice = dice,
    total = total,
)

fun DieResult.toEntity(): DieEntity = DieEntity(
    id = id,
    playerId = playerID,
    rollId = rollID,
    order = order,
    value = die.value,
)

fun DieEntity.toDomain(): DieResult = DieResult(
    id = id,
    playerID = playerId,
    rollID = rollId,
    order = order,
    die = value.toDomainDie(),
)

fun TurnEffect.toEntity(): TurnEffectEntity = TurnEffectEntity(
    id = id,
    turnId = turnID,
    effectType = effect.toStorageValue(),
    affectedPlayerId = affectedPlayerID,
)

fun TurnEffectEntity.toDomain(
    penaltyValue: Int = 0,
): TurnEffect = TurnEffect(
    id = id,
    turnID = turnId,
    affectedPlayerID = affectedPlayerId,
    effect = effectType.toDomainEffect(),
    penaltyValue = penaltyValue,
)

private fun Int.toDomainDie(): Die = when (this) {
    1 -> Die.ONE
    2 -> Die.TWO
    3 -> Die.THREE
    4 -> Die.FOUR
    5 -> Die.FIVE
    6 -> Die.SIX
    else -> error("Unsupported die value: $this")
}

private fun Effect.toStorageValue(): String = when (this) {
    Effect.OVERTAKE -> "OVERTAKE"
    Effect.SKI_FALL -> "SKI_FALL"
    Effect.PIT_FALL -> "PIT_FALL"
    Effect.BARREL_LIMIT -> "BARREL_LIMIT"
}

private fun String.toDomainEffect(): Effect = when (this) {
    "OVERTAKE" -> Effect.OVERTAKE
    "SKI_FALL" -> Effect.SKI_FALL
    "PIT_FALL" -> Effect.PIT_FALL
    "BARREL_LIMIT" -> Effect.BARREL_LIMIT
    else -> error("Unsupported effect type: $this")
}
