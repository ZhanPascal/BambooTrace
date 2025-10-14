package com.calligraphy.practice.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主界面
 * 显示常用汉字选择和应用导航
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCharacterSelected: (String) -> Unit
) {
    // 演示用的常用字列表
    val commonCharacters = remember {
        listOf(
            // 数字
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
            // 常用字
            "人", "大", "天", "地", "日", "月", "山", "水", "火", "木",
            "金", "土", "王", "子", "女", "中", "国", "文", "字", "书",
            "法", "练", "习", "学", "生", "师", "好", "爱", "心", "永"
        )
    }

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("汉字书法练习") },
                actions = {
                    IconButton(onClick = { /* TODO: 打开设置 */ }) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "首页") },
                    label = { Text("首页") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LibraryBooks, "字库") },
                    label = { Text("字库") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, "历史") },
                    label = { Text("历史") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, "收藏") },
                    label = { Text("收藏") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // 首页 - 快速开始
                    Text(
                        text = "选择汉字开始练习",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(commonCharacters) { character ->
                            CharacterCard(
                                character = character,
                                onClick = { onCharacterSelected(character) }
                            )
                        }
                    }
                }

                1 -> {
                    // 字库
                    Text(
                        text = "字库浏览",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text("字库功能开发中...")
                }

                2 -> {
                    // 历史记录
                    Text(
                        text = "练习历史",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text("历史记录功能开发中...")
                }

                3 -> {
                    // 收藏
                    Text(
                        text = "收藏的字",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text("收藏功能开发中...")
                }
            }
        }
    }
}

/**
 * 汉字卡片组件
 */
@Composable
fun CharacterCard(
    character: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = character,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
