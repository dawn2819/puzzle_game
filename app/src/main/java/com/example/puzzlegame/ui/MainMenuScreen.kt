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

    PremiumBackground {
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
            // --- LOGO TIÊU ĐỀ ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .scale(titleScale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HỒN VIỆT",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FlagRed,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = FlagRed.copy(alpha = glowAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 30f
                        )
                    )
                )
                Text(
                    text = "Vietnamese Puzzle Universe",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EarthyBrown.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }

            // --- PROFILE USER CARD (XP, COINS, LEVEL) ---
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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
                                    .size(36.dp)
                                    .background(FlagRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$level",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cấp độ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text("Kẻ Sĩ Trí Tuệ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EarthyBrown)
                            }
                        }

                        // Vàng
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(StarGold.copy(alpha = 0.3f))
                                .border(1.dp, StarGold, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$coins 🪙",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = EarthyBrown
                            )
                        }
                    }

                    // XP Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Điểm kinh nghiệm (XP)", fontSize = 12.sp, color = EarthyBrown)
                            Text("$xp / $xpTarget XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                        }
                        LinearProgressIndicator(
                            progress = xp.toFloat() / xpTarget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = FlagRed,
                            trackColor = OutlineBrown.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // --- NÚT CHƠI TIẾP (Nếu có trận dở dang) ---
            if (hasContinue) {
                Button(
                    onClick = {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = StarGold)
                ) {
                    Text(
                        text = "⏳ CHƠI TIẾP VÁN DỞ DANG (" + prefs.getContinueGameType() + ")",
                        fontWeight = FontWeight.ExtraBold,
                        color = EarthyBrown,
                        fontSize = 14.sp
                    )
                }
            }

            // --- BENTO GRID MÀN HÌNH CHÍNH ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hàng 1: CHƠI GAME & BẢO TÀNG DI SẢN (Bento lớn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bento 1: Game Selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceNormal)
                            .border(2.dp, OutlineBrown, RoundedCornerShape(20.dp))
                            .clickable {
                                audioManager.playClick()
                                onNavigate(SelectGame)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🧠", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("TRÒ CHƠI", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FlagRed)
                            Text("Hệ sinh thái game", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }

                    // Bento 2: Bảo tàng di sản
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceNormal)
                            .border(2.dp, BambooGreen, RoundedCornerShape(20.dp))
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Museum)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏯", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("BẢO TÀNG", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BambooGreen)
                            Text("Khám phá văn hóa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                }

                // Hàng 2: ĐIỂM SỐ & CÀI ĐẶT (Bento nhỏ hơn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bento 3: Scores
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceNormal)
                            .border(1.5.dp, OutlineBrown, RoundedCornerShape(16.dp))
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Scores)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🏆", fontSize = 24.sp)
                            Column {
                                Text("BẢNG ĐIỂM", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EarthyBrown)
                                Text("Thành tích cá nhân", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    }

                    // Bento 4: Options
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceNormal)
                            .border(1.5.dp, OutlineBrown, RoundedCornerShape(16.dp))
                            .clickable {
                                audioManager.playClick()
                                onNavigate(Options)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚙️", fontSize = 24.sp)
                            Column {
                                Text("CÀI ĐẶT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EarthyBrown)
                                Text("Âm lượng & Giao diện", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
