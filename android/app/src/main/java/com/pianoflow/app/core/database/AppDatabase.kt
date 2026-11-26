package com.pianoflow.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pianoflow.app.core.database.dao.LessonProgressDao
import com.pianoflow.app.core.database.dao.RecordingDao
import com.pianoflow.app.core.database.dao.SongScoreDao
import com.pianoflow.app.core.database.entity.LessonProgressEntity
import com.pianoflow.app.core.database.entity.RecordingEntity
import com.pianoflow.app.core.database.entity.SongScoreEntity

@Database(
    entities = [
        RecordingEntity::class,
        LessonProgressEntity::class,
        SongScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun songScoreDao(): SongScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pianoflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
