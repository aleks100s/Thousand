package com.alextos.thousand.data

import com.alextos.thousand.data.mappers.firebase.FirebaseUserMapper
import com.alextos.thousand.data.mappers.room.toDomain
import com.alextos.thousand.data.mappers.room.toEntity
import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.service.MutableNativeAccountService
import kotlin.test.*

class PlayerAppearancePersistenceTest {
    @Test fun appearanceSurvivesRoomAndMultiplayerRoundTrips() {
        for (avatar in PlayerAvatar.entries) {
            val user = User("id", "Маша", color = PlayerColor.Sage, avatar = avatar)
            assertEquals(user, user.toEntity().toDomain())
            assertEquals(user, FirebaseUserMapper.user(FirebaseUserMapper.dictionary(user)))
            val lobbyUser = FirebaseUserMapper.lobbyPlayer(FirebaseUserMapper.lobbyPlayerDictionary(user))
            assertEquals(user.avatar, lobbyUser.avatar)
            assertEquals(user.color, lobbyUser.color)
        }
    }

    @Test fun profileAppearancePatchPreservesOtherBackendFields() {
        val player = User("id", "Маша", color = PlayerColor.Sage, avatar = PlayerAvatar.Female)
        val patch = FirebaseUserMapper.appearanceDictionary(player)
        assertEquals(mapOf("color" to "Sage", "avatar" to "Female"), patch)
        val previous = mapOf<String, Any>("name" to "Маша", "gameCount" to 12, "winCount" to 7)
        val updated = previous + patch
        assertEquals(12, updated["gameCount"])
        assertEquals(7, updated["winCount"])
        assertEquals(player.color, FirebaseUserMapper.user(updated).color)
        assertEquals(player.avatar, FirebaseUserMapper.user(updated).avatar)
    }

    @Test fun legacyAndUnknownAppearanceUseStableSafeDefaults() {
        val legacy = UserEntity("legacy", "Маша", UserKind.LocalUser.ordinal)
        assertEquals(legacy.toDomain().color, legacy.copy(name = "Мария").toDomain().color)
        assertNotEquals(PlayerColor.Gold, legacy.toDomain().color)
        assertEquals(PlayerColor.Gold, legacy.copy(kind = UserKind.MainUser.ordinal).toDomain().color)
        val malformed = FirebaseUserMapper.user(mapOf("id" to "legacy", "color" to "invalid", "avatar" to "invalid"))
        assertEquals(legacy.toDomain().color, malformed.color)
        assertEquals(PlayerAvatar.Neutral, malformed.avatar)
    }

    @Test fun accountRefreshPreservesAppearanceAndDoesNotLeakItAcrossAccounts() {
        val account = MutableNativeAccountService()
        account.updateUserProfile("one", "Маша")
        account.updateLocalAppearance(User("one", "Маша", UserKind.MainUser, PlayerColor.Gold, PlayerAvatar.Female))
        account.updateUserProfile("one", "Мария")
        assertEquals(PlayerAvatar.Female, account.playerForAccount("one", "Мария").avatar)
        account.updateUserProfile("two", "Олег")
        assertEquals(PlayerAvatar.Neutral, account.playerForAccount("two", "Олег").avatar)
    }
}
