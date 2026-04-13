package com.alextos.thousand.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.DieEntity
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.UserEntity

internal const val THOUSAND_DATABASE_FILE_NAME = "thousand.db"

@Database(
    entities = [
        UserEntity::class,
        GameEntity::class,
        PlayerEntity::class,
        TurnEntity::class,
        DiceRollEntity::class,
        DieEntity::class,
        TurnEffectEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(ThousandDatabaseConstructor::class)
abstract class ThousandDatabase : RoomDatabase() {
    abstract fun thousandDao(): ThousandDao
}

@Suppress("KotlinNoActualForExpect")
expect object ThousandDatabaseConstructor : RoomDatabaseConstructor<ThousandDatabase> {
    override fun initialize(): ThousandDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<ThousandDatabase>): ThousandDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .build()
}
