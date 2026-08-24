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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineSokoban
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.ui.components.ConfettiEffect
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.audio.AudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaySokobanScreen(
    levelIndex: Int,
    isRestore: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var currentLevel by remember { mutableStateOf(levelIndex) }
    var unlockedLevel by remember { mutableStateOf(prefs.sokobanUnlockedLevel) }
    var allUnlocked by remember { mutableStateOf(prefs.sokobanAllUnlocked) }

    // Engine khởi tạo dựa trên currentLevel (reactive)
    val engine = remember(currentLevel) {
        if (currentLevel != -1) EngineSokoban(currentLevel) else null
    }

    val boardState = remember { mutableStateListOf<IntArray>() }
    var timeRemaining by remember { mutableStateOf(0) }
    var moves by remember { mutableStateOf(0) }
    var pushes by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var nextBoxId by remember { mutableStateOf(1) }
    val visualBoxes = remember { mutableStateListOf<VisualBox>() }
    var playerVisualRow by remember { mutableStateOf(0) }
    var playerVisualCol by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    var playerOffsetX by remember { mutableStateOf(0.dp) }
    var playerOffsetY by remember { mutableStateOf(0.dp) }

    // Đồng bộ state
    fun syncState() {
        engine?.let { eng ->
            boardState.clear()
            for (r in 0 until eng.rows) {
                boardState.add(eng.board[r].copyOf())
            }
            timeRemaining = eng.timeRemaining
            moves = eng.moves
            pushes = eng.pushes
            score = eng.score
            isGameOver = eng.isGameOver
            isVictory = eng.isVictory

            playerVisualRow = eng.playerRow
            playerVisualCol = eng.playerCol

            val oldBoxes = visualBoxes.toList()
            visualBoxes.clear()
            val unmatchedOldBoxes = oldBoxes.toMutableList()

            for (r in 0 until eng.rows) {
                for (c in 0 until eng.cols) {
                    val cell = eng.board[r][c]
                    if (cell == EngineSokoban.BOX || cell == EngineSokoban.BOX_ON_GOAL) {
                        var bestMatchIdx = -1
                        var minDistance = 999
                        for (i in unmatchedOldBoxes.indices) {
                            val old = unmatchedOldBoxes[i]
                            val dist = kotlin.math.abs(old.r - r) + kotlin.math.abs(old.c - c)
                            if (dist < minDistance) {
                                minDistance = dist
                                bestMatchIdx = i
                            }
                        }
                        if (bestMatchIdx != -1) {
                            val matched = unmatchedOldBoxes.removeAt(bestMatchIdx)
                            matched.r = r
                            matched.c = c
                            matched.value = cell
                            visualBoxes.add(matched)
                        } else {
                            visualBoxes.add(VisualBox(id = nextBoxId++, r = r, c = c, value = cell))
                        }
                    }
                }
            }

            if (!isGameOver && !isVictory && !isPaused) {
                prefs.saveContinueGame("SOKOBAN", eng.serializeState())
            } else if (isVictory) {
                prefs.clearContinueGame()
                prefs.saveScore("sokoban_level_$currentLevel", score)

                // Cập nhật tiến trình mở khóa
                if (currentLevel == unlockedLevel) {
                    if (currentLevel < 18) {
                        unlockedLevel = currentLevel + 1
                        prefs.sokobanUnlockedLevel = unlockedLevel
                    } else if (currentLevel == 18) {
                        allUnlocked = true
                        prefs.sokobanAllUnlocked = true
                    }
                }
            }
        }
    }

    // Khôi phục game nếu có yêu cầu
    LaunchedEffect(currentLevel) {
        if (currentLevel == -1) {
            prefs.clearContinueGame()
            return@LaunchedEffect
        }

        if (isRestore && prefs.hasContinueGame() && prefs.getContinueGameType() == "SOKOBAN") {
            try {
                val data = prefs.getContinueGameData()!!
                val parts = data.split("|")
                val savedLevel = parts[0].toInt()
                if (savedLevel == currentLevel) {
                    val savedMoves = parts[1].toInt()
                    val savedPushes = parts[2].toInt()
                    val savedTime = parts[3].toInt()
                    val savedScore = parts[4].toInt()
                    val savedBoard = prefs.deserializeGrid(parts[5])
                    val savedInitial = prefs.deserializeGrid(parts[6])
                    engine?.restoreState(savedBoard, savedInitial, savedTime, savedScore, savedMoves, savedPushes)
                } else {
                    engine?.loadLevel()
                }
            } catch (e: Exception) {
                engine?.loadLevel()
            }
        } else {
            engine?.loadLevel()
        }
        nextBoxId = 1
        visualBoxes.clear()
        syncState()
    }

    // Đếm ngược thời gian
    LaunchedEffect(currentLevel, isGameOver, isVictory, isPaused) {
        if (currentLevel != -1 && currentLevel > 5 && !isGameOver && !isVictory && !isPaused) {
            while (true) {
                delay(1000)
                engine?.tickSecond()
                syncState()
                if (engine?.isGameOver == true || engine?.isVictory == true) break
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    fun makeMove(dRow: Int, dCol: Int) {
        if (isPaused) return
        val moved = engine?.move(dRow, dCol) ?: false
        if (moved) {
            audioManager.playClick()
            syncState()
        } else {
            // Hiệu ứng phản lực/rung lắc vật lý khi va chạm tường/vật cản
            coroutineScope.launch {
                playerOffsetX = (dCol * 12).dp
                playerOffsetY = (dRow * 12).dp
                delay(80)
                playerOffsetX = 0.dp
                playerOffsetY = 0.dp
            }
        }
    }

    PremiumBackground {
        if (currentLevel == -1) {
            // --- LEVEL SELECTION SCREEN ---
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SOKOBAN - CHỌN MÀN CHƠI", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
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
                        text = "Vượt màn để mở khóa các cấp độ tiếp theo. Độ khó và thời gian sẽ tăng dần!",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(18) { index ->
                            val lvl = index + 1
                            val isLocked = lvl > unlockedLevel && !allUnlocked
                            val isCleared = lvl < unlockedLevel

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isLocked -> Color.Gray.copy(alpha = 0.2f)
                                            isCleared -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        BorderStroke(
                                            if (lvl == unlockedLevel) 2.dp else 1.dp,
                                            if (lvl == unlockedLevel) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        RoundedCornerShape(12.dp)
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
                                        Text("$lvl", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                                        if (isCleared) {
                                            Text("✔️", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Nút Reset All nếu đã vượt màn 18
                    if (unlockedLevel > 1 || allUnlocked) {
                        OutlinedButton(
                            onClick = {
                                audioManager.playClick()
                                prefs.sokobanUnlockedLevel = 1
                                prefs.sokobanAllUnlocked = false
                                unlockedLevel = 1
                                allUnlocked = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Đặt lại toàn bộ (Reset khóa)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // --- GAMEPLAY SCREEN ---
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SOKOBAN - MÀN $currentLevel", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = {
                                audioManager.playClick()
                                if (!isGameOver && !isVictory) {
                                    engine?.let { prefs.saveContinueGame("SOKOBAN", it.serializeState()) }
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
                                nextBoxId = 1
                                visualBoxes.clear()
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Thống số
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentLevel > 5) {
                            val minutes = timeRemaining / 60
                            val seconds = timeRemaining % 60
                            val timeColor = if (timeRemaining < 30) Color.Red else MaterialTheme.colorScheme.onBackground
                            Text(
                                text = String.format("%02d:%02d ⏳", minutes, seconds),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = timeColor
                            )
                        } else {
                            Text("Thời gian: Vô hạn ♾️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("Bước đi: $moves 🏃", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Đẩy hộp: $pushes 📦", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }

                    // Grid Board
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (boardState.isNotEmpty() && engine != null) {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val boardWidth = maxWidth
                                val boardHeight = maxHeight
                                val cols = engine.cols
                                val rows = engine.rows
                                val cellSpacing = 2.dp

                                val cellSize = minOf(
                                    (boardWidth - (cellSpacing * (cols + 1))) / cols,
                                    (boardHeight - (cellSpacing * (rows + 1))) / rows,
                                    48.dp
                                )
                                val gridWidth = (cellSize + cellSpacing) * cols + cellSpacing
                                val gridHeight = (cellSize + cellSpacing) * rows + cellSpacing

                                Box(
                                    modifier = Modifier
                                        .size(gridWidth, gridHeight)
                                        .background(Color(0x0A000000), RoundedCornerShape(8.dp))
                                ) {
                                    // 1. Vẽ các ô trống nền tĩnh làm background
                                    for (r in 0 until rows) {
                                        for (c in 0 until cols) {
                                            val initialCell = if (engine.initialBoard.size > r && engine.initialBoard[r].size > c) {
                                                engine.initialBoard[r][c]
                                            } else {
                                                EngineSokoban.FLOOR
                                            }
                                            val targetX = cellSpacing + (cellSize + cellSpacing) * c
                                            val targetY = cellSpacing + (cellSize + cellSpacing) * r

                                            Box(
                                                modifier = Modifier
                                                    .offset(x = targetX, y = targetY)
                                                    .size(cellSize),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                when (initialCell) {
                                                     EngineSokoban.WALL -> {
                                                         Canvas(modifier = Modifier.fillMaxSize()) {
                                                             val w = size.width
                                                             val h = size.height
                                                             val corner = 4.dp.toPx()
                                                             // Nền xanh tre trúc di sản
                                                             drawRoundRect(
                                                                 color = Color(0xFF3E6137),
                                                                 cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                             )
                                                             // Vẽ các sọc dọc thân tre trúc 3D tinh tế
                                                             val strokeW = 1.5.dp.toPx()
                                                             val segmentCount = 4
                                                             val segW = w / segmentCount
                                                             for (i in 1 until segmentCount) {
                                                                 drawLine(
                                                                     color = Color(0x33FFFFFF), // Sọc sáng tạo bóng
                                                                     start = Offset(i * segW, 0f),
                                                                     end = Offset(i * segW, h),
                                                                     strokeWidth = strokeW
                                                                 )
                                                                 drawLine(
                                                                     color = Color(0x22000000), // Sọc tối tạo chiều sâu
                                                                     start = Offset(i * segW + strokeW, 0f),
                                                                     end = Offset(i * segW + strokeW, h),
                                                                     strokeWidth = strokeW
                                                                 )
                                                             }
                                                             // Vẽ lóng tre ngang đặc trưng
                                                             drawLine(
                                                                 color = Color(0xFF2C4F27),
                                                                 start = Offset(0f, h * 0.35f),
                                                                 end = Offset(w, h * 0.35f),
                                                                 strokeWidth = 2.dp.toPx()
                                                             )
                                                             drawLine(
                                                                 color = Color(0xFF2C4F27),
                                                                 start = Offset(0f, h * 0.7f),
                                                                 end = Offset(w, h * 0.7f),
                                                                 strokeWidth = 2.dp.toPx()
                                                             )
                                                         }
                                                     }
                                                     EngineSokoban.GOAL, EngineSokoban.PLAYER_ON_GOAL -> {
                                                         Canvas(modifier = Modifier.fillMaxSize()) {
                                                             val w = size.width
                                                             val h = size.height
                                                             val cx = w / 2
                                                             val cy = h / 2
                                                             val outerRadius = w * 0.3f
                                                             val innerRadius = w * 0.12f
                                                             val path = androidx.compose.ui.graphics.Path().apply {
                                                                 var angle = -Math.PI / 2
                                                                 val nextAngle = Math.PI / 5
                                                                 moveTo(
                                                                     (cx + outerRadius * Math.cos(angle)).toFloat(),
                                                                     (cy + outerRadius * Math.sin(angle)).toFloat()
                                                                 )
                                                                 for (step in 0 until 10) {
                                                                     angle += nextAngle
                                                                     val r = if (step % 2 == 0) innerRadius else outerRadius
                                                                     lineTo(
                                                                         (cx + r * Math.cos(angle)).toFloat(),
                                                                         (cy + r * Math.sin(angle)).toFloat()
                                                                     )
                                                                 }
                                                                 close()
                                                             }
                                                             // Vẽ ngôi sao vàng di sản lấp lánh (Star Gold)
                                                             drawPath(
                                                                 path = path,
                                                                 color = Color(0xFFEAEA00).copy(alpha = 0.5f)
                                                             )
                                                             drawPath(
                                                                 path = path,
                                                                 color = Color(0xFFCDCD00),
                                                                 style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                                                             )
                                                         }
                                                     }
                                                    else -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f), RoundedCornerShape(2.dp))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. Vẽ các hộp động có chuyển động trượt
                                    visualBoxes.forEach { box ->
                                        key(box.id) {
                                            val targetX = cellSpacing + (cellSize + cellSpacing) * box.c
                                            val targetY = cellSpacing + (cellSize + cellSpacing) * box.r

                                            val animatedX by animateDpAsState(
                                                targetValue = targetX,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                                label = "box_x_${box.id}"
                                            )
                                            val animatedY by animateDpAsState(
                                                targetValue = targetY,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                                label = "box_y_${box.id}"
                                            )

                                            val isDocked = box.value == EngineSokoban.BOX_ON_GOAL
                                            val scaleBox = if (isDocked) 1.05f else 1.0f
                                            val animatedScaleBox by animateFloatAsState(
                                                targetValue = scaleBox,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                                label = "box_scale_${box.id}"
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .offset(x = animatedX, y = animatedY)
                                                    .size(cellSize)
                                                    .graphicsLayer(scaleX = animatedScaleBox, scaleY = animatedScaleBox)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isDocked) {
                                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val w = size.width
                                                        val h = size.height
                                                        val corner = 4.dp.toPx()
                                                        // Nền kiện hàng gỗ màu gỗ
                                                        drawRoundRect(
                                                            color = Color(0xFFD2B48C),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                        // Họa tiết đan chéo viền nâu đậm
                                                        val strokeWidth = 2.dp.toPx()
                                                        drawRoundRect(
                                                            color = Color(0xFFEAEA00), // Gold border for docked state
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth * 1.5f),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                        // Các đường gân chéo kiện hàng gỗ
                                                        drawLine(Color(0xFF5D403B), Offset(w * 0.15f, h * 0.15f), Offset(w * 0.85f, h * 0.85f), strokeWidth)
                                                        drawLine(Color(0xFF5D403B), Offset(w * 0.85f, h * 0.15f), Offset(w * 0.15f, h * 0.85f), strokeWidth)
                                                        // Tâm hình vuông gỗ nhỏ
                                                        drawRoundRect(
                                                            color = Color(0xFF5D403B),
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                        // Vẽ ngôi sao vàng di sản lấp lánh (Star Gold)
                                                        drawCircle(Color(0xFFEAEA00), radius = w * 0.18f)
                                                        drawCircle(Color(0xFFCDCD00), radius = w * 0.18f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                                                    }
                                                } else {
                                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val w = size.width
                                                        val h = size.height
                                                        val corner = 4.dp.toPx()
                                                        drawRoundRect(
                                                            color = Color(0xFFD2B48C),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                        val strokeWidth = 2.dp.toPx()
                                                        drawRoundRect(
                                                            color = Color(0xFF5D403B),
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                        drawLine(Color(0xFF5D403B), Offset(w * 0.15f, h * 0.15f), Offset(w * 0.85f, h * 0.85f), strokeWidth)
                                                        drawLine(Color(0xFF5D403B), Offset(w * 0.85f, h * 0.15f), Offset(w * 0.15f, h * 0.85f), strokeWidth)
                                                        drawRoundRect(
                                                            color = Color(0xFF5D403B),
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. Vẽ Người chơi động có chuyển động trượt
                                    val playerTargetX = cellSpacing + (cellSize + cellSpacing) * playerVisualCol
                                    val playerTargetY = cellSpacing + (cellSize + cellSpacing) * playerVisualRow

                                    val animatedPlayerX by animateDpAsState(
                                        targetValue = playerTargetX,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                        label = "player_x"
                                    )
                                    val animatedPlayerY by animateDpAsState(
                                        targetValue = playerTargetY,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                        label = "player_y"
                                    )

                                    val animatedShakeX by animateDpAsState(
                                        targetValue = playerOffsetX,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                                        label = "player_shake_x"
                                    )
                                    val animatedShakeY by animateDpAsState(
                                        targetValue = playerOffsetY,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                                        label = "player_shake_y"
                                    )

                                    var scalePlayer by remember { mutableStateOf(1f) }
                                    val animatedScalePlayer by animateFloatAsState(
                                        targetValue = scalePlayer,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                        label = "player_scale"
                                    )
                                    LaunchedEffect(playerVisualRow, playerVisualCol) {
                                        scalePlayer = 1.25f
                                        delay(150)
                                        scalePlayer = 1.0f
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset(x = animatedPlayerX + animatedShakeX, y = animatedPlayerY + animatedShakeY)
                                            .size(cellSize)
                                            .graphicsLayer(scaleX = animatedScalePlayer, scaleY = animatedScalePlayer)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(FlagRed, LacquerRed)
                                                    )
                                                )
                                                .border(2.dp, StarGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Vẽ nón lá chồng đè lên trên avatar
                                                Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                                                    val w = size.width
                                                    val h = size.height
                                                    val hatPath = androidx.compose.ui.graphics.Path().apply {
                                                        moveTo(w / 2, h * 0.1f) // Đỉnh nón
                                                        lineTo(w * 0.85f, h * 0.45f) // Mép phải
                                                        lineTo(w * 0.15f, h * 0.45f) // Mép trái
                                                        close()
                                                    }
                                                    drawPath(
                                                        path = hatPath,
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(Color(0xFFFFF9EA), Color(0xFFEAEA00)) // Màu nón vàng nhạt sáng
                                                        )
                                                    )
                                                    drawPath(
                                                        path = hatPath,
                                                        color = Color(0xFFCDCD00),
                                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                                    )
                                                    // Vẽ sọc dọc nón lá
                                                    drawLine(Color(0x33000000), Offset(w/2, h*0.1f), Offset(w*0.5f, h*0.45f), 1.dp.toPx())
                                                    drawLine(Color(0x33000000), Offset(w/2, h*0.1f), Offset(w*0.35f, h*0.45f), 1.dp.toPx())
                                                    drawLine(Color(0x33000000), Offset(w/2, h*0.1f), Offset(w*0.65f, h*0.45f), 1.dp.toPx())
                                                }

                                                // Emoji khuôn mặt sĩ phu Việt bên dưới nón
                                                Text(
                                                    text = "🤠",
                                                    fontSize = (cellSize.value * 0.38f).sp,
                                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = (cellSize.value * 0.05f).dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // D-Pad điều khiển dạng tròn cao cấp
                    GlassCard(
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Button(
                                onClick = { makeMove(-1, 0) },
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("▲", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { makeMove(0, -1) },
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.size(52.dp))
                                Button(
                                    onClick = { makeMove(0, 1) },
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("▶", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { makeMove(1, 0) },
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("▼", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- VICTORY & GAMEOVER DIALOGS ---
                ConfettiEffect(isActive = isVictory)

                if (isVictory) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Chúc mừng!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (currentLevel == 18)
                                        "Xuất sắc! Bạn đã vượt qua màn 18 và CHINH PHỤC hoàn toàn trò chơi Sokoban!"
                                    else
                                        "Đã giải quyết xong màn $currentLevel thành công!\nĐiểm số đạt được: $score",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )

                                Button(
                                    onClick = {
                                        audioManager.playSuccess()
                                        engine?.loadLevel()
                                        nextBoxId = 1
                                        visualBoxes.clear()
                                        syncState()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Chơi lại màn này 🔄", fontWeight = FontWeight.Bold)
                                }

                                if (currentLevel < 18) {
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
                                    // Màn 18 chúc mừng chọn reset all hoặc giữ nguyên
                                    Button(
                                        onClick = {
                                            audioManager.playClick()
                                            prefs.sokobanUnlockedLevel = 1
                                            prefs.sokobanAllUnlocked = false
                                            unlockedLevel = 1
                                            allUnlocked = false
                                            currentLevel = -1
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("Khóa lại toàn bộ (Reset all) 🔒", fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        audioManager.playClick()
                                        currentLevel = -1
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Trở về danh sách màn 🏠", fontWeight = FontWeight.Bold)
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
                        text = { Text("Đồng hồ đã chỉ về 0. Bạn đã không kịp dọn kho Sokoban lần này.") },
                        confirmButton = {
                            Button(onClick = {
                                audioManager.playClick()
                                engine?.loadLevel()
                                nextBoxId = 1
                                visualBoxes.clear()
                                syncState()
                            }) {
                                Text("Thử lại")
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

                // --- PAUSE SCREEN OVERLAY ---
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
                                    nextBoxId = 1
                                    visualBoxes.clear()
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
                                        prefs.saveContinueGame("SOKOBAN", engine.serializeState())
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

class VisualBox(
    val id: Int,
    var r: Int,
    var c: Int,
    var value: Int
)
