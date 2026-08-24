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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayBitMatDeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(1) }
    var attemptsLeft by remember { mutableStateOf(5) }
    var goatPos by remember { mutableStateOf(Pair(2, 2)) }
    var lastSelectedPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    var lastDistanceHint by remember { mutableStateOf("") }
    var lastDirectionHint by remember { mutableStateOf("") }
    var lastSoundHint by remember { mutableStateOf("") }

    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Bản ghi lịch sử các ô đã đoán để vẽ vòng sáng gợi ý
    val guessedCells = remember { mutableStateMapOf<Pair<Int, Int>, String>() }

    fun initNewGame() {
        val maxAttempts = when (currentLevel) {
            1 -> 6
            2 -> 5
            else -> 4
        }
        attemptsLeft = maxAttempts
        guessedCells.clear()
        lastSelectedPos = null
        lastDistanceHint = ""
        lastDirectionHint = ""
        lastSoundHint = ""
        isVictory = false
        isGameOver = false

        // Đặt dê ngẫu nhiên trong lưới 5x5
        val r = Random.nextInt(5)
        val c = Random.nextInt(5)
        goatPos = Pair(r, c)
    }

    LaunchedEffect(currentLevel) {
        initNewGame()
    }

    // Thưởng khi thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "bitmatde_win_credited_level_$currentLevel"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                prefs.saveScore(winKey, 1)
            }
        }
    }

    fun guessCell(r: Int, c: Int) {
        if (isGameOver || isVictory || guessedCells.containsKey(Pair(r, c))) return

        val target = Pair(r, c)
        lastSelectedPos = target
        attemptsLeft--

        // 1. Đoán trúng dê
        if (target == goatPos) {
            audioManager.playClick()
            guessedCells[target] = "🐐"
            isVictory = true
            return
        }

        // 2. Tính toán khoảng cách Chebyshev/Manhattan
        val dr = goatPos.first - r
        val dc = goatPos.second - c
        val manhattanDist = kotlin.math.abs(dr) + kotlin.math.abs(dc)

        // Khoảng cách gợi ý
        val distText = when {
            manhattanDist <= 1 -> "Rất Gần 🎯"
            manhattanDist == 2 -> "Gần"
            else -> "Xa 🍃"
        }
        lastDistanceHint = distText

        // Âm thanh dê kêu
        val soundText = when {
            manhattanDist <= 1 -> "Be be kêu: RẤT TO! 🔊"
            manhattanDist == 2 -> "Be be kêu: Vừa phải 🔉"
            else -> "Be be kêu: Rất nhỏ... 🔈"
        }
        lastSoundHint = soundText

        // Xác định hướng (Bắc, Nam, Đông, Tây)
        // Vị trí dê so với ô người chơi click
        val dirText = buildString {
            // Level 3 thỉnh thoảng có nhiễu loạn thông tin hướng
            val hasNoise = currentLevel == 3 && Random.nextFloat() < 0.25f
            if (hasNoise) {
                append("La bàn nhiễu loạn! 🌀")
            } else {
                append("Dê ở phía: ")
                if (dr < 0) append("Bắc ")
                if (dr > 0) append("Nam ")
                if (dc < 0) append("Tây")
                if (dc > 0) append("Đông")
                if (dr == 0 && dc == 0) append("Tại chỗ")
            }
        }
        lastDirectionHint = dirText

        audioManager.playError()
        guessedCells[target] = "❌"

        // Kiểm tra thua cuộc
        if (attemptsLeft <= 0) {
            isGameOver = true
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("BỊT MẮT BẮT DÊ", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        Text(
                            text = "Màn $currentLevel/3 🏆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                // Header chỉ số đoán
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số lượt còn: $attemptsLeft 💔",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (attemptsLeft < 3) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }

                // Gợi ý phản hồi âm thanh la bàn
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceNormal.copy(alpha = 0.8f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (lastSoundHint.isEmpty()) "Hệ thống Bịt mắt bắt dê đã sẵn sàng." else lastSoundHint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagRed,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (lastDirectionHint.isEmpty()) "Nhấn chọn ô trên lưới để lắng nghe dê kêu." else lastDirectionHint,
                        fontSize = 14.sp,
                        color = EarthyBrown,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (lastDistanceHint.isEmpty()) "Khoảng cách dê: Chưa rõ" else "Khoảng cách dê: $lastDistanceHint",
                        fontSize = 14.sp,
                        color = BambooGreen,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // --- LƯỚI MA TRẬN ĐOÁN DÊ 5x5 ---
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val w = maxWidth
                    val cellSize = w / 5

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (r in 0 until 5) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 5) {
                                    val isGuessed = guessedCells.containsKey(Pair(r, c))
                                    val symbol = guessedCells[Pair(r, c)] ?: ""

                                    Box(
                                        modifier = Modifier
                                            .size(cellSize - 6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isGuessed) Color.LightGray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.7f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isGuessed) Color.Gray else OutlineBrown,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(!isGuessed && !isGameOver && !isVictory) {
                                                guessCell(r, c)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Hướng dẫn
                Text(
                    text = "Dê đang nấp ngẫu nhiên dưới bụi cỏ mù sương. Lắng nghe hướng la bàn gợi ý để dò ra dê!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Hộp thoại thất bại (Game Over)
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            initNewGame()
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
                        "💔 BẮT HỤT DÊ!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bạn đã dùng hết số lượt tìm kiếm. Vị trí con dê ẩn giấu là ô khoanh vùng đỏ:", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Hàng ${goatPos.first + 1}, Cột ${goatPos.second + 1} 🐐", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại thắng (Victory)
        if (isVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            if (currentLevel < 3) {
                                currentLevel++
                            } else {
                                currentLevel = 1
                            }
                            initNewGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (currentLevel < 3) "Màn Tiếp Theo" else "Chơi Lại Màn 1", fontWeight = FontWeight.Bold)
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
                        "🎉 ĐÃ TÌM ĐƯỢC DÊ!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = BambooGreen
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lắng tai suy luận xuất sắc! Bạn đã vồ trúng con dê đang ẩn nấp 🐐!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+100 XP và +20 Tiền vàng thưởng đã được ghi nhận vào hồ sơ di sản Hồn Việt!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BambooGreen
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }
    }
}
