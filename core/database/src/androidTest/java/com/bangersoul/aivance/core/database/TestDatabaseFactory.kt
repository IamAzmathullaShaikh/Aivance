package com.bangersoul.aivance.core.database

import android.content.Context
import androidx.room.Room
import com.bangersoul.aivance.core.database.converter.EncryptedTypeConverters
import com.bangersoul.aivance.core.database.security.EncryptionService

/**
 * Builds an in-memory [AivanceDatabase] for DAO instrumented tests.
 *
 * Mirrors the production [com.bangersoul.aivance.core.database.di.DatabaseModule]
 * wiring: `ResumeDao` maps [com.bangersoul.aivance.core.common.security.EncryptedString]
 * columns through [EncryptedTypeConverters], so a builder that omits them fails
 * Room's type-converter validation at open time.
 */
fun buildTestDatabase(context: Context): AivanceDatabase =
    Room.inMemoryDatabaseBuilder(context, AivanceDatabase::class.java)
        .allowMainThreadQueries()
        .addTypeConverter(EncryptedTypeConverters(EncryptionService(context)))
        .build()
