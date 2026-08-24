package com.example.puzzlegame.engine

import kotlin.random.Random

class EngineOAnQuan {
    // Bàn cờ 12 ô:
    // Ô 0..4: Ô dân của Người chơi 2 (hàng trên, phải sang trái)
    // Ô 5: Ô quan bên trái
    // Ô 6..10: Ô dân của Người chơi 1 (hàng dưới, trái sang phải)
    // Ô 11: Ô quan bên phải
    val board = IntArray(12) { 5 } // Mặc định mỗi ô có 5 dân

    var hasQuanLeft = true
    var hasQuanRight = true

    var scorePlayer1 = 0
    var scorePlayer2 = 0

    var isPlayer1Turn = true
    var isGameOver = false
    var winner = 0 // 1: Player 1, 2: Player 2, 0: Hòa

    init {
        // Thiết lập ban đầu cho 2 ô Quan (mỗi ô chứa 10 dân quy đổi hoặc 1 quan = 10 điểm)
        board[5] = 10 // Ô Quan trái
        board[11] = 10 // Ô Quan phải
    }

    fun reset() {
        for (i in 0..4) board[i] = 5
        board[5] = 10
        for (i in 6..10) board[i] = 5
        board[11] = 10

        hasQuanLeft = true
        hasQuanRight = true
        scorePlayer1 = 0
        scorePlayer2 = 0
        isPlayer1Turn = true
        isGameOver = false
        winner = 0
    }

    // Kiểm tra xem người chơi hiện tại có cờ để đi không. Nếu không, phải tự bỏ ra 5 dân (mỗi ô 1 dân) để rải tiếp.
    fun checkAndRefillPits() {
        val startRange = if (isPlayer1Turn) 6..10 else 0..4
        val totalSeeds = startRange.sumOf { board[it] }

        if (totalSeeds == 0) {
            // Người chơi phải rút từ số điểm của mình ra 5 điểm để điền vào 5 ô
            val playerScore = if (isPlayer1Turn) scorePlayer1 else scorePlayer2
            val refillAmount = minOf(playerScore, 5)
            
            if (isPlayer1Turn) {
                scorePlayer1 -= refillAmount
            } else {
                scorePlayer2 -= refillAmount
            }

            // Rải đều refillAmount vào 5 ô dân của mình
            for (i in 0 until refillAmount) {
                board[startRange.first + i] += 1
            }
        }
    }

