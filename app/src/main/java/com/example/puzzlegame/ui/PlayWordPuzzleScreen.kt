package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

data class WordLevel(
    val target: String,
    val letters: List<String>,
    val hint: String
)

data class GridWordLevel(
    val target: String,
    val grid: List<List<String>>,
    val hint: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayWordPuzzleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var selectedTab by remember { mutableStateOf(0) } // 0: Nối chữ (Word Connect), 1: Sắp xếp (Anagram), 2: Tìm ô chữ (Word Search)
    
    // Level indices for the three tabs (0-indexed, up to 4)
    var connectLevelIdx by remember { mutableStateOf(0) }
    var anagramLevelIdx by remember { mutableStateOf(0) }
    var searchLevelIdx by remember { mutableStateOf(0) }

    var showLevelVictory by remember { mutableStateOf(false) }
    var showGameCompletedByTab by remember { mutableStateOf<Int?>(null) } // Tab index which completed all levels

    val connectLevels = remember {
        listOf(
            WordLevel("LÀNG", listOf("À", "L", "G", "N"), "Đơn vị hành chính cấp cơ sở ở nông thôn Việt Nam 🏡"),
            WordLevel("CỐM", listOf("M", "C", "Ố", "N"), "Đặc sản lúa non bọc lá sen của làng Vòng, Hà Nội 🌾"),
            WordLevel("CHÙA", listOf("Ù", "H", "A", "C", "I"), "Nơi thờ Phật trang nghiêm cổ kính ở làng quê Việt Nam ⛩️"),
            WordLevel("ĐÌNH", listOf("Ì", "Đ", "H", "N", "T"), "Nơi hội họp và thờ thành hoàng bản thổ của làng xã Việt Nam 🏛️"),
            WordLevel("TRÚC", listOf("Ú", "T", "C", "R", "U"), "Loại cây thân tre thanh mảnh biểu tượng cho khí chất quân tử 🎋")
        )
    }

    val anagramLevels = remember {
        listOf(
            WordLevel("NÓNLÁ", listOf("Á", "N", "L", "Ó", "N"), "Vật che nắng mưa hình chóp làm bằng lá cọ 👒"),
            WordLevel("ÁODÀI", listOf("Ì", "Á", "O", "D", "À"), "Trang phục truyền thống tôn vinh nét đẹp phụ nữ Việt 👗"),
            WordLevel("BÁNHCHƯNG", listOf("B", "Á", "N", "H", "C", "H", "Ư", "N", "G").shuffled(), "Món bánh Tết truyền thống bọc lá dong chứa nhân đậu xanh thịt mỡ 🟩"),
            WordLevel("CỒNGCHIÊNG", listOf("C", "Ồ", "N", "G", "C", "H", "I", "Ê", "N", "G").shuffled(), "Không gian di sản âm nhạc phi vật thể Tây Nguyên 🥁"),
            WordLevel("BẢOTÀNG", listOf("B", "Ả", "O", "T", "À", "N", "G").shuffled(), "Nơi lưu giữ tài liệu lịch sử, hiện vật di sản văn hóa tổ tiên 🏛️")
        )
    }

    val searchLevels = remember {
        listOf(
            GridWordLevel(
                target = "PHỞ",
                grid = listOf(
                    listOf("H", "X", "P", "T", "L", "M"),
                    listOf("G", "A", "H", "O", "B", "R"),
                    listOf("N", "N", "Ở", "M", "A", "P"),
                    listOf("D", "U", "N", "A", "N", "O"),
                    listOf("A", "I", "C", "H", "U", "A"),
                    listOf("T", "B", "N", "O", "N", "L")
                ),
                hint = "Món nước truyền thống bò/gà nổi tiếng thế giới 🍜"
            ),
            GridWordLevel(
                target = "SEN",
                grid = listOf(
                    listOf("S", "E", "N", "T", "L", "M"),
                    listOf("X", "A", "B", "O", "G", "R"),
                    listOf("M", "N", "G", "M", "A", "P"),
                    listOf("D", "U", "N", "A", "N", "O"),
                    listOf("A", "I", "C", "H", "U", "A"),
                    listOf("T", "B", "N", "O", "N", "L")
                ),
                hint = "Loài hoa thanh cao, quốc hoa biểu trưng cho nhà Phật 🌸"
            ),
            GridWordLevel(
                target = "BÁNHMÌ",
                grid = listOf(
                    listOf("B", "Á", "N", "H", "M", "Ì"),
                    listOf("X", "A", "B", "O", "G", "R"),
                    listOf("M", "N", "G", "M", "A", "P"),
                    listOf("D", "U", "N", "A", "N", "O"),
                    listOf("A", "I", "C", "H", "U", "A"),
                    listOf("T", "B", "N", "O", "N", "L")
                ),
                hint = "Món ăn đường phố Việt Nam lọt top ngon nhất thế giới 🥖"
            ),
            GridWordLevel(
                target = "ÁODÀI",
                grid = listOf(
                    listOf("Á", "X", "Y", "Z", "W", "K"),
                    listOf("O", "D", "G", "O", "B", "R"),
                    listOf("D", "N", "À", "M", "A", "P"),
                    listOf("À", "U", "N", "I", "N", "O"),
                    listOf("I", "I", "C", "H", "U", "A"),
                    listOf("T", "B", "N", "O", "N", "L")
                ),
                hint = "Trang phục cổ truyền thướt tha hai tà áo lụa Việt Nam 👗"
            ),
            GridWordLevel(
                target = "CỒNG",
                grid = listOf(
                    listOf("C", "Ồ", "N", "G", "L", "M"),
                    listOf("X", "A", "B", "O", "G", "R"),
                    listOf("M", "N", "G", "M", "A", "P"),
                    listOf("D", "U", "N", "A", "N", "O"),
                    listOf("A", "I", "C", "H", "U", "A"),
                    listOf("T", "B", "N", "O", "N", "L")
                ),
                hint = "Nhạc cụ đúc bằng đồng tiêu biểu của Tây Nguyên 🔔"
            )
        )
    }

    fun onLevelSolved() {
        audioManager.playClick()
        
        // Thưởng điểm XP/Coins sau mỗi level
        prefs.addXpAndCoins(50, 10)
        
        showLevelVictory = true
    }

    fun handleNextLevel() {
        showLevelVictory = false
        when (selectedTab) {
            0 -> {
                if (connectLevelIdx < connectLevels.size - 1) {
                    connectLevelIdx++
                } else {
                    showGameCompletedByTab = 0
                }
            }
            1 -> {
                if (anagramLevelIdx < anagramLevels.size - 1) {
                    anagramLevelIdx++
                } else {
                    showGameCompletedByTab = 1
                }
            }
            2 -> {
                if (searchLevelIdx < searchLevels.size - 1) {
                    searchLevelIdx++
                } else {
                    showGameCompletedByTab = 2
                }
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("GIẢI CÂU ĐỐ CHỮ VIỆT", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = FlagRed
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 0
                            showLevelVictory = false
                        },
                        text = { Text("Nối Chữ", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 1
                            showLevelVictory = false
                        },
                        text = { Text("Tráo Chữ", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 2
                            showLevelVictory = false
                        },
                        text = { Text("Tìm Chữ", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị cấp độ hiện tại
                val currentLevelText = when (selectedTab) {
                    0 -> "Màn ${connectLevelIdx + 1}/${connectLevels.size}"
                    1 -> "Màn ${anagramLevelIdx + 1}/${anagramLevels.size}"
                    else -> "Màn ${searchLevelIdx + 1}/${searchLevels.size}"
                }
                Text(
                    text = currentLevelText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FlagRed,
                    modifier = Modifier.align(Alignment.End)
                )

                // Nội dung các Tab
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    when (selectedTab) {
                        0 -> {
                            key(connectLevelIdx) {
                                WordConnectGame(
                                    level = connectLevels[connectLevelIdx],
                                    onWin = { onLevelSolved() },
                                    audioManager = audioManager
                                )
                            }
                        }
                        1 -> {
                            key(anagramLevelIdx) {
                                AnagramGame(
                                    level = anagramLevels[anagramLevelIdx],
                                    onWin = { onLevelSolved() },
                                    audioManager = audioManager
                                )
                            }
                        }
                        2 -> {
                            key(searchLevelIdx) {
                                WordSearchGame(
                                    level = searchLevels[searchLevelIdx],
                                    onWin = { onLevelSolved() },
                                    audioManager = audioManager
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hộp thoại thắng màn chơi đơn
        if (showLevelVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = { handleNextLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tiếp Tục", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        "🎉 ĐÁP ÁN CHÍNH XÁC!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = BambooGreen
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chúc mừng bạn đã giải câu đố chữ Việt thành công!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+50 XP và +10 Tiền vàng thưởng đã được ghi nhận!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BambooGreen
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }

        // Hộp thoại hoàn thành toàn bộ tất cả màn chơi
        showGameCompletedByTab?.let { tabIdx ->
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            showGameCompletedByTab = null
                            when (tabIdx) {
                                0 -> connectLevelIdx = 0
                                1 -> anagramLevelIdx = 0
                                2 -> searchLevelIdx = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BambooGreen)
                    ) {
                        Text("Chơi Lại Từ Đầu", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        audioManager.playClick()
                        showGameCompletedByTab = null
                        onBack()
                    }) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        "🏆 ĐÃ PHÁ ĐẢO CHẾ ĐỘ!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = FlagRed
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tuyệt đỉnh trí tuệ! Bạn đã vượt qua tất cả 5 màn chơi khó nhằn của chế độ này!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+200 XP và +40 Tiền vàng thưởng siêu cấp đã được ghi danh vào Bảo tàng Hồn Việt!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BambooGreen
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
            )
        }
    }
}

// ----------------------------------------------------
// 1. CHẾ ĐỘ NỐI CHỮ (WORD CONNECT)
// ----------------------------------------------------
@Composable
fun WordConnectGame(
    level: WordLevel,
    onWin: () -> Unit,
    audioManager: AudioManager
) {
    var selectedLetters by remember { mutableStateOf("") }
    val lettersList = remember { level.letters }

    fun onLetterClick(letter: String) {
        audioManager.playClick()
        if (selectedLetters.length < level.target.length) {
            selectedLetters += letter
            if (selectedLetters == level.target) {
                onWin()
            } else if (selectedLetters.length == level.target.length) {
                selectedLetters = ""
                audioManager.playError()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Ghép các chữ cái để tạo thành từ:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gợi ý: ${level.hint}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Hiển thị từ đang chọn
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceNormal)
                .border(2.dp, OutlineBrown, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedLetters,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = FlagRed
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Đĩa tròn chữ cái
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lettersList.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, OutlineBrown, CircleShape)
                        .clickable { onLetterClick(letter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EarthyBrown
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = {
            audioManager.playClick()
            selectedLetters = ""
        }) {
            Text("Xóa hết chữ", fontWeight = FontWeight.Bold, color = FlagRed)
        }
    }
}

// ----------------------------------------------------
// 2. CHẾ ĐỘ TRÁO CHỮ (ANAGRAM)
// ----------------------------------------------------
@Composable
fun AnagramGame(
    level: WordLevel,
    onWin: () -> Unit,
    audioManager: AudioManager
) {
    val targetWord = level.target
    val shuffledList = remember { mutableStateListOf<String>().apply { addAll(level.letters) } }
    val userOrder = remember { mutableStateListOf<String>() }

    fun addLetter(letter: String) {
        audioManager.playClick()
        userOrder.add(letter)
        shuffledList.remove(letter)

        if (userOrder.joinToString("") == targetWord) {
            onWin()
        } else if (shuffledList.isEmpty()) {
            shuffledList.clear()
            shuffledList.addAll(level.letters)
            userOrder.clear()
            audioManager.playError()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Nhấp chọn chữ cái theo đúng thứ tự từ vựng:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gợi ý: ${level.hint}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Kết quả lắp ghép của user
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(60.dp)
        ) {
            userOrder.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, BambooGreen, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BambooGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Các chữ cái lộn xộn để chọn
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shuffledList.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.5.dp, OutlineBrown, RoundedCornerShape(8.dp))
                        .clickable { addLetter(letter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EarthyBrown
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = {
            audioManager.playClick()
            shuffledList.clear()
            shuffledList.addAll(level.letters)
            userOrder.clear()
        }) {
            Text("Đặt lại", fontWeight = FontWeight.Bold, color = FlagRed)
        }
    }
}

// ----------------------------------------------------
// 3. CHẾ ĐỘ TÌM CHỮ (WORD SEARCH)
// ----------------------------------------------------
@Composable
fun WordSearchGame(
    level: GridWordLevel,
    onWin: () -> Unit,
    audioManager: AudioManager
) {
    val wordToFind = level.target
    val grid = remember { level.grid }
    val selectedCells = remember { mutableStateListOf<Pair<Int, Int>>() }

    fun onCellClick(r: Int, c: Int) {
        audioManager.playClick()
        val pos = Pair(r, c)
        if (selectedCells.contains(pos)) {
            selectedCells.remove(pos)
        } else {
            selectedCells.add(pos)
            val word = selectedCells.map { grid[it.first][it.second] }.joinToString("")
            if (word == wordToFind) {
                onWin()
            } else if (word.length >= wordToFind.length) {
                selectedCells.clear()
                audioManager.playError()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Tìm từ vựng ẩn giấu trên lưới chữ:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Từ cần tìm: $wordToFind (${level.hint})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = FlagRed,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Lưới ô chữ 6x6
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceNormal)
                .border(2.dp, OutlineBrown, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0 until 6) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0 until 6) {
                            val letter = grid[r][c]
                            val isSelected = selectedCells.contains(Pair(r, c))

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) FlagRed.copy(alpha = 0.2f) else Color.White
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) FlagRed else OutlineBrown.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onCellClick(r, c) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) FlagRed else EarthyBrown
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
