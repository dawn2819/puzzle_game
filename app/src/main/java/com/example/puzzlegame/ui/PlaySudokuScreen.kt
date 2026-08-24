package com.example.puzzlegame.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineSudoku
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.ui.components.ConfettiEffect
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.audio.AudioManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaySudokuScreen(
    difficulty: String,
    isRestore: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentDifficulty by remember { mutableStateOf(difficulty) }
    val engine = remember(currentDifficulty) { EngineSudoku(currentDifficulty) }
    val boardState = remember { mutableStateListOf<IntArray>() }

    var selectedRow by remember { mutableStateOf(-1) }
    var selectedCol by remember { mutableStateOf(-1) }

    var isNotesMode by remember { mutableStateOf(false) }
    val notesState = remember { mutableStateMapOf<String, Set<Int>>() }

    val completedNumbers = remember(boardState) {
        val counts = IntArray(10)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val valAt = if (boardState.size > r) boardState[r][c] else 0
                if (valAt in 1..9) {
                    counts[valAt]++
                }
            }
        }
        (1..9).filter { counts[it] >= 9 }.toSet()
    }

    var timeRemaining by remember { mutableStateOf(engine.timeRemaining) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // Đồng bộ state từ engine
    fun syncState() {
        boardState.clear()
        for (r in 0 until 9) {
            boardState.add(engine.currentBoard[r].copyOf())
            for (c in 0 until 9) {
                if (engine.currentBoard[r][c] > 0) {
                    notesState.remove("${r}_${c}")
                }
            }
        }
        timeRemaining = engine.timeRemaining
        score = engine.score
        isGameOver = engine.isGameOver
        isVictory = engine.isVictory

        // Lưu trạng thái Continue
        if (!isGameOver && !isVictory) {
            prefs.saveContinueGame("SUDOKU", engine.serializeState())
        } else {
            prefs.clearContinueGame()
            if (isVictory) {
                prefs.saveScore("sudoku_$currentDifficulty", score)
                val winKey = "sudoku_win_credited_${currentDifficulty}_${engine.score}"
                val alreadyCredited = prefs.getHighScore(winKey) > 0
                if (!alreadyCredited) {
                    val xpGained = when (currentDifficulty) {
                        "EASY" -> 50
                        "MEDIUM" -> 150
                        else -> 300
                    }
                    val coinsGained = when (currentDifficulty) {
                        "EASY" -> 10
                        "MEDIUM" -> 30
                        else -> 60
                    }
                    prefs.addXpAndCoins(xpGained, coinsGained)
                    prefs.saveScore(winKey, 1)
                }
            }
        }
    }

    // Khởi tạo
    LaunchedEffect(Unit) {
        if (isRestore && prefs.hasContinueGame() && prefs.getContinueGameType() == "SUDOKU") {
            try {
                val data = prefs.getContinueGameData()!!
                val parts = data.split("|")
                val savedScore = parts[1].toInt()
                val savedTime = parts[2].toInt()
                val savedInitial = prefs.deserializeGrid(parts[3])
                val savedCurrent = prefs.deserializeGrid(parts[4])
                val savedSolution = prefs.deserializeGrid(parts[5])
                engine.restoreState(savedInitial, savedCurrent, savedSolution, savedTime, savedScore)
            } catch (e: Exception) {
                engine.generateGame()
            }
        } else {
            engine.generateGame()
        }
        syncState()
    }

    // Vòng lặp đếm ngược thời gian
    LaunchedEffect(isGameOver, isVictory, isPaused, currentDifficulty) {
        if (currentDifficulty != "EASY" && !isGameOver && !isVictory && !isPaused) {
            while (true) {
                delay(1000)
                engine.tickSecond()
                syncState()
                if (engine.isGameOver || engine.isVictory) break
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SUDOKU - $currentDifficulty", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            if (!engine.isGameOver && !engine.isVictory) {
                                prefs.saveContinueGame("SUDOKU", engine.serializeState())
                            }
                            onBack()
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
                            engine.generateGame()
                            selectedRow = -1
                            selectedCol = -1
                            syncState()
                        }) {
                            Text("🔄", fontSize = 18.sp)
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thanh thông số: Thời gian (nếu có), Nhân hệ số
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentDifficulty != "EASY") {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        val timeColor = if (timeRemaining < 30) Color.Red else MaterialTheme.colorScheme.onBackground
                        Text(
                            text = String.format("Thời gian: %02d:%02d", minutes, seconds),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = timeColor
                        )
                    } else {
                        Text("Thời gian: Vô hạn", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    val mult = when (currentDifficulty) {
                        "EASY" -> "1x"
                        "MEDIUM" -> "2x"
                        "HARD" -> "5x"
                        else -> "1x"
                    }
                    Text("Hệ số điểm: $mult", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Grid 9x9 Sudoku Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (r in 0 until 9) {
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 9) {
                                    val valAt = if (boardState.size > r) boardState[r][c] else 0
                                    val isEditable = engine.initialBoard[r][c] == 0
                                    val isCorrect = engine.isCellCorrect(r, c)
                                    val isSelected = selectedRow == r && selectedCol == c

                                    val selectedVal = if (selectedRow != -1 && selectedCol != -1 && boardState.size > selectedRow) boardState[selectedRow][selectedCol] else 0
                                    val isSameValue = selectedVal > 0 && valAt == selectedVal
                                    val inSameRow = selectedRow == r
                                    val inSameCol = selectedCol == c
                                    val inSameBlock = selectedRow != -1 && selectedCol != -1 && (r / 3 == selectedRow / 3) && (c / 3 == selectedCol / 3)

                                    key(r, c) {
                                        var shakeOffset by remember { mutableStateOf(0f) }
                                        val animatedShakeOffset by animateFloatAsState(
                                            targetValue = shakeOffset,
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
                                            label = "shake"
                                        )

                                        LaunchedEffect(valAt) {
                                            if (isEditable && valAt > 0 && !isCorrect) {
                                                shakeOffset = 1f
                                                delay(300)
                                                shakeOffset = 0f
                                            }
                                        }

                                        val cellColor = when {
                                            isSelected -> LacquerRed.copy(alpha = 0.2f)
                                            isSameValue -> StarGold.copy(alpha = 0.2f)
                                            inSameRow || inSameCol || inSameBlock -> FlagRed.copy(alpha = 0.06f)
                                            !isEditable -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                                            else -> Color.Transparent
                                        }
                                        val animatedCellColor by animateColorAsState(
                                            targetValue = cellColor,
                                            animationSpec = tween(200),
                                            label = "cell_color"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .graphicsLayer(translationX = animatedShakeOffset)
                                                .border(
                                                    BorderStroke(
                                                        if (isSelected) 2.dp else 0.5.dp,
                                                        if (isSelected) FlagRed else OutlineVariant.copy(alpha = 0.5f)
                                                    )
                                                )
                                                .background(animatedCellColor)
                                                .clickable {
                                                    audioManager.playClick()
                                                    selectedRow = r
                                                    selectedCol = c
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (valAt > 0) {
                                                Text(
                                                    text = "$valAt",
                                                    fontWeight = if (isEditable) FontWeight.Medium else FontWeight.ExtraBold,
                                                    fontSize = 20.sp,
                                                    color = when {
                                                        !isEditable -> MaterialTheme.colorScheme.onBackground
                                                        !isCorrect -> Color(0xFFBA1A1A)
                                                        else -> FlagRed
                                                    }
                                                )
                                            } else {
                                                val notes = notesState["${r}_${c}"] ?: emptySet()
                                                if (notes.isNotEmpty()) {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize().padding(3.dp),
                                                        verticalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        for (i in 0 until 3) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                for (j in 1..3) {
                                                                    val num = i * 3 + j
                                                                    val hasNote = notes.contains(num)
                                                                    Text(
                                                                        text = if (hasNote) "$num" else "",
                                                                        fontSize = 8.sp,
                                                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                                        fontWeight = FontWeight.Bold
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
                            }
                        }
                    }

                    // Vẽ các đường kẻ 3x3 dày ngăn cách chuyên nghiệp theo quy chuẩn Sudoku
                    val gridLineColor = if (MaterialTheme.colorScheme.background == DarkBgStart) {
                        DarkOutline
                    } else {
                        OutlineBrown
                    }
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val strokeWidth = 2.5.dp.toPx()
                        val outerStroke = 3.5.dp.toPx()

                        // Đường dọc
                        drawLine(gridLineColor, Offset(width / 3f, 0f), Offset(width / 3f, height), strokeWidth)
                        drawLine(gridLineColor, Offset(2f * width / 3f, 0f), Offset(2f * width / 3f, height), strokeWidth)

                        // Đường ngang
                        drawLine(gridLineColor, Offset(0f, height / 3f), Offset(width, height / 3f), strokeWidth)
                        drawLine(gridLineColor, Offset(0f, 2f * height / 3f), Offset(width, 2f * height / 3f), strokeWidth)

                        // Khung viền ngoài cùng sắc nét
                        drawRect(
                            color = gridLineColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = outerStroke)
                        )
                    }
                }

                // Input Pad: Số từ 1-9 + Nút Xóa
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val remainingNumbers = (1..9).filter { !completedNumbers.contains(it) }
                    val allButtons = remainingNumbers.map { it.toString() } + listOf("✏️ Nháp", "❌ Xóa")
                    val chunkedButtons = allButtons.chunked(5)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunkedButtons.forEach { rowButtons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowButtons.forEach { btnText ->
                                    val isActionNote = btnText == "✏️ Nháp"
                                    val isActionClear = btnText == "❌ Xóa"

                                    val buttonColor = when {
                                        isActionClear -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        isActionNote -> ButtonDefaults.buttonColors(
                                            containerColor = if (isNotesMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                        )
                                        else -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    Button(
                                        onClick = {
                                            audioManager.playClick()
                                            if (isActionClear) {
                                                if (selectedRow != -1 && selectedCol != -1) {
                                                    engine.setNumber(selectedRow, selectedCol, 0)
                                                    syncState()
                                                }
                                            } else if (isActionNote) {
                                                isNotesMode = !isNotesMode
                                            } else {
                                                val num = btnText.toInt()
                                                if (selectedRow != -1 && selectedCol != -1) {
                                                    if (isNotesMode) {
                                                        val key = "${selectedRow}_${selectedCol}"
                                                        val currentNotes = notesState[key] ?: emptySet()
                                                        val newNotes = if (currentNotes.contains(num)) {
                                                            currentNotes - num
                                                        } else {
                                                            currentNotes + num
                                                        }
                                                        notesState[key] = newNotes
                                                    } else {
                                                        val changed = engine.setNumber(selectedRow, selectedCol, num)
                                                        if (changed) {
                                                            syncState()
                                                            if (!engine.isCellCorrect(selectedRow, selectedCol)) {
                                                                audioManager.playError()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = buttonColor,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = btnText,
                                            fontSize = if (isActionNote || isActionClear) 12.sp else 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                if (rowButtons.size < 5) {
                                    repeat(5 - rowButtons.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                ConfettiEffect(isActive = isVictory)

                // Victory/Gameover Overlay
                if (isVictory) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("CHIẾN THẮNG!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Chúc mừng! Bạn đã giải xong bảng Sudoku xuất sắc!\nĐiểm đạt được: $score", textAlign = TextAlign.Center, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        audioManager.playSuccess()
                                        engine.generateGame()
                                        selectedRow = -1
                                        selectedCol = -1
                                        syncState()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Chơi lại màn này 🔄", fontWeight = FontWeight.Bold)
                                }

                                if (currentDifficulty != "HARD") {
                                    Button(
                                        onClick = {
                                            audioManager.playSuccess()
                                            currentDifficulty = if (currentDifficulty == "EASY") "MEDIUM" else "HARD"
                                            selectedRow = -1
                                            selectedCol = -1
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("Chơi tiếp mức cao hơn 🚀", fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        audioManager.playClick()
                                        onBack()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Trở về màn hình chính 🏠", fontWeight = FontWeight.Bold)
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
                        title = { Text("HẾT GIỜ!", color = Color.Red, fontWeight = FontWeight.Bold) },
                        text = { Text("Đồng hồ đã chỉ về 0. Bạn đã không kịp giải Sudoku lần này.") },
                        confirmButton = {
                            Button(onClick = {
                                audioManager.playClick()
                                engine.generateGame()
                                selectedRow = -1
                                selectedCol = -1
                                syncState()
                            }) {
                                Text("Thử lại")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                audioManager.playClick()
                                onBack()
                            }) {
                                Text("Quay lại menu")
                            }
                        }
                    )
                }

                // Pause Screen Overlay (che mờ lưới)
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
                                    engine.generateGame()
                                    selectedRow = -1
                                    selectedCol = -1
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
                                    if (!engine.isGameOver && !engine.isVictory) {
                                        prefs.saveContinueGame("SUDOKU", engine.serializeState())
                                    }
                                    onBack()
                                },
                                modifier = Modifier.width(180.dp)
                            ) {
                                Text("Thoát")
                            }
                        }
                    }
                }
            }
        }
    }
}
