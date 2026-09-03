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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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

    PremiumBackground(drawableId = R.drawable.bg_museum, bgDimAlpha = 0.30f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "BẢO TÀNG HỒN VIỆT",
                            fontFamily = OngDoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontSize = 22.sp,
                            color = FlagRed,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.9f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xF5FFFBEB))
                                    .border(1.dp, Color(0x66B40006), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("◀", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FlagRed)
                            }
                        }
                    },
                    actions = {
                        // Hiển thị tiền vàng ở góc trên
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFEF08A))
                                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$coins 🪙",
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF78350F)
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thuyết minh nhỏ đặt trong dải thẻ nền ngà trang nhã, không bao giờ bị chìm vào nền
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xF5FFFBEB))
                        .border(1.dp, Color(0x66B40006), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Thu thập tiền vàng từ việc thắng game dân gian để mở khóa các thẻ di sản văn hóa Việt Nam.",
                        fontFamily = OngDoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        textAlign = TextAlign.Center
                    )
                }

                // Thanh chọn phân loại ngang (Categories)
                val categories = listOf("tất cả", "trò chơi", "di sản", "văn học", "ẩm thực")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) FlagRed else Color(0xF5FFFBEB))
                                .border(1.5.dp, if (isSelected) Color(0xFFD97706) else Color(0x66B40006), RoundedCornerShape(16.dp))
                                .clickable {
                                    audioManager.playClick()
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.uppercase(),
                                fontFamily = OngDoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Color(0xFF4A2810)
                            )
                        }
                    }
                }

                // Lưới hiển thị các thẻ di sản bento - Nền thẻ giấy ngà dày dặn, chống hòa vào nền 100%
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
                                .aspectRatio(0.88f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isUnlocked) Color(0xF8FFFBEB)
                                    else Color(0xF2FFFBEB)
                                )
                                .border(
                                    width = if (isUnlocked) 2.dp else 1.5.dp,
                                    color = if (isUnlocked) Color(0xFF059669) else Color(0x66B40006),
                                    shape = RoundedCornerShape(18.dp)
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
                                // Biểu tượng di sản / Khóa
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isUnlocked) Color(0xFFE8F5E9)
                                            else Color(0xFFFEF08A)
                                        )
                                        .border(
                                            1.dp,
                                            if (isUnlocked) Color(0xFF4CAF50)
                                            else Color(0xFFD97706),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isUnlocked) item.iconEmoji else "🔒",
                                        fontSize = 26.sp
                                    )
                                }

                                // Tiêu đề thẻ - Chữ cọ thư pháp đậm nét, tương phản tối đa trên nền ngà
                                Text(
                                    text = item.title,
                                    fontFamily = OngDoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = if (isUnlocked) FlagRed else Color(0xFF3E1F0A),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )

                                // Phân loại hoặc giá trị mở khóa
                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "ĐÃ MỞ KHÓA",
                                            fontFamily = OngDoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFEF08A))
                                            .border(1.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${item.coinCost} 🪙",
                                            fontFamily = OngDoFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF78350F)
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
                            Text("Đóng", fontFamily = OngDoFontFamily, fontWeight = FontWeight.Bold)
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
                            Text("Mở khóa bằng ${item.coinCost} 🪙", fontFamily = OngDoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (!isUnlocked) {
                        TextButton(onClick = { activeDetailItem = null }) {
                            Text("Đóng", fontFamily = OngDoFontFamily)
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.iconEmoji, fontSize = 28.sp)
                        Text(item.title, fontFamily = OngDoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = FlagRed)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Chủ đề: " + item.category.uppercase(),
                            fontSize = 13.sp,
                            fontFamily = OngDoFontFamily,
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
