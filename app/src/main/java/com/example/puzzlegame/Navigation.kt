package com.example.puzzlegame

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.puzzlegame.ui.*

@Composable
fun MainNavigation(
    onDarkModeChanged: (Boolean) -> Unit
) {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // 1. Màn hình chính
            entry<Main> {
                MainMenuScreen(onNavigate = { navKey -> backStack.add(navKey) })
            }
            // 2. Màn hình chọn game
            entry<SelectGame> {
                GameSelectionScreen(
                    onNavigate = { navKey -> backStack.add(navKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            // 3. Màn hình cài đặt
            entry<Options> {
                OptionsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onDarkModeChanged = onDarkModeChanged
                )
            }
            // 4. Màn hình bảng điểm
            entry<Scores> {
                ScoresScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            // 5. Màn chơi 2048
            entry<Play2048> { key ->
                Play2048Screen(
                    size = key.size,
                    isRestore = key.isRestore,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            // 6. Màn chơi Sudoku
            entry<PlaySudoku> { key ->
                PlaySudokuScreen(
                    difficulty = key.difficulty,
                    isRestore = key.isRestore,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            // 7. Màn chơi Sokoban
            entry<PlaySokoban> { key ->
                PlaySokobanScreen(
                    levelIndex = key.levelIndex,
                    isRestore = key.isRestore,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            // 8. Màn chơi Nonogram
            entry<PlayNonogram> { key ->
                PlayNonogramScreen(
                    levelIndex = key.levelIndex,
                    isRestore = key.isRestore,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
