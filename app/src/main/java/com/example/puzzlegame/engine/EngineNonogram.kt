package com.example.puzzlegame.engine

class EngineNonogram(val levelIndex: Int) {
    var size = 8
    var solution: Array<IntArray> = Array(0) { IntArray(0) }
    // 0: Empty, 1: Filled, 2: Crossed (X)
    var currentBoard: Array<IntArray> = Array(0) { IntArray(0) }

    var rowClues: List<List<Int>> = emptyList()
    var colClues: List<List<Int>> = emptyList()

    var score = 0
    var isVictory = false
    var isGameOver = false
    var mistakesCount = 0

    companion object {
        // 15 Màn chơi Nonogram thiết kế bằng văn bản gọn gàng
        private val LEVEL_MAPS = listOf(
            // Màn 1: Trái tim (8x8)
            """
             ##  ## 
            ########
            ########
             ###### 
              ####  
               ##   
            """.trimIndent(),
            // Màn 2: Mặt cười (8x8)
            """
              ####  
             #    # 
            # #  # #
            #      #
            # #  # #
            #  ##  #
             #    # 
              ####  
            """.trimIndent(),
            // Màn 3: Dấu tích (8x8)
            """
                   #
                  ##
                 ## 
            #   ##  
            ## ##   
             ###    
              #     
            """.trimIndent(),
            // Màn 4: Cái cốc (8x8)
            """
            ########
            ########
             #    # 
             ###### 
               ##   
               ##   
              ####  
             ###### 
            """.trimIndent(),
            // Màn 5: Ngôi nhà (8x8)
            """
               ##   
              ####  
             ###### 
            ########
             ##  ## 
             ###### 
             ##  ## 
             ###### 
            """.trimIndent(),
            // Màn 6: Cây thông (10x10)
            """
                ##    
               ####   
              ######  
               ####   
              ######  
             ######## 
               ####   
              ######  
                ##    
                ##    
            """.trimIndent(),
            // Màn 7: Thanh kiếm (10x10)
            """
                    ##
                  ### 
                 ###  
                ###   
               ###    
              ###     
             ###      
            ###       
            ##        
            #         
            """.trimIndent(),
            // Màn 8: Mỏ neo (10x10)
            """
                ##    
                ##    
              ######  
                ##    
                ##    
            #   ##   #
            ##  ##  ##
            ##########
              ####  
               ##   
            """.trimIndent(),
            // Màn 9: Mặt chú hề (10x10)
            """
              ######  
             #      # 
            #  #  #  #
            #        #
            #  ####  #
            # #    # #
            #        #
             #      # 
              ######  
            """.trimIndent(),
            // Màn 10: Vương miện (10x10)
            """
            #   ##   #
            ##  ##  ##
            ##########
            ##########
             ######## 
              ######  
              ######  
             ######## 
            ##########
            ##########
            """.trimIndent(),
            // Màn 11: Ngôi sao (12x12)
            """
                 ##     
                ####    
                ####    
            ############
             ########## 
              ########  
               ######   
              ########  
             # ##  ## #
            ## ##  ## ##
            ##        ##
            """.trimIndent(),
            // Màn 12: Cái khiên (12x12)
            """
            ############
            ############
            #    ##    #
            #    ##    #
            #    ##    #
             #   ##   # 
             #   ##   # 
              #  ##  #  
              #  ##  #  
               # ## #   
                ####    
                 ##     
            """.trimIndent(),
            // Màn 13: Lâu đài (12x12)
            """
            # #  ##  # #
            ###  ##  ###
            ############
            ############
            #   #  #   #
            #   #  #   #
            #   #  #   #
            ############
            ####    ####
            ####    ####
            ############
            ############
            """.trimIndent(),
            // Màn 14: Bông hoa (12x12)
            """
                ####    
              ########  
              ########  
             ########## 
            #####  #####
            ####    ####
            ####    ####
            #####  #####
             ########## 
              ########  
              ########  
                ####    
            """.trimIndent(),
            // Màn 15: Con bướm (12x12)
            """
            ##        ##
            ###  ##  ###
            ###  ##  ###
             #########  
              #######   
               #####    
               #####    
              #######   
             #########  
            ###  ##  ###
            ###  ##  ###
            ##        ##
            """.trimIndent()
        )

        fun parseMap(mapStr: String): Pair<Int, Array<IntArray>> {
            val lines = mapStr.split("\n").filter { it.isNotEmpty() }
            val rows = lines.size
            val cols = lines.maxOf { it.length }
            val size = maxOf(rows, cols)
            val grid = Array(size) { r ->
                val line = if (r < lines.size) lines[r] else ""
                IntArray(size) { c ->
                    val char = if (c < line.length) line[c] else ' '
                    if (char == '#') 1 else 0
                }
            }
            return Pair(size, grid)
        }
    }

    init {
        loadLevel()
    }

