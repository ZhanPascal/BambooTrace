package com.calligraphy.practice.ui.components

/**
 * 书法笔画数据模型
 */
data class CalligraphyStroke(
    val points: MutableList<StrokePoint>,
    val color: androidx.compose.ui.graphics.Color,
    val baseWidth: Float
)

/**
 * 笔画点数据
 * @param x X坐标
 * @param y Y坐标
 * @param pressure 压力值 (0.0 - 1.0，M-Pencil支持)
 * @param timestamp 时间戳（毫秒）
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val timestamp: Long
)
