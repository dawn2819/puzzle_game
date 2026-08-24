package com.example.puzzlegame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    var alpha: Float = 1.0f
)

@Composable
fun ConfettiEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val colors = remember {
        listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFFEC4899), // Pink
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6)  // Purple
        )
    }

    // Sinh hạt pháo hoa
    LaunchedEffect(isActive) {
        if (isActive) {
            particles.clear()
            repeat(100) {
                particles.add(
                    ConfettiParticle(
                        x = Random.nextFloat(), // Vị trí ngang ngẫu nhiên
                        y = -0.05f - Random.nextFloat() * 0.2f, // Xuất phát rải rác trên mép màn hình
                        vx = (Random.nextFloat() - 0.5f) * 0.008f,
                        vy = Random.nextFloat() * 0.012f + 0.006f,
                        color = colors.random(),
                        size = Random.nextFloat() * 14f + 8f,
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = (Random.nextFloat() - 0.5f) * 8f
                    )
                )
            }
        }
    }

    if (particles.isNotEmpty()) {
        // Tốc độ cập nhật 60fps sử dụng delay
        LaunchedEffect(Unit) {
            while (isActive && particles.isNotEmpty()) {
                delay(16)
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.rotation += p.rotationSpeed
                    p.alpha = maxOf(0f, 1.0f - (p.y * 0.85f))
                }
                particles.removeAll { p -> p.y > 1.1f || p.alpha <= 0f }
            }
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                val px = p.x * width
                val py = p.y * height
                val pSize = p.size

                rotate(p.rotation, Offset(px + pSize / 2, py + pSize / 2)) {
                    drawRect(
                        color = p.color.copy(alpha = p.alpha),
                        topLeft = Offset(px, py),
                        size = androidx.compose.ui.geometry.Size(pSize, pSize * 0.5f)
                    )
                }
            }
        }
    }
}
