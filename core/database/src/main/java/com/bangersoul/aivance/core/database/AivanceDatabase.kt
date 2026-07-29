package com.bangersoul.aivance.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bangersoul.aivance.core.database.dao.AivanceDao
import com.bangersoul.aivance.core.database.dao.ApplicationDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import com.bangersoul.aivance.core.database.dao.RoadmapDao
import com.bangersoul.aivance.core.database.model.AivanceEntity
import com.bangersoul.aivance.core.database.model.ApplicationEntity
import com.bangersoul.aivance.core.database.model.AtsResultEntity
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity

@Database(
    entities = [
        AivanceEntity::class,
        ApplicationEntity::class,
        AtsResultEntity::class,
        CoverLetterEntity::class,
        RoadmapEntity::class,
        RoadmapStepEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AivanceDatabase : RoomDatabase() {
    abstract fun aivanceDao(): AivanceDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun atsDao(): AtsDao
    abstract fun coverLetterDao(): CoverLetterDao
    abstract fun roadmapDao(): RoadmapDao
}
