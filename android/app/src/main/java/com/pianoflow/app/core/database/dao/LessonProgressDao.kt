package com.pianoflow.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pianoflow.app.core.database.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {

    @Query("SELECT * FROM lesson_progress")
    fun getAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getProgressForLesson(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: LessonProgressEntity)

    @Query("SELECT SUM(xpEarned) FROM lesson_progress WHERE isCompleted = 1")
    fun getTotalXp(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE isCompleted = 1")
    fun getCompletedLessonsCount(): Flow<Int>
}
