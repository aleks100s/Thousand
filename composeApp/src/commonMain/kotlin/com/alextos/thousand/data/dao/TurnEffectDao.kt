package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alextos.thousand.data.models.TurnEffectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnEffectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(effects: List<TurnEffectEntity>)

    @Query(
        """
        SELECT COUNT(*)
        FROM turn_effects
        INNER JOIN players ON players.id = turn_effects.affectedPlayerId
        WHERE players.userId = :userId AND turn_effects.effectType = :effectType
        """,
    )
    fun getEffectsCount(userId: Long, effectType: String): Flow<Int>
}
