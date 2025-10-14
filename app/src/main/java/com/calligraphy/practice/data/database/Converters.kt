package com.calligraphy.practice.data.database

import androidx.room.TypeConverter
import com.calligraphy.practice.data.model.CalligraphyFont
import com.calligraphy.practice.data.model.CharacterCategory
import com.calligraphy.practice.data.model.DifficultyLevel

/**
 * Room数据库类型转换器
 */
class Converters {

    @TypeConverter
    fun fromDifficultyLevel(value: DifficultyLevel): String {
        return value.name
    }

    @TypeConverter
    fun toDifficultyLevel(value: String): DifficultyLevel {
        return DifficultyLevel.valueOf(value)
    }

    @TypeConverter
    fun fromCharacterCategory(value: CharacterCategory): String {
        return value.name
    }

    @TypeConverter
    fun toCharacterCategory(value: String): CharacterCategory {
        return CharacterCategory.valueOf(value)
    }

    @TypeConverter
    fun fromCalligraphyFont(value: CalligraphyFont): String {
        return value.name
    }

    @TypeConverter
    fun toCalligraphyFont(value: String): CalligraphyFont {
        return CalligraphyFont.valueOf(value)
    }
}
