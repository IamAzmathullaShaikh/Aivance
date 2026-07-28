package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aivance_entities")
data class AivanceEntity(
    @PrimaryKey val id: Int,
    val name: String
)
