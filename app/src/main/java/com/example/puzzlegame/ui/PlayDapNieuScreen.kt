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
import com.example.puzzlegame.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDapNieuScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(1) }
    var winningPotIndex by remember { mutableStateOf(3) }
    var brokenPotIndex by remember { mutableStateOf(-1) }
    
    val clues = remember { mutableStateListOf<String>() }

    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // 5 màu niêu đất và tên màu sắc tương ứng
    val potColors = remember { listOf(Color(0xFFB40006), Color(0xFF3E6137), Color(0xFFF57C00), Color(0xFF1976D2), Color(0xFF7B1FA2)) }
    val potColorNames = remember { listOf("Đỏ", "Xanh lá", "Cam", "Xanh dương", "Tím") }

    // State lưu chỉ số màu thực tế tại vị trí i (0..4) để tránh bug logic
    val potColorIndices = remember { mutableStateListOf(0, 1, 2, 3, 4) }

    fun generateClues() {
        clues.clear()
        brokenPotIndex = -1
        isVictory = false
        isGameOver = false

        // 1. Chọn ngẫu nhiên vị trí niêu chứa vàng (1..4)
        winningPotIndex = Random.nextInt(1, 5)

        // 2. Phân bổ các màu sắc vào các vị trí
        val positions = mutableMapOf<Int, Int>() // colorIndex -> position (0..4)
        
        // Đặt màu Đỏ (index 0) ở phía bên trái niêu thắng
        val redPos = Random.nextInt(0, winningPotIndex)
        positions[0] = redPos
        positions[winningPotIndex] = -99 // Đánh dấu ô thắng tạm thời để tránh trùng

        // Đặt màu Xanh lá (index 1) không nằm sát niêu thắng
        val availableForGreen = (0..4).filter { 
            it != winningPotIndex && 
            it != winningPotIndex - 1 && 
            it != winningPotIndex + 1 && 
            it != redPos 
        }
        val greenPos = if (availableForGreen.isNotEmpty()) availableForGreen.random() else {
            (0..4).filter { it != winningPotIndex && it != redPos }.random()
        }
        positions[1] = greenPos

        // Phân bổ các màu còn lại (2: Cam, 3: Xanh dương, 4: Tím) vào các ô trống
        val remainingColors = mutableListOf(2, 3, 4)
        val filledPositions = setOf(redPos, greenPos, winningPotIndex)
        val emptyPositions = (0..4).filter { !filledPositions.contains(it) }

        for (pos in emptyPositions) {
            if (remainingColors.isNotEmpty()) {
                val color = remainingColors.removeAt(0)
                positions[color] = pos
            }
        }

        // Cập nhật lại danh sách chỉ số màu hiển thị
        val tempIndices = IntArray(5)
        positions.forEach { (colorIdx, pos) ->
            if (pos >= 0) {
                tempIndices[pos] = colorIdx
            }
        }
        val winColor = remainingColors.firstOrNull() ?: 2
        tempIndices[winningPotIndex] = winColor
        
        potColorIndices.clear()
        potColorIndices.addAll(tempIndices.toList())

        // Tạo câu gợi ý logic loại trừ dựa trên vị trí thực tế
        clues.add("🔔 Niêu chứa vàng không nằm ở niêu số 1 (ô đầu tiên).")
        clues.add("🔔 Niêu chứa vàng nằm bên phải niêu màu Đỏ.")
        clues.add("🔔 Niêu chứa vàng không nằm sát cạnh niêu màu Xanh lá.")

        if (currentLevel >= 2) {
            val parityText = if (winningPotIndex % 2 == 0) "số lẻ (1, 3 hoặc 5)" else "số chẵn (2 hoặc 4)"
            clues.add("🔔 Niêu chứa vàng nằm ở vị trí thứ $parityText trên dây treo.")
        }
        if (currentLevel >= 3) {
            val adjacentPos = if (winningPotIndex > 0) winningPotIndex - 1 else winningPotIndex + 1
            val adjColorIdx = potColorIndices[adjacentPos]
            clues.add("🔔 Niêu sát cạnh niêu chứa vàng có màu ${potColorNames[adjColorIdx]}.")
        }
        if (currentLevel >= 4) {
            val bluePos = potColorIndices.indexOf(3)
            if (bluePos != -1) {
                val dist = kotlin.math.abs(winningPotIndex - bluePos)
                clues.add("🔔 Niêu chứa vàng cách niêu màu Xanh dương $dist ô.")
            }
        }
        if (currentLevel >= 5) {
            val purplePos = potColorIndices.indexOf(4)
            if (purplePos != -1) {
                val purpleParity = purplePos % 2 == winningPotIndex % 2
                val relationText = if (purpleParity) "cùng" else "khác"
                clues.add("🔔 Niêu chứa vàng và niêu màu Tím có vị trí $relationText tính chất chẵn/lẻ.")
            }
        }
    }

    LaunchedEffect(currentLevel) {
        generateClues()
    }

    // Thưởng thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "dapnieu_win_credited_level_$currentLevel"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                prefs.saveScore(winKey, 1)
            }
        }
    }

    fun breakPot(index: Int) {
        if (brokenPotIndex != -1 || isGameOver || isVictory) return

        brokenPotIndex = index
        audioManager.playClick()

        if (index == winningPotIndex) {
            isVictory = true
        } else {
            audioManager.playError()
            isGameOver = true
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ĐẬP NIÊU ĐẤT (LOGIC)", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
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
                // Hướng dẫn & Manh mối logic bento card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal.copy(alpha = 0.9f))
                        .border(1.5.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📜 GỢI Ý CỦA TRƯỞNG LÀNG:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagRed
                    )
                    clues.forEach { clue ->
                        Text(
                            text = clue,
                            fontSize = 14.sp,
                            color = EarthyBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dây treo niêu vẽ mộc mạc
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(OutlineBrown)
                    )

                    // 5 chiếc niêu đất treo lửng lơ
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        for (i in 0 until 5) {
                            val isBroken = brokenPotIndex == i
                            val isWinning = winningPotIndex == i
                            val colorIdx = potColorIndices[i]
                            val color = potColors[colorIdx]
                            val colorName = potColorNames[colorIdx]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable(!isGameOver && !isVictory) {
                                        breakPot(i)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(30.dp)
                                        .background(OutlineBrown)
                                )

                                val potDrawable = when (colorIdx) {
                                    0 -> R.drawable.nieu_pot_orange
                                    1 -> R.drawable.nieu_pot_blue
                                    2 -> R.drawable.nieu_pot_purple
                                    else -> R.drawable.nieu_pot_red
                                }

                                Box(
                                    modifier = Modifier
                                        .size(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBroken) {
                                        val brokenRes = if (isWinning) R.drawable.nieu_gold else R.drawable.nieu_broken
                                        Image(
                                            painter = painterResource(id = brokenRes),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = potDrawable),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Số ${i + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EarthyBrown
                                )
                                Text(
                                    text = "($colorName)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // Dẫn nhập
                Text(
                    text = "Hãy suy luận chiếc niêu đất nào chứa vàng thỏi dựa trên các manh mối loại trừ.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Hộp thoại đập sai (Game Over)
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            generateClues()
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
                        "💥 NIÊU RỖNG BỊ ĐẬP VỠ!",
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
                        val winningColorName = potColorNames[potColorIndices[winningPotIndex]]
                        Text("Rất tiếc! Chiếc niêu bạn vừa đập hoàn toàn rỗng không. Niêu chứa vàng thỏi thực sự là:", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Niêu số ${winningPotIndex + 1} ($winningColorName) 🏺", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại đập trúng (Victory)
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
                            generateClues()
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
                        "🎉 VÀNG RƠI ĐẦY NIÊU!",
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
                        Text("Lập luận tuyệt vời! Bạn đã đập vỡ niêu đất số ${winningPotIndex + 1} và nhận đầy ắp vàng thỏi 🪙!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+100 XP và +20 Tiền vàng thưởng đã được ghi nhận vào di sản Hồn Việt!",
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
