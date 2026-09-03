package com.example.puzzlegame.engine

import kotlin.math.sin
import kotlin.random.Random

enum class TigerType {
    YELLOW, // Hổ vàng (8763..8766): Kích thước chuẩn
    WHITE,  // Bạch hổ (8771..8775): Kích thước lớn, đồ sộ
    BLUE    // Lam hổ (8779..8783): Kích thước nhỏ, nhanh nhẹn
}

data class DinoObstacle(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: TigerType,
    var animFrame: Int = 0,
    var animTimer: Float = 0f,
    val speedFactor: Float = 1.0f
)

data class SkyFlyer(
    var x: Float,
    var y: Float,
    val speed: Float,
    val type: Int, // 0: Vàng (8768), 1: Trắng (8776), 2: Xanh (8784)
    var angle: Float = 0f,
    val size: Float = 88f
)

class EngineDinoRunner {
    var isPlaying: Boolean = false
    var isGameOver: Boolean = false
    var isPaused: Boolean = false

    var score: Float = 0f
    var highScore: Int = 0
    var coinsEarned: Int = 0

    var groundY: Float = 0f
    var screenWidth: Float = 1080f
    var screenHeight: Float = 2400f

    // Nhân vật
    val playerX: Float = 140f
    var playerY: Float = 0f
    var playerVY: Float = 0f
    var isGrounded: Boolean = true
    var isSliding: Boolean = false
    var slideTimer: Float = 0f

    // 3 Mức nhảy (Số lần nhảy cao hơn tùy số lượt bấm liên tiếp trên không)
    var jumpsRemaining: Int = 3
    var currentJumpLevel: Int = 0 // 0: Mặt đất, 1: Nhảy 1, 2: Nhảy 2, 3: Nhảy 3

    var playerRunFrame: Int = 0
    var playerRunTimer: Float = 0f

    // Kích thước chuẩn của nhân vật
    var playerWidth: Float = 120f
    var playerHeight: Float = 160f

    // Vật lý
    val gravity: Float = 1.35f

    // Tốc độ game
    var baseSpeed: Float = 9.5f
    val currentSpeed: Float
        get() = baseSpeed + (score / 350f).coerceAtMost(10f)

    var groundOffset: Float = 0f

    // Quản lý chướng ngại vật (Hổ các loại)
    val obstacles = mutableListOf<DinoObstacle>()
    private var spawnCooldown: Float = 0f

    // Quản lý các vật thể bay trên trời (8768, 8776, 8784)
    val skyFlyers = mutableListOf<SkyFlyer>()
    private var skySpawnCooldown: Float = 0f

    fun startNewGame(highScore: Int = 0) {
        this.highScore = highScore
        score = 0f
        coinsEarned = 0
        isPlaying = true
        isGameOver = false
        isPaused = false

        playerY = groundY - playerHeight
        playerVY = 0f
        isGrounded = true
        isSliding = false
        slideTimer = 0f
        jumpsRemaining = 3
        currentJumpLevel = 0
        playerRunFrame = 0
        playerRunTimer = 0f

        obstacles.clear()
        skyFlyers.clear()
        spawnCooldown = 80f // Đợi 1 đoạn ngắn rồi mới thả hổ đầu tiên
        skySpawnCooldown = 30f
        groundOffset = 0f

        // Khởi tạo sẵn 2 vật thể bay trên bầu trời
        skyFlyers.add(SkyFlyer(screenWidth * 0.4f, 120f, 1.6f, 0, Random.nextFloat() * 6f))
        skyFlyers.add(SkyFlyer(screenWidth * 0.85f, 210f, 2.2f, 1, Random.nextFloat() * 6f))
    }

    /**
     * Nhảy 3 cấp độ:
     * - Cấp 1 (Chạm lần 1): Nhảy chuẩn (-21f)
     * - Cấp 2 (Chạm lần 2 trên không): Bật cao hơn (-24.5f)
     * - Cấp 3 (Chạm lần 3 trên không): Đại phi thân (-27.5f)
     * Trả về cấp độ nhảy vừa kích hoạt (1, 2, 3) hoặc 0 nếu không thể nhảy
     */
    fun jump(): Int {
        if (!isPlaying || isGameOver || isPaused) return 0
        if (jumpsRemaining > 0) {
            when (jumpsRemaining) {
                3 -> {
                    playerVY = -21f
                    currentJumpLevel = 1
                }
                2 -> {
                    playerVY = -24.5f
                    currentJumpLevel = 2
                }
                1 -> {
                    playerVY = -27.5f
                    currentJumpLevel = 3
                }
            }
            jumpsRemaining--
            isGrounded = false
            isSliding = false
            return currentJumpLevel
        }
        return 0
    }

    fun setSlide(slide: Boolean) {
        if (!isPlaying || isGameOver || isPaused) return
        if (isGrounded) {
            isSliding = slide
            if (slide) {
                slideTimer = 35f // Tự động trượt trong 35 frames (~0.6 giây) nếu bấm 1 chạm
            }
        }
    }

