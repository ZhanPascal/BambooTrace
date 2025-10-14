package com.calligraphy.practice.data.database

import androidx.room.*
import com.calligraphy.practice.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow

/**
 * 练习记录数据访问对象
 */
@Dao
interface PracticeSessionDao {

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions WHERE character = :character ORDER BY timestamp DESC")
    fun getSessionsByCharacter(character: String): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSessionsByTimeRange(startTime: Long, endTime: Long): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): PracticeSession?

    @Insert
    suspend fun insertSession(session: PracticeSession): Long

    @Update
    suspend fun updateSession(session: PracticeSession)

    @Delete
    suspend fun deleteSession(session: PracticeSession)

    @Query("DELETE FROM practice_sessions WHERE character = :character")
    suspend fun deleteSessionsByCharacter(character: String)

    @Query("DELETE FROM practice_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT COUNT(*) FROM practice_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT SUM(duration) FROM practice_sessions")
    suspend fun getTotalPracticeDuration(): Long?

    @Query("SELECT COUNT(DISTINCT character) FROM practice_sessions")
    suspend fun getUniquePracticedCharacterCount(): Int
}
