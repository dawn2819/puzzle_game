package com.example.puzzlegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.theme.PuzzleGameTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val prefs = remember { GamePreferences(applicationContext) }
      var isDark by remember { mutableStateOf(prefs.isDarkMode) }

      PuzzleGameTheme(darkTheme = isDark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(onDarkModeChanged = { isDark = it })
        }
      }
    }
  }
}
