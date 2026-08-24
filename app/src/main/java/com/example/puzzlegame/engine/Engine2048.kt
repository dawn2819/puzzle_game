package com.example.puzzlegame.engine

import kotlin.random.Random

class Engine2048(val size: Int) {
    var board: Array<IntArray> = Array(size) { IntArray(size) }
    var score: Int = 0
    var isGameOver: Boolean = false
    var hasReached2048: Boolean = false

    init {
        reset()
    }

    fun reset() {
        board = Array(size) { IntArray(size) }
        score = 0
        isGameOver = false
        hasReached2048 = false
        spawnRandomTile()
        spawnRandomTile()
    }

    fun checkHas2048(): Boolean {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] >= 2048) return true
            }
        }
        return false
    }

    // Khởi dựng từ trạng thái khôi phục (Continue)
    fun restoreState(savedBoard: Array<IntArray>, savedScore: Int) {
        board = Array(size) { r -> savedBoard[r].copyOf() }
        score = savedScore
        isGameOver = checkGameOver()
        hasReached2048 = checkHas2048()
    }

    // Sinh ô ngẫu nhiên (2 với tỉ lệ 90%, 4 với tỉ lệ 10%)
    fun spawnRandomTile(): Boolean {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == 0) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }
        if (emptyCells.isEmpty()) return false

        val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
        board[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        return true
    }

    // Thực hiện di chuyển theo 4 hướng: 0=Left, 1=Up, 2=Right, 3=Down
    fun move(direction: Int): Boolean {
        if (isGameOver) return false

        val rotatedBoard = when (direction) {
            0 -> copyBoard(board)          // Trái: Giữ nguyên
            1 -> rotate90CCW(board)        // Lên: Xoay ngược chiều 90
            2 -> rotate180(board)          // Phải: Xoay 180
            3 -> rotate90CW(board)         // Xuống: Xoay thuận chiều 90
            else -> return false
        }

        var moved = false
        val newScoreAdd = slideAndMergeLeft(rotatedBoard)
        if (newScoreAdd >= 0) {
            moved = true
            score += newScoreAdd
        }

        val finalBoard = when (direction) {
            0 -> rotatedBoard
            1 -> rotate90CW(rotatedBoard)
            2 -> rotate180(rotatedBoard)
            3 -> rotate90CCW(rotatedBoard)
            else -> rotatedBoard
        }

        // Nếu bảng thay đổi thì cập nhật và sinh ô mới
        if (moved || !boardsEqual(board, finalBoard)) {
            board = finalBoard
            if (!hasReached2048 && checkHas2048()) {
                hasReached2048 = true
            }
            spawnRandomTile()
            isGameOver = checkGameOver()
            return true
        }

        return false
    }

    private fun slideAndMergeLeft(grid: Array<IntArray>): Int {
        var addedScore = 0
        var boardChanged = false

        for (r in 0 until size) {
            val originalRow = grid[r].copyOf()
            // 1. Slide các ô khác 0 về bên trái
            val tempRow = IntArray(size)
            var index = 0
            for (c in 0 until size) {
                if (grid[r][c] != 0) {
                    tempRow[index++] = grid[r][c]
                }
            }

            // 2. Merge các ô trùng nhau
            val mergedRow = IntArray(size)
            var mergedIndex = 0
            var c = 0
            while (c < size) {
                if (c < size - 1 && tempRow[c] != 0 && tempRow[c] == tempRow[c + 1]) {
                    val mergedValue = tempRow[c] * 2
                    mergedRow[mergedIndex++] = mergedValue
                    addedScore += mergedValue
                    c += 2
                } else {
                    mergedRow[mergedIndex++] = tempRow[c]
                    c++
                }
            }

            if (!originalRow.contentEquals(mergedRow)) {
                boardChanged = true
            }
            grid[r] = mergedRow
        }

        return if (boardChanged) addedScore else -1
    }

    private fun checkGameOver(): Boolean {
        // Nếu còn ô trống thì chưa game over
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == 0) return false
            }
        }
        // Nếu còn ô liền kề cùng giá trị thì chưa game over
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (r < size - 1 && board[r][c] == board[r + 1][c]) return false
                if (c < size - 1 && board[r][c] == board[r][c + 1]) return false
            }
        }
        return true
    }

    // --- Các hàm xoay ma trận trợ giúp ---
    private fun copyBoard(src: Array<IntArray>): Array<IntArray> {
        return Array(size) { r -> src[r].copyOf() }
    }

    private fun boardsEqual(a: Array<IntArray>, b: Array<IntArray>): Boolean {
        for (r in 0 until size) {
            if (!a[r].contentEquals(b[r])) return false
        }
        return true
    }

    // Xoay 90 độ thuận chiều kim đồng hồ
    private fun rotate90CW(src: Array<IntArray>): Array<IntArray> {
        val dest = Array(size) { IntArray(size) }
        for (r in 0 until size) {
            for (c in 0 until size) {
                dest[c][size - 1 - r] = src[r][c]
            }
        }
        return dest
    }

    // Xoay 90 độ ngược chiều kim đồng hồ
    private fun rotate90CCW(src: Array<IntArray>): Array<IntArray> {
        val dest = Array(size) { IntArray(size) }
        for (r in 0 until size) {
            for (c in 0 until size) {
                dest[size - 1 - c][r] = src[r][c]
            }
        }
        return dest
    }

    // Xoay 180 độ
    private fun rotate180(src: Array<IntArray>): Array<IntArray> {
        val dest = Array(size) { IntArray(size) }
        for (r in 0 until size) {
            for (c in 0 until size) {
                dest[size - 1 - r][size - 1 - c] = src[r][c]
            }
        }
        return dest
    }

    // Serial hóa trạng thái để lưu
    fun serializeState(): String {
        val builder = StringBuilder()
        builder.append(size).append("|").append(score).append("|")
        for (r in 0 until size) {
            builder.append(board[r].joinToString(","))
            if (r < size - 1) builder.append(";")
        }
        return builder.toString()
    }
}
