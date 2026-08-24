package com.example.puzzlegame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.puzzlegame.*
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.theme.StarGold

@Composable
fun MainMenuScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var hasContinue by remember { mutableStateOf(prefs.hasContinueGame()) }

    // Hoạt ảnh xuất hiện tuần tự (Staggered Entrance)
    var showMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showMenu = true
    }

    val b1Alpha by animateFloatAsState(targetValue = if (showMenu) 1f else 0f, animationSpec = tween(500, delayMillis = 100), label = "b1_a")
    val b1Offset by animateFloatAsState(targetValue = if (showMenu) 0f else 30f, animationSpec = tween(500, delayMillis = 100), label = "b1_o")

    val b2Alpha by animateFloatAsState(targetValue = if (showMenu) 1f else 0f, animationSpec = tween(500, delayMillis = 200), label = "b2_a")
    val b2Offset by animateFloatAsState(targetValue = if (showMenu) 0f else 30f, animationSpec = tween(500, delayMillis = 200), label = "b2_o")

    val b3Alpha by animateFloatAsState(targetValue = if (showMenu) 1f else 0f, animationSpec = tween(500, delayMillis = 300), label = "b3_a")
    val b3Offset by animateFloatAsState(targetValue = if (showMenu) 0f else 30f, animationSpec = tween(500, delayMillis = 300), label = "b3_o")

    val b4Alpha by animateFloatAsState(targetValue = if (showMenu) 1f else 0f, animationSpec = tween(500, delayMillis = 400), label = "b4_a")
    val b4Offset by animateFloatAsState(targetValue = if (showMenu) 0f else 30f, animationSpec = tween(500, delayMillis = 400), label = "b4_o")

    // Hiệu ứng scale động cho logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow"
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Logo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .scale(scale),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TRÒ CHƠI",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 6.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 35f
                        )
                    )
                )
                Text(
                    text = "DÂN GIAN",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF626200), // Star Gold Olive/Secondary color in Light theme
                    textAlign = TextAlign.Center,
                    letterSpacing = 10.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = StarGold.copy(alpha = glowAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 25f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Trí Tuệ Việt",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

              GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Play Button
                    Button(
                        onClick = {
                            audioManager.playClick()
                            onNavigate(SelectGame)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer(alpha = b1Alpha, translationY = b1Offset),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("PLAY", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Continue Button
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
                        enabled = hasContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer(alpha = b2Alpha, translationY = b2Offset),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("CONTINUE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Score Button
                    OutlinedButton(
                        onClick = {
                            audioManager.playClick()
                            onNavigate(Scores)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer(alpha = b3Alpha, translationY = b3Offset),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("SCORE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Option Button
                    OutlinedButton(
                        onClick = {
                            audioManager.playClick()
                            onNavigate(Options)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer(alpha = b4Alpha, translationY = b4Offset),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("OPTION", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
