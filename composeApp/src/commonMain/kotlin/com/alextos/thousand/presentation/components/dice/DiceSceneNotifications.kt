package com.alextos.thousand.presentation.components.dice

import com.alextos.thousand.presentation.components.GameMessageBubble
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

fun List<GameMessageBubble>.sceneMessagesJson() = buildJsonArray {
    forEach { message ->
        add(buildJsonObject {
            put("id", message.id.toString())
            put("text", message.text)
            put("kind", when {
                message.isReaction -> "reaction"
                message.isReply -> "reply"
                else -> "notice"
            })
            put("durationMs", if (message.isReply) 8000 else 3000)
        })
    }
}
