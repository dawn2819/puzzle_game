package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.R
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import kotlinx.coroutines.delay

/**
 * Màn hình Splash Screen mở đầu trò chơi:
 * - Sử dụng biểu tượng Trống Đồng Đông Sơn xoay tròn liên tục thay cho loading truyền thống
 * - Bầu trời mây trôi chân thực lướt qua phía sau (Slide 20)
 * - Thanh tiến trình nạp tài nguyên và thông điệp chào đón
 */
@Composable
fun SplashScreen(
    onFinishLoading: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Đang khởi tạo không gian Hồn Việt...") }
    var isLoaded by remember { mutableStateOf(false) }

    // Hiệu ứng nạp tiến trình mượt mà
    LaunchedEffect(Unit) {
        val steps = listOf(
            0.20f to "Nạp tài nguyên đồ họa dân gian...",
            0.50f to "Khởi tạo hệ thống giải đố Hồn Việt...",
            0.80f to "Tải bầu trời mây trôi chân thực (Slide 20)...",
            1.00f to "Sẵn sàng khai hội!"
        )

        for ((target, text) in steps) {
            statusText = text
            while (progress < target) {
                progress = (progress + 0.025f).coerceAtMost(target)
                delay(30)
            }
            delay(150)
        }
        isLoaded = true
        delay(400)
        onFinishLoading()
    }

    // Hiệu ứng xoay tròn liên tục của Trống Đồng Đông Sơn (1.8s một vòng quay mượt mà)
    val infiniteTransition = rememberInfiniteTransition(label = "splash_drum")
    val drumRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drum_angle"
    )

    // Hiệu ứng nhịp thở và ánh hào quang vàng kim
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    PremiumBackground(drawableId = R.drawable.bg_main_menu, bgDimAlpha = 0.35f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. LOGO TRỐNG ĐỒNG ĐÔNG SƠN XOAY TRÒN (THAY THẾ LOADING)
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .size(165.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), StarGold)
                            )
                        )
                        .border(4.dp, StarGold.copy(alpha = glowPulse), CircleShape)
                        .shadow(24.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dong_son_drum),
                        contentDescription = "Logo Trống Đồng Đông Sơn",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .rotate(drumRotation) // Xoay tròn liên tục
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. TIÊU ĐỀ THƯ PHÁP HỒN VIỆT
                Text(
                    text = "HỒN VIỆT",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = OngDoFontFamily,
                    color = FlagRed,
                    letterSpacing = 4.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = FlagRed.copy(alpha = glowPulse),
                            offset = Offset(0f, 0f),
                            blurRadius = 25f
                        )
                    )
                )

                Text(
                    text = "VIETNAMESE PUZZLE UNIVERSE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EarthyBrown.copy(alpha = 0.85f),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 3. THẺ GIỚI THIỆU
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🏯 TRÒ CHƠI TRÍ TUỆ DÂN GIAN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = OngDoFontFamily,
                            color = BambooGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Khám phá tinh hoa văn hóa truyền thống Việt Nam qua các trò chơi trí tuệ kết hợp đồ họa mây trôi chuyển động tự nhiên.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // 4. THANH TIẾN TRÌNH TẢI & THÔNG BÁO TRẠNG THÁI
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EarthyBrown
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlagRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = FlagRed,
                        trackColor = OutlineBrown.copy(alpha = 0.25f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút vào game nếu nạp xong
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300))
                ) {
                    Button(
                        onClick = onFinishLoading,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlagRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "VÀO SẢNH CHÍNH  ➔",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = OngDoFontFamily,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
