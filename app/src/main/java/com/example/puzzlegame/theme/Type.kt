package com.example.puzzlegame.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.R

// Font chữ Thư Pháp Ông Đồ Việt Hóa (UTM Ong Do Style)
val OngDoFontFamily = FontFamily(
    Font(R.font.utm_ong_do, FontWeight.Normal),
    Font(R.font.utm_ong_do, FontWeight.Bold),
    Font(R.font.utm_ong_do, FontWeight.ExtraBold)
)

val NomFontFamily = OngDoFontFamily

// Typography styles đồng bộ chuẩn phong cách Thư Pháp Dân Gian
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        letterSpacing = 1.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 1.2.sp
    ),
    displaySmall = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 1.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.8.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.8.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.8.sp
    ),
    titleSmall = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = OngDoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)
