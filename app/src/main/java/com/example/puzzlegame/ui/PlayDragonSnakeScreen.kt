package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineDragonSnake
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDragonSnakeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val engine = remember { EngineDragonSnake() }
    
    var level by remember { mutableStateOf(engine.level) }
    var snake by remember { mutableStateOf(engine.snake.toList()) }
    var walls by remember { mutableStateOf(engine.walls.toSet()) }
    var gems by remember { mutableStateOf(engine.gems.toSet()) }
    var traps by remember { mutableStateOf(engine.traps.toSet()) }
    var portal by remember { mutableStateOf(engine.portal) }
    var moves by remember { mutableStateOf(engine.moves) }
    var isGameOver by remember { mutableStateOf(engine.isGameOver) }
    var isVictory by remember { mutableStateOf(engine.isVictory) }

    val swipeThreshold = 50f
    var totalDragX = 0f
    var totalDragY = 0f

    fun syncState() {
        level = engine.level
        snake = engine.snake.toList()
        walls = engine.walls.toSet()
        gems = engine.gems.toSet()
        traps = engine.traps.toSet()
        portal = engine.portal
        moves = engine.moves
        isGameOver = engine.isGameOver
        isVictory = engine.isVictory
    }

    fun makeMove(dir: Int) {
        val moved = engine.move(dir)
        if (moved) {
            audioManager.playClick()
            syncState()
            if (engine.isGameOver) {
                audioManager.playError()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    // Tích lũy XP/Coins khi chiến thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "dragonsnake_win_credited_level_$level"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20) // +100 XP, +20 Coins
                prefs.saveScore(winKey, 1)
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RỒNG RẮN LÊN MÂY", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
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
                            text = "Màn $level/5 🏆",
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số bước: $moves 🏃",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Ngọc ngậm: ${gems.size} 🔴",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagRed
                    )
                }

                // --- BÀN CHƠI ME CUNG GRID ---
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
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
                                            if (totalDragX > 0) makeMove(2) else makeMove(0) // 2: Phải, 0: Trái
                                        }
                                    } else {
                                        if (abs(totalDragY) > swipeThreshold) {
                                            if (totalDragY > 0) makeMove(3) else makeMove(1) // 3: Xuống, 1: Lên
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val boardWidth = maxWidth
                    val gridCellSize = boardWidth / engine.size

                    // Ô vuông trống ở dưới nền
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(engine.size) { r ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(engine.size) { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(gridCellSize - 2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceLow)
                                            .border(0.5.dp, OutlineVariant.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Vẽ các thực thể trong maze
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. Tường vách tre cản lối
                        walls.forEach { wall ->
                            Box(
                                modifier = Modifier
                                    .offset(x = gridCellSize * wall.second, y = gridCellSize * wall.first)
                                    .size(gridCellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                // Tre trúc biểu diễn tường cản
                                Canvas(modifier = Modifier.fillMaxSize().padding(1.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val corner = 4.dp.toPx()
                                    drawRoundRect(Color(0xFF3E6137), cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner))
                                    // Sọc tre dọc
                                    drawLine(Color(0x33FFFFFF), Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), 1.5.dp.toPx())
                                    drawLine(Color(0x33FFFFFF), Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), 1.5.dp.toPx())
                                    // Đốt tre ngang
                                    drawLine(Color(0xFF2C4F27), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 2.dp.toPx())
                                }
                            }
                        }

                        // 2. Bẫy đinh
                        traps.forEach { trap ->
                            Box(
                                modifier = Modifier
                                    .offset(x = gridCellSize * trap.second, y = gridCellSize * trap.first)
                                    .size(gridCellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    // Vẽ cọc sắt nhọn màu nâu xám gỉ
                                    val c = Color(0xFF5D403B)
                                    drawLine(c, Offset(w * 0.2f, h * 0.8f), Offset(w * 0.5f, h * 0.2f), 2.dp.toPx())
                                    drawLine(c, Offset(w * 0.8f, h * 0.8f), Offset(w * 0.5f, h * 0.2f), 2.dp.toPx())
                                    drawLine(c, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.5f, h * 0.1f), 2.dp.toPx())
                                }
                            }
                        }

                        // 3. Ngọc rồng tỏa sáng
                        gems.forEach { gem ->
                            Box(
                                modifier = Modifier
                                    .offset(x = gridCellSize * gem.second, y = gridCellSize * gem.first)
                                    .size(gridCellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                // Viên ngọc sáng tròn màu vàng/cam lung linh
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .shadow(4.dp, CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFFFFF9EA), StarGold, Color(0xFFF97316))
                                            ),
                                            shape = CircleShape
                                        )
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }

                        // 4. Cổng làng đích đến
                        Box(
                            modifier = Modifier
                                .offset(x = gridCellSize * portal.second, y = gridCellSize * portal.first)
                                .size(gridCellSize),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏯",
                                fontSize = (gridCellSize.value * 0.65f).sp
                            )
                        }

                        // 5. Rồng di chuyển (Thân đèn lồng và Đầu mạ vàng)
                        snake.forEachIndexed { idx, bodyNode ->
                            val isHead = idx == 0

                            val x = gridCellSize * bodyNode.second
                            val y = gridCellSize * bodyNode.first

                            // Sử dụng hoạt ảnh nảy nhẹ khi rồng trượt qua ô
                            val animatedX by animateDpAsState(
                                targetValue = x,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                label = "snake_x_$idx"
                            )
                            val animatedY by animateDpAsState(
                                targetValue = y,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                label = "snake_y_$idx"
                            )

                            Box(
                                modifier = Modifier
                                    .offset(x = animatedX, y = animatedY)
                                    .size(gridCellSize)
                                    .padding(if (isHead) 1.dp else 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isHead) {
                                    // Đầu rồng Lý mạ vàng đặc trưng
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shadow(3.dp, CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFFF9EA), StarGold, DarkGold)
                                                ),
                                                shape = CircleShape
                                            )
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐉", fontSize = (gridCellSize.value * 0.5f).sp)
                                    }
                                } else {
                                    // Thân rồng là chuỗi các đèn lồng đỏ truyền thống thu nhỏ dần
                                    val scaleFactor = 1.0f - (idx.toFloat() / (snake.size + 2))
                                    Box(
                                        modifier = Modifier
                                            .size((gridCellSize.value * 0.7f * scaleFactor).dp)
                                            .shadow(2.dp, CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFCA5A5), FlagRed, Color(0xFF991B1B))
                                                ),
                                                shape = CircleShape
                                            )
                                            .border(1.dp, StarGold.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Tua rua vàng dưới đèn lồng
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(StarGold, CircleShape)
                                                .align(Alignment.BottomCenter)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // D-pad điều khiển ảo dưới màn hình
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = { makeMove(1) }, // Lên
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
                            onClick = { makeMove(0) }, // Trái
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Text("◀", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                        IconButton(
                            onClick = { makeMove(2) }, // Phải
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Text("▶", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(
                        onClick = { makeMove(3) }, // Xuống
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
                            engine.loadLevel(level)
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
                        "💥 RỒNG BỊ TỔN THƯƠNG!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Text(
                        "Rồng của bạn đã tự cắn trúng đuôi hoặc dẫm phải bẫy đinh. Hãy thử sức lại để khéo léo vượt ải nhé!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại thắng màn chơi (Victory)
        if (isVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            if (level < 5) {
                                engine.loadLevel(level + 1)
                            } else {
                                engine.loadLevel(1)
                            }
                            syncState()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (level < 5) "Màn Tiếp Theo" else "Chơi Lại Màn 1", fontWeight = FontWeight.Bold)
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
                        "🎉 VƯỢT ẢI THÀNH CÔNG!",
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
                        Text("Bạn đã đưa đầu Rồng ăn hết ngọc và chui vào cổng làng an toàn!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+100 XP và +20 Tiền vàng đã được tích lũy vào tài khoản di sản Hồn Việt!",
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
