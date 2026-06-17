package com.ivangarzab.kluvs.database

import androidx.room.RoomDatabase
import com.ivangarzab.kluvs.database.migrations.MIGRATION_1_2
import com.ivangarzab.kluvs.database.migrations.MIGRATION_2_3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Platform-agnostic database instantiation function.
 * Takes a platform-specific builder and configures the database with:
 * - IO dispatcher for query coroutines
 * - Platform-specific driver (handled in platform implementations)
 */
fun getKluvsDatabase(
    builder: RoomDatabase.Builder<KluvsDatabaseImpl>
): KluvsDatabase {
    return builder
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

internal const val DATABASE_NAME = "kluvs.db"