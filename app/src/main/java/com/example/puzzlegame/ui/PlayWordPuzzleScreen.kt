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
    var isVictory by remember { mutableStateOf(false) }

    fun playWin() {
        audioManager.playClick()
        isVictory = true
    }

    // Thưởng XP/Coins khi thắng ở mỗi chế độ
    LaunchedEffect(isVictory, selectedTab) {
        if (isVictory) {
            val winKey = "wordpuzzle_win_credited_tab_$selectedTab"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(80, 15) // +80 XP, +15 Coins
                prefs.saveScore(winKey, 1)
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
                            isVictory = false
                        },
                        text = { Text("Nối Chữ", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 1
                            isVictory = false
                        },
                        text = { Text("Tráo Chữ", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            audioManager.playClick()
                            selectedTab = 2
                            isVictory = false
                        },
                        text = { Text("Tìm Chữ", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nội dung các Tab
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    when (selectedTab) {
                        0 -> WordConnectGame(onWin = { playWin() }, audioManager = audioManager)
                        1 -> AnagramGame(onWin = { playWin() }, audioManager = audioManager)
                        2 -> WordSearchGame(onWin = { playWin() }, audioManager = audioManager)
                    }
                }
            }
        }

        // Hộp thoại chiến thắng chúc mừng
        if (isVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            isVictory = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tiếp Tục", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        audioManager.playClick()
                        onBack()
                    }) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
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
                        Text("Bạn đã tìm đúng từ vựng tinh hoa văn hóa Việt Nam thành công!", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+80 XP và +15 Tiền vàng thưởng đã được thêm vào Bảo tàng Hồn Việt!",
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
// 1. CHẾ ĐỘ NỐI CHỮ (WORD CONNECT) - Tìm từ "LÀNG"
// ----------------------------------------------------
@Composable
fun WordConnectGame(onWin: () -> Unit, audioManager: AudioManager) {
    val targetWord = "LÀNG"
    var selectedLetters by remember { mutableStateOf("") }
    val lettersList = listOf("À", "L", "G", "N")

    fun onLetterClick(letter: String) {
        audioManager.playClick()
        if (selectedLetters.length < targetWord.length) {
            selectedLetters += letter
            if (selectedLetters == targetWord) {
                onWin()
            } else if (selectedLetters.length == targetWord.length) {
                // Nhập sai, tự động reset
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
            text = "Gợi ý: Đơn vị hành chính cấp cơ sở ở nông thôn Việt Nam 🏡",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Hiển thị từ đang chọn
        Box(
            modifier = Modifier
                .width(200.dp)
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
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, OutlineBrown, CircleShape)
                        .clickable { onLetterClick(letter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 20.sp,
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
// 2. CHẾ ĐỘ TRÁO CHỮ (ANAGRAM) - Tìm từ "NÓNLÁ"
// ----------------------------------------------------
@Composable
fun AnagramGame(onWin: () -> Unit, audioManager: AudioManager) {
    val targetWord = "NÓNLÁ"
    val shuffledList = remember { mutableStateListOf("Á", "N", "L", "Ó", "N") }
    val userOrder = remember { mutableStateListOf<String>() }

    fun addLetter(letter: String) {
        audioManager.playClick()
        userOrder.add(letter)
        shuffledList.remove(letter)

        if (userOrder.joinToString("") == targetWord) {
            onWin()
        } else if (shuffledList.isEmpty()) {
            // Không khớp, tự động reset
            shuffledList.clear()
            shuffledList.addAll(listOf("Á", "N", "L", "Ó", "N"))
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
            text = "Gợi ý: Vật che nắng mưa hình chóp làm bằng lá cọ 👒",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Kết quả lắp ghép của user
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(60.dp)
        ) {
            userOrder.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, BambooGreen, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BambooGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Các chữ cái lộn xộn để chọn
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            shuffledList.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineBrown, RoundedCornerShape(8.dp))
                        .clickable { addLetter(letter) },
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
            shuffledList.clear()
            shuffledList.addAll(listOf("Á", "N", "L", "Ó", "N"))
            userOrder.clear()
        }) {
            Text("Đặt lại", fontWeight = FontWeight.Bold, color = FlagRed)
        }
    }
}

// ----------------------------------------------------
// 3. CHẾ ĐỘ TÌM CHỮ (WORD SEARCH) - Tìm từ "PHỞ"
// ----------------------------------------------------
@Composable
fun WordSearchGame(onWin: () -> Unit, audioManager: AudioManager) {
    val wordToFind = "PHỞ"
    val grid = listOf(
        listOf("H", "X", "P", "T", "L", "M"),
        listOf("G", "A", "H", "O", "B", "R"),
        listOf("N", "N", "Ở", "M", "A", "P"),
        listOf("D", "U", "N", "A", "N", "O5"),
        listOf("A", "I", "C", "H", "U", "A"),
        listOf("T", "B", "N", "O", "N", "L")
    )
    val selectedCells = remember { mutableStateListOf<Pair<Int, Int>>() }

    fun onCellClick(r: Int, c: Int) {
        audioManager.playClick()
        val pos = Pair(r, c)
        if (selectedCells.contains(pos)) {
            selectedCells.remove(pos)
        } else {
            selectedCells.add(pos)
            // Lắp từ ghép từ các ô đã click theo đúng thứ tự chọn
            val word = selectedCells.map { grid[it.first][it.second] }.joinToString("")
            if (word == wordToFind) {
                onWin()
            } else if (word.length >= wordToFind.length) {
                // Nhập sai, reset
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
            text = "Từ cần tìm: $wordToFind (Món nước truyền thống bò/gà) 🍜",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = FlagRed
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
