package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.UserEntity

data class PlayerWithUser(
    @Embedded val player: PlayerEntity,
    @Relation(
        entity = UserEntity::class,
        parentColumn = "userId",
        entityColumn = "id",
    )
    val user: UserEntity,
)