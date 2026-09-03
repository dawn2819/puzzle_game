package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.puzzlegame.*
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground

@Composable
fun MainMenuScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var hasContinue by remember { mutableStateOf(prefs.hasContinueGame()) }
    var coins by remember { mutableStateOf(prefs.profileCoins) }
    var level by remember { mutableStateOf(prefs.profileLevel) }
    var xp by remember { mutableStateOf(prefs.profileXp) }
    val xpTarget = level * 1000

    // Cập nhật chỉ số mỗi khi vào lại màn hình chính
    LaunchedEffect(Unit) {
        hasContinue = prefs.hasContinueGame()
        coins = prefs.profileCoins
        level = prefs.profileLevel
        xp = prefs.profileXp
    }

    // Hoạt ảnh xuất hiện
    var showMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showMenu = true
    }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (showMenu) 1f else 0f,
        animationSpec = tween(600),
        label = "entrance_alpha"
    )

    // Hiệu ứng bập bùng nhẹ của tiêu đề
    val infiniteTransition = rememberInfiniteTransition(label = "title_animation")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_glow"
    )

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground(drawableId = R.drawable.bg_main_menu, bgDimAlpha = 0.45f) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(alpha = entranceAlpha),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- LOGO TIÊU ĐỀ HỒN VIỆT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .scale(titleScale),
                contentAlignment = Alignment.Center
            ) {
                // Vầng sương mây trắng ngà đỡ phía sau logo, tôn rõ từng nét cọ đỏ son & mực đen
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(135.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.60f),
                                    Color(0xFFFFFBEB).copy(alpha = 0.38f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_hon_viet),
                        contentDescription = "Hồn Việt Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(118.dp)
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Vietnamese Puzzle Universe",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = OngDoFontFamily,
                        color = Color(0xFF3E1F0A),
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.White.copy(alpha = 0.85f),
                                offset = Offset(0f, 1f),
                                blurRadius = 4f
                            )
                        ),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                }
            }

            // --- PROFILE USER CARD (XP, COINS, LEVEL) ---
            // Thẻ nền kem ngà ấm áp, chống chìm chữ hoàn toàn trên nền cây đa
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                cornerRadius = 18.dp,
                borderWidth = 1.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(FlagRed, CircleShape)
                                    .border(1.5.dp, Color(0xFFFEF08A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$level",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = OngDoFontFamily,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CẤP ĐỘ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OngDoFontFamily,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "Kẻ Sĩ Trí Tuệ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = OngDoFontFamily,
                                    color = FlagRed
                                )
                            }
                        }

                        // Vàng
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFEF08A))
                                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$coins 🪙",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    // XP Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Điểm kinh nghiệm (XP)",
                                fontSize = 13.sp,
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "$xp / $xpTarget XP",
                                fontSize = 13.sp,
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = FlagRed
                            )
                        }
                        LinearProgressIndicator(
                            progress = xp.toFloat() / xpTarget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = FlagRed,
                            trackColor = Color(0xFFE2D5C3)
                        )
                    }
                }
            }

            if (hasContinue) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            audioManager.playClick()
                            val type = prefs.getContinueGameType()
                            val data = prefs.getContinueGameData()
                            if (type != null && data != null) {
                                when (type) {
                                    "2048" -> {
                                        val size = data.substringBefore("|").toInt()
                                        onNavigate(Play2048(size, isRestore = true))
                                    }
                                    "SUDOKU" -> {
                                        val difficulty = data.substringBefore("|")
                                        onNavigate(PlaySudoku(difficulty, isRestore = true))
                                    }
                                    "SOKOBAN" -> {
                                        val levelIndex = data.substringBefore("|").toInt()
                                        onNavigate(PlaySokoban(levelIndex, isRestore = true))
                                    }
                                    "NONOGRAM" -> {
                                        val levelIndex = data.substringBefore("|").toInt()
                                        onNavigate(PlayNonogram(levelIndex, isRestore = true))
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_bamboo_normal),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "⏳ CHƠI TIẾP VÁN DỞ DANG (" + prefs.getContinueGameType() + ")",
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = OngDoFontFamily,
                        color = Color(0xFFFFFBEB),
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color(0xFF1B1108),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 4f
                            )
                        ),
                        fontSize = 17.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable {
                                audioManager.playClick()
                                onNavigate(SelectGame)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.panel_bento_wood),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "TRÒ CHƠI",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFFFFD54F), // Dát vàng hoàng kim
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color(0xFF260D00),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hệ sinh thái game",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFFFFBEB),
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Museum)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.panel_bento_wood),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "BẢO TÀNG",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFFA7F3D0), // Ngọc bích sáng ánh vàng
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color(0xFF0F2414),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Khám phá văn hóa",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFFFFBEB),
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Scores)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.panel_bento_wood),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "BẢNG ĐIỂM",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = Color(0xFFFFE082), // Vàng kim sáng
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color(0xFF260D00),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 5f
                                    )
                                )
                            )
                            Text(
                                text = "Thành tích cá nhân",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFFFBEB),
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Options)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.panel_bento_wood),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "CÀI ĐẶT",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = Color(0xFFFFE082), // Vàng kim sáng
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color(0xFF260D00),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 5f
                                    )
                                )
                            )
                            Text(
                                text = "Âm lượng & Giao diện",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFFFBEB),
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
