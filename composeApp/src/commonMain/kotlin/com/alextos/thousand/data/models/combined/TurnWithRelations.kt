package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.User

data class TurnWithRelations(
    @Embedded val turn: TurnEntity,
    @Relation(
        entity = UserEntity::class,
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User,
    @Relation(
        entity = DiceRollEntity::class,
        parentColumn = "id",
        entityColumn = "turnId",
    )
    val rolls: List<DiceRollWithDice>,
    @Relation(
        entity = TurnEffectEntity::class,
        parentColumn = "id",
        entityColumn = "turnId",
    )
    val effects: List<TurnEffectWithPlayer>,
)