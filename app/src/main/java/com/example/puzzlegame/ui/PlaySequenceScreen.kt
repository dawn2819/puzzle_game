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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaySequenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var selectedTab by remember { mutableStateOf(0) } // 0: Chơi chuyền, 1: Tập tầm vông
    var isVictory by remember { mutableStateOf(false) } // Sử dụng cho Tập Tầm Vông

    fun playWin() {
        audioManager.playClick()
        isVictory = true
    }

    // Thưởng XP/Coins của Tập Tầm Vông khi thắng
    LaunchedEffect(isVictory, selectedTab) {
        if (isVictory && selectedTab == 1) {
            val winKey = "sequence_win_credited_tab_1"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                prefs.saveScore(winKey, 1)
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
                    title = { Text("GAME TRÍ NHỚ & QUAN SÁT", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = FlagRed
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 0
                            isVictory = false
                        },
                        text = { Text("Chơi Chuyền", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 1
                            isVictory = false
                        },
                        text = { Text("Tập Tầm Vông", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (selectedTab == 0) {
                        ChoiChuyenGame(onBack = onBack, audioManager = audioManager)
                    } else {
                        TapTamVongGame(onWin = { playWin() }, audioManager = audioManager)
                    }
                }
            }
        }

        // Hộp thoại thắng (Chỉ dành cho Tập Tầm Vông)
        if (isVictory && selectedTab == 1) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            isVictory = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tiếp Tục", fontWeight = FontWeight.Bold)
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
                        "🎉 ĐÔI MẮT TINH TƯỜNG!",
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
                        Text(
                            text = "Bạn đoán đúng chính xác tay chứa sỏi vàng!",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+100 XP và +20 Tiền vàng thưởng đã được ghi nhận vào hồ sơ Hồn Việt!",
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

// ----------------------------------------------------
// 1. GAME CHƠI CHUYỀN (LOOP MODE UNTIL DEFEAT)
// ----------------------------------------------------
@Composable
fun ChoiChuyenGame(onBack: () -> Unit, audioManager: AudioManager) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var sequenceLength by remember { mutableStateOf(3) }
    val fullSequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }

    var flashingNodeIndex by remember { mutableStateOf(-1) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var livesLeft by remember { mutableStateOf(3) }

    fun generateSequence() {
        fullSequence.clear()
        userSequence.clear()
        repeat(sequenceLength) {
            fullSequence.add(Random.nextInt(5)) // Có 5 hạt chuyền tròn
        }
    }

    fun playSequence() {
        coroutineScope.launch {
            isShowingSequence = true
            userSequence.clear()
            delay(500)
            for (idx in fullSequence) {
                flashingNodeIndex = idx
                audioManager.playClick()
                delay(400)
                flashingNodeIndex = -1
                delay(200)
            }
            isShowingSequence = false
        }
    }

    fun initNewGame() {
        sequenceLength = 3
        livesLeft = 3
        isGameOver = false
        generateSequence()
        playSequence()
    }

    LaunchedEffect(Unit) {
        initNewGame()
    }

    fun onNodeClick(nodeIndex: Int) {
        if (isShowingSequence || isGameOver) return

        audioManager.playClick()
        userSequence.add(nodeIndex)

        // Kiểm tra xem khớp với sequence không
        val currentCheckIdx = userSequence.size - 1
        if (userSequence[currentCheckIdx] != fullSequence[currentCheckIdx]) {
            // Nhập sai!
            audioManager.playError()
            livesLeft--
            if (livesLeft <= 0) {
                isGameOver = true
            } else {
                // Cho hiển thị lại sequence để đoán tiếp
                playSequence()
            }
            return
        }

        // Kiểm tra hoàn thành chuỗi -> Tự động tăng độ dài không giới hạn (Loop Mode)
        if (userSequence.size == fullSequence.size) {
            coroutineScope.launch {
                delay(300)
                sequenceLength++
                generateSequence()
                playSequence()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Số mạng còn: " + "❤️ ".repeat(livesLeft),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Độ dài hiện tại: $sequenceLength Hạt 🪵",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Banner trạng thái
        Box(
            modifier = Modifier.height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isShowingSequence) "👀 Hãy nhớ thứ tự nhấp nháy!" else "👉 Gõ lại thứ tự chuỗi chuyền!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isShowingSequence) FlagRed else BambooGreen
            )
        }

        // --- VẼ HẠT QUẢ CHUYỀN VÒNG TRÒN (Tactile Layout) ---
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(SurfaceNormal.copy(alpha = 0.5f))
                .border(2.dp, OutlineBrown, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val radius = 80.dp
            val angles = listOf(90, 162, 234, 306, 18)

            for (i in 0 until 5) {
                val angleRad = Math.toRadians(angles[i].toDouble())
                val offsetX = (Math.cos(angleRad) * 65).dp
                val offsetY = (Math.sin(angleRad) * 65).dp
                val isFlashing = flashingNodeIndex == i

                val animatedScale by animateFloatAsState(
                    targetValue = if (isFlashing) 1.25f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "node_scale_$i"
                )

                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .size(46.dp)
                        .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
                        .shadow(if (isFlashing) 10.dp else 2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isFlashing) StarGold else Color.White)
                        .border(
                            width = if (isFlashing) 3.dp else 1.5.dp,
                            color = if (isFlashing) FlagRed else OutlineBrown,
                            shape = CircleShape
                        )
                        .clickable(enabled = !isShowingSequence && !isGameOver) {
                            onNodeClick(i)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🥥",
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Hộp thoại khi kết thúc game (Game Over) ở chế độ loop vô tận
    if (isGameOver) {
        val currentScore = sequenceLength - 1 // Đạt chuỗi thành công trước khi bị lỗi
        val highScore = prefs.getHighScore("choichuyen_highscore")
        val isNewRecord = currentScore > highScore

        if (isNewRecord) {
            prefs.saveScore("choichuyen_highscore", currentScore)
        }

        // Tự động cộng thưởng XP & Coins
        val xpGained = currentScore * 15
        val coinsGained = currentScore * 3
        LaunchedEffect(Unit) {
            if (xpGained > 0 || coinsGained > 0) {
                prefs.addXpAndCoins(xpGained, coinsGained)
            }
        }

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
                    text = if (isNewRecord && highScore > 0) "🎉 KỶ LỤC MỚI XUẤT SẮC!" else "💥 KẾT THÚC CHUỖI CHUYỀN!",
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isNewRecord) BambooGreen else FlagRed
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bạn đã vượt qua chuỗi dài: $currentScore hạt 🥥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kỷ lục cũ: $highScore hạt",
                        fontSize = 14.sp,
                        color = EarthyBrown.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "+$xpGained XP & +$coinsGained Tiền vàng đã được tích lũy thưởng!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BambooGreen,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = SurfaceNormal,
            modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
        )
    }
}

// ----------------------------------------------------
// 2. GAME TẬP TẦM VÔNG
// ----------------------------------------------------
@Composable
fun TapTamVongGame(onWin: () -> Unit, audioManager: AudioManager) {
    val coroutineScope = rememberCoroutineScope()
    var isShuffling by remember { mutableStateOf(false) }
    var correctHandIsLeft by remember { mutableStateOf(true) } // Sỏi ở tay trái hay tay phải
    
    var showPebbleInHand by remember { mutableStateOf<Boolean?>(null) } // null: chưa đoán, true: trúng, false: sai
    var chosenHandIsLeft by remember { mutableStateOf<Boolean?>(null) }

    val scaleLeft by animateFloatAsState(
        targetValue = if (isShuffling && correctHandIsLeft) 1.2f else if (chosenHandIsLeft == true) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "hand_left_scale"
    )
    val scaleRight by animateFloatAsState(
        targetValue = if (isShuffling && !correctHandIsLeft) 1.2f else if (chosenHandIsLeft == false) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "hand_right_scale"
    )

    fun startShuffle() {
        coroutineScope.launch {
            isShuffling = true
            showPebbleInHand = null
            chosenHandIsLeft = null
            
            // Hiện sỏi ở tay đúng ban đầu để người chơi quan sát
            correctHandIsLeft = Random.nextBoolean()
            delay(800)
            
            // Giả lập tráo đổi tay bằng vòng lặp nhấp nháy/vị trí tráo đổi nhanh
            repeat(6) {
                correctHandIsLeft = !correctHandIsLeft
                audioManager.playClick()
                delay(220)
            }
            
            correctHandIsLeft = Random.nextBoolean()
            isShuffling = false
        }
    }

    LaunchedEffect(Unit) {
        startShuffle()
    }

    fun chooseHand(isLeft: Boolean) {
        if (isShuffling || showPebbleInHand != null) return

        chosenHandIsLeft = isLeft
        val isCorrect = isLeft == correctHandIsLeft
        showPebbleInHand = isCorrect

        if (isCorrect) {
            onWin()
        } else {
            audioManager.playError()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Tập tầm vông tay không tay có...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Hãy căng mắt quan sát tay chứa sỏi vàng!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        
        Spacer(modifier = Modifier.height(48.dp))

        // Hiển thị 2 bàn tay trái và phải
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tay trái
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = scaleLeft, scaleY = scaleLeft)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (chosenHandIsLeft == true) {
                                if (showPebbleInHand == true) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                            } else Color.White
                        )
                        .border(2.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isShuffling && showPebbleInHand == null) {
                            chooseHand(isLeft = true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val handSymbol = when {
                        isShuffling -> "👊" // Đang nắm chặt tráo đổi
                        showPebbleInHand != null && correctHandIsLeft -> "🤚" // Mở ra có sỏi
                        showPebbleInHand != null && !correctHandIsLeft -> "🤚" // Mở ra rỗng
                        else -> "👊"
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(handSymbol, fontSize = 32.sp)
                        if (showPebbleInHand != null && correctHandIsLeft) {
                            Text("🟡 Sỏi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StarGold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tay Trái", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EarthyBrown)
            }

            // Tay phải
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = scaleRight, scaleY = scaleRight)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (chosenHandIsLeft == false) {
                                if (showPebbleInHand == true) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                            } else Color.White
                        )
                        .border(2.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isShuffling && showPebbleInHand == null) {
                            chooseHand(isLeft = false)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val handSymbol = when {
                        isShuffling -> "👊"
                        showPebbleInHand != null && !correctHandIsLeft -> "🤚"
                        showPebbleInHand != null && correctHandIsLeft -> "🤚"
                        else -> "👊"
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(handSymbol, fontSize = 32.sp)
                        if (showPebbleInHand != null && !correctHandIsLeft) {
                            Text("🟡 Sỏi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StarGold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tay Phải", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EarthyBrown)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (showPebbleInHand == false) {
            Button(
                onClick = { startShuffle() },
                colors = ButtonDefaults.buttonColors(containerColor = FlagRed)
            ) {
                Text("Tráo Lại", fontWeight = FontWeight.Bold)
            }
        } else if (showPebbleInHand == null && !isShuffling) {
            Text("Hãy chọn tay chứa sỏi!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BambooGreen)
        } else if (isShuffling) {
            Text("🌀 Tay đang tráo đổi nhanh...", fontSize = 14.sp, color = FlagRed, fontWeight = FontWeight.Bold)
        }
    }
}
