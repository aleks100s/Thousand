package com.alextos.thousand.data

import com.alextos.thousand.data.mappers.firebase.FirebaseUserMapper
import com.alextos.thousand.data.mappers.room.toDomain
import com.alextos.thousand.data.mappers.room.toEntity
import com.alextos.thousand.data.models.UserEntity
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.service.MutableNativeAccountService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

class PlayerAppearancePersistenceTest {
    @Test fun appearanceSurvivesRoomAndMultiplayerRoundTrips() {
        for (color in PlayerColor.entries) {
            for (avatar in PlayerAvatar.entries) {
                val user = User("id", "Маша", color = color, avatar = avatar)
                assertEquals(user, user.toEntity().toDomain())
                assertEquals(user, FirebaseUserMapper.user(FirebaseUserMapper.dictionary(user)))
                val lobbyUser = FirebaseUserMapper.lobbyPlayer(FirebaseUserMapper.lobbyPlayerDictionary(user))
                assertEquals(avatar, lobbyUser.avatar)
                assertEquals(color, lobbyUser.color)
            }
        }
    }

    @Test fun legacyAndUnknownAppearanceUseStableSafeDefaults() {
        val legacy = UserEntity("legacy", "Маша", UserKind.LocalUser.ordinal)
        assertEquals(PlayerColor.Terracotta, legacy.toDomain().color)
        assertEquals(PlayerColor.Terracotta, legacy.copy(name = "Мария").toDomain().color)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun profileDeletionWaitsForInFlightSignInAndClearsLocalDataAfterAccountDeletion() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val signInGate = CompletableDeferred<Unit>()
            val events = mutableListOf<String>()
            val account = object : MutableNativeAccountService() {
                override suspend fun signInAnonymouslyPlatform() {
                    signInGate.await()
                    updateUserProfile("uid", "")
                }

                override suspend fun deleteAccountPlatform() {
                    events += "delete account"
                }
            }
            val signIn = async { account.signInAnonymously() }
            runCurrent()

            val deletion = async {
                account.deleteAccount(
                    deleteOwnedOnlineData = { events += "clean up $it" },
                    clearLocalData = {
                        assertNull(account.userProfile.value)
                        assertFailsWith<IllegalStateException> { account.signInAnonymously() }
                        events += "clear local data"
                    },
                )
            }
            runCurrent()
            assertTrue(events.isEmpty())

            signInGate.complete(Unit)
            signIn.await()
            deletion.await()
            assertEquals(listOf("clean up uid", "delete account", "clear local data"), events)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
