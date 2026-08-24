package com.example.puzzlegame.engine

class EngineDragonSnake {
    val size = 8 // Bản đồ ma trận 8x8 cho câu đố cân đối trên di động

    var snake = mutableListOf<Pair<Int, Int>>() // Vị trí (r, c) của đầu rồng (phần tử 0) và thân rồng
    val walls = mutableSetOf<Pair<Int, Int>>()
    val gems = mutableSetOf<Pair<Int, Int>>()
    val traps = mutableSetOf<Pair<Int, Int>>()
    var portal: Pair<Int, Int> = Pair(7, 7)

    var isGameOver = false
    var isVictory = false
    var moves = 0
    var level = 1

    init {
        loadLevel(1)
    }

    fun loadLevel(lvl: Int) {
        level = lvl
        snake.clear()
        walls.clear()
        gems.clear()
        traps.clear()
        isGameOver = false
        isVictory = false
        moves = 0

        // Thiết lập các map màn chơi khác nhau đậm nét làng quê Việt
        when (lvl) {
            1 -> {
                // Màn 1: Tập bò cơ bản
                snake.add(Pair(1, 1))
                snake.add(Pair(1, 2))
                portal = Pair(6, 6)
                
                // Ngọc
                gems.add(Pair(3, 3))
                gems.add(Pair(5, 5))
            }
            2 -> {
                // Màn 2: Lũy tre cản lối (Walls)
                snake.add(Pair(1, 1))
                snake.add(Pair(1, 2))
                portal = Pair(7, 7)

                // Lũy tre (Tường cản)
                for (c in 2..5) walls.add(Pair(3, c))
                walls.add(Pair(4, 5))

                // Ngọc
                gems.add(Pair(2, 5))
                gems.add(Pair(5, 2))
                gems.add(Pair(6, 6))
            }
            3 -> {
                // Màn 3: Ao sen và Bẫy đinh
                snake.add(Pair(0, 0))
                snake.add(Pair(1, 0))
                portal = Pair(7, 5)

                // Ao sen cản lối (Walls)
                walls.add(Pair(2, 2))
                walls.add(Pair(2, 3))
                walls.add(Pair(3, 2))
                walls.add(Pair(3, 3))

                // Bẫy đinh nguy hiểm (Traps)
                traps.add(Pair(4, 4))
                traps.add(Pair(1, 4))
                traps.add(Pair(5, 1))

                // Ngọc
                gems.add(Pair(0, 5))
                gems.add(Pair(4, 1))
                gems.add(Pair(6, 3))
            }
            4 -> {
                // Màn 4: Đường quanh co
                snake.add(Pair(0, 0))
                snake.add(Pair(0, 1))
                portal = Pair(7, 0)

                // Vách đá (Walls)
                for (r in 0..5) walls.add(Pair(r, 3))
                for (r in 2..7) walls.add(Pair(r, 5))

                // Ngọc
                gems.add(Pair(5, 1))
                gems.add(Pair(2, 4))
                gems.add(Pair(6, 7))
            }
            else -> {
                // Màn 5: Thử thách cực hạn
                snake.add(Pair(3, 3))
                snake.add(Pair(3, 4))
                portal = Pair(0, 0)

                // Khắp nơi là bẫy và tường cản
                walls.add(Pair(1, 1))
                walls.add(Pair(1, 2))
                walls.add(Pair(5, 5))
                walls.add(Pair(5, 6))
                
                traps.add(Pair(2, 2))
                traps.add(Pair(4, 4))
                traps.add(Pair(6, 1))
                traps.add(Pair(1, 6))

                // Ngọc
                gems.add(Pair(0, 7))
                gems.add(Pair(7, 0))
                gems.add(Pair(7, 7))
            }
        }
    }

    // 0: Trái, 1: Lên, 2: Phải, 3: Xuống
    fun move(direction: Int): Boolean {
        if (isGameOver || isVictory) return false

        val head = snake[0]
        var nextR = head.first
        var nextC = head.second

        when (direction) {
            0 -> nextC -= 1 // Trái
            1 -> nextR -= 1 // Lên
            2 -> nextC += 1 // Phải
            3 -> nextR += 1 // Xuống
        }

        val target = Pair(nextR, nextC)

        // 1. Kiểm tra va chạm tường bao và lũy tre cản
        if (nextR !in 0 until size || nextC !in 0 until size) {
            return false // Di chuyển không hợp lệ
        }
        if (walls.contains(target)) {
            return false // Đâm đầu vào lũy tre cản
        }

        // 2. Kiểm tra đâm vào thân rồng (tự va chạm)
        // Lưu ý: Nếu ô tiếp theo là đuôi rồng và ta không ăn ngọc thì đuôi sẽ co lại, nhưng để đơn giản trong puzzle thì ta cấm tuyệt đối chạm thân
        if (snake.contains(target)) {
            isGameOver = true
            return true
        }

        // 3. Kiểm tra dẫm bẫy đinh
        if (traps.contains(target)) {
            isGameOver = true
            snake.add(0, target) // Đầu rồng dẫm bẫy
            return true
        }

        moves++

        // 4. Kiểm tra ăn Ngọc rồng
        if (gems.contains(target)) {
            gems.remove(target)
            // Phát triển thân dài ra: thêm đầu mới và không xóa đuôi cũ
            snake.add(0, target)
        } else {
            // Đi bình thường: thêm đầu mới và xóa đuôi cũ
            snake.add(0, target)
            snake.removeAt(snake.size - 1)
        }

        // 5. Kiểm tra đến đúng cổng làng đích
        if (target == portal) {
            // Phải ăn hết ngọc thì mới qua màn thành công
            if (gems.isEmpty()) {
                isVictory = true
            }
        }

        return true
    }
}
