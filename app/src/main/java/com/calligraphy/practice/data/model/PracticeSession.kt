package com.calligraphy.practice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 练习记录实体
 */
@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val character: String,              // 练习的汉字
    val font: CalligraphyFont,          // 使用的字体
    val timestamp: Long,                // 练习时间戳
    val duration: Long,                 // 练习时长(毫秒)
    val strokeCount: Int,               // 笔画数量
    val imagePath: String? = null,      // 保存的图片路径
    val gridType: String                // 使用的网格类型
)

/**
 * 练习统计数据
 */
data class PracticeStatistics(
    val totalSessions: Int,             // 总练习次数
    val totalDuration: Long,            // 总练习时长
    val totalCharacters: Int,           // 练习过的字数
    val favoriteCharacters: List<ChineseCharacter>, // 收藏的字
    val recentPractices: List<PracticeSession>      // 最近练习
)
