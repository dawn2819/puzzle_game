package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineOAnQuan
import com.example.puzzlegame.engine.MoveStep
import com.example.puzzlegame.engine.StepType
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayOAnQuanScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val engine = remember { EngineOAnQuan() }
    val coroutineScope = rememberCoroutineScope()

    var board by remember { mutableStateOf(engine.board.clone()) }
    var score1 by remember { mutableStateOf(engine.scorePlayer1) }
    var score2 by remember { mutableStateOf(engine.scorePlayer2) }
    var isPlayer1Turn by remember { mutableStateOf(engine.isPlayer1Turn) }
    var isGameOver by remember { mutableStateOf(engine.isGameOver) }
    var winner by remember { mutableStateOf(engine.winner) }
    var hasQuanLeft by remember { mutableStateOf(engine.hasQuanLeft) }
    var hasQuanRight by remember { mutableStateOf(engine.hasQuanRight) }

    var isVsAI by remember { mutableStateOf(true) } // Chế độ mặc định đấu với máy
    var selectedPitForDirection by remember { mutableStateOf(-1) }
    var isAnimating by remember { mutableStateOf(false) }
    var activeStepInfo by remember { mutableStateOf("") }

    // Đồng bộ trạng thái
    fun syncState() {
        board = engine.board.clone()
        score1 = engine.scorePlayer1
        score2 = engine.scorePlayer2
        isPlayer1Turn = engine.isPlayer1Turn
        isGameOver = engine.isGameOver
        winner = engine.winner
        hasQuanLeft = engine.hasQuanLeft
        hasQuanRight = engine.hasQuanRight
    }

    // Chạy hoạt ảnh rải sỏi từng bước cho sinh động
    fun runAnimation(steps: List<MoveStep>) {
        coroutineScope.launch {
            isAnimating = true
            selectedPitForDirection = -1

            for (step in steps) {
                // Đồng bộ bảng từng bước một
                board = step.boardState.clone()
                activeStepInfo = when (step.type) {
                    StepType.PICKUP -> {
                        audioManager.playClick()
                        "Bốc sỏi từ ô ${if (step.pitIndex >= 6) "hàng dưới" else "hàng trên"} ${step.pitIndex % 6 + 1}..."
                    }
                    StepType.DISTRIBUTE -> {
                        audioManager.playClick()
                        "Rải 1 viên vào ô ${step.pitIndex}..."
                    }
                    StepType.CAPTURE -> {
                        audioManager.playClick() // Phát âm thanh ăn cờ vang dội
                        "🎉 Ăn được ${step.count} sỏi ở ô ${step.pitIndex}!"
                    }
                }
                delay(220) // Độ trễ giữa các hạt rải sỏi
            }

            // Đồng bộ trạng thái cuối
            syncState()
            activeStepInfo = ""
            isAnimating = false

            // Nếu đến lượt AI đi
            if (!isPlayer1Turn && isVsAI && !isGameOver) {
                delay(800)
                val best = engine.getBestMoveForAI()
                if (best != null) {
                    val aiSteps = engine.makeMove(best.first, best.second)
                    runAnimation(aiSteps)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    // Kích hoạt thưởng nếu chiến thắng (chỉ lưu một lần)
    LaunchedEffect(isGameOver, winner) {
        if (isGameOver && winner == 1) {
            val winKey = "oanquan_win_xp_credited"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(150, 30) // +150 XP, +30 Coins
                prefs.saveScore(winKey, 1)
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ô ĂN QUAN", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                audioManager.playClick()
                                isVsAI = !isVsAI
                                engine.reset()
                                syncState()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isVsAI) "🤖 Đấu với Máy" else "👥 Đấu 2 Người", fontWeight = FontWeight.Bold)
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
                // Khu vực hiển thị điểm Người chơi 2 (ở trên)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isVsAI) "🤖 Máy (AI)" else "👥 Người chơi 2",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (!isPlayer1Turn) FlagRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Điểm: $score2 🔴",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = FlagRed
                    )
                }

                // Banner hiển thị thông tin từng bước rải sỏi
                Box(
                    modifier = Modifier.height(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeStepInfo.isNotEmpty()) {
                        Text(
                            text = activeStepInfo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (isAnimating) {
                        Text(
                            text = "Đang rải sỏi...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            text = if (isPlayer1Turn) "👉 Lượt của bạn (Hàng dưới)" else "👈 Lượt đối thủ (Hàng trên)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlayer1Turn) BambooGreen else FlagRed
                        )
                    }
                }

                // --- BÀN CỜ Ô ĂN QUAN (Custom Canvas & Board Layout) ---
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.8f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(24.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    // Lưới chia 10 ô dân nằm giữa:
                    // Ô quan trái (cột trái cùng chiếm 15%), Ô quan phải (cột phải cùng chiếm 15%)
                    // 5 ô dân ở giữa chia đều
                    val quanWidth = w * 0.15f
                    val middleWidth = w * 0.7f
                    val cellWidth = middleWidth / 5

                    // Vẽ bàn cờ gỗ cổ xưa
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 2.dp.toPx()
                        val outerColor = Color(0xFF916F6A)
                        
                        // Đường biên bao quanh
                        drawRoundRect(
                            color = outerColor,
                            style = Stroke(width = strokeW),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                        )

                        // 1. Phân chia 2 ô Quan hình bán nguyệt 2 bên
                        // Quan trái
                        drawArc(
                            color = outerColor,
                            startAngle = 90f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(quanWidth.toPx() - (quanWidth.toPx() * 2), 0f),
                            size = Size(quanWidth.toPx() * 2, size.height),
                            style = Stroke(width = strokeW)
                        )
                        // Quan phải
                        drawArc(
                            color = outerColor,
                            startAngle = 270f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(quanWidth.toPx() + middleWidth.toPx(), 0f),
                            size = Size(quanWidth.toPx() * 2, size.height),
                            style = Stroke(width = strokeW)
                        )

                        // 2. Kẻ đường ngang giữa đôi 2 hàng ô dân
                        drawLine(
                            color = outerColor,
                            start = Offset(quanWidth.toPx(), size.height / 2),
                            end = Offset(quanWidth.toPx() + middleWidth.toPx(), size.height / 2),
                            strokeWidth = strokeW
                        )

                        // 3. Kẻ các vách ngăn 5 ô dân ở giữa
                        for (i in 1..4) {
                            val x = quanWidth.toPx() + (cellWidth.toPx() * i)
                            drawLine(
                                color = outerColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = strokeW
                            )
                        }
                    }

                    // Đặt các ô lên trên Canvas để nhấn chọn tương tác
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Quan trái (Ô index 5)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(quanWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            QuanCell(board[5], hasQuanLeft)
                        }

                        // 5 ô dân ở giữa chia 2 hàng
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(middleWidth)
                        ) {
                            // Hàng trên (Ô 4, 3, 2, 1, 0 từ trái sang phải)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                for (i in 4 downTo 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1f)
                                            .clickable {
                                                if (!isAnimating && !isPlayer1Turn && !isVsAI) {
                                                    selectedPitForDirection = i
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DanCell(board[i], isSelected = selectedPitForDirection == i)
                                    }
                                }
                            }

                            // Hàng dưới (Ô 6, 7, 8, 9, 10 từ trái sang phải)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                for (i in 6..10) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1f)
                                            .clickable {
                                                if (!isAnimating && isPlayer1Turn) {
                                                    selectedPitForDirection = i
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DanCell(board[i], isSelected = selectedPitForDirection == i)
                                    }
                                }
                            }
                        }

                        // Quan phải (Ô index 11)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(quanWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            QuanCell(board[11], hasQuanRight)
                        }
                    }
                }

                // Bảng chọn hướng đi xuất hiện khi người chơi nhấp chọn ô dân hợp lệ
                Box(
                    modifier = Modifier.height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPitForDirection != -1 && !isAnimating) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    val steps = engine.makeMove(selectedPitForDirection, isClockwise = true)
                                    runAnimation(steps)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FlagRed)
                            ) {
                                Text("↩️ Trái (Clockwise)", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    val steps = engine.makeMove(selectedPitForDirection, isClockwise = false)
                                    runAnimation(steps)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BambooGreen)
                            ) {
                                Text("↪️ Phải (Counter-Clockwise)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Khu vực hiển thị điểm Người chơi 1 (ở dưới)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 Bạn (Người chơi 1)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isPlayer1Turn) BambooGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Điểm: $score1 🟢",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BambooGreen
                    )
                }
            }
        }

        // Hộp thoại kết thúc game (Game Over)
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            engine.reset()
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
                        text = if (winner == 1) "🎉 BẠN CHIẾN THẮNG!" else if (winner == 2) "🤖 MÁY CHIẾN THẮNG!" else "🤝 KẾT QUẢ HÒA!",
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
                        Text("Điểm của bạn: $score1 sỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Điểm đối thủ: $score2 sỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (winner == 1) {
                            Text(
                                "Thành tích đáng nể! Nhận ngay +150 XP và +30 Tiền vàng thưởng di sản Việt!",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = BambooGreen
                            )
                        }
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }
    }
}

@Composable
fun DanCell(count: Int, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) StarGold.copy(alpha = 0.4f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) FlagRed else OutlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🟢".repeat(minOf(count, 3)) + if (count > 3) ".." else "",
                fontSize = 11.sp
            )
            Text(
                text = "$count",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = EarthyBrown
            )
        }
    }
}

@Composable
fun QuanCell(count: Int, hasQuan: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (hasQuan) {
                Text("🏺", fontSize = 20.sp) // Đại diện sỏi lớn của ô Quan
            }
            Text(
                text = "🟢".repeat(minOf(maxOf(0, count - if (hasQuan) 10 else 0), 2)) + if (count > 12) ".." else "",
                fontSize = 11.sp
            )
            Text(
                text = "$count",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = FlagRed
            )
        }
    }
}
