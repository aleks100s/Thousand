package com.alextos.thousand.domain.models

/** Shared by Android and iOS; legacy invitation codes remain usable. */
object LobbyCode {
    private const val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val newCodePattern = Regex("[A-Za-z0-9]{5}")

    fun generate(): String {
        while (true) {
            val code = buildString { repeat(5) { append(alphabet.random()) } }
            if (code.any { it in 'A'..'Z' } && code.any { it in '0'..'9' }) return code
        }
    }

    fun normalize(value: String): String {
        val code = value.trim()
        return if (newCodePattern.matches(code)) code.uppercase() else code
    }
}
