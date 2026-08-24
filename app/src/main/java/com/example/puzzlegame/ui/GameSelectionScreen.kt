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
import androidx.navigation3.runtime.NavKey
import com.example.puzzlegame.*
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.audio.AudioManager

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

    PremiumBackground {
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
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedGameType == null) {
                    // Màn hình chọn Grid 4 Game
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            GameCard(
                                title = "2048",
                                description = "Trượt gộp ô số",
                                color = MaterialTheme.colorScheme.primary,
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
                                color = MaterialTheme.colorScheme.secondary,
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
                                color = MaterialTheme.colorScheme.tertiary,
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
                                color = Color(0xFF10B981), // Emerald
                                onClick = {
                                    audioManager.playClick()
                                    onNavigate(PlayNonogram(-1))
                                }
                            )
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
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.5f)),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}
