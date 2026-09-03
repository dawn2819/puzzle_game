package com.example.puzzlegame.ui.components

import android.graphics.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import kotlin.random.Random

/**
 * Cấu trúc đại diện cho một đám mây chuyển động
 * Chuẩn Slide 20 - Module 4.1:
 * - x -= speed mỗi khung hình
 * - Khi trôi hoàn toàn khỏi biên trái (x + width < -60f), tự động tái sinh ở biên phải
 */
class CloudItem(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val speed: Float,
    val alpha: Float,
    val bitmapIndex: Int,
    val minY: Float = 60f,
    val maxY: Float = 380f
) {
    fun update(screenWidth: Float) {
        x -= speed
        if (x + width < -60f) {
            // Tái sinh ở mép phải màn hình với độ trễ ngẫu nhiên
            x = screenWidth + Random.nextFloat() * 140f
            // Đổi độ cao ngẫu nhiên trong khoảng tầng mây cho phép
            y = Random.nextFloat() * (maxY - minY) + minY
        }
    }
}

/**
 * Thành phần vẽ mây trôi bồng bềnh chân thực đa tầng (Soft Volumetric Mist Clouds)
 * - Tầng mây tựa Logo dày đặc, trắng sáng tự nhiên: Đi qua ngay dưới logo để làm nổi bật nét cọ thư pháp đỏ/đen của HỒN VIỆT
 * - 100% Không góc cạnh, không đường viền thẳng (Zero Clipping / Pure Feathered Edges)
 * - Tầng 1: Mây xa trôi chậm, mờ ảo
 * - Tầng 2: Mây bồng bềnh trắng sáng trực diện khu vực Logo
 * - Tầng 3: Mây tơ sương khói lượn sóng
 */
@Composable
fun RealisticClouds(
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1.0f
) {
    var screenWidth by remember { mutableStateOf(1080f) }
    var screenHeight by remember { mutableStateOf(2400f) }

    // Sinh trước các Bitmap mây thể tích mềm mại với lề an toàn tuyệt đối (Không bao giờ chạm mép)
    val cloudBitmaps = remember {
        listOf(
            createSoftFluffyCloudBitmap(width = 750, height = 380, seed = 101), // Mây tích lớn bồng bềnh, trắng sáng
            createSoftFluffyCloudBitmap(width = 620, height = 320, seed = 202), // Mây tích vừa
            createWispyMistBitmap(width = 680, height = 300, seed = 303),       // Mây tơ mềm mại
            createSoftFluffyCloudBitmap(width = 500, height = 260, seed = 404)  // Mây nhỏ tầm xa
        )
    }

    val clouds = remember { mutableStateListOf<CloudItem>() }

    LaunchedEffect(screenWidth) {
        if (screenWidth > 100f && clouds.isEmpty()) {
            val random = Random(88)

            // 1. Tầng mây xa (4 cụm mây nền trôi chậm trên bầu trời)
            repeat(4) { i ->
                val w = 380f * (0.85f + random.nextFloat() * 0.35f)
                val h = w * 0.5f
                clouds.add(
                    CloudItem(
                        x = (screenWidth / 3.5f) * i + random.nextFloat() * 80f - 40f,
                        y = random.nextFloat() * 120f + 10f,
                        width = w,
                        height = h,
                        speed = (0.28f + random.nextFloat() * 0.22f) * speedMultiplier,
                        alpha = 0.35f + random.nextFloat() * 0.15f,
                        bitmapIndex = if (random.nextBoolean()) 2 else 3,
                        minY = 10f,
                        maxY = 160f
                    )
                )
            }

            // 2. Tầng MÂY TỰA LOGO (5 cụm mây lớn, trắng sáng rực rỡ, liên tục lướt qua ngay dưới Logo Hồn Việt)
            repeat(5) { i ->
                val w = 580f * (0.9f + random.nextFloat() * 0.35f)
                val h = w * 0.52f
                clouds.add(
                    CloudItem(
                        x = (screenWidth / 4f) * i + random.nextFloat() * 120f - 60f,
                        y = random.nextFloat() * 260f + 70f, // Phủ trọn vẹn tầng Logo (Y từ 70px đến 330px)
                        width = w,
                        height = h,
                        speed = (0.55f + random.nextFloat() * 0.35f) * speedMultiplier,
                        alpha = 0.68f + random.nextFloat() * 0.20f, // Độ sáng cao, tạo thảm mây trắng tinh khôi làm nổi chữ
                        bitmapIndex = if (random.nextBoolean()) 0 else 1,
                        minY = 70f,
                        maxY = 360f
                    )
                )
            }

            // 3. Tầng mây tơ sương mù lướt êm dịu (4 cụm mây mỏng bổ trợ)
            repeat(4) { i ->
                val w = 620f * (0.85f + random.nextFloat() * 0.3f)
                val h = w * 0.45f
                clouds.add(
                    CloudItem(
                        x = (screenWidth / 3f) * i + random.nextFloat() * 100f,
                        y = random.nextFloat() * 220f + 140f,
                        width = w,
                        height = h,
                        speed = (0.75f + random.nextFloat() * 0.45f) * speedMultiplier,
                        alpha = 0.45f + random.nextFloat() * 0.18f,
                        bitmapIndex = 2,
                        minY = 120f,
                        maxY = 400f
                    )
                )
            }
        }
    }

    // Animation loop mượt mà 60 FPS
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                for (cloud in clouds) {
                    cloud.update(screenWidth)
                }
                tick = frameTime
            }
        }
    }

    val cloudPaint = remember {
        Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        if (tick >= 0 && clouds.isNotEmpty()) {
            val canvas = drawContext.canvas.nativeCanvas

            // Vẽ từng đám mây
            for (cloud in clouds) {
                val bmp = cloudBitmaps[cloud.bitmapIndex]
                cloudPaint.alpha = (cloud.alpha * 255).toInt().coerceIn(0, 255)

                val destRect = RectF(
                    cloud.x,
                    cloud.y,
                    cloud.x + cloud.width,
                    cloud.y + cloud.height
                )
                canvas.drawBitmap(bmp, null, destRect, cloudPaint)
            }
        }
    }
}

