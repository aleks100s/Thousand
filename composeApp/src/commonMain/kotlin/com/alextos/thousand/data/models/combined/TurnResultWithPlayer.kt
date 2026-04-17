package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnResultEntity

data class TurnResultWithPlayer(
    @Embedded val turnResult: TurnResultEntity,
    @Relation(
        entity = PlayerEntity::class,
        parentColumn = "playerId",
        entityColumn = "id",
    )
    val player: PlayerWithUser,
)
