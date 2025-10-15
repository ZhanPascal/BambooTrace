package com.calligraphy.practice.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.sqrt

/**
 * 低延迟书法Canvas - 使用SurfaceView实现零延迟绘制
 * 专为M-Pencil手写笔优化
 */
@Composable
fun LowLatencyCalligraphyCanvas(
    modifier: Modifier = Modifier,
    baseBrushWidth: Float = 18f,
    onStrokeAdded: (CalligraphyStroke) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LowLatencyCalligraphyView(context, baseBrushWidth, onStrokeAdded)
        }
    )
}

/**
 * 原生SurfaceView实现 - 独立线程绘制
 */
class LowLatencyCalligraphyView(
    context: Context,
    private val baseBrushWidth: Float,
    private val onStrokeAdded: (CalligraphyStroke) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private var currentPath: Path? = null
    private var currentStroke: CalligraphyStroke? = null
    private val completedStrokes = mutableListOf<CalligraphyStroke>()

    private var lastX = 0f
    private var lastY = 0f
    private var lastTime = 0L

    // Paint对象复用，避免重复创建
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    init {
        holder.addCallback(this)
        setZOrderOnTop(true)
        holder.setFormat(android.graphics.PixelFormat.TRANSPARENT)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Surface创建完成
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface大小改变
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // 清理资源
        currentPath = null
        currentStroke = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startNewStroke(event)
                drawImmediate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                addPointToStroke(event)
                drawImmediate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                finishStroke()
                drawImmediate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startNewStroke(event: MotionEvent) {
        val pressure = event.getPressure(event.actionIndex)
        val toolType = event.getToolType(0)
        val isStylusOrPen = toolType == MotionEvent.TOOL_TYPE_STYLUS ||
                           toolType == MotionEvent.TOOL_TYPE_ERASER

        currentPath = Path().apply {
            moveTo(event.x, event.y)
        }

        currentStroke = CalligraphyStroke(
            points = mutableListOf(
                StrokePoint(
                    x = event.x,
                    y = event.y,
                    pressure = if (isStylusOrPen) pressure else 1f,
                    timestamp = System.currentTimeMillis()
                )
            ),
            color = androidx.compose.ui.graphics.Color.Black,
            baseWidth = baseBrushWidth
        )

        lastX = event.x
        lastY = event.y
        lastTime = System.currentTimeMillis()
    }

    private fun addPointToStroke(event: MotionEvent) {
        val stroke = currentStroke ?: return
        val path = currentPath ?: return

        val pressure = event.getPressure(event.actionIndex)
        val toolType = event.getToolType(0)
        val isStylusOrPen = toolType == MotionEvent.TOOL_TYPE_STYLUS ||
                           toolType == MotionEvent.TOOL_TYPE_ERASER

        // 添加历史点（更流畅）
        val historySize = event.historySize
        for (i in 0 until historySize) {
            val hx = event.getHistoricalX(i)
            val hy = event.getHistoricalY(i)
            val hp = if (isStylusOrPen) event.getHistoricalPressure(i) else 1f

            stroke.points.add(StrokePoint(hx, hy, hp, event.getHistoricalEventTime(i)))
            path.lineTo(hx, hy)
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

        path.lineTo(event.x, event.y)

        lastX = event.x
        lastY = event.y
        lastTime = System.currentTimeMillis()
    }

    private fun finishStroke() {
        currentStroke?.let { stroke ->
            if (stroke.points.size > 1) {
                completedStrokes.add(stroke)
                onStrokeAdded(stroke)
            }
        }
        currentStroke = null
        currentPath = null
    }

    /**
     * 立即绘制 - 直接绘制到Surface，无延迟
     */
    private fun drawImmediate() {
        if (!holder.surface.isValid) return

        val canvas = try {
            // 尝试使用硬件加速Canvas (Android 8+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                holder.lockHardwareCanvas()
            } else {
                holder.lockCanvas()
            }
        } catch (e: Exception) {
            return
        }

        try {
            // 清空画布
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // 绘制已完成的笔画
            completedStrokes.forEach { stroke ->
                drawStrokeWithBlur(canvas, stroke)
            }

            // 绘制当前笔画
            currentStroke?.let { stroke ->
                drawStrokeWithBlur(canvas, stroke)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    /**
     * 绘制带晕染效果的笔画
     */
    private fun drawStrokeWithBlur(canvas: Canvas, stroke: CalligraphyStroke) {
        if (stroke.points.isEmpty()) return

        val points = stroke.points
        val paintColor = android.graphics.Color.argb(
            (stroke.color.alpha * 255).toInt(),
            (stroke.color.red * 255).toInt(),
            (stroke.color.green * 255).toInt(),
            (stroke.color.blue * 255).toInt()
        )

        // 单点绘制
        if (points.size == 1) {
            val point = points[0]
            val radius = stroke.baseWidth * point.pressure * 1.5f

            fillPaint.color = paintColor

            // 三层晕染
            fillPaint.alpha = 60
            canvas.drawCircle(point.x, point.y, radius * 1.5f, fillPaint)
            fillPaint.alpha = 150
            canvas.drawCircle(point.x, point.y, radius * 1.2f, fillPaint)
            fillPaint.alpha = 255
            canvas.drawCircle(point.x, point.y, radius, fillPaint)
            return
        }

        strokePaint.color = paintColor

        // 绘制多点笔画
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            // 计算速度因子
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            val distance = sqrt(dx * dx + dy * dy)

            val speedFactor = when {
                distance > 15f -> 0.4f
                distance > 8f -> 0.7f
                else -> 1.3f
            }

            val pressureFactor = p1.pressure.coerceIn(0.5f, 2.0f)
            val width = stroke.baseWidth * speedFactor * pressureFactor

            // 三层晕染绘制
            strokePaint.strokeWidth = width * 1.5f
            strokePaint.alpha = 60
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)

            strokePaint.strokeWidth = width * 1.2f
            strokePaint.alpha = 150
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)

            strokePaint.strokeWidth = width
            strokePaint.alpha = 255
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)

            // 连接点圆形
            fillPaint.color = paintColor
            fillPaint.alpha = 60
            canvas.drawCircle(p1.x, p1.y, width * 1.5f / 2, fillPaint)
            fillPaint.alpha = 150
            canvas.drawCircle(p1.x, p1.y, width * 1.2f / 2, fillPaint)
            fillPaint.alpha = 255
            canvas.drawCircle(p1.x, p1.y, width / 2, fillPaint)

            // 最后一个点
            if (i == points.size - 2) {
                fillPaint.alpha = 60
                canvas.drawCircle(p2.x, p2.y, width * 1.5f / 2, fillPaint)
                fillPaint.alpha = 150
                canvas.drawCircle(p2.x, p2.y, width * 1.2f / 2, fillPaint)
                fillPaint.alpha = 255
                canvas.drawCircle(p2.x, p2.y, width / 2, fillPaint)
            }
        }
    }
}
