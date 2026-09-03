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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.puzzlegame.audio.AudioManager
import com.example.puzzlegame.data.GamePreferences
import com.example.puzzlegame.data.MuseumItem
import com.example.puzzlegame.data.MuseumRepository
import com.example.puzzlegame.theme.*
import com.example.puzzlegame.ui.components.GlassCard
import com.example.puzzlegame.ui.components.PremiumBackground
import com.example.puzzlegame.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuseumScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    var coins by remember { mutableStateOf(prefs.profileCoins) }
    var unlockedIds by remember { mutableStateOf(prefs.unlockedCollectionIds) }
    
    var selectedCategory by remember { mutableStateOf("tất cả") } // "tất cả", "trò chơi", "di sản", "văn học", "ẩm thực"
    var activeDetailItem by remember { mutableStateOf<MuseumItem?>(null) }
    var showErrorPopup by remember { mutableStateOf(false) }

    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == "tất cả") {
            MuseumRepository.items
        } else {
            MuseumRepository.items.filter { it.category == selectedCategory }
        }
    }

    fun syncCoins() {
        coins = prefs.profileCoins
        unlockedIds = prefs.unlockedCollectionIds
    }

    fun tryUnlockItem(item: MuseumItem) {
        if (coins >= item.coinCost) {
            audioManager.playClick()
            prefs.profileCoins = coins - item.coinCost
            val newSet = unlockedIds.toMutableSet().apply { add(item.id) }
            prefs.unlockedCollectionIds = newSet
            syncCoins()
        } else {
            audioManager.playError()
            showErrorPopup = true
        }
    }

    PremiumBackground(drawableId = R.drawable.bg_museum) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("BẢO TÀNG HỒN VIỆT", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        // Hiển thị tiền vàng ở góc trên
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(StarGold.copy(alpha = 0.25f))
                                .border(1.5.dp, StarGold, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$coins 🪙",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = EarthyBrown
                            )
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
                // Thuyết minh nhỏ
                Text(
                    text = "Thu thập tiền vàng từ việc thắng game dân gian để mở khóa các thẻ di sản văn hóa Việt Nam.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Thanh chọn phân loại ngang (Categories)
                val categories = listOf("tất cả", "trò chơi", "di sản", "văn học", "ẩm thực")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) FlagRed else SurfaceNormal)
                                .border(1.dp, if (isSelected) FlagRed else OutlineBrown, RoundedCornerShape(16.dp))
                                .clickable {
                                    audioManager.playClick()
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else EarthyBrown
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lưới hiển thị các thẻ di sản bento
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val isUnlocked = unlockedIds.contains(item.id)

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.9f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isUnlocked) SurfaceNormal else Color.Gray.copy(alpha = 0.15f))
                                .border(
                                    2.dp,
                                    if (isUnlocked) BambooGreen else OutlineBrown.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    audioManager.playClick()
                                    activeDetailItem = item
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                // Biểu tượng di sản
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            if (isUnlocked) Color.White else Color.Gray.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isUnlocked) item.iconEmoji else "🔒",
                                        fontSize = 28.sp
                                    )
                                }

                                // Tiêu đề thẻ
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (isUnlocked) EarthyBrown else Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                // Phân loại hoặc giá trị mở khóa
                                if (isUnlocked) {
                                    Text(
                                        text = "ĐÃ MỞ KHÓA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = BambooGreen
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StarGold.copy(alpha = 0.3f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${item.coinCost} 🪙",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EarthyBrown
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hộp thoại xem chi tiết thẻ di sản đã mở khóa / Hỗ trợ mua mở khóa
        activeDetailItem?.let { item ->
            val isUnlocked = unlockedIds.contains(item.id)

            AlertDialog(
                onDismissRequest = { activeDetailItem = null },
                confirmButton = {
                    if (isUnlocked) {
                        Button(
                            onClick = { activeDetailItem = null },
                            colors = ButtonDefaults.buttonColors(containerColor = BambooGreen)
                        ) {
                            Text("Đóng", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                tryUnlockItem(item)
                                if (prefs.unlockedCollectionIds.contains(item.id)) {
                                    activeDetailItem = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlagRed)
                        ) {
                            Text("Mở khóa bằng ${item.coinCost} 🪙", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (!isUnlocked) {
                        TextButton(onClick = { activeDetailItem = null }) {
                            Text("Đóng")
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.iconEmoji, fontSize = 28.sp)
                        Text(item.title, fontWeight = FontWeight.ExtraBold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Chủ đề: " + item.category.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlagRed
                        )
                        Divider(color = OutlineBrown.copy(alpha = 0.3f))
                        Text(
                            text = if (isUnlocked) item.description else "Nội dung di sản này hiện đang bị khóa. Hãy mở khóa bằng tiền vàng tích lũy từ trò chơi để đọc mô tả văn hóa lịch sử thú vị của Việt Nam!",
                            fontSize = 14.sp,
                            color = EarthyBrown,
                            textAlign = TextAlign.Justify,
                            lineHeight = 20.sp
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(24.dp))
            )
        }

        // Hộp thoại thông báo lỗi thiếu tiền vàng
        if (showErrorPopup) {
            AlertDialog(
                onDismissRequest = { showErrorPopup = false },
                confirmButton = {
                    Button(onClick = { showErrorPopup = false }) {
                        Text("Đồng ý", fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("THIẾU TIỀN VÀNG 🪙", fontWeight = FontWeight.ExtraBold, color = FlagRed) },
                text = { Text("Bạn không có đủ số lượng tiền vàng để mở khóa di sản này. Hãy chiến thắng thêm nhiều ván game dân gian để nhận tiền vàng nhé!") },
                containerColor = SurfaceNormal
            )
        }
    }
}
