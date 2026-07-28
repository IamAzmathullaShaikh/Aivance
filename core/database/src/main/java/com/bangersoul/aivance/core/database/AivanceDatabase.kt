package com.bangersoul.aivance.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bangersoul.aivance.core.database.dao.AivanceDao
import com.bangersoul.aivance.core.database.model.AivanceEntity

@Database(entities = [AivanceEntity::class], version = 1, exportSchema = false)
abstract class AivanceDatabase : RoomDatabase() {
    abstract fun aivanceDao(): AivanceDao
}
