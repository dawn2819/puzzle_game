package com.example.puzzlegame.engine

class EngineCoGanh {
    // 0: Ô trống, 1: Quân Xanh (Người chơi), 2: Quân Đỏ (Máy/AI)
    val board = IntArray(25) { 0 }

    var isPlayerTurn = true
    var isGameOver = false
    var winner = 0 // 1: Player, 2: AI, 0: Chưa kết thúc

    init {
        reset()
    }

    fun reset() {
        for (i in 0..24) board[i] = 0
        
        // Cấu hình vị trí ban đầu của Cờ Gánh
        // Hàng 0 (đỏ): 0, 1, 2, 3, 4
        for (i in 0..4) board[i] = 2
        // Hàng 1 (đỏ ở góc biên): 5, 9
        board[5] = 2
        board[9] = 2

        // Hàng 3 (xanh ở góc biên): 15, 19
        board[15] = 1
        board[19] = 1
        // Hàng 4 (xanh): 20, 21, 22, 23, 24
        for (i in 20..24) board[i] = 1

        isPlayerTurn = true
        isGameOver = false
        winner = 0
    }

    // Lấy tọa độ dòng
    fun getRow(idx: Int) = idx / 5
    // Lấy tọa độ cột
    fun getCol(idx: Int) = idx % 5
    // Lấy chỉ số từ dòng, cột
    fun getIndex(r: Int, c: Int) = r * 5 + c

    // Kiểm tra xem hai ô có kết nối trực tiếp với nhau không (theo luật Cờ gánh)
    fun isConnected(from: Int, to: Int): Boolean {
        val r1 = getRow(from)
        val c1 = getCol(from)
        val r2 = getRow(to)
        val c2 = getCol(to)

        val dr = kotlin.math.abs(r1 - r2)
        val dc = kotlin.math.abs(c1 - c2)

        if (dr > 1 || dc > 1 || (dr == 0 && dc == 0)) return false

        // Nếu di chuyển thẳng/ngang thì luôn được phép
        if (dr == 0 || dc == 0) return true

        // Nếu di chuyển chéo, ô đi xuất phát phải có thuộc tính giao đường chéo (chẵn parity: (r+c)%2 == 0)
        return (r1 + c1) % 2 == 0
    }

    // Lấy tất cả các nước đi hợp lệ của quân ở ô `from`
    fun getValidMovesFrom(from: Int): List<Int> {
        val moves = mutableListOf<Int>()
        if (board[from] == 0) return moves

        for (to in 0..24) {
            if (board[to] == 0 && isConnected(from, to)) {
                moves.add(to)
            }
        }
        return moves
    }

    // Thực hiện nước đi từ `from` đến `to`. Trả về true nếu thành công và xử lý luật Gánh + Vây/Chẹt
    fun makeMove(from: Int, to: Int): Boolean {
        if (isGameOver) return false

        // Kiểm tra hợp lệ lượt chơi
        val piece = board[from]
        if (piece == 0) return false
        if (isPlayerTurn && piece != 1) return false
        if (!isPlayerTurn && piece != 2) return false

        // Kiểm tra đường đi
        if (board[to] != 0 || !isConnected(from, to)) return false

        // Thực hiện di chuyển
        board[from] = 0
        board[to] = piece

        // 1. Áp dụng luật GÁNH
        applyGanhRule(to, piece)

        // 2. Áp dụng luật VÂY / CHẸT
        applyVayRule(piece)

        // Kiểm tra điều kiện thắng
        checkGameStatus()

        if (!isGameOver) {
            isPlayerTurn = !isPlayerTurn
        }
        return true
    }

    // Luật Gánh: Xét các cặp ô đối xứng qua ô vừa đi `center`
    private fun applyGanhRule(center: Int, playerPiece: Int) {
        val r = getRow(center)
        val c = getCol(center)
        val opponentPiece = if (playerPiece == 1) 2 else 1

        // Các cặp hướng đối xứng
        val directions = listOf(
            Pair(Pair(-1, 0), Pair(1, 0)),   // Dọc
            Pair(Pair(0, -1), Pair(0, 1)),   // Ngang
            Pair(Pair(-1, -1), Pair(1, 1)),  // Chéo chính
            Pair(Pair(-1, 1), Pair(1, -1))   // Chéo phụ
        )

        for (dirPair in directions) {
            val d1 = dirPair.first
            val d2 = dirPair.second

            val nr1 = r + d1.first
            val nc1 = c + d1.second
            val nr2 = r + d2.first
            val nc2 = c + d2.second

            // Kiểm tra hai ô đối xứng có nằm trong bàn cờ không
            if (nr1 in 0..4 && nc1 in 0..4 && nr2 in 0..4 && nc2 in 0..4) {
                val idx1 = getIndex(nr1, nc1)
                val idx2 = getIndex(nr2, nc2)

                // Cả hai ô phải đang được kết nối với ô trung tâm
                if (isConnected(center, idx1) && isConnected(center, idx2)) {
                    // Nếu cả hai ô đều chứa quân đối phương -> Gánh (đổi sang quân mình)
                    if (board[idx1] == opponentPiece && board[idx2] == opponentPiece) {
                        board[idx1] = playerPiece
                        board[idx2] = playerPiece
                    }
                }
            }
        }
    }

