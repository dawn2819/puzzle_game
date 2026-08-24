package com.example.puzzlegame.engine

import org.junit.Assert.*
import org.junit.Test

class EngineTest {

    @Test
    fun testEngine2048_SlideAndMerge() {
        val engine = Engine2048(4)
        // Cấu hình ma trận tùy chỉnh để kiểm tra di chuyển
        val customBoard = arrayOf(
            intArrayOf(2, 2, 0, 0),
            intArrayOf(2, 0, 2, 0),
            intArrayOf(4, 4, 4, 0),
            intArrayOf(2, 4, 8, 16)
        )
        engine.restoreState(customBoard, 100)

        // Thực hiện di chuyển sang trái (dir = 0)
        engine.move(0)

        // Hàng 0: [2, 2, 0, 0] -> [4, 0, 0, 0] (gộp 2+2)
        assertEquals(4, engine.board[0][0])

        // Hàng 1: [2, 0, 2, 0] -> [4, 0, 0, 0] (gộp 2+2 qua ô trống)
        assertEquals(4, engine.board[1][0])

        // Hàng 2: [4, 4, 4, 0] -> [8, 4, 0, 0] (chỉ gộp 4+4 đầu tiên)
        assertEquals(8, engine.board[2][0])
        assertEquals(4, engine.board[2][1])
    }

    @Test
    fun testEngineSudoku_Validation() {
        val engine = EngineSudoku(EngineSudoku.DIFFICULTY_EASY)
        
        // Đảm bảo solution sinh ra hợp lệ
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val valSol = engine.solution[r][c]
                assertTrue(valSol in 1..9)
            }
        }

        // Kiểm tra các hàng/cột/khối của solution không bị trùng lắp
        for (r in 0 until 9) {
            val rowSet = mutableSetOf<Int>()
            for (c in 0 until 9) {
                rowSet.add(engine.solution[r][c])
            }
            assertEquals(9, rowSet.size)
        }
    }

    @Test
    fun testEngineSokoban_Movement() {
        // Màn 1 mới có kích thước 5x5
        val engine = EngineSokoban(1)
        
        // Vị trí xuất phát của người chơi trong màn 1 mới là (3, 1)
        assertEquals(3, engine.playerRow)
        assertEquals(1, engine.playerCol)

        // Di chuyển người chơi lên trên (dRow=-1, dCol=0)
        // Ô bên trên (2, 1) là sàn trống -> di chuyển được
        val moved = engine.move(-1, 0)
        assertTrue(moved)
        assertEquals(2, engine.playerRow)
        assertEquals(1, engine.playerCol)
    }

    @Test
    fun testEngineNonogram_Clues() {
        // Kiểm tra logic sinh gợi ý clues của ảnh Trái tim 8x8 (Màn 1)
        val engine = EngineNonogram(1)
        
        // Hàng 0 của Trái tim: [0, 1, 1, 0, 0, 1, 1, 0] -> Clues phải là [2, 2]
        val expectedRow0 = listOf(2, 2)
        assertEquals(expectedRow0, engine.rowClues[0])

        // Hàng 3: [0, 1, 1, 1, 1, 1, 1, 0] -> Clues phải là [6]
        val expectedRow3 = listOf(6)
        assertEquals(expectedRow3, engine.rowClues[3])
    }
}
