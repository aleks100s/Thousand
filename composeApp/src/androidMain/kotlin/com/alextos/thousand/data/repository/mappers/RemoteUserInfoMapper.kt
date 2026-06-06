package com.alextos.thousand.data.repository.mappers

import com.alextos.thousand.data.mappers.firebase.FirebaseRemoteUserInfoMapper
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.google.firebase.database.DataSnapshot

internal fun DataSnapshot.toRemoteUserInfo(userId: String): RemoteUserInfo? {
    if (!exists()) return null

    return FirebaseRemoteUserInfoMapper.remoteUserInfo(from = value, key = userId)
}
