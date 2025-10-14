package com.calligraphy.practice.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

/**
 * 汉字显示组件
 * 用于显示标准书法范本字
 */
@Composable
fun CharacterDisplay(
    character: String,
    modifier: Modifier = Modifier,
    fontPath: String? = null,
    textColor: Color = Color.Black,
    opacity: Float = 1f,
    showOutline: Boolean = false
) {
    val context = LocalContext.current

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = textColor.copy(alpha = opacity).toArgb()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER

                // 加载自定义字体
                if (fontPath != null) {
                    try {
                        typeface = Typeface.createFromAsset(context.assets, fontPath)
                    } catch (e: Exception) {
                        // 如果字体加载失败,使用系统默认字体
                        typeface = Typeface.DEFAULT
                    }
                } else {
                    typeface = Typeface.DEFAULT
                }

                // 设置文字大小为格子大小的80%
                textSize = (width.coerceAtMost(height) * 0.8f)

                // 如果需要显示轮廓
                if (showOutline) {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 2f
                } else {
                    style = android.graphics.Paint.Style.FILL
                }
            }

            // 计算文字的基线位置
            val fontMetrics = paint.fontMetrics
            val textHeight = fontMetrics.descent - fontMetrics.ascent
            val textOffset = textHeight / 2 - fontMetrics.descent

            // 绘制字符
            canvas.nativeCanvas.drawText(
                character,
                centerX,
                centerY + textOffset,
                paint
            )
        }
    }
}

/**
 * 可配置的汉字范本显示器
 * 支持范本字和临摹字同时显示
 */
@Composable
fun CharacterTemplate(
    character: String,
    modifier: Modifier = Modifier,
    fontPath: String? = null,
    showReference: Boolean = true,
    showOutline: Boolean = true,
    referenceOpacity: Float = 0.3f,
    outlineOpacity: Float = 0.5f
) {
    // 背景范本字(半透明)
    if (showReference) {
        CharacterDisplay(
            character = character,
            modifier = modifier,
            fontPath = fontPath,
            textColor = Color.Gray,
            opacity = referenceOpacity,
            showOutline = false
        )
    }

    // 轮廓描边(用于临摹)
    if (showOutline) {
        CharacterDisplay(
            character = character,
            modifier = modifier,
            fontPath = fontPath,
            textColor = Color.Red,
            opacity = outlineOpacity,
            showOutline = true
        )
    }
}
