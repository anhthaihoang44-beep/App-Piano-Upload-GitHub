package com.pianoflow.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pianoflow.app.core.database.entity.SongScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongScoreDao {

    @Query("SELECT * FROM song_scores")
    fun getAllScores(): Flow<List<SongScoreEntity>>

    @Query("SELECT * FROM song_scores WHERE songId = :songId")
    suspend fun getScoreForSong(songId: String): SongScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: SongScoreEntity)

    @Query("SELECT COUNT(*) FROM song_scores WHERE stars > 0")
    fun getCompletedSongsCount(): Flow<Int>
}
