package com.calligraphy.practice.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.calligraphy.practice.utils.FontCache
import com.calligraphy.practice.utils.StrokeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 单个练习格子组件
 * 固定大小，包含范本字、网格和书写区域
 */
@Composable
fun PracticeCell(
    character: String,
    modifier: Modifier = Modifier,
    cellSize: Int = 120, // dp
    fontPath: String,
    gridType: GridType = GridType.MI_GRID,
    showReference: Boolean = true,
    showOutline: Boolean = true,
    triggerScore: Int = 0, // 触发评分的计数器
    clearTrigger: Int = 0, // 清空触发器
    undoTrigger: Int = 0, // 撤回触发器
    isActive: Boolean = false, // 是否是活跃格子
    onActive: () -> Unit = {}, // 格子被触摸时回调
    onScoreChanged: (Float) -> Unit = {},
    onStrokesChanged: (List<CalligraphyStroke>) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var strokes by remember { mutableStateOf<List<CalligraphyStroke>>(emptyList()) }
    var score by remember { mutableStateOf(0f) }
    var isScoring by remember { mutableStateOf(false) }

    // 使用缓存的字体（避免重复加载）
    val typeface = remember(fontPath) {
        FontCache.getTypeface(context, fontPath)
    }

    // 计算像素尺寸
    val cellSizePx = with(density) { cellSize.dp.toPx().toInt() }

    // 创建背景位图（范本字+网格）
    val backgroundBitmap = remember(character, fontPath, showReference, showOutline, gridType, cellSizePx) {
        createBackgroundBitmap(
            context = context,
            character = character,
            fontPath = fontPath,
            showReference = showReference,
            showOutline = showOutline,
            gridType = gridType,
            width = cellSizePx,
            height = cellSizePx,
            typeface = typeface
        )
    }

    // 监听triggerScore变化，触发评分
    LaunchedEffect(triggerScore) {
        if (triggerScore > 0 && strokes.isNotEmpty() && !isScoring) {
            isScoring = true
            val similarity = withContext(Dispatchers.Default) {
                StrokeAnalyzer.calculateSimilarity(
                    userStrokes = strokes,
                    targetCharacter = character,
                    fontPath = fontPath,
                    typeface = typeface,
                    width = cellSizePx,
                    height = cellSizePx
                )
            }
            score = similarity
            onScoreChanged(similarity)
            isScoring = false
        }
    }

    Box(
        modifier = modifier
            .size(cellSize.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(Color.White)
    ) {
        // 手写Canvas层（SurfaceView，包含背景和笔画）
        // 背景位图包含范本字和网格，直接在SurfaceView内部绘制
        LowLatencyCalligraphyCanvas(
            modifier = Modifier.fillMaxSize(),
            baseBrushWidth = 12f, // 毛笔基础宽度（调细）
            clearTrigger = clearTrigger,
            undoTrigger = undoTrigger,
            isActive = isActive, // 传递活跃状态
            backgroundBitmap = backgroundBitmap, // 传递背景位图
            onTouchStart = {
                onActive() // 通知父组件这个格子被触摸了
            },
            onStrokeAdded = { stroke ->
                // onStrokeAdded 只用于通知，不更新 strokes
                // strokes 的更新由 onStrokesChanged 统一处理
            },
            onStrokesChanged = { newStrokes ->
                strokes = newStrokes
                onStrokesChanged(newStrokes)
            }
        )

        // 评分显示（右上角）- 禁用触摸穿透
        if (score > 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .pointerInput(Unit) {}, // 禁用触摸，让事件穿透
                color = when {
                    score >= 80f -> Color(0xFF4CAF50) // 绿色
                    score >= 60f -> Color(0xFFFF9800) // 橙色
                    else -> Color(0xFFF44336) // 红色
                }.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "${score.toInt()}%",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        // 通过标记（中心）- 禁用触摸穿透
        if (score >= 80f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp)
                    .pointerInput(Unit) {}, // 禁用触摸，让事件穿透
                color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "✓ 通过",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 创建背景位图（范本字+网格）
 */
private fun createBackgroundBitmap(
    context: Context,
    character: String,
    fontPath: String,
    showReference: Boolean,
    showOutline: Boolean,
    gridType: GridType,
    width: Int,
    height: Int,
    typeface: Typeface
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 绘制范本字
    if (showReference || showOutline) {
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            this.typeface = typeface
            textSize = width * 0.8f
        }

        val fontMetrics = paint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val textOffset = textHeight / 2 - fontMetrics.descent

        // 半透明范本字
        if (showReference) {
            paint.style = Paint.Style.FILL
            paint.color = android.graphics.Color.argb(76, 128, 128, 128) // 30% alpha gray
            canvas.drawText(character, width / 2f, height / 2f + textOffset, paint)
        }

        // 轮廓
        if (showOutline) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = android.graphics.Color.argb(128, 255, 0, 0) // 50% alpha red
            canvas.drawText(character, width / 2f, height / 2f + textOffset, paint)
        }
    }

    // 绘制网格
    val gridPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = android.graphics.Color.argb(77, 0, 0, 0) // 30% alpha black
    }

    when (gridType) {
        GridType.MI_GRID -> {
            // 米字格
            canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), gridPaint)
            canvas.drawLine(width.toFloat(), 0f, 0f, height.toFloat(), gridPaint)
            canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), gridPaint)
            canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, gridPaint)
        }
        GridType.TIAN_GRID -> {
            // 田字格
            canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), gridPaint)
            canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, gridPaint)
        }
        GridType.NINE_GRID -> {
            // 九宫格
            canvas.drawLine(width / 3f, 0f, width / 3f, height.toFloat(), gridPaint)
            canvas.drawLine(width * 2 / 3f, 0f, width * 2 / 3f, height.toFloat(), gridPaint)
            canvas.drawLine(0f, height / 3f, width.toFloat(), height / 3f, gridPaint)
            canvas.drawLine(0f, height * 2 / 3f, width.toFloat(), height * 2 / 3f, gridPaint)
        }
        GridType.NONE -> {
            // 无网格
        }
    }

    return bitmap
}
