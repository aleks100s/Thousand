package com.alextos.thousand.data.repository

import com.alextos.thousand.data.dao.DiceRollDao
import com.alextos.thousand.data.dao.DieDao
import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.dao.TurnDao
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

    override suspend fun saveUsers(users: List<User>) {
        userDao.insert(users.map { it.toEntity() })
    }

    override suspend fun saveGame(game: Game): Long {
        val gameID = gameDao.insert(game.toEntity())
        playerDao.insert(game.players.map { it.toEntity(gameId = gameID) })
        return gameID
    }

    override suspend fun getGame(id: Long): Game? {
       return gameDao.getGame(id)?.toDomain()
    }

    override suspend fun getAllTurns(gameID: Long): List<Turn> {
        return turnDao.getTurns(gameID).map {
            it.toDomain()
        }
    }

    override suspend fun saveTurn(turn: Turn, game: Game): Long {
        val turnID = turnDao.insert(
            turn.toEntity(gameId = game.id)
        )

        turn.rolls.forEach { roll ->
            val rollID = diceRollDao.insert(
                roll.toEntity(
                    userId = turn.user.id,
                    turnId = turnID
                )
            )

            roll.dice.forEach { die ->
                dieDao.insert(
                    die.toEntity(
                        userId = turn.user.id,
                        rollId = rollID
                    )
                )
            }
        }

        return turnID
    }
}
