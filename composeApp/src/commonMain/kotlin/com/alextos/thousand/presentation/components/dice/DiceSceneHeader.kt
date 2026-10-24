package com.alextos.thousand.presentation.components.dice

import com.alextos.thousand.domain.GameConstants
import com.alextos.thousand.domain.usecase.game.server.GameState
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

data class DiceScenePlayer(
    val id: String,
    val name: String,
    val score: Int,
    val bot: Boolean,
    val winner: Boolean,
    val bolts: Int,
    val online: Boolean?,
    val reactionId: String?,
    val reaction: String?,
)

data class DiceSceneHeader(
    val gameId: Long,
    val players: List<DiceScenePlayer>,
    val activePlayerId: String?,
    val barrelBoundaries: List<Int>,
    val clickable: Boolean,
) {
    fun json() = buildJsonObject {
        put("gameId", gameId.toString())
        put("activePlayerId", activePlayerId)
        put("goal", GameConstants.GAME_GOAL)
        put("clickable", clickable)
        put("barrelBoundaries", buildJsonArray { barrelBoundaries.forEach { add(it) } })
        put("players", buildJsonArray {
            players.forEach { player ->
                add(buildJsonObject {
                    put("id", player.id)
                    put("name", player.name)
                    put("score", player.score)
                    put("bot", player.bot)
                    put("winner", player.winner)
                    put("bolts", player.bolts)
                    player.online?.let { put("online", it) }
                    put("reactionId", player.reactionId)
                    put("reaction", player.reaction)
                })
            }
        })
    }

    companion object {
        fun from(state: GameState, clickable: Boolean): DiceSceneHeader? {
            val game = state.game ?: return null
            val boundaries = buildList {
                if (game.settings.isBarrel1Active) addAll(listOf(GameConstants.BARREL_1.first, GameConstants.BARREL_1.last + 1))
                if (game.settings.isBarrel2Active) addAll(listOf(GameConstants.BARREL_2.first, GameConstants.BARREL_2.last + 1))
                if (game.settings.isBarrel3Active) addAll(listOf(GameConstants.BARREL_3.first, GameConstants.BARREL_3.last + 1))
            }
            return DiceSceneHeader(
                gameId = game.id,
                players = game.players.map { player ->
                    val reaction = state.reactionsByAuthorId[player.user.id]
                    DiceScenePlayer(
                        player.user.id, player.user.name, player.currentScore, player.isBot(), player.isWinner,
                        if (game.settings.isTripleBoltFineActive && !game.isFinished()) player.boltCount else 0,
                        if (state.isOnlineGame && !player.isBot()) player.user.id in state.onlinePlayerIds else null,
                        reaction?.id, reaction?.reaction,
                    )
                },
                activePlayerId = state.currentPlayer?.user?.id,
                barrelBoundaries = boundaries,
                clickable = clickable,
            )
        }
    }
}
