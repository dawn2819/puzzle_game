package com.example.puzzlegame.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.R
import com.example.puzzlegame.theme.FlagRed
import com.example.puzzlegame.theme.StarGold
import com.example.puzzlegame.ui.components.PremiumBackground

private const val LOAD_DURATION_MS = 2500

/** Splash: logo + tagline + loading bar; auto-continues at 100%. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(LOAD_DURATION_MS, easing = LinearEasing),
        finishedListener = { if (it >= 1f && !done) { done = true; onFinished() } },
        label = "splash_progress"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900),
        label = "splash_logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(900),
        label = "splash_logo_scale"
    )

    LaunchedEffect(Unit) { started = true }

    PremiumBackground(drawableId = R.drawable.bg_main_menu, bgDimAlpha = 0.35f, scrollSpeed = 0f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo_hon_viet),
                contentDescription = "Hồn Việt Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(160.dp)
                    .alpha(logoAlpha)
                    .scale(logoScale)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Vietnamese Puzzle Universe",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { progress },
                color = FlagRed,
                trackColor = StarGold.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Đang tải... ${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}
