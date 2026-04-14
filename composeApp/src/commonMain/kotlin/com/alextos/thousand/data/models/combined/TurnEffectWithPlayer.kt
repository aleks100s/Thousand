package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity

data class TurnEffectWithPlayer(
    @Embedded val turnEffect: TurnEffectEntity,
    @Relation(
        entity = PlayerEntity::class,
        parentColumn = "affectedPlayerId",
        entityColumn = "id",
    )
    val affectedPlayer: PlayerWithUser?,
)