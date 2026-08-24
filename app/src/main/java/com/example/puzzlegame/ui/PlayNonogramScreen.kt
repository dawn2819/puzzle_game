package com.example.puzzlegame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineNonogram
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.ui.components.ConfettiEffect
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.audio.AudioManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayNonogramScreen(
    levelIndex: Int,
    isRestore: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(levelIndex) }
    var unlockedLevel by remember { mutableStateOf(prefs.nonogramUnlockedLevel) }

    val engine = remember(currentLevel) {
        if (currentLevel != -1) EngineNonogram(currentLevel) else null
    }

    val boardState = remember { mutableStateListOf<IntArray>() }
    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var mistakesCount by remember { mutableStateOf(0) }
    var inputMode by remember { mutableStateOf(1) } // 1: Tô màu, 2: Đánh dấu X
    var isPaused by remember { mutableStateOf(false) }

    // Rung lắc khi sai
    var shakeTrigger by remember { mutableStateOf(0) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeTrigger > 0) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 300
            0f at 0
            -8f at 50
            8f at 100
            -8f at 150
            8f at 200
            -4f at 250
            0f at 300
        },
        label = "grid_shake",
        finishedListener = { shakeTrigger = 0 }
    )

    fun syncState() {
        engine?.let { eng ->
            boardState.clear()
            for (r in 0 until eng.size) {
                boardState.add(eng.currentBoard[r].copyOf())
            }
            isVictory = eng.isVictory
            isGameOver = eng.isGameOver
            score = eng.score
            mistakesCount = eng.mistakesCount

            if (!isVictory && !isGameOver && !isPaused) {
                prefs.saveContinueGame("NONOGRAM", eng.serializeState())
            } else if (isVictory) {
                prefs.clearContinueGame()
                prefs.saveScore("nonogram_level_$currentLevel", score)

                val winKey = "nonogram_win_credited_$currentLevel"
                val alreadyCredited = prefs.getHighScore(winKey) > 0
                if (!alreadyCredited) {
                    prefs.addXpAndCoins(100, 20)
                    prefs.saveScore(winKey, 1)
                }

                // Mở khóa màn tiếp theo
                if (currentLevel == unlockedLevel && currentLevel < 15) {
                    unlockedLevel = currentLevel + 1
                    prefs.nonogramUnlockedLevel = unlockedLevel
                }
            }
        }
    }

    LaunchedEffect(currentLevel) {
        if (currentLevel == -1) {
            prefs.clearContinueGame()
            return@LaunchedEffect
        }

        if (isRestore && prefs.hasContinueGame() && prefs.getContinueGameType() == "NONOGRAM") {
            try {
                val data = prefs.getContinueGameData()!!
                val parts = data.split("|")
                val savedLevel = parts[0].toInt()
                if (savedLevel == currentLevel) {
                    val savedScore = parts[1].toInt()
                    val savedMistakes = parts[2].toInt()
                    val savedBoard = prefs.deserializeGrid(parts[3])
                    engine?.restoreState(savedBoard, savedScore, savedMistakes)
                } else {
                    engine?.loadLevel()
                }
            } catch (e: Exception) {
                engine?.loadLevel()
            }
        } else {
            engine?.loadLevel()
        }
        syncState()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground {
        if (currentLevel == -1) {
            // --- LEVEL SELECTION SCREEN ---
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("NONOGRAM - CHỌN MÀN CHƠI", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                        navigationIcon = {
                            IconButton(onClick = {
                                audioManager.playClick()
                                onBack()
                            }) {
                                Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Giải đáp lưới ô số để mở khóa tranh nghệ thuật pixel tiếp theo!",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(15) { index ->
                            val lvl = index + 1
                            val isLocked = lvl > unlockedLevel
                            val isCleared = lvl < unlockedLevel
                            val sizeText = when {
                                lvl <= 5 -> "8x8"
                                lvl <= 10 -> "10x10"
                                else -> "12x12"
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            isLocked -> Color.Gray.copy(alpha = 0.2f)
                                            isCleared -> Color(0xFF10B981).copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        BorderStroke(
                                            if (lvl == unlockedLevel) 2.dp else 1.dp,
                                            if (lvl == unlockedLevel) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(!isLocked) {
                                        audioManager.playClick()
                                        currentLevel = lvl
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLocked) {
                                    Text("🔒", fontSize = 16.sp)
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Màn $lvl", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                        Text(sizeText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                        if (isCleared) {
                                            Text("✔️ Cleaned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (unlockedLevel > 1) {
                        OutlinedButton(
                            onClick = {
                                audioManager.playClick()
                                prefs.nonogramUnlockedLevel = 1
                                unlockedLevel = 1
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Đặt lại tiến trình chơi 🔄", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // --- GAMEPLAY SCREEN ---
            val size = engine?.size ?: 8
            val cellSize = when (size) {
                8 -> 32.dp
                10 -> 26.dp
                else -> 22.dp
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("NONOGRAM - MÀN $currentLevel", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = {
                                audioManager.playClick()
                                if (!isGameOver && !isVictory) {
                                    engine?.let { prefs.saveContinueGame("NONOGRAM", it.serializeState()) }
                                }
                                currentLevel = -1
                            }) {
                                Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                audioManager.playClick()
                                isPaused = true
                            }) {
                                Text("⏸", fontSize = 18.sp)
                            }
                            IconButton(onClick = {
                                audioManager.playClick()
                                engine?.loadLevel()
                                syncState()
                            }) {
                                Text("🔄", fontSize = 18.sp)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Stats: Mạng (Hearts)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hearts = "❤️".repeat(maxOf(0, 3 - mistakesCount)) + "🖤".repeat(mistakesCount.coerceIn(0, 3))
                        Text("Lượt sai: $hearts", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(engine?.getArtName() ?: "", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    // Grid Board + Clues (Shaking enabled on mistakes)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer(translationX = shakeOffset)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // 1. Column Clues (Top Clues)
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Empty corner alignment box
                                Box(modifier = Modifier.size(width = 54.dp, height = 60.dp))

                                for (c in 0 until size) {
                                    val clues = engine?.colClues?.getOrNull(c) ?: listOf(0)
                                    val isColDone = engine?.isColCompleted(c) ?: false

                                    Box(
                                        modifier = Modifier.size(width = cellSize, height = 60.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            clues.forEach { num ->
                                                Text(
                                                    text = if (num > 0) "$num" else "",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isColDone) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onBackground,
                                                    textDecoration = if (isColDone) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Row Clues + Board Grid Cells
                            for (r in 0 until size) {
                                Row(
                                    modifier = Modifier.wrapContentWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Row clues
                                    val clues = engine?.rowClues?.getOrNull(r) ?: listOf(0)
                                    val isRowDone = engine?.isRowCompleted(r) ?: false

                                    Box(
                                        modifier = Modifier.size(width = 54.dp, height = cellSize),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            clues.forEach { num ->
                                                Text(
                                                    text = if (num > 0) "$num" else "",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isRowDone) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onBackground,
                                                    textDecoration = if (isRowDone) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                            }
                                        }
                                    }

                                    // Row cell items
                                    for (c in 0 until size) {
                                        val valAt = if (boardState.size > r && boardState[r].size > c) boardState[r][c] else 0
                                        key(r, c, valAt) {
                                            var cellScale by remember { mutableStateOf(1f) }
                                            val animatedCellScale by animateFloatAsState(
                                                targetValue = cellScale,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                                label = "nonogram_cell_scale"
                                            )
                                            LaunchedEffect(valAt) {
                                                if (valAt > 0) {
                                                    cellScale = 1.25f
                                                    delay(120)
                                                    cellScale = 1.0f
                                                }
                                            }

                                            val isCellFilled = valAt == 1 || (isVictory && engine?.solution?.get(r)?.get(c) == 1)
                                            val isCellCrossed = valAt == 2 && !isVictory

                                            val borderW = if ((r + 1) % 5 == 0 || (c + 1) % 5 == 0) 1.5.dp else 0.5.dp
                                            val borderColor = if ((r + 1) % 5 == 0 || (c + 1) % 5 == 0) OutlineBrown else OutlineVariant

                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .graphicsLayer(scaleX = animatedCellScale, scaleY = animatedCellScale)
                                                    .border(borderW, borderColor)
                                                    .background(
                                                        if (isCellFilled) {
                                                            if (isVictory) getNonogramArtColor(currentLevel) else NonogramFilled
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    )
                                                    .clickable {
                                                        if (isVictory || isGameOver) return@clickable
                                                        engine?.let { eng ->
                                                            val correct = eng.selectCell(r, c, inputMode)
                                                            if (!correct) {
                                                                 audioManager.playError()
                                                                 shakeTrigger = 1
                                                            } else {
                                                                 audioManager.playClick()
                                                            }
                                                            syncState()
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCellCrossed) {
                                                    Text("✕", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NonogramCross)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Interactive Switch Mode: Fill or Cross
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    inputMode = 1
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (inputMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("TÔ MÀU 🟦", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    inputMode = 2
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (inputMode == 2) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("ĐÁNH X ❌", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // --- DIALOGS OVERLAYS ---
                ConfettiEffect(isActive = isVictory)

                if (isVictory) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("CHIẾN THẮNG!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (currentLevel == 15)
                                        "Xuất sắc! Bạn đã giải mã toàn bộ 15 bức tranh pixel tuyệt đẹp của Nonogram!"
                                    else
                                        "Bức tranh pixel đã được giải mã thành công!\nĐiểm số đạt được: $score",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )

                                Button(
                                    onClick = {
                                        audioManager.playSuccess()
                                        engine?.loadLevel()
                                        syncState()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Chơi lại màn này 🔄", fontWeight = FontWeight.Bold)
                                }

                                if (currentLevel < 15) {
                                    Button(
                                        onClick = {
                                            audioManager.playSuccess()
                                            currentLevel += 1
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("Màn tiếp theo 🚀", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Màn 15 reset all
                                    Button(
                                        onClick = {
                                            audioManager.playClick()
                                            prefs.nonogramUnlockedLevel = 1
                                            unlockedLevel = 1
                                            currentLevel = -1
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("Đặt lại từ đầu (Reset all) 🔒", fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        audioManager.playClick()
                                        currentLevel = -1
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Trở về chọn màn 🏠", fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {}
                    )
                }

                if (isGameOver) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("THẤT BẠI!", color = Color.Red, fontWeight = FontWeight.Bold) },
                        text = { Text("Bạn đã vượt quá 3 lần phạm lỗi sai quy định!") },
                        confirmButton = {
                            Button(onClick = {
                                audioManager.playClick()
                                engine?.loadLevel()
                                syncState()
                            }) {
                                Text("Chơi lại")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                audioManager.playClick()
                                currentLevel = -1
                            }) {
                                Text("Về chọn màn")
                            }
                        }
                    )
                }

                // --- PAUSE OVERLAY ---
                if (isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("ĐÃ TẠM DỪNG", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    isPaused = false
                                },
                                modifier = Modifier.width(180.dp)
                            ) {
                                Text("Tiếp tục")
                            }
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    engine?.loadLevel()
                                    syncState()
                                    isPaused = false
                                },
                                modifier = Modifier.width(180.dp)
                            ) {
                                Text("Chơi lại")
                            }
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    if (engine != null && !engine.isGameOver && !engine.isVictory) {
                                        prefs.saveContinueGame("NONOGRAM", engine.serializeState())
                                    }
                                    currentLevel = -1
                                    isPaused = false
                                },
                                modifier = Modifier.width(180.dp)
                            ) {
                                Text("Về chọn màn")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getNonogramArtColor(levelIndex: Int): Color {
    return when (levelIndex) {
        1 -> Color(0xFFEF4444) // Trái tim -> Đỏ
        2 -> Color(0xFFFBBF24) // Mặt cười -> Vàng ấm
        3 -> Color(0xFF10B981) // Dấu tích -> Xanh lá
        4 -> Color(0xFF06B6D4) // Cái cốc -> Xanh cyan
        5 -> Color(0xFFD97706) // Ngôi nhà -> Nâu hổ phách
        6 -> Color(0xFF047857) // Cây thông -> Xanh thông
        7 -> Color(0xFF8B5CF6) // Thanh kiếm -> Tím Violet
        8 -> Color(0xFF3B82F6) // Mỏ neo -> Xanh lam
        9 -> Color(0xFFEC4899) // Chú hề -> Hồng tươi
        10 -> Color(0xFFFBBF24) // Vương miện -> Vàng hoàng kim
        11 -> Color(0xFFEAB308) // Ngôi sao -> Vàng chanh
        12 -> Color(0xFF6366F1) // Cái khiên -> Xanh Indigo
        13 -> Color(0xFF854D0E) // Lâu đài -> Nâu sẫm
        14 -> Color(0xFFD946EF) // Bông hoa -> Hồng cánh sen
        15 -> Color(0xFFF43F5E) // Con bướm -> Đỏ hồng
        else -> Color(0xFF3B82F6)
    }
}
