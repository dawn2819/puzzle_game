package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineCoGanh
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayCoGanhScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val engine = remember { EngineCoGanh() }
    val coroutineScope = rememberCoroutineScope()

    var board by remember { mutableStateOf(engine.board.clone()) }
    var isPlayerTurn by remember { mutableStateOf(engine.isPlayerTurn) }
    var isGameOver by remember { mutableStateOf(engine.isGameOver) }
    var winner by remember { mutableStateOf(engine.winner) }

    var selectedIndex by remember { mutableStateOf(-1) }
    var validTargets by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isAiCalculating by remember { mutableStateOf(false) }

    val greenCount = board.count { it == 1 }
    val redCount = board.count { it == 2 }

    fun syncState() {
        board = engine.board.clone()
        isPlayerTurn = engine.isPlayerTurn
        isGameOver = engine.isGameOver
        winner = engine.winner
    }

    // AI tính nước đi trên background thread để chống lag UI
    fun triggerAiMove() {
        if (isGameOver || isPlayerTurn) return
        coroutineScope.launch {
            isAiCalculating = true
            delay(600) // Tạo độ trễ tự nhiên cho người dùng quan sát
            
            val bestMove = withContext(Dispatchers.Default) {
                engine.getBestMoveForAI()
            }

            if (bestMove != null) {
                engine.makeMove(bestMove.first, bestMove.second)
                audioManager.playClick()
            }
            
            syncState()
            isAiCalculating = false
        }
    }

    // Kích hoạt thưởng nếu chiến thắng (chỉ lưu một lần)
    LaunchedEffect(isGameOver, winner) {
        if (isGameOver && winner == 1) {
            val winKey = "coganh_win_xp_credited"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(200, 40) // +200 XP, +40 Coins
                prefs.saveScore(winKey, 1)
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CỜ GÁNH", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            engine.reset()
                            selectedIndex = -1
                            validTargets = emptyList()
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Chỉ số cờ trên bàn
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("🤖 Đối thủ (AI)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                        Text("Đỏ: $redCount quân", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FlagRed)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPlayerTurn) BambooGreen.copy(alpha = 0.2f) else FlagRed.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAiCalculating) "🤖 AI đang nghĩ..." else if (isPlayerTurn) "👉 Lượt của bạn" else "👈 Lượt của Máy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isPlayerTurn) BambooGreen else FlagRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("👤 Bạn (Xanh)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BambooGreen)
                        Text("Xanh: $greenCount quân", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BambooGreen)
                    }
                }

                // --- BÀN CỜ GÁNH 5x5 CANVAS ---
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    val cellW = w / 4
                    val cellH = h / 4

                    // 1. Vẽ các đường bàn cờ (Đường ngang, dọc, chéo)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 2.dp.toPx()
                        val lineColor = Color(0xFF916F6A)

                        // Các đường dọc và ngang
                        for (i in 0..4) {
                            // Đường ngang
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, cellH.toPx() * i),
                                end = Offset(size.width, cellH.toPx() * i),
                                strokeWidth = strokeW
                            )
                            // Đường dọc
                            drawLine(
                                color = lineColor,
                                start = Offset(cellW.toPx() * i, 0f),
                                end = Offset(cellW.toPx() * i, size.height),
                                strokeWidth = strokeW
                            )
                        }

                        // Các đường chéo nối các đỉnh giao lộ chẵn (r+c)%2 == 0
                        // Kẻ chéo bao quanh các ô bàn cờ vuông lớn
                        // Đường chéo lớn chính từ (0,0) tới (4,4)
                        drawLine(lineColor, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = strokeW)
                        // Đường chéo lớn phụ từ (0,4) tới (4,0)
                        drawLine(lineColor, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = strokeW)

                        // Các đường chéo phụ hình thoi nối giữa các cạnh
                        drawLine(lineColor, Offset(2 * cellW.toPx(), 0f), Offset(0f, 2 * cellH.toPx()), strokeWidth = strokeW)
                        drawLine(lineColor, Offset(2 * cellW.toPx(), 0f), Offset(size.width, 2 * cellH.toPx()), strokeWidth = strokeW)
                        drawLine(lineColor, Offset(0f, 2 * cellH.toPx()), Offset(2 * cellW.toPx(), size.height), strokeWidth = strokeW)
                        drawLine(lineColor, Offset(size.width, 2 * cellH.toPx()), Offset(2 * cellW.toPx(), size.height), strokeWidth = strokeW)
                    }

                    // 2. Đặt các quân cờ lên giao lộ
                    Box(modifier = Modifier.fillMaxSize()) {
                        for (idx in 0..24) {
                            val r = idx / 5
                            val c = idx % 5

                            val x = cellW * c
                            val y = cellH * r

                            val pieceValue = board[idx]
                            val isSelected = selectedIndex == idx
                            val isValidTarget = validTargets.contains(idx)

                            Box(
                                modifier = Modifier
                                    .offset(x = x - 22.dp, y = y - 22.dp)
                                    .size(44.dp)
                                    .clickable(enabled = !isAiCalculating && !isGameOver) {
                                        if (pieceValue == 1 && isPlayerTurn) {
                                            // Chọn quân ta
                                            selectedIndex = idx
                                            validTargets = engine.getValidMovesFrom(idx)
                                        } else if (isValidTarget && selectedIndex != -1) {
                                            // Thực hiện đi quân
                                            val success = engine.makeMove(selectedIndex, idx)
                                            if (success) {
                                                audioManager.playClick()
                                                syncState()
                                                selectedIndex = -1
                                                validTargets = emptyList()
                                                triggerAiMove() // Kích hoạt AI phản công
                                            }
                                        } else {
                                            // Reset chọn ô khác
                                            selectedIndex = -1
                                            validTargets = emptyList()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (pieceValue > 0) {
                                    // Hoạt ảnh xoay lật khi đổi màu quân
                                    val rotation by animateFloatAsState(
                                        targetValue = if (pieceValue == 1) 0f else 180f,
                                        animationSpec = tween(400),
                                        label = "flip"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .graphicsLayer(rotationY = rotation)
                                            .shadow(
                                                elevation = if (isSelected) 8.dp else 2.dp,
                                                shape = CircleShape
                                            )
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = if (pieceValue == 1) {
                                                        listOf(Color(0xFF81C784), BambooGreen) // Men gốm xanh nhạt sang đậm
                                                    } else {
                                                        listOf(Color(0xFFE57373), FlagRed) // Men gốm đỏ nhạt sang đỏ cờ
                                                    }
                                                ),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) StarGold else Color.White.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    )
                                } else if (isValidTarget) {
                                    // Điểm gợi ý nước đi xanh neon mờ tinh tế
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color(0xFF22C55E).copy(alpha = 0.5f), CircleShape)
                                            .border(2.dp, Color(0xFF22C55E), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // Phụ đề hướng dẫn luật chơi cơ bản
                Text(
                    text = "Gánh: kẹp đối phương ở giữa. Chẹt: vây kín không cho đi chuyển.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Hộp thoại kết thúc cờ (Game Over / Thắng thua)
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            engine.reset()
                            selectedIndex = -1
                            validTargets = emptyList()
                            syncState()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Chơi Lại", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        audioManager.playClick()
                        onBack()
                    }) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = if (winner == 1) "🎉 BẠN CHIẾN THẮNG!" else "🤖 AI ĐÃ THẮNG!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (winner == 1) {
                                "Chúc mừng bạn đã xuất sắc chiến thắng AI cờ gánh! Bạn nhận được +200 XP và +40 Tiền vàng thưởng."
                            } else {
                                "AI đã ăn hết quân cờ của bạn. Hãy thử sức lại ván mới để nâng cao chiến thuật nhé."
                            },
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = if (winner == 1) BambooGreen else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }
    }
}
