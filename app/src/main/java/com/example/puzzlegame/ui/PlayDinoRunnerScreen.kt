package com.example.puzzlegame.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.R
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.engine.EngineDinoRunner
import com.example.puzzlegame.engine.TigerType
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.PremiumBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDinoRunnerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val engine = remember { EngineDinoRunner() }

    var scoreDisplay by remember { mutableStateOf(0) }
    var highScoreDisplay by remember { mutableStateOf(prefs.getHighScore("dino_runner")) }
    var isGameOver by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableStateOf(0) }
    var xpEarned by remember { mutableStateOf(0) }
    var lastMilestone by remember { mutableStateOf(0) }

    // 1. Load các Bitmap sprite của Nhân vật Lính áo hồng (8448..8456)
    val playerRunBitmaps = remember {
        listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_run_0),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_run_1),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_run_2),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_run_3),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_run_4)
        )
    }
    val playerSlideBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_slide)
    }
    val playerJumpUpBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_jump_up)
    }
    val playerJumpDownBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_jump_down)
    }
    val playerLandBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.dino_player_land)
    }

    // 2. Load các Bitmap Hổ Vàng (8763..8766) - lật gương ngang hướng sang trái
    val yellowTigerBitmaps = remember {
        val rawBitmaps = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_run_0),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_run_1),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_run_2),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_run_3)
        )
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        rawBitmaps.map { raw ->
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        }
    }

    // 3. Load các Bitmap Bạch Hổ (8771..8775) - lật gương ngang hướng sang trái
    val whiteTigerBitmaps = remember {
        val rawBitmaps = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_white_0),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_white_1),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_white_2),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_white_3),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_white_4)
        )
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        rawBitmaps.map { raw ->
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        }
    }

    // 4. Load các Bitmap Lam Hổ (8779..8783) - lật gương ngang hướng sang trái
    val blueTigerBitmaps = remember {
        val rawBitmaps = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_blue_0),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_blue_1),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_blue_2),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_blue_3),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_tiger_blue_4)
        )
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        rawBitmaps.map { raw ->
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        }
    }

    // 5. Load các Bitmap đầu hổ bay trên trời (8768, 8776, 8784)
    val skyFlyerBitmaps = remember {
        listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_sky_tiger_yellow),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_sky_tiger_white),
            BitmapFactory.decodeResource(context.resources, R.drawable.dino_sky_tiger_blue)
        )
    }

    // Thực hiện cú nhảy (1 trong 3 mức)
    fun triggerJump() {
        val level = engine.jump()
        if (level > 0) {
            when (level) {
                1 -> audioManager.playClick()
                2 -> audioManager.playClick()
                3 -> audioManager.playSuccess() // Cấp 3: Đại phi thân âm vang!
            }
        }
    }

    // Khởi động ván game mới
    fun restartGame() {
        engine.startNewGame(prefs.getHighScore("dino_runner"))
        scoreDisplay = 0
        isGameOver = false
        lastMilestone = 0
    }

    // Xử lý sự kiện Game Over & Cộng thưởng
    fun handleGameOver() {
        isGameOver = true
        coinsEarned = engine.coinsEarned
        xpEarned = (engine.score / 10f).toInt().coerceAtLeast(10)

        // Lưu kỷ lục & cộng thưởng
        if (engine.score.toInt() > prefs.getHighScore("dino_runner")) {
            prefs.saveScore("dino_runner", engine.score.toInt())
            highScoreDisplay = engine.score.toInt()
        }
        prefs.profileCoins += coinsEarned
        prefs.profileXp += xpEarned

        audioManager.playError()
    }

    // Khởi tạo kích thước màn hình
    LaunchedEffect(Unit) {
        restartGame()
    }

    // Vòng lặp cập nhật vật lý 60 FPS
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            withFrameNanos { frameTime ->
                val collided = engine.update()
                if (collided) {
                    handleGameOver()
                } else {
                    scoreDisplay = engine.score.toInt()

                    // Âm thanh mốc điểm mỗi 100m
                    if (scoreDisplay > 0 && scoreDisplay / 100 > lastMilestone) {
                        lastMilestone = scoreDisplay / 100
                        audioManager.playSuccess()
                    }
                }
                tick = frameTime
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground(drawableId = R.drawable.bg_selection, bgDimAlpha = 0.25f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "VƯỢT HỔ",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = FlagRed,
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(Color.White.copy(alpha = 0.9f), offset = Offset(0f, 1f), blurRadius = 4f)
                                )
                            )
                            Text(
                                text = "(Dino Runner)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xF5FFFBEB))
                                    .border(1.dp, Color(0x66B40006), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("◀", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                            }
                        }
                    },
                    actions = {
                        // Hiển thị điểm số & kỷ lục trên thanh điều hướng
                        Row(
                            modifier = Modifier.padding(end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xF5FFFBEB))
                                    .border(1.dp, Color(0x66B40006), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🏆 $highScoreDisplay m",
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF08A))
                                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$scoreDisplay m",
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = FlagRed
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(isGameOver) {
                        // Thao tác vuốt màn hình (Vuốt lên để nhảy, vuốt xuống để trượt)
                        var totalDragY = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDragY = 0f },
                            onDragEnd = {
                                if (totalDragY < -30f) {
                                    triggerJump()
                                } else if (totalDragY > 30f) {
                                    engine.setSlide(true)
                                    audioManager.playClick()
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                totalDragY += dragAmount
                            }
                        )
                    }
                    .pointerInput(isGameOver) {
                        // Chạm vào màn hình để nhảy (hỗ trợ bấm liên tiếp nhảy 3 cấp)
                        detectTapGestures {
                            triggerJump()
                        }
                    }
                    .onSizeChanged { size ->
                        engine.screenWidth = size.width.toFloat()
                        engine.screenHeight = size.height.toFloat()
                        engine.groundY = size.height * 0.72f // Mặt đất ở 72% chiều cao màn hình
                        if (!engine.isPlaying && !isGameOver) {
                            engine.playerY = engine.groundY - engine.playerHeight
                        }
                    }
            ) {
                // Canvas vẽ thế giới game Runner 60 FPS
                val paint = remember {
                    Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (tick >= 0 && engine.groundY > 0f) {
                        val canvas = drawContext.canvas.nativeCanvas
                        val w = size.width
                        val h = size.height
                        val gy = engine.groundY

                        // 1. Vẽ các vật thể đầu hổ bay trên trời (8768, 8776, 8784)
                        for (flyer in engine.skyFlyers) {
                            val flyerBmp = skyFlyerBitmaps[flyer.type % skyFlyerBitmaps.size]
                            val fDst = android.graphics.RectF(
                                flyer.x,
                                flyer.y,
                                flyer.x + flyer.size,
                                flyer.y + flyer.size
                            )
                            canvas.drawBitmap(flyerBmp, null, fDst, paint)
                        }

                        // 2. Vẽ mặt đất truyền thống cuộn liên tục (Scrolling Ground)
                        val groundPaint = Paint().apply {
                            color = android.graphics.Color.rgb(112, 70, 42)
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(0f, gy, w, h, groundPaint)

                        val grassPaint = Paint().apply {
                            color = android.graphics.Color.rgb(67, 160, 71)
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(0f, gy, w, gy + 12f, grassPaint)

                        val darkLinePaint = Paint().apply {
                            color = android.graphics.Color.rgb(46, 27, 16)
                            strokeWidth = 3f
                            style = Paint.Style.STROKE
                        }
                        canvas.drawLine(0f, gy, w, gy, darkLinePaint)

                        // Các vạch đá cuộn ngang tạo cảm giác tốc độ mặt đất
                        val stonePaint = Paint().apply {
                            color = android.graphics.Color.argb(120, 245, 235, 220)
                            strokeWidth = 4f
                            strokeCap = Paint.Cap.ROUND
                        }
                        val step = 140f
                        var startX = -engine.groundOffset
                        while (startX < w + step) {
                            canvas.drawLine(startX, gy + 22f, startX + 35f, gy + 22f, stonePaint)
                            canvas.drawLine(startX + 60f, gy + 38f, startX + 85f, gy + 38f, stonePaint)
                            startX += step
                        }

                        // 3. Hiệu ứng vệt sáng dưới chân khi nhảy cấp 2 hoặc cấp 3 (Air Jump Burst)
                        if (!engine.isGrounded && engine.currentJumpLevel >= 2) {
                            val auraPaint = Paint().apply {
                                color = if (engine.currentJumpLevel == 3) {
                                    android.graphics.Color.argb(160, 255, 215, 0) // Vàng kim rực rỡ cấp 3
                                } else {
                                    android.graphics.Color.argb(140, 100, 220, 255) // Lam ngọc cấp 2
                                }
                                style = Paint.Style.STROKE
                                strokeWidth = 5f
                            }
                            canvas.drawCircle(
                                engine.playerX + engine.playerWidth * 0.5f,
                                engine.playerY + engine.playerHeight,
                                32f,
                                auraPaint
                            )
                        }

                        // 4. Vẽ nhân vật Lính áo hồng (Player)
                        val playerBmp = when {
                            engine.isSliding -> playerSlideBitmap
                            !engine.isGrounded -> {
                                if (engine.playerVY < 0f) playerJumpUpBitmap else playerJumpDownBitmap
                            }
                            else -> playerRunBitmaps[engine.playerRunFrame % playerRunBitmaps.size]
                        }

                        val pDst = android.graphics.RectF(
                            engine.playerX,
                            engine.playerY,
                            engine.playerX + engine.playerWidth,
                            engine.playerY + engine.playerHeight
                        )
                        canvas.drawBitmap(playerBmp, null, pDst, paint)

                        // 5. Vẽ các loại Hổ mập với kích thước & hoạt ảnh tương ứng
                        for (obs in engine.obstacles) {
                            val tigerBmp = when (obs.type) {
                                TigerType.YELLOW -> yellowTigerBitmaps[obs.animFrame % yellowTigerBitmaps.size]
                                TigerType.WHITE -> whiteTigerBitmaps[obs.animFrame % whiteTigerBitmaps.size]
                                TigerType.BLUE -> blueTigerBitmaps[obs.animFrame % blueTigerBitmaps.size]
                            }
                            val oDst = android.graphics.RectF(
                                obs.x,
                                obs.y,
                                obs.x + obs.width,
                                obs.y + obs.height
                            )
                            canvas.drawBitmap(tigerBmp, null, oDst, paint)
                        }
                    }
                }

                // 6. Huy hiệu hiển thị cấp độ nhảy hiện tại khi đang trên không
                if (!engine.isGrounded && engine.currentJumpLevel > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (engine.currentJumpLevel == 3) FlagRed else Color(0xFFD97706)
                            )
                            .border(1.5.dp, Color(0xFFFEF08A), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (engine.currentJumpLevel) {
                                1 -> "NHẢY CẤP 1"
                                2 -> "⚡ NHẢY CẤP 2 (CAO HƠN)"
                                else -> "🔥 ĐẠI PHI THÂN CẤP 3!"
                            },
                            fontFamily = OngDoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // 7. Các nút điều khiển cảm ứng nhanh ở 2 góc dưới màn hình
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút TRƯỢT (Slide)
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(Color(0xF5FFFBEB))
                            .border(2.dp, Color(0xFFD97706), CircleShape)
                            .clickable {
                                engine.setSlide(true)
                                audioManager.playClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⬇", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            Text(
                                "TRƯỢT",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    // Nút NHẢY (Hỗ trợ nhảy 3 mức độ, hiển thị số lượt nhảy còn lại)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF08A))
                            .border(2.5.dp, FlagRed, CircleShape)
                            .clickable {
                                triggerJump()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⬆", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                            Text(
                                "NHẢY",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = FlagRed
                            )
                            // 3 Dấu chấm biểu thị 3 mức nhảy
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                repeat(3) { i ->
                                    val isAvailable = i < engine.jumpsRemaining
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isAvailable) FlagRed else Color.Gray.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Hộp thoại Game Over
                if (isGameOver) {
                    AlertDialog(
                        onDismissRequest = { },
                        confirmButton = {
                            Button(
                                onClick = {
                                    audioManager.playClick()
                                    restartGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FlagRed),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    "CHƠI LẠI",
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    audioManager.playClick()
                                    onBack()
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    "QUAY VỀ",
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "BỊ HỔ VỒ RỒI! 🐯",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = FlagRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFEF08A))
                                        .border(1.dp, Color(0xFFD97706), RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Quãng đường đạt được",
                                            fontFamily = OngDoFontFamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFF78350F)
                                        )
                                        Text(
                                            text = "$scoreDisplay m",
                                            fontFamily = OngDoFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 28.sp,
                                            color = FlagRed
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = "+$coinsEarned 🪙 Tiền vàng",
                                        fontFamily = OngDoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "+$xpEarned XP",
                                        fontFamily = OngDoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        },
                        containerColor = Color(0xFDF8EC),
                        modifier = Modifier.shadow(16.dp, RoundedCornerShape(24.dp))
                    )
                }
            }
        }
    }
}
