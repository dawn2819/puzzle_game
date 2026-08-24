package com.example.puzzlegame

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object SelectGame : NavKey
@Serializable data object Options : NavKey
@Serializable data object Scores : NavKey

@Serializable
data class Play2048(val size: Int, val isRestore: Boolean = false) : NavKey

@Serializable
data class PlaySudoku(val difficulty: String, val isRestore: Boolean = false) : NavKey

@Serializable
data class PlaySokoban(val levelIndex: Int, val isRestore: Boolean = false) : NavKey

@Serializable
data class PlayNonogram(val levelIndex: Int, val isRestore: Boolean = false) : NavKey
