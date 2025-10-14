package com.calligraphy.practice.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calligraphy.practice.data.model.ChineseCharacter
import com.calligraphy.practice.data.model.PracticeSession

/**
 * 书法练习应用数据库
 */
@Database(
    entities = [ChineseCharacter::class, PracticeSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CalligraphyDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun practiceSessionDao(): PracticeSessionDao

    companion object {
        @Volatile
        private var INSTANCE: CalligraphyDatabase? = null

        fun getDatabase(context: Context): CalligraphyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalligraphyDatabase::class.java,
                    "calligraphy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
