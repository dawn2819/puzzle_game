package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.lazy.LazyRow
import androidx.navigation3.runtime.NavKey
import com.example.puzzlegame.*
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.theme.*
import androidx.compose.foundation.Image
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.puzzlegame.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectionScreen(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var selectedGameType by remember { mutableStateOf<String?>(null) }

    // State cho 2048
    var size2048 by remember { mutableStateOf(4) }

    // State cho Sudoku
    var difficultySudoku by remember { mutableStateOf("EASY") }

    // State cho Sokoban
    var difficultySokoban by remember { mutableStateOf("EASY") }

    // State cho Nonogram
    var sizeNonogram by remember { mutableStateOf(8) }
    var artIndexNonogram by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    PremiumBackground(drawableId = R.drawable.bg_selection, bgDimAlpha = 0.35f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CHỌN TRÒ CHƠI", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            var selectedTab by remember { mutableStateOf(0) }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedGameType == null) {
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
                            },
                            text = { Text("Quốc Tế", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                audioManager.playClick()
                                selectedTab = 1
                            },
                            text = { Text("Dân Gian", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                audioManager.playClick()
                                selectedTab = 2
                            },
                            text = { Text("Trí Tuệ", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    when (selectedTab) {
                        0 -> {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                item {
                                    GameCard(
                                        title = "2048",
                                        description = "Trượt gộp ô số",
                                        onClick = {
                                            audioManager.playClick()
                                            selectedGameType = "2048"
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "SUDOKU",
                                        description = "Điền số logic 9x9",
                                        onClick = {
                                            audioManager.playClick()
                                            selectedGameType = "SUDOKU"
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "SOKOBAN",
                                        description = "Đẩy hộp vào kho",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlaySokoban(-1))
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "NONOGRAM",
                                        description = "Giải đố tô tranh pixel",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayNonogram(-1))
                                        }
                                    )
                                }
                            }
                        }

                        1 -> {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                item {
                                    GameCard(
                                        title = "Ô ĂN QUAN",
                                        description = "Rải sỏi ăn dân & quan",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayOAnQuan)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "CỜ GÁNH",
                                        description = "Vây chẹt đổi màu cờ",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayCoGanh)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "RỒNG RẮN",
                                        description = "Uốn lượn ngậm ngọc rồng",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayDragonSnake)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "NHẢY LÒ CÒ",
                                        description = "Nhảy ô lò cò 1-9",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayLofo)
                                        }
                                    )
                                }
                            }
                        }

                        2 -> {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                item {
                                    GameCard(
                                        title = "THẢ ĐỈA",
                                        description = "Lội sông né đỉa đuổi bám",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayThaSua)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "BẮT DÊ",
                                        description = "Dò tìm dê ẩn nấp",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayBitMatDe)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "ĐỐ CHỮ",
                                        description = "Tìm ô chữ ẩm thực Việt",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayWordPuzzle)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "CHƠI CHUYỀN",
                                        description = "Chuỗi nhấp nháy hạt gỗ",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlaySequence)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "ĐẬP NIÊU",
                                        description = "Đập niêu đất bằng suy luận",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayDapNieu)
                                        }
                                    )
                                }
                                item {
                                    GameCard(
                                        title = "NÉM CÒN",
                                        description = "Ném còn bay chịu sức gió",
                                        onClick = {
                                            audioManager.playClick()
                                            onNavigate(PlayNemCon)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Màn hình chọn Cấu hình chi tiết
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedGameType!!,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (selectedGameType) {
                                        "2048" -> MaterialTheme.colorScheme.primary
                                        "SUDOKU" -> MaterialTheme.colorScheme.secondary
                                        "SOKOBAN" -> MaterialTheme.colorScheme.tertiary
                                        else -> Color(0xFF10B981)
                                    }
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                when (selectedGameType) {
                                    "2048" -> {
                                        Text("Chọn kích thước ma trận:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            (4..8).forEach { size ->
                                                val isSelected = size2048 == size
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(50.dp)
                                                        .clickable {
                                                            audioManager.playClick()
                                                            size2048 = size
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                        ),
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                            Text("${size}x${size}", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "SUDOKU" -> {
                                        Text("Chọn độ khó:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(
                                                Triple("EASY", "Dễ (Không giới hạn thời gian)", MaterialTheme.colorScheme.primary),
                                                Triple("MEDIUM", "Trung bình (Giới hạn 20 phút)", MaterialTheme.colorScheme.secondary),
                                                Triple("HARD", "Khó (Giới hạn 10 phút)", MaterialTheme.colorScheme.error)
                                            ).forEach { (diff, desc, color) ->
                                                val isSelected = difficultySudoku == diff
                                                Card(
                                                    onClick = {
                                                        audioManager.playClick()
                                                        difficultySudoku = diff
                                                    },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().height(60.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                                                        Text(desc, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "SOKOBAN" -> {
                                        Text("Chọn độ khó:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(
                                                Triple("EASY", "Dễ (*1 điểm, Không giới hạn)", MaterialTheme.colorScheme.primary),
                                                Triple("MEDIUM", "Trung bình (*2 điểm, Giới hạn 5 phút)", MaterialTheme.colorScheme.secondary),
                                                Triple("HARD", "Khó (*5 điểm, Giới hạn 3 phút)", MaterialTheme.colorScheme.error)
                                            ).forEach { (diff, desc, color) ->
                                                val isSelected = difficultySokoban == diff
                                                Card(
                                                    onClick = {
                                                        audioManager.playClick()
                                                        difficultySokoban = diff
                                                    },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().height(60.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                                                        Text(desc, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "NONOGRAM" -> {
                                        Text("Kích thước lưới tranh:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(8, 10, 12).forEach { size ->
                                                val isSelected = sizeNonogram == size
                                                Card(
                                                    onClick = {
                                                        audioManager.playClick()
                                                        sizeNonogram = size
                                                        artIndexNonogram = 0 // Reset tranh
                                                    },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    modifier = Modifier.weight(1f).height(50.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text("${size}x${size}", fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text("Chọn hình vẽ nghệ thuật:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(0, 1).forEach { index ->
                                                val name = when (sizeNonogram) {
                                                    8 -> if (index == 0) "Trái tim" else "Mặt cười"
                                                    10 -> if (index == 0) "Cây thông" else "Thanh kiếm"
                                                    12 -> if (index == 0) "Ngôi sao" else "Cái khiên"
                                                    else -> "Mẫu $index"
                                                }
                                                val isSelected = artIndexNonogram == index
                                                Card(
                                                    onClick = {
                                                        audioManager.playClick()
                                                        artIndexNonogram = index
                                                    },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    modifier = Modifier.weight(1f).height(55.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text(name, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Điều hướng / Quay lại
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        audioManager.playClick()
                                        selectedGameType = null
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("Quay lại", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        audioManager.playClick()
                                        when (selectedGameType) {
                                            "2048" -> onNavigate(Play2048(size2048))
                                            "SUDOKU" -> onNavigate(PlaySudoku(difficultySudoku))
                                            "SOKOBAN" -> onNavigate(PlaySokoban(1))
                                            "NONOGRAM" -> onNavigate(PlayNonogram(1))
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("Bắt đầu", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.frame_scroll_banner),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontFamily = NomFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = FlagRed,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color(0x60000000),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                fontFamily = NomFontFamily,
                fontWeight = FontWeight.Bold,
                color = EarthyBrown.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
