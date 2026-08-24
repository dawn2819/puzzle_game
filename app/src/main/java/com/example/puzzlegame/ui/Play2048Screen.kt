package com.example.puzzlegame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.Engine2048
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.ui.components.ConfettiEffect
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.audio.AudioManager
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Play2048Screen(
    size: Int,
    isRestore: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val engine = remember { Engine2048(size) }
    val boardState = remember { mutableStateListOf<IntArray>() }

    var currentScore by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(prefs.getHighScore("2048_$size")) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var show2048Celebration by remember { mutableStateOf(false) }
    var celebrated2048 by remember { mutableStateOf(false) }
    var lastDirection by remember { mutableStateOf(-1) }

    var nextTileId by remember { mutableStateOf(1) }
    val visualTiles = remember { mutableStateListOf<VisualTile>() }

    fun syncVisualTiles(newBoard: Array<IntArray>, direction: Int) {
        val oldTiles = visualTiles.toList()
        visualTiles.clear()

        val unmatchedOldTiles = oldTiles.toMutableList()

        if (direction == -1) {
            // Khởi tạo hoặc khôi phục: Khớp theo khoảng cách tối thiểu
            for (r in 0 until size) {
                for (c in 0 until size) {
                    val valAt = if (newBoard.size > r) newBoard[r][c] else 0
                    if (valAt > 0) {
                        var bestMatchIndex = -1
                        var minDistance = 999
                        for (i in unmatchedOldTiles.indices) {
                            val old = unmatchedOldTiles[i]
                            if (old.value == valAt) {
                                val dist = kotlin.math.abs(old.r - r) + kotlin.math.abs(old.c - c)
                                if (dist < minDistance) {
                                    minDistance = dist
                                    bestMatchIndex = i
                                }
                            }
                        }
                        if (bestMatchIndex != -1) {
                            val matched = unmatchedOldTiles.removeAt(bestMatchIndex)
                            visualTiles.add(VisualTile(id = matched.id, value = valAt, r = r, c = c))
                        } else {
                            visualTiles.add(VisualTile(id = nextTileId++, value = valAt, r = r, c = c))
                        }
                    }
                }
            }
            return
        }

        // Hướng vuốt: 0 = Trái, 1 = Lên, 2 = Phải, 3 = Xuống
        if (direction == 0 || direction == 2) {
            // Ngang -> Duyệt từng hàng độc lập
            for (r in 0 until size) {
                val rowOldTiles = unmatchedOldTiles.filter { it.r == r }.sortedBy { it.c }
                val rowNewCols = mutableListOf<Int>()
                for (c in 0 until size) {
                    if ((if (newBoard.size > r) newBoard[r][c] else 0) > 0) {
                        rowNewCols.add(c)
                    }
                }

                if (direction == 0) {
                    // Vuốt Trái: Khớp từ trái sang phải
                    var oldIdx = 0
                    var newIdx = 0
                    while (newIdx < rowNewCols.size) {
                        val c = rowNewCols[newIdx]
                        val valAt = newBoard[r][c]

                        if (oldIdx < rowOldTiles.size) {
                            val oldTile = rowOldTiles[oldIdx++]
                            unmatchedOldTiles.remove(oldTile)
                            visualTiles.add(VisualTile(id = oldTile.id, value = valAt, r = r, c = c))

                            // Gộp ô: hai ô trượt vào cùng một ô mới
                            if (oldIdx < rowOldTiles.size && oldTile.value * 2 == valAt) {
                                val secondOldTile = rowOldTiles[oldIdx++]
                                unmatchedOldTiles.remove(secondOldTile)
                                visualTiles.add(VisualTile(id = secondOldTile.id, value = valAt, r = r, c = c, displayValue = oldTile.value, isMergedAway = true))
                            }
                        } else {
                            visualTiles.add(VisualTile(id = nextTileId++, value = valAt, r = r, c = c))
                        }
                        newIdx++
                    }
                } else {
                    // Vuốt Phải: Khớp từ phải sang trái
                    val revOldTiles = rowOldTiles.reversed()
                    val revNewCols = rowNewCols.reversed()
                    var oldIdx = 0
                    var newIdx = 0
                    while (newIdx < revNewCols.size) {
                        val c = revNewCols[newIdx]
                        val valAt = newBoard[r][c]

                        if (oldIdx < revOldTiles.size) {
                            val oldTile = revOldTiles[oldIdx++]
                            unmatchedOldTiles.remove(oldTile)
                            visualTiles.add(VisualTile(id = oldTile.id, value = valAt, r = r, c = c))

                            if (oldIdx < revOldTiles.size && oldTile.value * 2 == valAt) {
                                val secondOldTile = revOldTiles[oldIdx++]
                                unmatchedOldTiles.remove(secondOldTile)
                                visualTiles.add(VisualTile(id = secondOldTile.id, value = valAt, r = r, c = c, displayValue = oldTile.value, isMergedAway = true))
                            }
                        } else {
                            visualTiles.add(VisualTile(id = nextTileId++, value = valAt, r = r, c = c))
                        }
                        newIdx++
                    }
                }
            }
        } else {
            // Dọc -> Duyệt từng cột độc lập
            for (c in 0 until size) {
                val colOldTiles = unmatchedOldTiles.filter { it.c == c }.sortedBy { it.r }
                val colNewRows = mutableListOf<Int>()
                for (r in 0 until size) {
                    if ((if (newBoard.size > r) newBoard[r][c] else 0) > 0) {
                        colNewRows.add(r)
                    }
                }

                if (direction == 1) {
                    // Vuốt Lên: Khớp từ trên xuống dưới
                    var oldIdx = 0
                    var newIdx = 0
                    while (newIdx < colNewRows.size) {
                        val r = colNewRows[newIdx]
                        val valAt = newBoard[r][c]

                        if (oldIdx < colOldTiles.size) {
                            val oldTile = colOldTiles[oldIdx++]
                            unmatchedOldTiles.remove(oldTile)
                            visualTiles.add(VisualTile(id = oldTile.id, value = valAt, r = r, c = c))

                            if (oldIdx < colOldTiles.size && oldTile.value * 2 == valAt) {
                                val secondOldTile = colOldTiles[oldIdx++]
                                unmatchedOldTiles.remove(secondOldTile)
                                visualTiles.add(VisualTile(id = secondOldTile.id, value = valAt, r = r, c = c, displayValue = oldTile.value, isMergedAway = true))
                            }
                        } else {
                            visualTiles.add(VisualTile(id = nextTileId++, value = valAt, r = r, c = c))
                        }
                        newIdx++
                    }
                } else {
                    // Vuốt Xuống: Khớp từ dưới lên trên
                    val revOldTiles = colOldTiles.reversed()
                    val revNewRows = colNewRows.reversed()
                    var oldIdx = 0
                    var newIdx = 0
                    while (newIdx < revNewRows.size) {
                        val r = revNewRows[newIdx]
                        val valAt = newBoard[r][c]

                        if (oldIdx < revOldTiles.size) {
                            val oldTile = revOldTiles[oldIdx++]
                            unmatchedOldTiles.remove(oldTile)
                            visualTiles.add(VisualTile(id = oldTile.id, value = valAt, r = r, c = c))

                            if (oldIdx < revOldTiles.size && oldTile.value * 2 == valAt) {
                                val secondOldTile = revOldTiles[oldIdx++]
                                unmatchedOldTiles.remove(secondOldTile)
                                visualTiles.add(VisualTile(id = secondOldTile.id, value = valAt, r = r, c = c, displayValue = oldTile.value, isMergedAway = true))
                            }
                        } else {
                            visualTiles.add(VisualTile(id = nextTileId++, value = valAt, r = r, c = c))
                        }
                        newIdx++
                    }
                }
            }
        }
    }

    // Đồng bộ state từ engine
    fun syncState() {
        boardState.clear()
        for (r in 0 until size) {
            boardState.add(engine.board[r].copyOf())
        }
        currentScore = engine.score
        isGameOver = engine.isGameOver

        // Đồng bộ các ô số vẽ trực quan
        syncVisualTiles(engine.board, lastDirection)

        // Phát hiện đạt 2048 khối đầu tiên
        if (engine.hasReached2048 && !celebrated2048) {
            show2048Celebration = true
            celebrated2048 = true
        }

        // Cập nhật điểm cao tức thời nếu vượt qua
        if (currentScore > highScore) {
            highScore = currentScore
        }

        // Tự động lưu trạng thái để phục vụ tính năng Continue
        if (!isGameOver) {
            prefs.saveContinueGame("2048", engine.serializeState())
        } else {
            prefs.clearContinueGame()
            prefs.saveScore("2048_$size", currentScore)
        }
    }

    // Khởi tạo
    LaunchedEffect(Unit) {
        if (isRestore && prefs.hasContinueGame() && prefs.getContinueGameType() == "2048") {
            try {
                val data = prefs.getContinueGameData()!!
                val parts = data.split("|")
                val savedScore = parts[1].toInt()
                val savedBoard = prefs.deserializeGrid(parts[2])
                engine.restoreState(savedBoard, savedScore)
                celebrated2048 = engine.hasReached2048
            } catch (e: Exception) {
                engine.reset()
                celebrated2048 = false
            }
        } else {
            engine.reset()
            celebrated2048 = false
        }
        syncState()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    // Xử lý vuốt cử chỉ
    var totalDragX = 0f
    var totalDragY = 0f
    val swipeThreshold = 50f // Pixel threshold to register a swipe

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("2048 (${size}x${size})", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            // Lưu trạng thái trước khi thoát
                            if (!engine.isGameOver) {
                                prefs.saveContinueGame("2048", engine.serializeState())
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
                            engine.reset()
                            celebrated2048 = false
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
                // Score Board
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Điểm số hiện tại
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Điểm số", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("$currentScore", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Điểm số cao nhất
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Kỷ lục", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("$highScore", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Grid Game Board
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                },
                                onDragEnd = {
                                    if (abs(totalDragX) > abs(totalDragY)) {
                                        if (abs(totalDragX) > swipeThreshold) {
                                            val dir = if (totalDragX > 0) 2 else 0 // 2=Right, 0=Left
                                            lastDirection = dir
                                            val didMove = engine.move(dir)
                                            if (didMove) {
                                                audioManager.playClick()
                                                syncState()
                                            }
                                        }
                                    } else {
                                        if (abs(totalDragY) > swipeThreshold) {
                                            val dir = if (totalDragY > 0) 3 else 1 // 3=Down, 1=Up
                                            lastDirection = dir
                                            val didMove = engine.move(dir)
                                            if (didMove) {
                                                audioManager.playClick()
                                                syncState()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    val boardWidth = maxWidth
                    val cellSpacing = 6.dp
                    val cellSize = (boardWidth - (cellSpacing * (size + 1))) / size

                    // 1. Vẽ các ô trống nền tĩnh làm background
                    Column(
                        verticalArrangement = Arrangement.spacedBy(cellSpacing),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(cellSpacing)
                    ) {
                        repeat(size) {
                            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                                repeat(size) {
                                    Box(
                                        modifier = Modifier
                                            .size(cellSize)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDim)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Vẽ các ô số động trượt trên nền
                    visualTiles.forEach { tile ->
                        key(tile.id) {
                            val targetX = cellSpacing + (cellSize + cellSpacing) * tile.c
                            val targetY = cellSpacing + (cellSize + cellSpacing) * tile.r

                            val animatedX by animateDpAsState(
                                targetValue = targetX,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                label = "tile_x_${tile.id}"
                            )
                            val animatedY by animateDpAsState(
                                targetValue = targetY,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                label = "tile_y_${tile.id}"
                            )
                            val animatedAlpha by animateFloatAsState(
                                targetValue = if (tile.isMergedAway) 0f else 1f,
                                animationSpec = tween(180, easing = LinearOutSlowInEasing),
                                label = "tile_alpha_${tile.id}"
                            )

                            var tileScale by remember(tile.displayValue) { 
                                // Mới xuất hiện phồng lên từ 0.2f, gộp ô phồng lớn 1.25f
                                mutableStateOf(if (tile.displayValue == 2 || tile.displayValue == 4) 0.2f else 1.25f) 
                            }
                            val animatedTileScale by animateFloatAsState(
                                targetValue = tileScale,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "tile_scale_${tile.id}"
                            )
                            LaunchedEffect(tile.displayValue) {
                                delay(50)
                                tileScale = 1f
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = animatedX, y = animatedY)
                                    .size(cellSize)
                                    .graphicsLayer(alpha = animatedAlpha, scaleX = animatedTileScale, scaleY = animatedTileScale)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getTileColor(tile.displayValue)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${tile.displayValue}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = when {
                                        tile.displayValue >= 1024 -> 16.sp
                                        tile.displayValue >= 128 -> 20.sp
                                        else -> 24.sp
                                    },
                                    color = when (tile.displayValue) {
                                        2, 4 -> Color(0xFF92400E)
                                        1024 -> EarthyBrown
                                        else -> Color.White
                                    }
                                )
                            }
                        }
                    }

                    // Game Over Screen Overlay
                    if (isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GAME OVER", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bạn đạt được: $currentScore điểm", fontSize = 16.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(24.dp))
                                 Button(onClick = {
                                     audioManager.playClick()
                                     engine.reset()
                                     celebrated2048 = false
                                     lastDirection = -1
                                     nextTileId = 1
                                     visualTiles.clear()
                                     syncState()
                                 }) {
                                     Text("Chơi lại")
                                 }
                            }
                        }
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
                                    modifier = Modifier.width(160.dp)
                                ) {
                                    Text("Tiếp tục")
                                }
                                 Button(
                                     onClick = {
                                         audioManager.playClick()
                                         engine.reset()
                                         celebrated2048 = false
                                         lastDirection = -1
                                         nextTileId = 1
                                         visualTiles.clear()
                                         syncState()
                                         isPaused = false
                                     },
                                     modifier = Modifier.width(160.dp)
                                 ) {
                                     Text("Chơi lại")
                                 }
                                Button(
                                    onClick = {
                                        audioManager.playClick()
                                        if (!engine.isGameOver) {
                                            prefs.saveContinueGame("2048", engine.serializeState())
                                        }
                                        onBack()
                                    },
                                    modifier = Modifier.width(160.dp)
                                ) {
                                    Text("Thoát")
                                }
                            }
                        }
                    }

                    // Hộp thoại chúc mừng 2048
                    if (show2048Celebration) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = { Text("CHÚC MỪNG CHIẾN THẮNG!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                            text = { Text("Bạn đã xuất sắc tạo ra khối 2048 đầu tiên! Bạn muốn tiếp tục thử thách bản thân với các số lớn hơn hay dừng lại ghi danh điểm kỷ lục?") },
                            confirmButton = {
                                Button(onClick = {
                                    audioManager.playSuccess()
                                    show2048Celebration = false
                                }) {
                                    Text("Chơi tiếp 🚀")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    audioManager.playClick()
                                    show2048Celebration = false
                                    prefs.clearContinueGame()
                                    prefs.saveScore("2048_$size", engine.score)
                                    onBack()
                                }) {
                                    Text("Dừng lại 🛑")
                                }
                            }
                        )
                    }
                }

                // Hiệu ứng pháo hoa Confetti ăn mừng đạt mốc 2048
                ConfettiEffect(isActive = show2048Celebration)

                Text(
                    "Vuốt (Lên/Xuống/Trái/Phải) trên lưới để gộp các ô số!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

fun getTileColor(value: Int): Color {
    return when (value) {
        0 -> SurfaceDim
        2 -> Tile2
        4 -> Tile4
        8 -> Tile8
        16 -> Tile16
        32 -> Tile32
        64 -> Tile64
        128 -> Tile128
        256 -> Tile256
        512 -> Tile512
        1024 -> Tile1024
        2048 -> Tile2048
        else -> TileHigher
    }
}

class VisualTile(
    val id: Int,
    val value: Int,
    val r: Int,
    val c: Int,
    val displayValue: Int = value,
    val isMergedAway: Boolean = false
)
