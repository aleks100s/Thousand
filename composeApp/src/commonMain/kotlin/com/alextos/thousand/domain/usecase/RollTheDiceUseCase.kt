package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue

class RollTheDiceUseCase {
    operator fun invoke(count: Int): List<Die> {
        return (1..count).map { _ ->
            Die(value = DieValue.entries.random())
        }
    }
}