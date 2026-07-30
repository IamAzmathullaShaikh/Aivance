package com.bangersoul.aivance.core.database

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManager @Inject constructor(
    private val database: AivanceDatabase,
    @ApplicationContext private val context: Context
) {
    /**
     * Resets the database by clearing all tables.
     */
    suspend fun reset() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    /**
     * Compacts the database by running the VACUUM command.
     */
    suspend fun vacuum() = withContext(Dispatchers.IO) {
        database.query(SimpleSQLiteQuery("VACUUM"), null)
    }

    /**
     * Performs an integrity check on the database.
     * Returns true if the database is healthy.
     */
    suspend fun integrityCheck(): Boolean = withContext(Dispatchers.IO) {
        val cursor = database.query(SimpleSQLiteQuery("PRAGMA integrity_check"), null)
        cursor.use {
            if (it.moveToFirst()) {
                val result = it.getString(0)
                return@withContext result.equals("ok", ignoreCase = true)
            }
        }
        false
    }

    /**
     * Backups the database to the specified file path.
     */
    suspend fun backup(backupPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.close()
            val dbFile = context.getDatabasePath("aivance-database")
            val backupFile = File(backupPath)
            dbFile.inputStream().use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            Unit
        }
    }

    /**
     * Restores the database from the specified file path.
     */
    suspend fun restore(restorePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.close()
            val dbFile = context.getDatabasePath("aivance-database")
            val restoreFile = File(restorePath)
            restoreFile.inputStream().use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Unit
        }
    }
}
