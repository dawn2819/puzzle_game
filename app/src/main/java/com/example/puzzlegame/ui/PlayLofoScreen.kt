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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun PlayLofoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(1) }
    var movesLeft by remember { mutableStateOf(15) }
    var playerPos by remember { mutableStateOf(Pair(6, 1)) } // Hàng 6, cột 1 (Vị trí ô số 1)
    var nextTargetNumber by remember { mutableStateOf(2) } // Mục tiêu nhảy tiếp theo là 2
    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Ma trận lò cò mặc định (7 hàng x 3 cột)
    // -1: Ô trống nền đất
    // 1..9: Ô phấn số lò cò
    // -10: Ô bẫy bùn sụt lún
    // -20, -21: Cặp cổng dịch chuyển
    val boardLayout = remember(currentLevel) {
        val base = Array(7) { IntArray(3) { -1 } }
        base[6][1] = 1 // Ô 1
        base[5][1] = 2 // Ô 2
        base[4][1] = 3 // Ô 3
        base[3][0] = 5 // Ô 5
        base[3][2] = 4 // Ô 4
        base[2][1] = 6 // Ô 6
        base[1][0] = 8 // Ô 8
        base[1][2] = 7 // Ô 7
        base[0][1] = 9 // Ô 9

        // Bổ sung bẫy hoặc cổng dịch chuyển tùy level
        if (currentLevel >= 2) {
            base[3][1] = -10 // Bẫy bùn ở giữa ô 4 và 5
        }
        if (currentLevel >= 3) {
            base[5][0] = -20 // Cổng dịch chuyển A
            base[2][2] = -21 // Cổng dịch chuyển B
        }
        if (currentLevel >= 4) {
            base[5][2] = -10 // Thêm bẫy bùn thứ 2
        }
        if (currentLevel == 5) {
            base[2][0] = -10 // Thêm bẫy bùn thứ 3
        }
        base
    }

    fun resetGame() {
        movesLeft = when (currentLevel) {
            1 -> 15
            2 -> 12
            3 -> 10
            4 -> 9
            else -> 8
        }
        playerPos = Pair(6, 1)
        nextTargetNumber = 2
        isVictory = false
        isGameOver = false
    }

    LaunchedEffect(currentLevel) {
        resetGame()
    }

    // Tích lũy điểm khi thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "lofo_win_credited_level_$currentLevel"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                prefs.saveScore(winKey, 1)
            }
        }
    }

    fun makeHop(targetRow: Int, targetCol: Int) {
        if (isGameOver || isVictory) return

        // 1. Chỉ cho phép nhảy sang ô liền kề (vua chéo dọc ngang khoáng cách tối đa 1 ô)
        val dr = kotlin.math.abs(playerPos.first - targetRow)
        val dc = kotlin.math.abs(playerPos.second - targetCol)
        if (dr > 1 || dc > 1 || (dr == 0 && dc == 0)) {
            return // Nhảy quá xa hoặc nhảy tại chỗ -> không hợp lệ
        }

        val cellVal = boardLayout[targetRow][targetCol]

        // Không được nhảy vào ô đất trống (-1)
        if (cellVal == -1) {
            audioManager.playError()
            return
        }

        movesLeft--

        // 2. Nhảy trúng ô số lò cò tiếp theo
        if (cellVal == nextTargetNumber) {
            audioManager.playClick()
            playerPos = Pair(targetRow, targetCol)
            nextTargetNumber++
            
            if (cellVal == 9) {
                isVictory = true
            }
        } 
        // 3. Dẫm phải bẫy bùn sụt lún
        else if (cellVal == -10) {
            audioManager.playError()
            playerPos = Pair(targetRow, targetCol)
            movesLeft -= 2 // Bị trừ thêm 2 bước đi
        } 
        // 4. Bước vào cổng dịch chuyển
        else if (cellVal == -20 || cellVal == -21) {
            audioManager.playClick()
            // Tìm cổng còn lại để dịch chuyển tức thời tới đó
            val destPos = if (cellVal == -20) {
                findCellCoordinates(boardLayout, -21)
            } else {
                findCellCoordinates(boardLayout, -20)
            }
            if (destPos != null) {
                playerPos = destPos
            }
        }
        // 5. Nhảy sai số (nhảy linh tinh hoặc nhảy lùi)
        else {
            audioManager.playError()
            playerPos = Pair(targetRow, targetCol)
        }

        // Kiểm tra hết lượt
        if (movesLeft <= 0 && !isVictory) {
            isGameOver = true
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("NHẢY LÒ CÒ", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
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
                            text = "Màn $currentLevel/5 🏆",
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
                // Header chỉ số
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số bước còn: $movesLeft 👣",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (movesLeft < 4) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Mục tiêu: Nhảy vào ô [$nextTargetNumber] 🎯",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BambooGreen
                    )
                }

                // --- BẢN ĐỒ LÒ CÒ CANVAS / GRID ---
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boardWidth = maxWidth
                    val boardHeight = maxHeight

                    val cellW = boardWidth / 3
                    val cellH = boardHeight / 7

                    // Vẽ nét vẽ phấn lò cò
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        // Kẻ viền phấn xung quanh các ô có tồn tại lò cò để mô tả vẽ phấn sân trường
                        // Để đơn giản, chỉ vẽ nét phấn lò cò bao quanh
                    }

                    // Đặt các ô lên
                    Box(modifier = Modifier.fillMaxSize()) {
                        for (r in 0 until 7) {
                            for (c in 0 until 3) {
                                val cellVal = boardLayout[r][c]
                                if (cellVal == -1) continue // Đất trống không vẽ

                                val x = cellW * c
                                val y = cellH * r
                                val isPlayerHere = playerPos.first == r && playerPos.second == c

                                Box(
                                    modifier = Modifier
                                        .offset(x = x, y = y)
                                        .size(cellW, cellH)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isPlayerHere -> BambooGreen.copy(alpha = 0.3f)
                                                cellVal == -10 -> Color(0xFF5D403B).copy(alpha = 0.2f) // Màu bùn
                                                cellVal == -20 || cellVal == -21 -> Color.Blue.copy(alpha = 0.15f) // Cổng dịch chuyển
                                                else -> Color.White.copy(alpha = 0.5f) // Ô lò cò mặc định
                                            }
                                        )
                                        .border(
                                            width = if (isPlayerHere) 2.5.dp else 1.5.dp,
                                            color = when {
                                                isPlayerHere -> BambooGreen
                                                cellVal == -10 -> Color(0xFFBA1A1A)
                                                cellVal == -20 || cellVal == -21 -> Color.Blue
                                                else -> OutlineBrown
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            makeHop(r, c)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        when {
                                            cellVal in 1..9 -> {
                                                Text(
                                                    text = "$cellVal",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EarthyBrown
                                                )
                                            }
                                            cellVal == -10 -> {
                                                Text("💩 Bùn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D403B))
                                            }
                                            cellVal == -20 || cellVal == -21 -> {
                                                Text("🌀 Cổng", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                                            }
                                        }
                                        
                                        if (isPlayerHere) {
                                            Text("👣 Bạn ở đây", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BambooGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Hướng dẫn nhỏ dưới đáy
                Text(
                    text = "Hãy nhảy lần lượt qua các ô từ 1 tới 9. Tránh bẫy bùn sụt lún trừ 2 bước chân!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
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
                        "👣 BẠN ĐÃ MỆT LẢ!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Text(
                        "Bạn đã hết sạch số bước chân nhảy lò cò cho phép. Hãy lên kế hoạch nhịp nhảy tối ưu hơn nhé!",
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
                            if (currentLevel < 5) {
                                currentLevel++
                            } else {
                                currentLevel = 1
                            }
                            resetGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (currentLevel < 5) "Màn Tiếp Theo" else "Chơi Lại Màn 1", fontWeight = FontWeight.Bold)
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
                        "🎉 HOÀN THÀNH CHẶNG LÒ CÒ!",
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
                        Text("Bạn đã khéo léo hoàn thành chuỗi lò cò 1-9 nhảy chuẩn xác!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+100 XP và +20 Tiền vàng thưởng di sản Hồn Việt đã được cộng dồn!",
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

// Hàm hỗ trợ tìm vị trí dòng, cột của một giá trị trong ma trận lò cò
private fun findCellCoordinates(board: Array<IntArray>, value: Int): Pair<Int, Int>? {
    for (r in 0 until 7) {
        for (c in 0 until 3) {
            if (board[r][c] == value) {
                return Pair(r, c)
            }
        }
    }
    return null
}
