package com.alextos.thousand.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.DieEntity
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.UserEntity

@Dao
interface ThousandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: TurnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoll(roll: DiceRollEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDie(die: DieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurnEffect(effect: TurnEffectEntity)
}
