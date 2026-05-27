package com.alextos.thousand.domain.usecase.statistics

import com.alextos.thousand.domain.models.Effect
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.repository.StatisticsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class EventsStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<EventsStatistics> {
        return statisticsRepository.getAllUsers().flatMapLatest { users ->
            val sortedUsers = users
                .distinctBy { user -> user.id }
                .sortedBy { user -> user.name }

            if (sortedUsers.isEmpty()) {
                flowOf(EventsStatistics())
            } else {
                combine(sortedUsers.toEventCountFlows()) { eventCounts ->
                    eventCounts.toStatistics()
                }
            }
        }
    }

    private fun List<User>.toEventCountFlows(): List<Flow<EventCount>> {
        return flatMap { user ->
            statisticsEvents.map { event ->
                statisticsRepository.getTurnEffectCount(user.id, event).map { count ->
                    EventCount(
                        user = user,
                        effect = event,
                        count = count,
                    )
                }
            }
        }
    }

    private fun Array<EventCount>.toStatistics(): EventsStatistics {
        val players = groupBy { eventCount -> eventCount.user.id }
            .map { (_, eventCounts) ->
                val user = eventCounts.first().user

                PlayerWithEventsStatistics(
                    userId = user.id,
                    userName = user.name,
                    pitFalls = eventCounts.countOf(Effect.PIT_FALL),
                    overtakes = eventCounts.countOf(Effect.OVERTAKE),
                    tripleBolts = eventCounts.countOf(Effect.TRIPLE_BOLT),
                )
            }
            .sortedWith(
                compareByDescending<PlayerWithEventsStatistics> { it.total }
                    .thenBy { it.userName },
            )

        return EventsStatistics(
            pitFalls = players.sumOf { player -> player.pitFalls },
            overtakes = players.sumOf { player -> player.overtakes },
            tripleBolts = players.sumOf { player -> player.tripleBolts },
            players = players,
        )
    }

    private fun List<EventCount>.countOf(effect: Effect): Int {
        return firstOrNull { eventCount -> eventCount.effect == effect }?.count ?: 0
    }

    private companion object {
        val statisticsEvents = listOf(
            Effect.PIT_FALL,
            Effect.OVERTAKE,
            Effect.TRIPLE_BOLT,
        )
    }
}

data class EventsStatistics(
    val pitFalls: Int = 0,
    val overtakes: Int = 0,
    val tripleBolts: Int = 0,
    val players: List<PlayerWithEventsStatistics> = emptyList(),
)

data class PlayerWithEventsStatistics(
    val userId: String,
    val userName: String,
    val pitFalls: Int,
    val overtakes: Int,
    val tripleBolts: Int,
) {
    val total: Int
        get() = pitFalls + overtakes + tripleBolts
}

private data class EventCount(
    val user: User,
    val effect: Effect,
    val count: Int,
)
