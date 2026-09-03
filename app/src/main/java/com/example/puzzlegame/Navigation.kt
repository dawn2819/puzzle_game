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
    val backStack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // 0. Splash: thay thế chính nó bằng Main để Back không quay lại splash
            entry<Splash> {
                SplashScreen(onFinished = {
                    backStack.clear()
                    backStack.add(Main)
                })
            }
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
            // 9. Màn chơi Ô ăn quan
            entry<PlayOAnQuan> {
                PlayOAnQuanScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 10. Màn chơi Cờ gánh
            entry<PlayCoGanh> {
                PlayCoGanhScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 11. Màn chơi Rồng rắn lên mây
            entry<PlayDragonSnake> {
                PlayDragonSnakeScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 12. Màn chơi Nhảy lò cò
            entry<PlayLofo> {
                PlayLofoScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 13. Màn chơi Thả đỉa ba ba
            entry<PlayThaSua> {
                PlayThaSuaScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 14. Màn chơi Bịt mắt bắt dê
            entry<PlayBitMatDe> {
                PlayBitMatDeScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 15. Màn chơi Câu đố chữ
            entry<PlayWordPuzzle> {
                PlayWordPuzzleScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 16. Màn chơi Chơi chuyền
            entry<PlaySequence> {
                PlaySequenceScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 17. Màn chơi Đập niêu
            entry<PlayDapNieu> {
                PlayDapNieuScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 18. Màn chơi Ném còn
            entry<PlayNemCon> {
                PlayNemConScreen(onBack = { backStack.removeLastOrNull() })
            }
            // 19. Màn Bảo tàng Hồn Việt
            entry<Museum> {
                MuseumScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
