package com.alextos.thousand.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isShakeEnabled INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isVirtualDiceEnabled INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isNotificationEnabled INTEGER NOT NULL DEFAULT 1",
        )
    }
}

private fun SQLiteConnection.executeSql(sql: String) {
    val statement = prepare(sql)
    try {
        statement.step()
    } finally {
        statement.close()
    }
}
