package com.calligraphy.practice.data.database

import androidx.room.*
import com.calligraphy.practice.data.model.ChineseCharacter
import com.calligraphy.practice.data.model.CharacterCategory
import com.calligraphy.practice.data.model.DifficultyLevel
import kotlinx.coroutines.flow.Flow

/**
 * 汉字数据访问对象
 */
@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY strokeCount ASC")
    fun getAllCharacters(): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters WHERE char = :character")
    suspend fun getCharacter(character: String): ChineseCharacter?

    @Query("SELECT * FROM characters WHERE difficulty = :level ORDER BY strokeCount ASC")
    fun getCharactersByDifficulty(level: DifficultyLevel): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters WHERE category = :category ORDER BY strokeCount ASC")
    fun getCharactersByCategory(category: CharacterCategory): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters WHERE strokeCount BETWEEN :minStrokes AND :maxStrokes ORDER BY strokeCount ASC")
    fun getCharactersByStrokeRange(minStrokes: Int, maxStrokes: Int): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters WHERE isFavorite = 1 ORDER BY lastPracticeTime DESC")
    fun getFavoriteCharacters(): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters ORDER BY practiceCount DESC LIMIT :limit")
    fun getMostPracticedCharacters(limit: Int = 10): Flow<List<ChineseCharacter>>

    @Query("SELECT * FROM characters WHERE lastPracticeTime > 0 ORDER BY lastPracticeTime DESC LIMIT :limit")
    fun getRecentlyPracticedCharacters(limit: Int = 20): Flow<List<ChineseCharacter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: ChineseCharacter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<ChineseCharacter>)

    @Update
    suspend fun updateCharacter(character: ChineseCharacter)

    @Delete
    suspend fun deleteCharacter(character: ChineseCharacter)

    @Query("UPDATE characters SET isFavorite = :isFavorite WHERE char = :character")
    suspend fun updateFavoriteStatus(character: String, isFavorite: Boolean)

    @Query("UPDATE characters SET practiceCount = practiceCount + 1, lastPracticeTime = :timestamp WHERE char = :character")
    suspend fun incrementPracticeCount(character: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharacterCount(): Int

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()
}
