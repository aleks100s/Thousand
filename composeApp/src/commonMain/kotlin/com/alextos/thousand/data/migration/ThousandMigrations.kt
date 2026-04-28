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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN hasStartLimit INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isBarrel1Active INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isBarrel2Active INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isBarrel3Active INTEGER NOT NULL DEFAULT 0",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isTripleBoltFineActive INTEGER NOT NULL DEFAULT 1",
        )
        connection.executeSql(
            "ALTER TABLE games ADD COLUMN isOvertakeFineActive INTEGER NOT NULL DEFAULT 1",
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.executeSql(
            "ALTER TABLE users ADD COLUMN kind INTEGER NOT NULL DEFAULT 0",
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
