package com.calligraphy.practice.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 辅助线网格类型
 */
enum class GridType {
    NONE,           // 无网格
    MI_GRID,        // 米字格
    TIAN_GRID,      // 田字格
    NINE_GRID      // 九宫格
}

/**
 * 辅助线网格组件
 * 用于汉字书写练习的辅助定位
 */
@Composable
fun GridOverlay(
    modifier: Modifier = Modifier,
    gridType: GridType = GridType.MI_GRID,
    gridColor: Color = Color.Gray.copy(alpha = 0.3f),
    lineWidth: Float = 1f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // 绘制外框
        drawRect(
            color = gridColor,
            style = Stroke(width = lineWidth * 2)
        )

        when (gridType) {
            GridType.MI_GRID -> {
                // 米字格: 十字 + 对角线
                // 竖线
                drawLine(
                    color = gridColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = lineWidth
                )
                // 横线
                drawLine(
                    color = gridColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = lineWidth
                )
                // 左上到右下对角线
                drawLine(
                    color = gridColor,
                    start = Offset(0f, 0f),
                    end = Offset(width, height),
                    strokeWidth = lineWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                )
                // 右上到左下对角线
                drawLine(
                    color = gridColor,
                    start = Offset(width, 0f),
                    end = Offset(0f, height),
                    strokeWidth = lineWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                )
            }

            GridType.TIAN_GRID -> {
                // 田字格: 井字
                // 竖线
                drawLine(
                    color = gridColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = lineWidth
                )
                // 横线
                drawLine(
                    color = gridColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = lineWidth
                )
            }

            GridType.NINE_GRID -> {
                // 九宫格: 3x3网格
                val cellWidth = width / 3
                val cellHeight = height / 3

                // 两条竖线
                for (i in 1..2) {
                    drawLine(
                        color = gridColor,
                        start = Offset(cellWidth * i, 0f),
                        end = Offset(cellWidth * i, height),
                        strokeWidth = lineWidth
                    )
                }

                // 两条横线
                for (i in 1..2) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, cellHeight * i),
                        end = Offset(width, cellHeight * i),
                        strokeWidth = lineWidth
                    )
                }

                // 中心点标记
                drawCircle(
                    color = gridColor,
                    radius = 4f,
                    center = Offset(centerX, centerY)
                )
            }

            GridType.NONE -> {
                // 不绘制网格
            }
        }
    }
}
