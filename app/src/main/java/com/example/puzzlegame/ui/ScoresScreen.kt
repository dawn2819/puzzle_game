package com.example.puzzlegame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("2048", "SUDOKU", "SOKOBAN", "NONOGRAM")

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "BẢNG ĐIỂM",
                            fontFamily = OngDoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = FlagRed
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Row chọn Game
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                audioManager.playClick()
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        )
                    }
                }

                // Chi tiết bảng điểm
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Bảng
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Chế độ/Kích cỡ",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1.5f),
                                color = Color(0xFF78350F)
                            )
                            Text(
                                "Gần nhất",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                "Cao nhất",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                color = FlagRed
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))

                        when (selectedTab) {
                            0 -> { // 2048
                                (4..8).forEach { size ->
                                    val key = "2048_$size"
                                    ScoreRow(
                                        label = "Kích cỡ ${size}x${size}",
                                        lastScore = prefs.getLastScore(key),
                                        highScore = prefs.getHighScore(key)
                                    )
                                }
                            }
                            1 -> { // Sudoku
                                listOf("EASY" to "Dễ (1x)", "MEDIUM" to "Trung bình (2x)", "HARD" to "Khó (5x)").forEach { (diff, label) ->
                                    val key = "sudoku_$diff"
                                    ScoreRow(
                                        label = label,
                                        lastScore = prefs.getLastScore(key),
                                        highScore = prefs.getHighScore(key)
                                    )
                                }
                            }
                            2 -> { // Sokoban
                                listOf("EASY" to "Dễ (1x)", "MEDIUM" to "Trung bình (2x)", "HARD" to "Khó (5x)").forEach { (diff, label) ->
                                    val key = "sokoban_$diff"
                                    ScoreRow(
                                        label = label,
                                        lastScore = prefs.getLastScore(key),
                                        highScore = prefs.getHighScore(key)
                                    )
                                }
                            }
                            3 -> { // Nonogram
                                listOf(8 to "Lưới 8x8", 10 to "Lưới 10x10", 12 to "Lưới 12x12").forEach { (size, label) ->
                                    val key = "nonogram_$size"
                                    ScoreRow(
                                        label = label,
                                        lastScore = prefs.getLastScore(key),
                                        highScore = prefs.getHighScore(key)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreRow(
    label: String,
    lastScore: Int,
    highScore: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = OngDoFontFamily,
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Medium,
            color = EarthyBrown
        )
        Text(
            text = "$lastScore",
            fontSize = 16.sp,
            fontFamily = OngDoFontFamily,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = EarthyBrown
        )
        Text(
            text = "$highScore",
            fontSize = 16.sp,
            fontFamily = OngDoFontFamily,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            color = FlagRed
        )
    }
}
