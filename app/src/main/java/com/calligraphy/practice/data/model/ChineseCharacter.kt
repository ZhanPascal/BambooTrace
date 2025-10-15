package com.calligraphy.practice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 汉字实体
 */
@Entity(tableName = "characters")
data class ChineseCharacter(
    @PrimaryKey
    val char: String,                    // 汉字
    val strokeCount: Int,                // 笔画数
    val difficulty: DifficultyLevel,     // 难度等级
    val pinyin: String,                  // 拼音
    val category: CharacterCategory,     // 分类
    val isFavorite: Boolean = false,     // 是否收藏
    val practiceCount: Int = 0,          // 练习次数
    val lastPracticeTime: Long = 0,      // 最后练习时间
    val hasStrokeData: Boolean = false   // 是否有笔画顺序数据
)

/**
 * 难度等级
 */
enum class DifficultyLevel {
    BEGINNER,    // 入门 (1-5画)
    ELEMENTARY,  // 初级 (6-10画)
    INTERMEDIATE,// 中级 (11-15画)
    ADVANCED     // 高级 (16+画)
}

/**
 * 汉字分类
 */
enum class CharacterCategory {
    NUMBERS,        // 数字
    COMMON,         // 常用字
    RADICALS,       // 部首
    IDIOMS,         // 成语
    POETRY,         // 古诗词
    CUSTOM          // 自定义
}

/**
 * 书法字体类型
 */
enum class CalligraphyFont(val fontFileName: String, val displayName: String) {
    KAISHU("fonts/kaishu.ttf", "楷书"),
    XINGSHU("fonts/xingshu.ttf", "行书"),
    LISHU("fonts/lishu.ttf", "隶书"),
    CAOSHU("fonts/caoshu.ttf", "草书"),
    ZHUANSHU("fonts/hanyikaiti.ttf", "汉仪楷体")
}
