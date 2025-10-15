package com.calligraphy.practice.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calligraphy.practice.data.model.CalligraphyFont
import com.calligraphy.practice.ui.components.*

/**
 * 练习界面
 * 显示汉字范本和手写Canvas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    character: String,
    onNavigateBack: () -> Unit
) {
    var selectedFont by remember { mutableStateOf(CalligraphyFont.KAISHU) }
    var gridType by remember { mutableStateOf(GridType.MI_GRID) }
    var showReference by remember { mutableStateOf(true) }
    var showOutline by remember { mutableStateOf(true) }
    var showToolbar by remember { mutableStateOf(true) }

    // 练习格子数量（可根据需要调整）
    val numCellsWithTemplate = 12 // 有范本的格子
    val numEmptyCells = 4 // 空白格子（无范本）
    val totalCells = numCellsWithTemplate + numEmptyCells

    val cellScores = remember { mutableStateMapOf<Int, Float>() }
    var activeCellIndex by remember { mutableStateOf<Int?>(null) } // 活跃格子索引
    var triggerScore by remember { mutableStateOf(0) } // 评分触发器
    var clearTrigger by remember { mutableStateOf(0) } // 清空触发器
    var undoTrigger by remember { mutableStateOf(0) } // 撤回触发器

    // 控制选项菜单
    var showFontMenu by remember { mutableStateOf(false) }
    var showGridMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showToolbar) {
                TopAppBar(
                    title = {
                        Column {
                            Text("练习: $character")
                            val passedCells = cellScores.values.count { it >= 80f }
                            Text(
                                "进度: $passedCells/$numCellsWithTemplate 通过 (+${numEmptyCells}空格)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    },
                    actions = {
                        // 撤回按钮
                        IconButton(
                            onClick = { undoTrigger++ }
                        ) {
                            Icon(Icons.Default.Undo, "撤回")
                        }

                        // 清空按钮
                        IconButton(
                            onClick = { clearTrigger++ }
                        ) {
                            Icon(Icons.Default.Delete, "清空")
                        }

                        // 评分按钮
                        IconButton(
                            onClick = { triggerScore++ }
                        ) {
                            Icon(Icons.Default.CheckCircle, "评分")
                        }

                        // 更多选项
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, "更多")
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("隐藏工具栏") },
                                onClick = {
                                    showToolbar = false
                                    showOptionsMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.VisibilityOff, null)
                                }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            // 悬浮按钮：隐藏工具栏时显示
            if (!showToolbar) {
                FloatingActionButton(
                    onClick = { showToolbar = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Menu, "显示工具栏")
                }
            }
        },
        bottomBar = {
            if (showToolbar) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 字体选择
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { showFontMenu = true }) {
                                Icon(Icons.Default.TextFields, "字体")
                            }
                            Text(
                                text = selectedFont.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )

                            DropdownMenu(
                                expanded = showFontMenu,
                                onDismissRequest = { showFontMenu = false }
                            ) {
                                CalligraphyFont.entries.forEach { font ->
                                    DropdownMenuItem(
                                        text = { Text(font.displayName) },
                                        onClick = {
                                            selectedFont = font
                                            showFontMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // 网格选择
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { showGridMenu = true }) {
                                Icon(Icons.Default.GridOn, "网格")
                            }
                            Text(
                                text = when (gridType) {
                                    GridType.MI_GRID -> "米字格"
                                    GridType.TIAN_GRID -> "田字格"
                                    GridType.NINE_GRID -> "九宫格"
                                    GridType.NONE -> "无"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )

                            DropdownMenu(
                                expanded = showGridMenu,
                                onDismissRequest = { showGridMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("米字格") },
                                    onClick = {
                                        gridType = GridType.MI_GRID
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("田字格") },
                                    onClick = {
                                        gridType = GridType.TIAN_GRID
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("九宫格") },
                                    onClick = {
                                        gridType = GridType.NINE_GRID
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("无网格") },
                                    onClick = {
                                        gridType = GridType.NONE
                                        showGridMenu = false
                                    }
                                )
                            }
                        }

                        // 显示范本
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showReference = !showReference }
                            ) {
                                Icon(
                                    if (showReference) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    "范本"
                                )
                            }
                            Text(
                                text = "范本",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // 显示轮廓
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showOutline = !showOutline }
                            ) {
                                Icon(
                                    if (showOutline) Icons.Default.BorderOuter
                                    else Icons.Default.BorderClear,
                                    "轮廓"
                                )
                            }
                            Text(
                                text = "轮廓",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // 使用LazyVerticalGrid实现懒加载，只渲染可见的格子
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = rememberLazyGridState()
        ) {
            items(
                count = totalCells,
                key = { index -> index }
            ) { index ->
                // 使用key确保每个格子的状态独立
                val isEmptyCell = index >= numCellsWithTemplate

                PracticeCell(
                    character = character,
                    cellSize = 120,
                    fontPath = selectedFont.fontFileName,
                    gridType = gridType,
                    showReference = if (isEmptyCell) false else showReference, // 空格子不显示范本
                    showOutline = if (isEmptyCell) false else showOutline, // 空格子不显示轮廓
                    triggerScore = triggerScore,
                    clearTrigger = clearTrigger,
                    undoTrigger = undoTrigger,
                    isActive = activeCellIndex == index, // 是否是活跃格子
                    onActive = {
                        activeCellIndex = index // 设置为活跃格子
                    },
                    onScoreChanged = { score ->
                        if (!isEmptyCell) { // 只计算有范本格子的分数
                            cellScores[index] = score
                        }
                    }
                )
            }
        }
    }
}
