package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.DiceRollDao
import com.alextos.thousand.data.dao.DieDao
import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.dao.TurnEffectDao
import com.alextos.thousand.data.dao.TurnResultDao
import com.alextos.thousand.data.dao.UserDao
import com.alextos.thousand.data.mappers.toEntity
import com.alextos.thousand.data.mappers.toDomain
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val userDao: UserDao,
    private val playerDao: PlayerDao,
    private val turnDao: TurnDao,
    private val diceRollDao: DiceRollDao,
    private val dieDao: DieDao,
    private val turnEffectDao: TurnEffectDao,
    private val turnResultDao: TurnResultDao,
) : GameRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { games ->
            games.map { it.toDomain() }
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { users ->
            users.map { it.toDomain() }
        }
    }

    override suspend fun saveUser(user: User) {
        userDao.insert(user.toEntity())
    }

    override suspend fun createGame(game: Game): Game {
        val gameId = gameDao.insert(game.toEntity())
        playerDao.upsert(game.players.map { it.toEntity(gameId = gameId) })
        return game.copy(id = gameId)
    }

    override suspend fun saveGame(game: Game) {
        gameDao.upsert(game.toEntity())
        playerDao.upsert(game.players.map { it.toEntity(gameId = game.id) })
    }

    override suspend fun getGame(id: Long): Game? {
       return gameDao.getGame(id)?.toDomain()
    }

    override suspend fun getAllTurns(gameID: Long): List<Turn> {
        return turnDao.getTurns(gameID).map {
            it.toDomain()
        }
    }

    override suspend fun saveTurn(turn: Turn, game: Game): Turn {
        val turnId = turnDao.insert(
            turn.toEntity(gameId = game.id)
        )

        turn.rolls.forEachIndexed { index, roll ->
            val rollID = diceRollDao.insert(
                roll.toEntity(
                    playerId = turn.player.id,
                    turnId = turnId,
                    order = index
                )
            )

            dieDao.insert(
                roll.dice.mapIndexed { index, die ->
                    die.toEntity(
                        playerId = turn.player.id,
                        rollId = rollID,
                        order = index
                    )
                }
            )
        }

        turnEffectDao.insert(
            turn.effects.mapIndexed { index, effect ->
                effect.toEntity(
                    turnId = turnId,
                    order = index + 1,
                )
            }
        )

        turnResultDao.insert(
            turn.results.map { result ->
                result.toEntity(turnId = turnId)
            }
        )

        return turn.copy(id = turnId)
    }

    override suspend fun deleteGame(gameId: Long) {
        gameDao.delete(gameId)
    }
}