    fun loadLevel() {
        val levelIndexZeroBased = (levelIndex - 1).coerceIn(0, 14)
        val (parsedSize, parsedGrid) = parseMap(LEVEL_MAPS[levelIndexZeroBased])
        size = parsedSize
        solution = Array(size) { r -> parsedGrid[r].copyOf() }
        currentBoard = Array(size) { IntArray(size) }
        isVictory = false
        isGameOver = false
        mistakesCount = 0
        score = 0

        calculateClues()
    }

    fun restoreState(savedCurrentBoard: Array<IntArray>, savedScore: Int, savedMistakes: Int) {
        currentBoard = Array(size) { r -> savedCurrentBoard[r].copyOf() }
        score = savedScore
        mistakesCount = savedMistakes
        if (mistakesCount >= 3) {
            isGameOver = true
        }
        checkVictory()
    }

    // Toggle/Click ô với luật lỗi Nonogram. playMode: 1 = Tô đen, 2 = Đánh dấu X
    fun selectCell(r: Int, c: Int, playMode: Int): Boolean {
        if (isVictory || isGameOver) return false
        if (r !in 0 until size || c !in 0 until size) return false
        if (currentBoard[r][c] != 0) return false // Ô đã có đáp án

        val correctVal = solution[r][c]

        if (playMode == 1) { // Người chơi muốn Tô Đen
            if (correctVal == 1) {
                currentBoard[r][c] = 1
            } else {
                // Sai! Ô này phải để trống. Tự động đổi thành X (2), mất 1 tim
                currentBoard[r][c] = 2
                mistakesCount++
                if (mistakesCount >= 3) {
                    isGameOver = true
                }
                return false
            }
        } else { // Người chơi muốn đánh dấu X
            if (correctVal == 0) {
                currentBoard[r][c] = 2
            } else {
                // Sai! Ô này phải tô đen. Tự động đổi thành tô đen (1), mất 1 tim
                currentBoard[r][c] = 1
                mistakesCount++
                if (mistakesCount >= 3) {
                    isGameOver = true
                }
                return false
            }
        }

        checkVictory()
        return true
    }

    fun isRowCompleted(r: Int): Boolean {
        if (r !in 0 until size) return false
        for (c in 0 until size) {
            val isFilledInSol = solution[r][c] == 1
            val isFilledInUser = currentBoard[r][c] == 1
            if (isFilledInSol != isFilledInUser) {
                return false
            }
        }
        return true
    }

    fun isColCompleted(c: Int): Boolean {
        if (c !in 0 until size) return false
        for (r in 0 until size) {
            val isFilledInSol = solution[r][c] == 1
            val isFilledInUser = currentBoard[r][c] == 1
            if (isFilledInSol != isFilledInUser) {
                return false
            }
        }
        return true
    }

    private fun checkVictory() {
        var match = true
        for (r in 0 until size) {
            for (c in 0 until size) {
                val isFilledInSol = solution[r][c] == 1
                val isFilledInUser = currentBoard[r][c] == 1
                if (isFilledInSol != isFilledInUser) {
                    match = false
                    break
                }
            }
        }

        if (match) {
            isVictory = true
            score = when (size) {
                8 -> 300
                10 -> 500
                12 -> 800
                else -> 300
            }
        }
    }

    private fun calculateClues() {
        val tempRowClues = mutableListOf<List<Int>>()
        for (r in 0 until size) {
            val clues = mutableListOf<Int>()
            var count = 0
            for (c in 0 until size) {
                if (solution[r][c] == 1) {
                    count++
                } else {
                    if (count > 0) {
                        clues.add(count)
                        count = 0
                    }
                }
            }
            if (count > 0) {
                clues.add(count)
            }
            if (clues.isEmpty()) clues.add(0)
            tempRowClues.add(clues)
        }
        rowClues = tempRowClues

        val tempColClues = mutableListOf<List<Int>>()
        for (c in 0 until size) {
            val clues = mutableListOf<Int>()
            var count = 0
            for (r in 0 until size) {
                if (solution[r][c] == 1) {
                    count++
                } else {
                    if (count > 0) {
                        clues.add(count)
                        count = 0
                    }
                }
            }
            if (count > 0) {
                clues.add(count)
            }
            if (clues.isEmpty()) clues.add(0)
            tempColClues.add(clues)
        }
        colClues = tempColClues
    }

    fun getArtName(): String {
        return when (levelIndex) {
            1 -> "Trái tim"
            2 -> "Mặt cười"
            3 -> "Dấu tích"
            4 -> "Cái cốc"
            5 -> "Ngôi nhà"
            6 -> "Cây thông"
            7 -> "Thanh kiếm"
            8 -> "Mỏ neo"
            9 -> "Chú hề"
            10 -> "Vương miện"
            11 -> "Ngôi sao"
            12 -> "Cái khiên"
            13 -> "Lâu đài"
            14 -> "Bông hoa"
            15 -> "Con bướm"
            else -> "Nghệ thuật"
        }
    }

    fun serializeState(): String {
        val boardStr = currentBoard.joinToString(";") { it.joinToString(",") }
        return "$levelIndex|$score|$mistakesCount|$boardStr"
    }
}