    fun update(): Boolean {
        if (!isPlaying || isGameOver || isPaused) return false

        // 1. Cập nhật quãng đường & điểm số
        score += 0.25f
        groundOffset = (groundOffset + currentSpeed) % 200f

        // 2. Cập nhật nhân vật
        if (!isGrounded) {
            playerVY += gravity
            playerY += playerVY

            // Chạm đất
            if (playerY >= groundY - playerHeight) {
                playerY = groundY - playerHeight
                playerVY = 0f
                isGrounded = true
                jumpsRemaining = 3 // Hồi phục 3 lượt nhảy khi chạm đất
                currentJumpLevel = 0
            }
        } else {
            // Chạy bộ trên mặt đất
            if (isSliding) {
                slideTimer -= 1f
                if (slideTimer <= 0f) {
                    isSliding = false
                }
            } else {
                playerRunTimer += 1f
                if (playerRunTimer >= 5f) { // Đổi frame mỗi 5 ticks (~80ms)
                    playerRunTimer = 0f
                    playerRunFrame = (playerRunFrame + 1) % 5
                }
            }
        }

        // 3. Cập nhật các vật thể bay trên trời (8768, 8776, 8784)
        skySpawnCooldown -= 1f
        if (skySpawnCooldown <= 0f && skyFlyers.size < 4) {
            val type = Random.nextInt(3)
            val speed = 1.4f + Random.nextFloat() * 1.6f
            val y = Random.nextFloat() * 220f + 60f
            skyFlyers.add(SkyFlyer(screenWidth + 100f, y, speed, type, Random.nextFloat() * 6f))
            skySpawnCooldown = 150f + Random.nextFloat() * 180f
        }

        val skyIter = skyFlyers.iterator()
        while (skyIter.hasNext()) {
            val flyer = skyIter.next()
            flyer.x -= flyer.speed
            flyer.angle += 0.04f
            flyer.y += sin(flyer.angle.toDouble()).toFloat() * 0.7f

            if (flyer.x < -120f) {
                skyIter.remove()
            }
        }

        // 4. Sinh chướng ngại vật ngẫu nhiên các loại hổ (Kích thước khác nhau)
        spawnCooldown -= 1f
        if (spawnCooldown <= 0f) {
            val rand = Random.nextFloat()
            val (tigerType, w, h, speedMod) = when {
                rand < 0.40f -> Quad(TigerType.YELLOW, 210f, 120f, 1.0f) // Hổ vàng chuẩn
                rand < 0.75f -> Quad(TigerType.WHITE, 260f, 145f, 0.92f) // Bạch hổ khổng lồ, cao to
                else -> Quad(TigerType.BLUE, 175f, 98f, 1.18f)           // Lam hổ nhỏ nhắn, lao nhanh
            }

            obstacles.add(
                DinoObstacle(
                    x = screenWidth + 60f,
                    y = groundY - h + 6f, // Chân hổ tiếp giáp mặt đất
                    width = w,
                    height = h,
                    type = tigerType,
                    speedFactor = speedMod
                )
            )

            // Khoảng cách ngẫu nhiên an toàn cho đợt hổ tiếp theo
            val minCooldown = (85f - (currentSpeed * 2.2f)).coerceAtLeast(50f)
            val maxCooldown = minCooldown + 65f + Random.nextFloat() * 55f
            spawnCooldown = Random.nextFloat() * (maxCooldown - minCooldown) + minCooldown
        }

        // 5. Cập nhật vị trí & hoạt ảnh các chú hổ
        val iter = obstacles.iterator()
        while (iter.hasNext()) {
            val obs = iter.next()
            obs.x -= currentSpeed * obs.speedFactor

            val maxFrames = if (obs.type == TigerType.YELLOW) 4 else 5
            obs.animTimer += 1f
            if (obs.animTimer >= 6f) {
                obs.animTimer = 0f
                obs.animFrame = (obs.animFrame + 1) % maxFrames
            }

            // Xóa nếu hổ chạy ra khỏi mép trái màn hình
            if (obs.x + obs.width < -100f) {
                iter.remove()
            }
        }

        // 6. Kiểm tra va chạm (Collision Detection)
        val pLeft = playerX + 25f
        val pRight = playerX + playerWidth - 25f
        val pTop = if (isSliding) playerY + playerHeight * 0.52f else playerY + 15f
        val pBottom = playerY + playerHeight - 5f

        for (obs in obstacles) {
            val oLeft = obs.x + 35f
            val oRight = obs.x + obs.width - 35f
            val oTop = obs.y + 25f
            val oBottom = obs.y + obs.height - 5f

            val overlapX = pLeft < oRight && pRight > oLeft
            val overlapY = pTop < oBottom && pBottom > oTop

            if (overlapX && overlapY) {
                // Va chạm! Game Over
                isGameOver = true
                isPlaying = false
                coinsEarned = (score / 15f).toInt()
                if (score.toInt() > highScore) {
                    highScore = score.toInt()
                }
                return true
            }
        }

        return false
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
