package com.alextos.thousand.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alextos.thousand.data.dao.DiceRollDao
import com.alextos.thousand.data.dao.DieDao
import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.dao.TurnEffectDao
import com.alextos.thousand.data.dao.TurnResultDao
import com.alextos.thousand.data.dao.UserDao
import com.alextos.thousand.data.migration.MIGRATION_1_2
import com.alextos.thousand.data.models.DiceRollEntity
import com.alextos.thousand.data.models.DieEntity
import com.alextos.thousand.data.models.GameEntity
import com.alextos.thousand.data.models.PlayerEntity
import com.alextos.thousand.data.models.TurnEffectEntity
import com.alextos.thousand.data.models.TurnEntity
import com.alextos.thousand.data.models.TurnResultEntity
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
        TurnResultEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(ThousandDatabaseConstructor::class)
abstract class ThousandDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun turnDao(): TurnDao
    abstract fun diceRollDao(): DiceRollDao
    abstract fun dieDao(): DieDao
    abstract fun turnEffectDao(): TurnEffectDao
    abstract fun turnResultDao(): TurnResultDao
}

@Suppress("KotlinNoActualForExpect")
expect object ThousandDatabaseConstructor : RoomDatabaseConstructor<ThousandDatabase> {
    override fun initialize(): ThousandDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<ThousandDatabase>): ThousandDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2)
        .build()
}
