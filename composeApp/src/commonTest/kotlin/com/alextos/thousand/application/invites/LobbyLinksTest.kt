package com.alextos.thousand.application.invites

import kotlin.test.*

class LobbyLinksTest {
    @Test
    fun sharedLinkRoundTripsWithoutChangingCodeCase() {
        assertEquals("AbC123-x_y", LobbyLinks.parse(LobbyLinks.create("AbC123-x_y")))
        assertEquals("AbC123", LobbyLinks.parse("THOUSAND://LOBBY/AbC123/"))
    }

    @Test
    fun unrelatedAndMalformedLinksAreIgnored() {
        listOf("https://example.com/lobby/ABC", "thousand://game/ABC", "thousand://lobby/",
            "thousand://lobby/ABC/DEF", "thousand://lobby/ABC?next=bad", "thousand://lobby/%2F", "thousand://lobby/<script>")
            .forEach { assertNull(LobbyLinks.parse(it), it) }
        assertFailsWith<IllegalArgumentException> { LobbyLinks.create("A/B") }
    }

    @Test
    fun newestInvitationWinsAndStaleAcknowledgementCannotClearIt() {
        assertTrue(LobbyInvitations.open("thousand://lobby/ONE"))
        val first = LobbyInvitations.pending.value!!
        assertFalse(LobbyInvitations.open("https://unrelated.example"))
        assertEquals(first, LobbyInvitations.pending.value)
        LobbyInvitations.open("thousand://lobby/TWO")
        val second = LobbyInvitations.pending.value!!
        LobbyInvitations.consume(first.id)
        assertEquals(second, LobbyInvitations.pending.value)
        LobbyInvitations.consume(second.id)
        assertNull(LobbyInvitations.pending.value)
    }
}