    // Luật Vây / Chẹt: Tìm tất cả các quân đối phương không còn đường đi hợp lệ và chuyển đổi chúng
    private fun applyVayRule(playerPiece: Int) {
        val opponentPiece = if (playerPiece == 1) 2 else 1
        val visited = BooleanArray(25) { false }

        for (i in 0..24) {
            if (board[i] == opponentPiece && !visited[i]) {
                // Sử dụng BFS tìm tập hợp liên thông các quân đối phương
                val component = mutableListOf<Int>()
                val queue = mutableListOf<Int>()
                
                queue.add(i)
                visited[i] = true
                
                var hasLiberties = false

                while (queue.isNotEmpty()) {
                    val curr = queue.removeAt(0)
                    component.add(curr)

                    // Kiểm tra tất cả các ô liên kết liền kề
                    for (adj in 0..24) {
                        if (isConnected(curr, adj)) {
                            if (board[adj] == 0) {
                                // Tìm thấy ô trống -> Cả khối này vẫn còn đường đi
                                hasLiberties = true
                            } else if (board[adj] == opponentPiece && !visited[adj]) {
                                visited[adj] = true
                                queue.add(adj)
                            }
                        }
                    }
                }

                // Nếu khối quân đối phương này không còn bất kỳ ô trống nào để đi tiếp -> Bị vây bắt!
                if (!hasLiberties) {
                    for (idx in component) {
                        board[idx] = playerPiece
                    }
                }
            }
        }
    }

    private fun checkGameStatus() {
        val greenCount = board.count { it == 1 }
        val redCount = board.count { it == 2 }

        if (greenCount == 0) {
            isGameOver = true
            winner = 2 // AI thắng
        } else if (redCount == 0) {
            isGameOver = true
            winner = 1 // Người thắng
        } else {
            // Kiểm tra xem bên tiếp theo còn nước đi hợp lệ không
            val nextPlayerPiece = if (isPlayerTurn) 2 else 1 // Lượt kế tiếp là ai
            var hasMoves = false
            for (i in 0..24) {
                if (board[i] == nextPlayerPiece) {
                    if (getValidMovesFrom(i).isNotEmpty()) {
                        hasMoves = true
                        break
                    }
                }
            }
            if (!hasMoves) {
                // Không còn nước đi nào hợp lệ -> Thua cuộc
                isGameOver = true
                winner = if (nextPlayerPiece == 1) 2 else 1
            }
        }
    }

    // AI đối thủ đơn giản sử dụng Minimax có cắt tỉa Alpha-Beta độ sâu 3
    fun getBestMoveForAI(): Pair<Int, Int>? {
        val moves = getValidsMovesFor(2)
        if (moves.isEmpty()) return null

        var bestMove = moves[0]
        var bestVal = -99999

        for (move in moves) {
            val simEngine = simulateMove(move.first, move.second)
            val moveVal = minimax(simEngine, depth = 3, alpha = -99999, beta = 99999, isMax = false)
            if (moveVal > bestVal) {
                bestVal = moveVal
                bestMove = move
            }
        }
        return bestMove
    }

    private fun getValidsMovesFor(player: Int): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (from in 0..24) {
            if (board[from] == player) {
                for (to in getValidMovesFrom(from)) {
                    moves.add(Pair(from, to))
                }
            }
        }
        return moves
    }

    private fun simulateMove(from: Int, to: Int): EngineCoGanh {
        val clone = EngineCoGanh()
        System.arraycopy(this.board, 0, clone.board, 0, 25)
        clone.isPlayerTurn = this.isPlayerTurn
        clone.isGameOver = this.isGameOver
        clone.winner = this.winner

        clone.board[from] = 0
        clone.board[to] = board[from]
        clone.applyGanhRule(to, board[from])
        clone.applyVayRule(board[from])
        clone.checkGameStatus()
        clone.isPlayerTurn = !clone.isPlayerTurn
        return clone
    }

    private fun evaluateBoard(engine: EngineCoGanh): Int {
        val redCount = engine.board.count { it == 2 }
        val greenCount = engine.board.count { it == 1 }
        // Hàm đánh giá: Hiệu số số lượng quân cờ đỏ (AI) trừ đi quân xanh
        return redCount - greenCount
    }

    private fun minimax(engine: EngineCoGanh, depth: Int, alpha: Int, beta: Int, isMax: Boolean): Int {
        if (depth == 0 || engine.isGameOver) {
            return evaluateBoard(engine)
        }

        var localAlpha = alpha
        var localBeta = beta

        if (isMax) {
            var maxEval = -99999
            for (move in engine.getValidsMovesFor(2)) {
                val sim = engine.simulateMove(move.first, move.second)
                val eval = minimax(sim, depth - 1, localAlpha, localBeta, false)
                maxEval = maxOf(maxEval, eval)
                localAlpha = maxOf(localAlpha, eval)
                if (localBeta <= localAlpha) break
            }
            return maxEval
        } else {
            var minEval = 99999
            for (move in engine.getValidsMovesFor(1)) {
                val sim = engine.simulateMove(move.first, move.second)
                val eval = minimax(sim, depth - 1, localAlpha, localBeta, true)
                minEval = minOf(minEval, eval)
                localBeta = minOf(localBeta, eval)
                if (localBeta <= localAlpha) break
            }
            return minEval
        }
    }
}
