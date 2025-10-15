package com.calligraphy.practice.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
    onScoreChanged: (Float) -> Unit = {},
    onStrokesChanged: (List<CalligraphyStroke>) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var strokes by remember { mutableStateOf<List<CalligraphyStroke>>(emptyList()) }
    var score by remember { mutableStateOf(0f) }
    var scoringJob by remember { mutableStateOf<Job?>(null) }

    // 加载字体
    val typeface = remember(fontPath) {
        try {
            Typeface.createFromAsset(context.assets, fontPath)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
    }

    // 计算像素尺寸
    val cellSizePx = with(density) { cellSize.dp.toPx().toInt() }

    // 延迟计算评分的函数
    fun scheduleScoring() {
        // 取消之前的评分任务
        scoringJob?.cancel()

        // 延迟800ms后计算评分（等待用户停止书写）
        scoringJob = scope.launch {
            delay(800)
            if (strokes.isNotEmpty()) {
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
            }
        }
    }

    Box(
        modifier = modifier
            .size(cellSize.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(Color.White)
    ) {
        // 汉字范本层
        CharacterTemplate(
            character = character,
            fontPath = fontPath,
            showReference = showReference,
            showOutline = showOutline,
            modifier = Modifier.fillMaxSize()
        )

        // 网格辅助线层
        GridOverlay(
            gridType = gridType,
            modifier = Modifier.fillMaxSize()
        )

        // 手写Canvas层 - 使用低延迟原生绘制
        LowLatencyCalligraphyCanvas(
            modifier = Modifier.fillMaxSize(),
            baseBrushWidth = 18f, // 毛笔基础宽度
            onStrokeAdded = { stroke ->
                strokes = strokes + stroke
                onStrokesChanged(strokes)

                // 延迟计算评分，避免阻塞绘制
                scheduleScoring()
            }
        )

        // 评分显示（右上角）
        if (score > 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
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

        // 通过标记（中心）
        if (score >= 80f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
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
