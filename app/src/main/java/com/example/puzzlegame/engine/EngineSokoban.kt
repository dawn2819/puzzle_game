package com.example.puzzlegame.engine

class EngineSokoban(val levelIndex: Int) {
    var board: Array<IntArray> = Array(0) { IntArray(0) }
    var initialBoard: Array<IntArray> = Array(0) { IntArray(0) }
    var rows = 0
    var cols = 0

    var playerRow = 0
    var playerCol = 0

    var moves = 0
    var pushes = 0
    var timeRemaining = 0
    var score = 0
    var isGameOver = false
    var isVictory = false

    companion object {
        // Định nghĩa các ô trong bảng
        const val FLOOR = 0
        const val WALL = 1
        const val GOAL = 2
        const val BOX = 3
        const val BOX_ON_GOAL = 4
        const val PLAYER = 5
        const val PLAYER_ON_GOAL = 6

        // 18 Màn chơi tuần tự đã kiểm thử khả năng giải được (Solvable)
        private val LEVEL_MAPS = listOf(
            // Màn 1 (1 hộp)
            """
            #####
            #  .#
            # $ #
            #@  #
            #####
            """.trimIndent(),
            // Màn 2 (1 hộp)
            """
            ######
            #@   #
            # #$ #
            #   .#
            ######
            """.trimIndent(),
            // Màn 3 (1 hộp)
            """
            #######
            #@  #.#
            #   # #
            # $   #
            #     #
            #######
            """.trimIndent(),
            // Màn 4 (2 hộp)
            """
            #######
            #@ $ .#
            # $  .#
            #######
            """.trimIndent(),
            // Màn 5 (2 hộp)
            """
            #######
            #@    #
            # $ $ #
            # ..  #
            #######
            """.trimIndent(),
            // Màn 6 (2 hộp)
            """
            #######
            #@ #  #
            # $ $ #
            # . . #
            #######
            """.trimIndent(),
            // Màn 7 (2 hộp)
            """
            ########
            #@ $  .#
            #  $  .#
            #      #
            ########
            """.trimIndent(),
            // Màn 8 (2 hộp)
            """
            ########
            #@  $ .#
            # $    #
            #   .  #
            ########
            """.trimIndent(),
            // Màn 9 (3 hộp, 3 mục tiêu)
            """
            ########
            #@ $  .#
            # $   .#
            # $   .#
            ########
            """.trimIndent(),
            // Màn 10 (3 hộp, 3 mục tiêu)
            """
            #########
            #@ $   .#
            # $    .#
            #  $   .#
            #########
            """.trimIndent(),
            // Màn 11 (3 hộp, 3 mục tiêu)
            """
            #########
            #@  $  .#
            # $    .#
            #   $  .#
            #########
            """.trimIndent(),
            // Màn 12 (3 hộp, 3 mục tiêu)
            """
            #######
            # .   #
            #@$ $ #
            # .$  #
            # .   #
            #######
            """.trimIndent(),
            // Màn 13 (3 hộp, 3 mục tiêu)
            """
            #######
            #  .  #
            # #$# #
            #@$ $ #
            # #.# #
            #  .  #
            #######
            """.trimIndent(),
            // Màn 14 (3 hộp, 3 mục tiêu)
            """
            ########
            #   .  #
            # @$$  #
            #  $ . #
            #  .   #
            ########
            """.trimIndent(),
            // Màn 15 (4 hộp, 4 mục tiêu)
            """
            ########
            #  ..  #
            #@$$$  #
            #  $   #
            #  ..  #
            ########
            """.trimIndent(),
            // Màn 16 (4 hộp, 4 mục tiêu)
            """
            ########
            #@ $ . #
            # $ $ .#
            #  $ . #
            #    . #
            ########
            """.trimIndent(),
            // Màn 17 (4 hộp, 4 mục tiêu)
            """
            #########
            #@ $  . #
            # $ $  .#
            #  $  . #
            #   .   #
            #########
            """.trimIndent(),
            // Màn 18 (5 hộp, 5 mục tiêu)
            """
            ##########
            #@ $ $ $ #
            #  . . . #
            #  $ $ . #
            #    .   #
            ##########
            """.trimIndent()
        )

        fun parseMap(mapStr: String): Array<IntArray> {
            val lines = mapStr.split("\n").filter { it.isNotEmpty() }
            val rows = lines.size
            val cols = lines[0].length
            return Array(rows) { r ->
                val line = lines[r]
                IntArray(cols) { c ->
                    val char = if (c < line.length) line[c] else ' '
                    when (char) {
                        '#' -> WALL
                        '.' -> GOAL
                        '$' -> BOX
                        '*' -> BOX_ON_GOAL
                        '@' -> PLAYER
                        '+' -> PLAYER_ON_GOAL
                        else -> FLOOR
                    }
                }
            }
        }
    }

