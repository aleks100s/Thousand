package com.alextos.thousand.data.mappers.firebase

import com.alextos.thousand.domain.models.DiceRoll

internal fun DiceRoll.toFirebaseMap(): Map<String, Any> =
    buildMap {
        put("id", id)
        put("dice", dice.map { die -> die.toFirebaseMap() })
        put("result", result)

        rollDescription?.let { value ->
            put("rollDescription", value)
        }
    }

internal fun Map<*, *>?.toFirebaseDiceRoll(): DiceRoll =
    DiceRoll(
        id = long("id") ?: 0L,
        dice = this?.get("dice").asFirebaseMapList().map { die -> die.toFirebaseDie() },
        result = int("result") ?: 0,
        rollDescription = string("rollDescription"),
    )
