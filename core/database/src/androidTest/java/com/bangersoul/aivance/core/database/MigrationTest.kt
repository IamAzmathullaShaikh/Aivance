package com.bangersoul.aivance.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AivanceDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        helper.createDatabase(TEST_DB, 5).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 6, true, AivanceDatabase.MIGRATION_5_6)
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        helper.createDatabase(TEST_DB, 6).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 7, true, AivanceDatabase.MIGRATION_6_7)
    }

    @Test
    @Throws(IOException::class)
    fun migrate7To8() {
        helper.createDatabase(TEST_DB, 7).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 8, true, AivanceDatabase.MIGRATION_7_8)
    }

    @Test
    @Throws(IOException::class)
    fun migrate8To9() {
        helper.createDatabase(TEST_DB, 8).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 9, true, AivanceDatabase.MIGRATION_8_9)
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 9, true, *AivanceDatabase.ALL_MIGRATIONS)
    }
}
