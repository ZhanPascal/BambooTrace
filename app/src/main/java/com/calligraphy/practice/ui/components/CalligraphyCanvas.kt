package com.calligraphy.practice.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 核心书法手写Canvas组件
 * 支持压感、流畅绘制和撤销/重做功能
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalligraphyCanvas(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    brushColor: Color = Color.Black,
    baseBrushWidth: Float = 10f,
    onStrokeAdded: (CalligraphyStroke) -> Unit = {},
    onClear: () -> Unit = {}
) {
    // 当前正在绘制的笔画
    var currentStroke by remember { mutableStateOf<CalligraphyStroke?>(null) }

    // 所有已完成的笔画
    var strokes by remember { mutableStateOf<List<CalligraphyStroke>>(emptyList()) }

    // 用于缓存的Bitmap
    var cacheBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cacheCanvas by remember { mutableStateOf<Canvas?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 开始新笔画
                        val pressure = event.getPressure(event.actionIndex)
                        val toolType = event.getToolType(0)
                        val isStylusOrPen = toolType == MotionEvent.TOOL_TYPE_STYLUS ||
                                           toolType == MotionEvent.TOOL_TYPE_ERASER

                        currentStroke = CalligraphyStroke(
                            points = mutableListOf(
                                StrokePoint(
                                    x = event.x,
                                    y = event.y,
                                    pressure = if (isStylusOrPen) pressure else 1f,
                                    timestamp = System.currentTimeMillis()
                                )
                            ),
                            color = brushColor,
                            baseWidth = baseBrushWidth
                        )
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // 添加笔画点
                        currentStroke?.let { stroke ->
                            val pressure = event.getPressure(event.actionIndex)
                            val toolType = event.getToolType(0)
                            val isStylusOrPen = toolType == MotionEvent.TOOL_TYPE_STYLUS ||
                                               toolType == MotionEvent.TOOL_TYPE_ERASER

                            // 使用历史数据获得更流畅的绘制
                            val historySize = event.historySize
                            for (i in 0 until historySize) {
                                stroke.points.add(
                                    StrokePoint(
                                        x = event.getHistoricalX(i),
                                        y = event.getHistoricalY(i),
                                        pressure = if (isStylusOrPen)
                                            event.getHistoricalPressure(i) else 1f,
                                        timestamp = event.getHistoricalEventTime(i)
                                    )
                                )
                            }

                            // 添加当前点
                            stroke.points.add(
                                StrokePoint(
                                    x = event.x,
                                    y = event.y,
                                    pressure = if (isStylusOrPen) pressure else 1f,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 完成笔画
                        currentStroke?.let { stroke ->
                            if (stroke.points.size > 1) {
                                strokes = strokes + stroke
                                onStrokeAdded(stroke)

                                // 将笔画绘制到缓存Bitmap
                                cacheCanvas?.let { canvas ->
                                    drawStrokeToCanvas(canvas, stroke)
                                }
                            }
                            currentStroke = null
                        }
                        true
                    }

                    else -> false
                }
            }
    ) {
        val width = size.width.toInt()
        val height = size.height.toInt()

        // 初始化缓存Bitmap
        if (cacheBitmap == null ||
            cacheBitmap?.width != width ||
            cacheBitmap?.height != height) {
            cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            cacheCanvas = Canvas(cacheBitmap!!)

            // 重绘所有笔画到缓存
            cacheCanvas?.let { canvas ->
                strokes.forEach { stroke ->
                    drawStrokeToCanvas(canvas, stroke)
                }
            }
        }

        // 绘制缓存的笔画
        cacheBitmap?.let { bitmap ->
            drawContext.canvas.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        // 绘制当前正在进行的笔画
        currentStroke?.let { stroke ->
            drawStrokeToCanvas(drawContext.canvas.nativeCanvas, stroke)
        }
    }
}

/**
 * 将笔画绘制到Canvas
 */
private fun drawStrokeToCanvas(canvas: Canvas, stroke: CalligraphyStroke) {
    if (stroke.points.size < 2) return

    val paint = Paint().apply {
        color = android.graphics.Color.argb(
            stroke.color.alpha.toInt(),
            (stroke.color.red * 255).toInt(),
            (stroke.color.green * 255).toInt(),
            (stroke.color.blue * 255).toInt()
        )
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    // 使用贝塞尔曲线平滑笔画
    val path = Path()
    val points = stroke.points

    // 移动到起点
    path.moveTo(points[0].x, points[0].y)

    // 绘制平滑曲线
    for (i in 1 until points.size) {
        val prevPoint = points[i - 1]
        val currentPoint = points[i]

        // 根据压感调整笔画宽度
        val avgPressure = (prevPoint.pressure + currentPoint.pressure) / 2
        paint.strokeWidth = stroke.baseWidth * avgPressure.coerceIn(0.3f, 1.5f)

        if (i < points.size - 1) {
            // 使用二次贝塞尔曲线平滑
            val nextPoint = points[i + 1]
            val controlX = (currentPoint.x + nextPoint.x) / 2
            val controlY = (currentPoint.y + nextPoint.y) / 2

            path.quadTo(currentPoint.x, currentPoint.y, controlX, controlY)
        } else {
            // 最后一个点直接连线
            path.lineTo(currentPoint.x, currentPoint.y)
        }
    }

    canvas.drawPath(path, paint)
}

/**
 * 笔画数据类
 */
data class CalligraphyStroke(
    val points: MutableList<StrokePoint>,
    val color: Color,
    val baseWidth: Float
)

/**
 * 笔画点数据类
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val timestamp: Long
)

/**
 * 计算两点间距离
 */
private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
}
