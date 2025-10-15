package com.calligraphy.practice.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.calligraphy.practice.ui.components.CalligraphyStroke
import kotlin.math.min

/**
 * 笔画分析器
 * 用于计算用户书写与标准字的相似度
 */
object StrokeAnalyzer {

    /**
     * 计算用户笔画与标准字的相似度
     * @param userStrokes 用户书写的笔画列表
     * @param targetCharacter 目标汉字
     * @param fontPath 字体文件路径
     * @param width 比较区域宽度（像素）
     * @param height 比较区域高度（像素）
     * @return 相似度百分比 (0-100)
     */
    fun calculateSimilarity(
        userStrokes: List<CalligraphyStroke>,
        targetCharacter: String,
        fontPath: String?,
        typeface: Typeface?,
        width: Int,
        height: Int
    ): Float {
        if (userStrokes.isEmpty()) return 0f
        if (width <= 0 || height <= 0) return 0f

        // 创建用户书写的bitmap
        val userBitmap = createUserStrokeBitmap(userStrokes, width, height)

        // 创建标准字的bitmap
        val targetBitmap = createTargetCharacterBitmap(
            targetCharacter,
            typeface,
            width,
            height
        )

        // 计算两个bitmap的重叠度
        val similarity = compareBitmaps(userBitmap, targetBitmap)

        // 清理bitmap
        userBitmap.recycle()
        targetBitmap.recycle()

        return similarity
    }

    /**
     * 创建用户笔画的bitmap
     */
    private fun createUserStrokeBitmap(
        strokes: List<CalligraphyStroke>,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 清空为白色背景
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // 绘制所有笔画
        strokes.forEach { stroke ->
            drawStroke(canvas, stroke, paint)
        }

        return bitmap
    }

    /**
     * 创建标准字的bitmap
     */
    private fun createTargetCharacterBitmap(
        character: String,
        typeface: Typeface?,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 清空为白色背景
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            this.typeface = typeface ?: Typeface.DEFAULT
            // 设置文字大小为格子大小的80%
            textSize = (width.coerceAtMost(height) * 0.8f)
            style = Paint.Style.FILL
        }

        // 计算文字的基线位置
        val centerX = width / 2f
        val centerY = height / 2f
        val fontMetrics = paint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val textOffset = textHeight / 2 - fontMetrics.descent

        // 绘制字符
        canvas.drawText(character, centerX, centerY + textOffset, paint)

        return bitmap
    }

    /**
     * 绘制单个笔画到Canvas
     */
    private fun drawStroke(canvas: Canvas, stroke: CalligraphyStroke, paint: Paint) {
        if (stroke.points.size < 2) return

        val points = stroke.points

        // 简化版绘制（仅用于比较，不需要完整的毛笔效果）
        for (i in 0 until points.size) {
            val point = points[i]
            val radius = stroke.baseWidth / 2

            canvas.drawCircle(point.x, point.y, radius, paint)

            if (i < points.size - 1) {
                val nextPoint = points[i + 1]
                canvas.drawLine(point.x, point.y, nextPoint.x, nextPoint.y, paint.apply {
                    strokeWidth = stroke.baseWidth
                    strokeCap = Paint.Cap.ROUND
                })
            }
        }
    }

    /**
     * 比较两个bitmap的相似度
     * 使用像素重叠度算法
     */
    private fun compareBitmaps(userBitmap: Bitmap, targetBitmap: Bitmap): Float {
        val width = min(userBitmap.width, targetBitmap.width)
        val height = min(userBitmap.height, targetBitmap.height)

        var matchingPixels = 0
        var totalTargetPixels = 0
        var totalUserPixels = 0

        // 放宽阈值：允许灰色也算匹配（更宽容）
        val threshold = 200 // 从128提高到200

        for (y in 0 until height) {
            for (x in 0 until width) {
                val userPixel = userBitmap.getPixel(x, y)
                val targetPixel = targetBitmap.getPixel(x, y)

                // 提取红色通道（灰度图中RGB相同）
                val userGray = android.graphics.Color.red(userPixel)
                val targetGray = android.graphics.Color.red(targetPixel)

                val userIsBlack = userGray < threshold
                val targetIsBlack = targetGray < threshold

                if (targetIsBlack) {
                    totalTargetPixels++
                }

                if (userIsBlack) {
                    totalUserPixels++
                }

                // 如果两个都是黑色，计为匹配
                if (userIsBlack && targetIsBlack) {
                    matchingPixels++
                }
            }
        }

        // 避免除零
        if (totalTargetPixels == 0) return 0f

        // 计算相似度：
        // 匹配的黑色像素 / 目标的黑色像素
        val coverage = matchingPixels.toFloat() / totalTargetPixels.toFloat()

        // 降低惩罚：用户画多了不要太严厉（0.3 -> 0.12）
        val excessPixels = (totalUserPixels - matchingPixels).coerceAtLeast(0)
        val penalty = (excessPixels.toFloat() / totalTargetPixels.toFloat()).coerceIn(0f, 0.12f)

        // 添加覆盖度加成：鼓励用户多写
        val bonus = when {
            coverage > 0.7f -> 0.15f  // 覆盖70%以上，加15分
            coverage > 0.5f -> 0.08f  // 覆盖50%以上，加8分
            else -> 0f
        }

        // 最终得分
        val score = ((coverage - penalty + bonus) * 100f).coerceIn(0f, 100f)

        return score
    }
}
