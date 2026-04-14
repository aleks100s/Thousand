package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.combined.PlayerWithUser
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.combined.TurnWithRelations

data class GameWithRelations(
    @Embedded val game: GameEntity,
    @Relation(
        entity = PlayerEntity::class,
        parentColumn = "id",
        entityColumn = "gameId",
    )
    val players: List<PlayerWithUser>,
)