package com.example.puzzlegame.theme

import androidx.compose.ui.graphics.Color

// Premium Vietnam Heritage Palette
val FlagRed = Color(0xFFB40006)       // primary
val LacquerRed = Color(0xFFDA251D)    // primary-container
val StarGold = Color(0xFFEAEA00)      // secondary-fixed
val DarkGold = Color(0xFFCDCD00)      // secondary-fixed-dim
val StarGoldContainer = Color(0xFFE7E700) // secondary-container
val EarthyBrown = Color(0xFF1D1C13)   // on-surface / on-background
val EarthyBrownVariant = Color(0xFF5D403B) // on-surface-variant
val BambooGreen = Color(0xFF3E6137)   // tertiary
val BambooGreenContainer = Color(0xFF557A4E) // tertiary-container
val FaintGreen = Color(0xFFE1FFD5)    // on-tertiary-container

// Backgrounds / Surface Container Layers
val ParchmentBg = Color(0xFFFFF9EA)   // background / surface-bright
val SurfaceDim = Color(0xFFDFDACB)    // surface-dim
val SurfaceLow = Color(0xFFF9F3E4)    // surface-container-low
val SurfaceNormal = Color(0xFFF3EDDE) // surface-container
val SurfaceHigh = Color(0xFFEEE8D9)   // surface-container-high
val SurfaceHighest = Color(0xFFE8E2D3)// surface-container-highest
val OutlineBrown = Color(0xFF916F6A)  // outline
val OutlineVariant = Color(0xFFE6BDB7)// outline-variant

// Retro Dark Mode (Deep warm charcoal with red/brown tones)
val DarkBgStart = Color(0xFF1B1917)
val DarkBgEnd = Color(0xFF141210)
val DarkSurface = Color(0xFF282522)
val DarkSurfaceHigh = Color(0xFF322E2B)
val DarkSurfaceHighest = Color(0xFF3D3834)
val DarkOnSurface = Color(0xFFF5EFE0)
val DarkOutline = Color(0xFF7D6460)

// Map existing component names to avoid build errors:
val PrimaryNeon = FlagRed
val SecondaryNeon = StarGold
val TertiaryNeon = BambooGreen

// Glassmorphism cards (adapted for heritage theme)
val GlassDark = Color(0x1F282522)
val GlassBorderDark = Color(0x337D6460)
val GlassLight = Color(0x66FFF9EA)
val GlassBorderLight = Color(0x66916F6A)

// Game-specific colors:
// 2048:
val Tile2 = Color(0xFFFEF3C7)        // Amber 100
val Tile4 = Color(0xFFFDE68A)        // Amber 200
val Tile8 = Color(0xFFF97316)        // Orange 500
val Tile16 = Color(0xFFEA580C)       // Orange 600
val Tile32 = Color(0xFFEF4444)       // Red 500
val Tile64 = Color(0xFFDC2626)       // Red 600
val Tile128 = Color(0xFF991B1B)      // Red 800
val Tile256 = Color(0xFF7F1D1D)      // Red 900
val Tile512 = Color(0xFF626200)      // Star Gold Olive
val Tile1024 = Color(0xFFCDCD00)     // Dark Gold
val Tile2048 = Color(0xFFB40006)     // Flag Red
val TileHigher = Color(0xFF3E6137)   // Bamboo Green

// Nonogram cell colors
val NonogramFilled = FlagRed
val NonogramCross = OutlineBrown

// Sokoban elements
val SokobanWall = BambooGreen
val SokobanBox = Color(0xFFD2B48C)    // wood crate
val SokobanBoxOnGoal = Color(0xFF81C784) // green crate
val SokobanGoal = StarGold
val SokobanPlayer = FlagRed