/**
 * Thuật toán tạo đám mây bồng bềnh, trắng sáng, hoàn toàn không có góc cạnh hay đường cắt:
 * - Bitmap kích thước rộng (width x height) với lề an toàn tối thiểu 80px mọi hướng
 * - Mọi khối phồng (puff) sử dụng RadialGradient trắng sáng với độ chuyển tiếp mềm mại
 * - Tạo độ dày khối ở giữa để làm nổi bật nét chữ cọ đen/đỏ của Logo
 */
fun createSoftFluffyCloudBitmap(width: Int, height: Int, seed: Int): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val random = Random(seed)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val centerX = width * 0.5f
    val centerY = height * 0.56f

    // 1. Lớp đáy mây (Đổ bóng lam nhẹ, nâng đỡ khối mây)
    for (i in 0 until 14) {
        val relX = (random.nextFloat() - 0.5f) * 1.35f
        val px = centerX + relX * (width * 0.28f)
        val py = centerY + (random.nextFloat() - 0.2f) * (height * 0.14f)
        val r = width * (0.13f + random.nextFloat() * 0.08f)

        paint.shader = RadialGradient(
            px, py, r,
            intArrayOf(
                Color.argb(70, 235, 242, 252),
                Color.argb(38, 240, 245, 255),
                Color.argb(12, 245, 248, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.45f, 0.75f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(px, py, r, paint)
    }

    // 2. Thân mây chính bồng bềnh dày khối (Trắng tinh khôi, tạo nền sáng cho logo)
    for (i in 0 until 20) {
        val relX = (random.nextFloat() - 0.5f) * 1.18f
        val px = centerX + relX * (width * 0.25f)
        val py = centerY - (random.nextFloat() * 0.22f) * height - (1.0f - kotlin.math.abs(relX)) * 24f
        val r = width * (0.15f + random.nextFloat() * 0.09f)

        paint.shader = RadialGradient(
            px, py - r * 0.15f, r,
            intArrayOf(
                Color.argb(125, 255, 255, 255),
                Color.argb(75, 253, 254, 255),
                Color.argb(25, 250, 252, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.45f, 0.78f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(px, py, r, paint)
    }

    // 3. Đỉnh mây đón ánh nắng (Ánh sáng trắng ấm rạng rỡ)
    for (i in 0 until 12) {
        val relX = (random.nextFloat() - 0.5f) * 0.85f
        val px = centerX + relX * (width * 0.20f)
        val py = centerY - (height * 0.17f) - (random.nextFloat() * 0.15f) * height
        val r = width * (0.11f + random.nextFloat() * 0.06f)

        paint.shader = RadialGradient(
            px, py - r * 0.2f, r,
            intArrayOf(
                Color.argb(165, 255, 255, 255),
                Color.argb(90, 255, 254, 250),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.52f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(px, py, r, paint)
    }

    return bmp
}

/**
 * Thuật toán sinh mây tơ lượn sóng mềm mại (Wispy Cirrus Mist)
 */
fun createWispyMistBitmap(width: Int, height: Int, seed: Int): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val random = Random(seed)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val centerX = width * 0.5f
    val centerY = height * 0.5f

    for (i in 0 until 16) {
        val relX = (random.nextFloat() - 0.5f) * 1.35f
        val px = centerX + relX * (width * 0.28f)
        val py = centerY + (random.nextFloat() - 0.5f) * (height * 0.32f)
        val rx = width * (0.19f + random.nextFloat() * 0.12f)
        val ry = height * (0.11f + random.nextFloat() * 0.08f)

        paint.shader = RadialGradient(
            px, py, rx,
            intArrayOf(
                Color.argb(100, 255, 255, 255),
                Color.argb(45, 250, 252, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.6f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val rect = RectF(px - rx, py - ry, px + rx, py + ry)
        canvas.drawOval(rect, paint)
    }

    return bmp
}
