package com.alextos.thousand.data.models.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.DieEntity

data class DiceRollWithDice(
    @Embedded val diceRoll: DiceRollEntity,
    @Relation(
        entity = DieEntity::class,
        parentColumn = "id",
        entityColumn = "rollId",
    )
    val dice: List<DieEntity>,
)