    // Thực hiện nước đi từ ô `startPit` theo hướng `isClockwise`
    // Trả về danh sách các bước di chuyển chi tiết để làm hoạt ảnh rải sỏi (visual feedback)
    fun makeMove(startPit: Int, isClockwise: Boolean): List<MoveStep> {
        val steps = mutableListOf<MoveStep>()
        if (isGameOver) return steps

        // Xác thực nước đi hợp pháp
        if (isPlayer1Turn && startPit !in 6..10) return steps
        if (!isPlayer1Turn && startPit !in 0..4) return steps
        if (board[startPit] == 0) return steps

        var handCount = board[startPit]
        board[startPit] = 0
        steps.add(MoveStep(type = StepType.PICKUP, pitIndex = startPit, count = handCount, boardState = board.clone()))

        var currentPit = startPit
        val delta = if (isClockwise) -1 else 1

        while (handCount > 0) {
            // Rải quân từng ô một
            currentPit = (currentPit + delta + 12) % 12
            board[currentPit] += 1
            handCount -= 1
            steps.add(MoveStep(type = StepType.DISTRIBUTE, pitIndex = currentPit, count = 1, boardState = board.clone()))

            if (handCount == 0) {
                // Kiểm tra ô tiếp theo để quyết định tiếp tục rải hay ăn quân
                val nextPit = (currentPit + delta + 12) % 12
                val nextNextPit = (nextPit + delta + 12) % 12

                if (nextPit == 5 || nextPit == 11) {
                    // Ô tiếp theo là ô Quan -> Không được đi tiếp, dừng lượt chơi
                    break
                } else if (board[nextPit] > 0) {
                    // Ô tiếp theo có quân dân -> Bốc lên rải tiếp
                    handCount = board[nextPit]
                    board[nextPit] = 0
                    steps.add(MoveStep(type = StepType.PICKUP, pitIndex = nextPit, count = handCount, boardState = board.clone()))
                    currentPit = nextPit
                } else {
                    // Ô tiếp theo trống -> Kiểm tra ô sau đó để ăn quân
                    var eatPointer = nextPit
                    while (true) {
                        val targetPit = (eatPointer + delta + 12) % 12
                        val afterTargetPit = (targetPit + delta + 12) % 12
                        
                        if (board[eatPointer] == 0 && board[targetPit] > 0) {
                            // Ăn quân ở ô targetPit
                            val captured = board[targetPit]
                            board[targetPit] = 0
                            
                            // Ghi điểm
                            if (isPlayer1Turn) {
                                scorePlayer1 += captured
                            } else {
                                scorePlayer2 += captured
                            }

                            // Cập nhật trạng thái ô quan
                            if (targetPit == 5) hasQuanLeft = false
                            if (targetPit == 11) hasQuanRight = false

                            steps.add(MoveStep(
                                type = StepType.CAPTURE, 
                                pitIndex = targetPit, 
                                count = captured, 
                                boardState = board.clone(),
                                isPlayer1 = isPlayer1Turn
                            ))

                            // Tiếp tục xét ăn dây chuyền
                            if (board[afterTargetPit] == 0) {
                                eatPointer = afterTargetPit
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    break // Dừng lượt chơi sau khi ăn quân
                }
            }
        }

        // Kiểm tra kết thúc game
        checkGameOver()

        if (!isGameOver) {
            // Chuyển lượt chơi
            isPlayer1Turn = !isPlayer1Turn
            checkAndRefillPits()
            // Sau khi refill lại kiểm tra xem game over chưa
            checkGameOver()
        }

        return steps
    }

    private fun checkGameOver() {
        // Game kết thúc khi cả 2 ô Quan đều trống (đã bị ăn hết)
        if (!hasQuanLeft && !hasQuanRight) {
            isGameOver = true
            // Gom tất cả quân dân còn lại trên hàng của ai về cho người đó
            for (i in 6..10) {
                scorePlayer1 += board[i]
                board[i] = 0
            }
            for (i in 0..4) {
                scorePlayer2 += board[i]
                board[i] = 0
            }

            winner = when {
                scorePlayer1 > scorePlayer2 -> 1
                scorePlayer2 > scorePlayer1 -> 2
                else -> 0
            }
        }
    }

    // AI đối thủ đơn giản (Heuristic/Greedy) cho Player 2
    fun getBestMoveForAI(): Pair<Int, Boolean>? {
        val validMoves = mutableListOf<Pair<Int, Boolean>>()
        for (pit in 0..4) {
            if (board[pit] > 0) {
                validMoves.add(Pair(pit, true)) // Clockwise
                validMoves.add(Pair(pit, false)) // Counter-Clockwise
            }
        }

        if (validMoves.isEmpty()) return null

        // Chọn nước đi mang lại số điểm ăn quân cao nhất lập tức (Greedy)
        var bestMove = validMoves[0]
        var maxScore = -1

        for (move in validMoves) {
            // Giả lập nước đi
            val simEngine = EngineOAnQuan()
            System.arraycopy(this.board, 0, simEngine.board, 0, 12)
            simEngine.hasQuanLeft = this.hasQuanLeft
            simEngine.hasQuanRight = this.hasQuanRight
            simEngine.scorePlayer1 = this.scorePlayer1
            simEngine.scorePlayer2 = this.scorePlayer2
            simEngine.isPlayer1Turn = false
            
            simEngine.makeMove(move.first, move.second)
            val scoreDiff = simEngine.scorePlayer2 - this.scorePlayer2
            if (scoreDiff > maxScore) {
                maxScore = scoreDiff
                bestMove = move
            }
        }

        // Nếu điểm rải sỏi bằng nhau, chọn ngẫu nhiên
        if (maxScore == 0) {
            return validMoves[Random.nextInt(validMoves.size)]
        }

        return bestMove
    }
}

enum class StepType {
    PICKUP,      // Bốc sỏi lên tay
    DISTRIBUTE,  // Rải 1 viên sỏi vào ô
    CAPTURE      // Ăn điểm sỏi từ ô
}

data class MoveStep(
    val type: StepType,
    val pitIndex: Int,
    val count: Int,
    val boardState: IntArray,
    val isPlayer1: Boolean = true
)
