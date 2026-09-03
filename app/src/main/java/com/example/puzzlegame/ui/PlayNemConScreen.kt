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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayNemConScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }
    val coroutineScope = rememberCoroutineScope()

    var level by remember { mutableStateOf(1) }
    var attemptsLeft by remember { mutableStateOf(3) }
    var windSpeed by remember { mutableStateOf(0f) } // Lực gió thổi ngang (-4.0f đến 4.0f)

    // Tọa độ quả Còn (vật ném)
    var ballX by remember { mutableStateOf(100f) }
    var ballY by remember { mutableStateOf(600f) }
    var isBallFlying by remember { mutableStateOf(false) }

    // Tọa độ vòng Tre treo đích ném (còn)
    var ringX by remember { mutableStateOf(400f) }
    var ringY by remember { mutableStateOf(250f) }
    val ringRadius = 45f

    //aim lines
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    var isVictory by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    fun initNewGame() {
        attemptsLeft = 3
        isBallFlying = false
        ballX = 100f
        ballY = 550f
        
        // Vị trí vòng tre di động theo level
        ringX = when (level) {
            1 -> 350f
            2 -> 420f
            3 -> 480f
            4 -> 540f
            else -> 600f
        }
        ringY = when (level) {
            1 -> 300f
            2 -> 220f
            3 -> 180f
            4 -> 150f
            else -> 120f
        }

        // Tạo lực gió ngẫu nhiên dựa trên level
        windSpeed = when (level) {
            1 -> 0f
            2 -> Random.nextFloat() * 4f - 2f
            3 -> Random.nextFloat() * 8f - 4f
            4 -> Random.nextFloat() * 12f - 6f
            else -> Random.nextFloat() * 18f - 9f
        }
        isVictory = false
        isGameOver = false
    }

    LaunchedEffect(level) {
        initNewGame()
    }

    // Thưởng khi thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "nemcon_win_credited_level_$level"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                prefs.saveScore(winKey, 1)
            }
        }
    }

    // Mô phỏng bay vật lý quả còn
    fun launchBall(vx: Float, vy: Float) {
        if (isBallFlying) return
        isBallFlying = true

        coroutineScope.launch {
            var curVx = vx
            var curVy = vy
            val dt = 0.08f // Time step
            val gravity = 12f // Trọng lực hướng xuống

            while (isBallFlying) {
                // Áp dụng sức gió và trọng lực
                curVx += windSpeed * 0.15f
                curVy += gravity

                ballX += curVx * dt
                ballY += curVy * dt

                // Kiểm tra xem quả còn có lọt qua vòng còn tre treo
                val dx = ballX - ringX
                val dy = ballY - ringY
                val distToRingCenter = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distToRingCenter < ringRadius) {
                    // Trúng mục tiêu!
                    isVictory = true
                    isBallFlying = false
                    audioManager.playClick()
                    break
                }

                // Chạm biên dưới sông/đất
                if (ballY > 800f || ballX > 800f || ballX < -100f) {
                    isBallFlying = false
                    audioManager.playError()
                    attemptsLeft--
                    if (attemptsLeft <= 0) {
                        isGameOver = true
                    } else {
                        // Trả quả còn về điểm xuất phát
                        ballX = 100f
                        ballY = 550f
                    }
                    break
                }

                delay(16) // ~60fps
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("NÉM CÒN DÂN GIAN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
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
                // Chỉ số gió và lượt bắn
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số lượt còn: " + "🎈 ".repeat(attemptsLeft),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lực gió: " + String.format("%.1f", windSpeed) + if (windSpeed > 0) " ➔ Đông" else " ⬅ Tây",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (Math.abs(windSpeed) > 2f) FlagRed else BambooGreen
                    )
                }

                // --- SÂN NÉM CÒN VẬT LÝ CANVAS COMPONENT ---
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal)
                        .border(3.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .pointerInput(isBallFlying, isGameOver, isVictory) {
                            if (isBallFlying || isGameOver || isVictory) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    dragStart = offset
                                    dragCurrent = offset
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragCurrent = dragCurrent?.plus(dragAmount)
                                },
                                onDragEnd = {
                                    val start = dragStart
                                    val current = dragCurrent
                                    if (start != null && current != null) {
                                        val vx = (start.x - current.x) * 0.4f
                                        val vy = (start.y - current.y) * 0.4f
                                        launchBall(vx, vy)
                                    }
                                    dragStart = null
                                    dragCurrent = null
                                }
                            )
                        }
                ) {
                    val w = maxWidth
                    val h = maxHeight
                    val scaleX = w.value / 600f
                    val scaleY = h.value / 700f

                    val poleX = (ringX * scaleX).dp
                    val poleRingY = (ringY * scaleY).dp
                    val drawBallX = (ballX * scaleX).dp
                    val drawBallY = (ballY * scaleY).dp

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height

                        val sX = widthPx / 600f
                        val sY = heightPx / 700f

                        if (!isBallFlying && dragStart != null && dragCurrent != null) {
                            val start = dragStart!!
                            val curr = dragCurrent!!
                            val diffX = start.x - curr.x
                            val diffY = start.y - curr.y

                            var tempX = ballX
                            var tempY = ballY
                            var tempVx = diffX * 0.4f
                            var tempVy = diffY * 0.4f
                            for (i in 0 until 12) {
                                val nextTx = tempX + tempVx * 0.08f
                                val nextTy = tempY + tempVy * 0.08f
                                tempVx += windSpeed * 0.15f
                                tempVy += 12f

                                drawLine(
                                    color = FlagRed.copy(alpha = 0.6f),
                                    start = Offset(tempX * sX, tempY * sY),
                                    end = Offset(nextTx * sX, nextTy * sY),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                tempX = nextTx
                                tempY = nextTy
                            }
                        }
                    }

                    // 1. Cột tre
                    Image(
                        painter = painterResource(id = R.drawable.con_bamboo_pole),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .offset(x = poleX - 10.dp, y = poleRingY)
                            .width(20.dp)
                            .fillMaxHeight()
                    )

                    // 2. Vòng tre ném còn
                    Image(
                        painter = painterResource(id = R.drawable.con_bamboo_ring),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .offset(x = poleX - 36.dp, y = poleRingY - 36.dp)
                            .size(72.dp)
                    )

                    // 3. Quả Còn ngũ sắc
                    Image(
                        painter = painterResource(id = R.drawable.con_ball),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .offset(x = drawBallX - 20.dp, y = drawBallY - 20.dp)
                            .size(40.dp)
                    )
                }

                // Hướng dẫn vuốt kéo bắn quả còn
                Text(
                    text = "Chạm kéo quả còn lùi lại để ngắm lực, thả tay để ném xuyên qua vòng tre.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Hộp thoại bắn hết lượt (Game Over)
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
                        "💥 HẾT LƯỢT NÉM CÒN!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Text(
                        "Các quả còn đã rơi hết xuống sân mà chưa lọt vòng tre. Hãy chú ý lực cản của sức gió để điều chỉnh lực kéo bắn nhé!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại bắn trúng vòng tre (Victory)
        if (isVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            if (level < 5) {
                                level++
                            } else {
                                level = 1
                            }
                            initNewGame()
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
                        "🎉 NÉM CÒN LỌT LƯỚI!",
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
                        Text("Quả còn bay tuyệt đẹp lọt thẳng qua tâm vòng tre di sản!", fontSize = 14.sp, textAlign = TextAlign.Center)
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
