package com.alextos.thousand.domain.models

import com.alextos.thousand.application.invites.LobbyLinks
import kotlin.test.*

class LobbyCodeTest {
    @Test fun generatedCodesHaveFiveLatinLettersAndDigits() {
        repeat(2000) {
            val code = LobbyCode.generate()
            assertEquals(5, code.length)
            assertTrue(code.all { it in 'A'..'Z' || it in '0'..'9' })
            assertTrue(code.any { it in 'A'..'Z' })
            assertTrue(code.any { it in '0'..'9' })
            assertEquals(code, LobbyLinks.parse(LobbyLinks.create(code)))
        }
    }

    @Test fun newCodesIgnoreInputCaseAndWhitespaceWithoutBreakingLegacyCodes() {
        assertEquals("A7K2M", LobbyCode.normalize(" a7k2m "))
        assertEquals("1234", LobbyCode.normalize(" 1234 "))
        assertEquals("AbC123-x_y", LobbyCode.normalize("AbC123-x_y"))
    }
}
