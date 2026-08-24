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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayThaSuaScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(1) }
    var playerPos by remember { mutableStateOf(Pair(6, 2)) } // Hàng 6 (dưới), cột 2 (giữa)
    var leeches by remember { mutableStateOf(listOf(Pair(0, 0), Pair(0, 4))) } // 2 con đỉa ở góc trên
    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var stepsCount by remember { mutableStateOf(0) }

    // Bản đồ lau sậy cản trở (bụi sậy = walls)
    val obstacles = remember(currentLevel) {
        val set = mutableSetOf<Pair<Int, Int>>()
        when (currentLevel) {
            1 -> {
                set.add(Pair(3, 1))
                set.add(Pair(3, 3))
            }
            2 -> {
                set.add(Pair(2, 1))
                set.add(Pair(2, 2))
                set.add(Pair(4, 2))
                set.add(Pair(4, 3))
            }
            else -> {
                set.add(Pair(2, 0))
                set.add(Pair(2, 1))
                set.add(Pair(4, 3))
                set.add(Pair(4, 4))
                set.add(Pair(3, 2))
            }
        }
        set
    }

    fun resetGame() {
        playerPos = Pair(6, 2)
        leeches = when (currentLevel) {
            1 -> listOf(Pair(0, 0))
            2 -> listOf(Pair(0, 0), Pair(0, 4))
            else -> listOf(Pair(0, 0), Pair(0, 2), Pair(0, 4))
        }
        isVictory = false
        isGameOver = false
        stepsCount = 0
    }

    LaunchedEffect(currentLevel) {
        resetGame()
    }

    // Tích lũy điểm khi thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "thasua_win_credited_level_$currentLevel"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(120, 25) // +120 XP, +25 Coins
                prefs.saveScore(winKey, 1)
            }
        }
    }

    // Logic di chuyển của đỉa đuổi theo người chơi (Manhattan distance)
    fun moveLeeches() {
        val nextLeeches = leeches.map { leech ->
            val r = leech.first
            val c = leech.second

            // Các hướng đi có thể của đỉa
            val candidates = listOf(
                Pair(r - 1, c),
                Pair(r + 1, c),
                Pair(r, c - 1),
                Pair(r, c + 1)
            )

            // Chọn ô di chuyển hợp lệ tối ưu gần người chơi nhất
            var bestCandidate = leech
            var minDist = 9999

            for (cand in candidates) {
                val cr = cand.first
                val cc = cand.second

                // Ranh giới bàn chơi 7 hàng x 5 cột
                if (cr in 0..6 && cc in 0..4 && !obstacles.contains(cand)) {
                    // Khoảng cách Manhattan tới người chơi
                    val dist = kotlin.math.abs(cr - playerPos.first) + kotlin.math.abs(cc - playerPos.second)
                    if (dist < minDist) {
                        minDist = dist
                        bestCandidate = cand
                    }
                }
            }
            bestCandidate
        }

        leeches = nextLeeches

        // Kiểm tra xem đỉa có cắn trúng người chơi không
        if (leeches.contains(playerPos)) {
            audioManager.playError()
            isGameOver = true
        }
    }

    fun makeMove(dr: Int, dc: Int) {
        if (isGameOver || isVictory) return

        val nr = playerPos.first + dr
        val nc = playerPos.second + dc

        // Kiểm tra ranh giới sông 7x5
        if (nr in 0..6 && nc in 0..4) {
            val target = Pair(nr, nc)
            if (!obstacles.contains(target)) {
                audioManager.playClick()
                playerPos = target
                stepsCount++

                // Sau bước đi của người chơi, đỉa di chuyển đuổi theo
                moveLeeches()

                // Kiểm tra xem người chơi đã qua sông thành công chưa (lên hàng 0 bờ bên kia)
                if (nr == 0 && !isGameOver) {
                    isVictory = true
                }
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("THẢ ĐỈA BA BA", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
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
                // Chỉ số trạng thái sông nước
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số bước di chuyển: $stepsCount 👣",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Số con đỉa: ${leeches.size} 🦟",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagRed
                    )
                }

                // --- DÒNG SÔNG 7x5 GRID BOARD ---
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0F2F1)) // Nền xanh nước sông mát dịu
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    val cellW = w / 5
                    val cellH = h / 7

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Kẻ gợn sóng nước sông nhẹ nhàng
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Vẽ gợn sóng nghệ thuật hoặc sọc ngang
                        }

                        // Vẽ các ô sông
                        for (r in 0 until 7) {
                            for (c in 0 until 5) {
                                val x = cellW * c
                                val y = cellH * r
                                val isPlayer = playerPos.first == r && playerPos.second == c
                                val isObstacle = obstacles.contains(Pair(r, c))
                                val isLeech = leeches.contains(Pair(r, c))

                                // Bờ sông: Hàng 0 và Hàng 6
                                val isBank = r == 0 || r == 6

                                Box(
                                    modifier = Modifier
                                        .offset(x = x, y = y)
                                        .size(cellW, cellH)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isBank -> Color(0xFFC8E6C9) // Bờ cỏ xanh lá mượt
                                                isObstacle -> Color(0xFF8D6E63).copy(alpha = 0.3f) // Bụi sậy nâu đất
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = if (isBank) Color(0x33000000) else Color(0x1A000000),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            // Cho phép di chuyển bằng cách nhấn thẳng vào ô cạnh bên
                                            val dr = r - playerPos.first
                                            val dc = c - playerPos.second
                                            if (kotlin.math.abs(dr) + kotlin.math.abs(dc) == 1) {
                                                makeMove(dr, dc)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isObstacle) {
                                        Text("🌾", fontSize = 18.sp) // Bụi lau sậy cản đường
                                    }
                                    if (isPlayer) {
                                        Text("👦", fontSize = 20.sp) // Cậu bé lội sông
                                    }
                                    if (isLeech) {
                                        Text("🦟", fontSize = 18.sp) // Con đỉa
                                    }
                                    if (r == 0 && !isObstacle && !isPlayer && !isLeech) {
                                        Text("🏁 Đích", fontSize = 10.sp, color = BambooGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // D-pad di chuyển ảo dưới màn hình
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = { makeMove(-1, 0) }, // Lên
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Text("▲", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { makeMove(0, -1) }, // Trái
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Text("◀", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                        IconButton(
                            onClick = { makeMove(0, 1) }, // Phải
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Text("▶", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(
                        onClick = { makeMove(1, 0) }, // Xuống
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Text("▼", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
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
                            resetGame()
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
                        "💥 BỊ ĐỈA CẮN TRÚNG!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Text(
                        "Con đỉa đã rình rập và bơi cắn trúng chân của bạn. Hãy đi lại lắt léo dụ đỉa kẹt vào các bụi sậy nhé!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại thắng màn (Victory)
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
                            resetGame()
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
                        "🎉 QUA SÔNG THÀNH CÔNG!",
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
                        Text("Bạn đã lội qua dòng sông rạch sậy tránh đỉa cắn về bờ an toàn tuyệt đối!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+120 XP và +25 Tiền vàng thưởng di sản Việt đã được tích lũy thành công!",
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
