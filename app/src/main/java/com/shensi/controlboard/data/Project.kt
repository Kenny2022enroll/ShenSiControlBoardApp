package com.shensi.controlboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val filePath: String,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val isTemplate: Boolean = false
)

data class ProjectFile(
    val name: String,
    val content: String,
    val isMain: Boolean = false
)

