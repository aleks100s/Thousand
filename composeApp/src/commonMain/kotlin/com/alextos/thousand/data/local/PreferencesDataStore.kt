package com.alextos.thousand.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val PREFERENCES_DATA_STORE_FILE_NAME = "thousand.preferences_pb"

fun createPreferencesDataStore(
    producePath: () -> String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            producePath().toPath()
        },
    )
}
