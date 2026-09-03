package com.example.puzzlegame

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Splash : NavKey
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

@Serializable data object PlayOAnQuan : NavKey
@Serializable data object PlayCoGanh : NavKey
@Serializable data object PlayDragonSnake : NavKey
@Serializable data object PlayLofo : NavKey
@Serializable data object PlayThaSua : NavKey
@Serializable data object PlayBitMatDe : NavKey
@Serializable data object PlayWordPuzzle : NavKey
@Serializable data object PlaySequence : NavKey
@Serializable data object PlayDapNieu : NavKey
@Serializable data object PlayNemCon : NavKey
@Serializable data object PlayDinoRunner : NavKey
@Serializable data object Museum : NavKey
