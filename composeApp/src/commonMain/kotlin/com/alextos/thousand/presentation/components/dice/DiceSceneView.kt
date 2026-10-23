package com.alextos.thousand.presentation.components.dice

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

/** Coordinates are fractions of the WebView; independent of pixel density and browser viewport. */
data class DiceSceneBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)

data class DiceSceneConfiguration(
    val request: DiceSceneRequest?,
    val background: String,
    val bounds: DiceSceneBounds,
    val active: Boolean,
    val isOpponentTurn: Boolean = false,
) {
    val promptText: String get() = if (isOpponentTurn) "ход противника" else "продолжить или сохранить?"

    fun script(): String {
        val message = buildJsonObject {
            put("active", active)
            put("promptText", promptText)
            put("background", background)
            put("bounds", buildJsonObject {
                put("left", bounds.left)
                put("top", bounds.top)
                put("right", bounds.right)
                put("bottom", bounds.bottom)
            })
            put("request", request?.let { request ->
                buildJsonObject {
                    put("presentationId", request.presentationId)
                    put("animate", request.animate)
                    put("scoreText", request.scoreText)
                    put("values", buildJsonArray { request.values.forEach { add(it) } })
                    put("scoringIndices", buildJsonArray { request.scoringIndices.forEach { add(it) } })
                }
            } ?: JsonNull)
        }.toString()
        // Serialize twice: the JS argument is a JSON string, never executable input.
        return "window.DiceScene && window.DiceScene.receive(${JsonPrimitive(message)});"
    }
}

fun decodeDiceSceneEvent(message: String): DiceSceneEvent? = runCatching {
    val json = Json.parseToJsonElement(message).jsonObject
    val id = json["presentationId"]?.jsonPrimitive?.contentOrNull
    when (json["type"]?.jsonPrimitive?.contentOrNull) {
        "ready" -> DiceSceneEvent.Ready
        "rollStarted" -> id?.let { DiceSceneEvent.RollStarted(it) }
        "rollFinished" -> id?.let { DiceSceneEvent.RollFinished(it) }
        "error" -> DiceSceneEvent.Error(id)
        else -> null
    }
}.getOrNull()

@Composable
expect fun DiceSceneView(
    html: String,
    configuration: DiceSceneConfiguration,
    onEvent: (DiceSceneEvent) -> Unit,
    modifier: Modifier = Modifier,
)
