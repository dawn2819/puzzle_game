package com.example.puzzlegame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.puzzlegame.theme.*
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBgStart
    val bgColor = if (isDark) GlassDark else GlassLight
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .padding(16.dp)
    ) {
        content()
    }
}

class PhysicsParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val color: Color
) {
    fun update(width: Float, height: Float) {
        x += vx
        y += vy

        // Ma sát nhẹ để giảm tốc khi không có tương tác
        vx *= 0.985f
        vy *= 0.985f

        // Giới hạn tốc độ tối đa
        val speed = sqrt(vx * vx + vy * vy)
        val maxSpeed = 15f
        if (speed > maxSpeed) {
            vx = (vx / speed) * maxSpeed
            vy = (vy / speed) * maxSpeed
        }

        // Va chạm biên màn hình và nảy lại
        if (x - radius < 0) {
            x = radius
            vx = -vx * 0.85f
        } else if (x + radius > width) {
            x = width - radius
            vx = -vx * 0.85f
        }

        if (y - radius < 0) {
            y = radius
            vy = -vy * 0.85f
        } else if (y + radius > height) {
            y = height - radius
            vy = -vy * 0.85f
        }
    }
}

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    drawableId: Int? = com.example.puzzlegame.R.drawable.bg_game_general,
    bgDimAlpha: Float = 0.0f,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBgStart
    val baseBgColor = if (isDark) DarkBgEnd else ParchmentBg

    val infiniteTransition = rememberInfiniteTransition(label = "bg_flow")

    // Di chuyển chậm hai cụm sáng neon tròn
    val blob1X by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val blob1Y by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )

    val blob2X by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val blob2Y by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    // --- HỆ THỐNG VẬT LÝ VÀ VA CHẠM HẠT BONG BÓNG ---
    var canvasWidth by remember { mutableStateOf(1080f) }
    var canvasHeight by remember { mutableStateOf(2400f) }

    var touchX by remember { mutableStateOf(-1000f) }
    var touchY by remember { mutableStateOf(-1000f) }
    var isTouching by remember { mutableStateOf(false) }

    val particles = remember { mutableStateListOf<PhysicsParticle>() }

    // Khởi tạo các bong bóng ngẫu nhiên
    LaunchedEffect(Unit) {
        if (particles.isEmpty()) {
            val colors = listOf(PrimaryNeon, SecondaryNeon, TertiaryNeon)
            repeat(14) {
                particles.add(
                    PhysicsParticle(
                        x = Random.nextFloat() * 800f + 100f,
                        y = Random.nextFloat() * 1600f + 200f,
                        vx = (Random.nextFloat() - 0.5f) * 8f,
                        vy = (Random.nextFloat() - 0.5f) * 8f,
                        radius = Random.nextFloat() * 35f + 35f, // Bán kính từ 35f đến 70f
                        color = colors.random()
                    )
                )
            }
        }
    }

    // Vòng lặp cập nhật vật lý theo đồng hồ Animation
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                // 1. Cập nhật vị trí và lực đẩy từ ngón tay
                for (i in 0 until particles.size) {
                    val p = particles[i]
                    p.update(canvasWidth, canvasHeight)

                    if (isTouching) {
                        val dx = p.x - touchX
                        val dy = p.y - touchY
                        val dist = sqrt(dx * dx + dy * dy)
                        // Bán kính ảnh hưởng đẩy ngón tay là 400px
                        if (dist < 400f && dist > 0f) {
                            val force = (400f - dist) / 400f
                            p.vx += (dx / dist) * force * 5.0f
                            p.vy += (dy / dist) * force * 5.0f
                        }
                    }
                }

                // 2. Xử lý va chạm đàn hồi hoàn toàn giữa các hạt bong bóng
                for (i in 0 until particles.size) {
                    for (j in i + 1 until particles.size) {
                        val p1 = particles[i]
                        val p2 = particles[j]
                        val dx = p2.x - p1.x
                        val dy = p2.y - p1.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val minDist = p1.radius + p2.radius
                        if (dist < minDist && dist > 0f) {
                            // Tách hai hạt ra để tránh lồng ghép vào nhau
                            val overlap = minDist - dist
                            val nx = dx / dist
                            val ny = dy / dist
                            p1.x -= nx * overlap * 0.5f
                            p1.y -= ny * overlap * 0.5f
                            p2.x += nx * overlap * 0.5f
                            p2.y += ny * overlap * 0.5f

                            // Vận tốc tiếp tuyến và pháp tuyến
                            val tx = -ny
                            val ty = nx

                            // Thành phần vận tốc pháp tuyến (dọc theo đường nối tâm)
                            val dpNorm1 = p1.vx * nx + p1.vy * ny
                            val dpNorm2 = p2.vx * nx + p2.vy * ny

                            // Thành phần vận tốc tiếp tuyến
                            val dpTan1 = p1.vx * tx + p1.vy * ty
                            val dpTan2 = p2.vx * tx + p2.vy * ty

                            // Trao đổi vận tốc pháp tuyến (bảo toàn động lượng của 2 vật cùng khối lượng)
                            val newNorm1 = dpNorm2
                            val newNorm2 = dpNorm1

                            // Cập nhật lại vector vận tốc
                            p1.vx = tx * dpTan1 + nx * newNorm1
                            p1.vy = ty * dpTan1 + ny * newNorm1
                            p2.vx = tx * dpTan2 + nx * newNorm2
                            p2.vy = ty * dpTan2 + ny * newNorm2
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
            .onSizeChanged { size ->
                canvasWidth = size.width.toFloat()
                canvasHeight = size.height.toFloat()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val changes = event.changes
                        val anyDown = changes.any { it.pressed }
                        isTouching = anyDown
                        if (anyDown) {
                            val firstActive = changes.firstOrNull { it.pressed }
                            if (firstActive != null) {
                                touchX = firstActive.position.x
                                touchY = firstActive.position.y
                            }
                        } else {
                            touchX = -1000f
                            touchY = -1000f
                        }
                    }
                }
            }
    ) {
        // Vẽ hình nền tùy chỉnh nếu có
        drawableId?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (bgDimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = bgDimAlpha))
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val minSize = minOf(width, height)

            // Chỉ vẽ vầng sáng neon nếu KHÔNG có ảnh nền tùy chỉnh
            if (drawableId == null) {
                // Vầng sáng màu Primary Neon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryNeon.copy(alpha = if (isDark) 0.15f else 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * blob1X, height * blob1Y),
                        radius = minSize * 0.6f
                    ),
                    center = Offset(width * blob1X, height * blob1Y),
                    radius = minSize * 0.6f
                )

                // Vầng sáng màu Tertiary Neon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TertiaryNeon.copy(alpha = if (isDark) 0.15f else 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(width * blob2X, height * blob2Y),
                        radius = minSize * 0.7f
                    ),
                    center = Offset(width * blob2X, height * blob2Y),
                    radius = minSize * 0.7f
                )
            }

            // Vẽ các bong bóng tương tác vật lý va chạm
            particles.forEach { p ->
                // Vẽ lòng bong bóng mờ ảo
                drawCircle(
                    color = p.color.copy(alpha = if (isDark) 0.08f else 0.05f),
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
                // Vẽ viền bong bóng mạ neon tinh xảo
                drawCircle(
                    color = p.color.copy(alpha = if (isDark) 0.22f else 0.15f),
                    radius = p.radius,
                    center = Offset(p.x, p.y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
        }

        content()
    }
}



