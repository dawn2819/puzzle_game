package com.example.puzzlegame.engine

import kotlin.random.Random

class EngineSudoku(val difficulty: String) {
    val size = 9
    var solution: Array<IntArray> = Array(size) { IntArray(size) }
    var initialBoard: Array<IntArray> = Array(size) { IntArray(size) }
    var currentBoard: Array<IntArray> = Array(size) { IntArray(size) }

    var timeRemaining: Int = 0 // Tính bằng giây
    var score: Int = 0
    var isGameOver: Boolean = false
    var isVictory: Boolean = false

    companion object {
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"
    }

    init {
        generateGame()
    }

    fun generateGame() {
        // 1. Sinh bảng đầy đủ hợp lệ
        fillSudoku()
        
        // 2. Sao chép sang solution
        solution = Array(size) { r -> initialBoard[r].copyOf() }

        // 3. Ẩn bớt các ô theo độ khó
        val cellsToKeep = when (difficulty) {
            DIFFICULTY_EASY -> 45
            DIFFICULTY_MEDIUM -> 35
            DIFFICULTY_HARD -> 25
            else -> 40
        }
        removeNumbers(81 - cellsToKeep)

        // Sao chép sang currentBoard để người chơi tương tác
        currentBoard = Array(size) { r -> initialBoard[r].copyOf() }

        // 4. Cài đặt thời gian: EASY = vô hạn, MEDIUM = 20 phút (1200 giây), HARD = 10 phút (600 giây)
        timeRemaining = when (difficulty) {
            DIFFICULTY_EASY -> 0
            DIFFICULTY_MEDIUM -> 1200
            DIFFICULTY_HARD -> 600
            else -> 0
        }

        score = 0
        isGameOver = false
        isVictory = false
    }

    // Khôi phục từ trạng thái lưu trữ (Continue)
    fun restoreState(
        savedInitial: Array<IntArray>,
        savedCurrent: Array<IntArray>,
        savedSolution: Array<IntArray>,
        savedTime: Int,
        savedScore: Int
    ) {
        initialBoard = Array(size) { r -> savedInitial[r].copyOf() }
        currentBoard = Array(size) { r -> savedCurrent[r].copyOf() }
        solution = Array(size) { r -> savedSolution[r].copyOf() }
        timeRemaining = savedTime
        score = savedScore
        checkGameState()
    }

    // Người chơi điền số vào ô (r, c)
    fun setNumber(r: Int, c: Int, number: Int): Boolean {
        if (isGameOver || isVictory) return false
        // Không cho sửa các ô mặc định ban đầu
        if (initialBoard[r][c] != 0) return false

        if (number in 0..9) {
            currentBoard[r][c] = number
            checkGameState()
            return true
        }
        return false
    }

    // Kiểm tra xem ô (r, c) điền số có hợp lệ hay có lỗi không
    fun isCellCorrect(r: Int, c: Int): Boolean {
        if (currentBoard[r][c] == 0) return true
        return currentBoard[r][c] == solution[r][c]
    }

    // Cập nhật thời gian trôi qua (gọi mỗi giây từ UI)
    fun tickSecond() {
        if (isGameOver || isVictory || difficulty == DIFFICULTY_EASY) return

        if (timeRemaining > 0) {
            timeRemaining--
            if (timeRemaining == 0) {
                isGameOver = true
                calculateScore()
            }
        }
    }

    private fun checkGameState() {
        // Kiểm tra xem đã điền đầy đủ và đúng chưa
        var complete = true
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (currentBoard[r][c] != solution[r][c]) {
                    complete = false
                    break
                }
            }
        }

        if (complete) {
            isVictory = true
            calculateScore()
        }
    }

    private fun calculateScore() {
        if (!isVictory) {
            score = 0
            return
        }

        // Tính điểm cơ sở
        val baseScore = 500
        val multiplier = when (difficulty) {
            DIFFICULTY_EASY -> 1
            DIFFICULTY_MEDIUM -> 2
            DIFFICULTY_HARD -> 5
            else -> 1
        }

        // Điểm cộng thời gian còn dư
        val timeBonus = if (difficulty != DIFFICULTY_EASY) {
            timeRemaining * when (difficulty) {
                DIFFICULTY_MEDIUM -> 2
                DIFFICULTY_HARD -> 5
                else -> 0
            }
        } else {
            0
        }

        score = baseScore * multiplier + timeBonus
    }

    // --- Thuật toán sinh Sudoku ---
    private fun fillSudoku(): Boolean {
        initialBoard = Array(size) { IntArray(size) }
        return fillCell(0, 0)
    }

    private fun fillCell(row: Int, col: Int): Boolean {
        var r = row
        var c = col
        if (c == size) {
            c = 0
            r++
            if (r == size) return true // Đã điền hết bảng
        }

        val numbers = (1..9).shuffled()
        for (num in numbers) {
            if (isValidPlacement(r, c, num)) {
                initialBoard[r][c] = num
                if (fillCell(r, c + 1)) return true
                initialBoard[r][c] = 0 // Quay lui
            }
        }
        return false
    }

    private fun isValidPlacement(row: Int, col: Int, num: Int): Boolean {
        // Kiểm tra hàng
        for (c in 0 until size) {
            if (initialBoard[row][c] == num) return false
        }
        // Kiểm tra cột
        for (r in 0 until size) {
            if (initialBoard[r][col] == num) return false
        }
        // Kiểm tra block 3x3
        val boxRow = row - row % 3
        val boxCol = col - col % 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (initialBoard[r][c] == num) return false
            }
        }
        return true
    }

    private fun removeNumbers(count: Int) {
        var removed = 0
        while (removed < count) {
            val r = Random.nextInt(size)
            val c = Random.nextInt(size)
            if (initialBoard[r][c] != 0) {
                initialBoard[r][c] = 0
                removed++
            }
        }
    }

    // Serial hóa trạng thái để lưu
    fun serializeState(): String {
        val initialStr = initialBoard.joinToString(";") { it.joinToString(",") }
        val currentStr = currentBoard.joinToString(";") { it.joinToString(",") }
        val solutionStr = solution.joinToString(";") { it.joinToString(",") }
        return "$difficulty|$score|$timeRemaining|$initialStr|$currentStr|$solutionStr"
    }
}
