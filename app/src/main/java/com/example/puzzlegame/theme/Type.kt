package com.example.puzzlegame.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NomFontFamily = FontFamily.Serif

// Set of Material typography styles to start with
val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = NomFontFamily,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 32.sp,
      letterSpacing = 1.5.sp
    ),
    titleLarge = TextStyle(
      fontFamily = NomFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      letterSpacing = 1.sp
    ),
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
  )
