package com.calligraphy.practice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.calligraphy.practice.ui.screens.MainScreen
import com.calligraphy.practice.ui.screens.PracticeScreen
import com.calligraphy.practice.ui.theme.BambooTraceTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BambooTraceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BambooTraceApp()
                }
            }
        }
    }
}

/**
 * 应用主组件
 * 处理导航逻辑
 */
@Composable
fun BambooTraceApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var selectedCharacter by remember { mutableStateOf("") }

    when (val screen = currentScreen) {
        is Screen.Main -> {
            MainScreen(
                onCharacterSelected = { character ->
                    selectedCharacter = character
                    currentScreen = Screen.Practice(character)
                }
            )
        }

        is Screen.Practice -> {
            PracticeScreen(
                character = screen.character,
                onNavigateBack = {
                    currentScreen = Screen.Main
                }
            )
        }
    }
}

/**
 * 屏幕导航密封类
 */
sealed class Screen {
    object Main : Screen()
    data class Practice(val character: String) : Screen()
}
