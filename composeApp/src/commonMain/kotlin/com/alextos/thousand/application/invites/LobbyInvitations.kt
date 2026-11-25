package com.alextos.thousand.application.invites

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object LobbyLinks {
    private const val invitePrefix = "https://thousand-688b0.web.app/lobby/"
    private val codePattern = Regex("[A-Za-z0-9_-]{1,128}")
    private val legacyLinkPattern = Regex("thousand://lobby/([A-Za-z0-9_-]{1,128})/?", RegexOption.IGNORE_CASE)

    fun create(code: String): String {
        require(codePattern.matches(code)) { "Invalid lobby code" }
        return "$invitePrefix$code"
    }

    fun parse(url: String): String? {
        if (url.startsWith(invitePrefix)) {
            return url.removePrefix(invitePrefix).removeSuffix("/").takeIf(codePattern::matches)
        }
        return legacyLinkPattern.matchEntire(url)?.groupValues?.get(1)
    }
}

data class LobbyInvitation(val code: String, val id: Long)

/** Holds invitations across launch/onboarding until the navigation graph can accept them. */
object LobbyInvitations {
    private val _pending = MutableStateFlow<LobbyInvitation?>(null)
    val pending = _pending.asStateFlow()

    fun open(url: String): Boolean {
        val code = LobbyLinks.parse(url) ?: return false
        _pending.value = LobbyInvitation(code, Random.nextLong())
        return true
    }

    fun consume(id: Long) {
        if (_pending.value?.id == id) _pending.value = null
    }
}
