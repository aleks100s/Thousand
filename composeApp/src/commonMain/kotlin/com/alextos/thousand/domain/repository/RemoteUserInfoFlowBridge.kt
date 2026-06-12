package com.alextos.thousand.domain.repository

import com.alextos.thousand.domain.models.RemoteUserInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class RemoteUserInfoFlowBridge {
    private val channel = Channel<RemoteUserInfo?>(Channel.BUFFERED)

    val flow: Flow<RemoteUserInfo?> = channel.receiveAsFlow()

    fun emit(userInfo: RemoteUserInfo?) {
        channel.trySend(userInfo)
    }

    fun closeWithError(message: String?) {
        channel.close(IllegalStateException(message ?: "Failed to load user info."))
    }
}