    init {
        loadLevel()
    }

    fun loadLevel() {
        val levelIndexZeroBased = (levelIndex - 1).coerceIn(0, 17)
        val originalMap = parseMap(LEVEL_MAPS[levelIndexZeroBased])

        rows = originalMap.size
        cols = originalMap[0].size

        board = Array(rows) { r -> originalMap[r].copyOf() }
        initialBoard = Array(rows) { r -> originalMap[r].copyOf() }

        findPlayer()

        moves = 0
        pushes = 0
        isGameOver = false
        isVictory = false

        timeRemaining = when {
            levelIndex <= 5 -> 0      // Màn 1-5 vô hạn thời gian
            levelIndex <= 12 -> 300   // Màn 6-12 giới hạn 5 phút
            else -> 180              // Màn 13-18 giới hạn 3 phút
        }
        score = 0
    }

    private fun findPlayer() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c] == PLAYER || board[r][c] == PLAYER_ON_GOAL) {
                    playerRow = r
                    playerCol = c
                    return
                }
            }
        }
    }

    fun restoreState(
        savedBoard: Array<IntArray>,
        savedInitial: Array<IntArray>,
        savedTime: Int,
        savedScore: Int,
        savedMoves: Int,
        savedPushes: Int
    ) {
        board = Array(savedBoard.size) { r -> savedBoard[r].copyOf() }
        initialBoard = Array(savedInitial.size) { r -> savedInitial[r].copyOf() }
        rows = board.size
        cols = board[0].size
        timeRemaining = savedTime
        score = savedScore
        moves = savedMoves
        pushes = savedPushes
        findPlayer()
        checkVictory()
    }

    fun move(dRow: Int, dCol: Int): Boolean {
        if (isGameOver || isVictory) return false

        val targetRow = playerRow + dRow
        val targetCol = playerCol + dCol

        if (targetRow !in 0 until rows || targetCol !in 0 until cols) return false

        val targetCell = board[targetRow][targetCol]

        if (targetCell == FLOOR || targetCell == GOAL) {
            board[playerRow][playerCol] = if (board[playerRow][playerCol] == PLAYER_ON_GOAL) GOAL else FLOOR
            board[targetRow][targetCol] = if (targetCell == GOAL) PLAYER_ON_GOAL else PLAYER
            playerRow = targetRow
            playerCol = targetCol
            moves++
            return true
        }

        if (targetCell == BOX || targetCell == BOX_ON_GOAL) {
            val boxNextRow = targetRow + dRow
            val boxNextCol = targetCol + dCol

            if (boxNextRow !in 0 until rows || boxNextCol !in 0 until cols) return false

            val boxNextCell = board[boxNextRow][boxNextCol]

            if (boxNextCell == FLOOR || boxNextCell == GOAL) {
                board[boxNextRow][boxNextCol] = if (boxNextCell == GOAL) BOX_ON_GOAL else BOX
                board[targetRow][targetCol] = if (targetCell == BOX_ON_GOAL) PLAYER_ON_GOAL else PLAYER
                board[playerRow][playerCol] = if (board[playerRow][playerCol] == PLAYER_ON_GOAL) GOAL else FLOOR

                playerRow = targetRow
                playerCol = targetCol
                moves++
                pushes++
                checkVictory()
                return true
            }
        }

        return false
    }

    fun tickSecond() {
        if (isGameOver || isVictory || levelIndex <= 5) return

        if (timeRemaining > 0) {
            timeRemaining--
            if (timeRemaining == 0) {
                isGameOver = true
                calculateScore()
            }
        }
    }

    private fun checkVictory() {
        var boxesLeft = false
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c] == BOX) {
                    boxesLeft = true
                    break
                }
            }
        }

        if (!boxesLeft) {
            isVictory = true
            calculateScore()
        }
    }

    private fun calculateScore() {
        if (!isVictory) {
            score = 0
            return
        }

        val baseScore = 200
        val multiplier = when {
            levelIndex <= 5 -> 1
            levelIndex <= 12 -> 2
            else -> 5
        }

        val timeBonus = if (levelIndex > 5) {
            timeRemaining * when {
                levelIndex <= 12 -> 1
                else -> 3
            }
        } else {
            0
        }

        val efficiencyBonus = maxOf(0, 150 - moves)
        score = (baseScore + efficiencyBonus) * multiplier + timeBonus
    }

    fun serializeState(): String {
        val boardStr = board.joinToString(";") { it.joinToString(",") }
        val initialStr = initialBoard.joinToString(";") { it.joinToString(",") }
        return "$levelIndex|$moves|$pushes|$timeRemaining|$score|$boardStr|$initialStr"
    }
}
