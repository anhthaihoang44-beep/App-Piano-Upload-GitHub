package com.pianoflow.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_scores")
data class SongScoreEntity(
    @PrimaryKey
    val songId: String,
    val highScore: Int = 0,
    val stars: Int = 0,
    val accuracyPercent: Float = 0f,
    val maxCombo: Int = 0,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
