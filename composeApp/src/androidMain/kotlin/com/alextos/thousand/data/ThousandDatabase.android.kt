package com.alextos.thousand.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<ThousandDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(THOUSAND_DATABASE_FILE_NAME)

    return Room.databaseBuilder<ThousandDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
