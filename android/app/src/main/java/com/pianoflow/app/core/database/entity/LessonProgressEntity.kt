package com.pianoflow.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey
    val lessonId: String,
    val stageId: String,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestAccuracy: Float = 0f,
    val xpEarned: Int = 0,
    val completedAt: Long = 0L
)
