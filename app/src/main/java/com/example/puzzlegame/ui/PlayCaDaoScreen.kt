package com.example.puzzlegame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import kotlin.random.Random

data class CaDaoVerse(
    val id: String,
    val correctParts: List<String>,
    val description: String // Ý nghĩa câu ca dao tục ngữ
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayCaDaoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audioManager = remember { AudioManager(context).apply { volume = prefs.volume } }

    val verses = remember {
        listOf(
            CaDaoVerse(
                id = "bau_oi",
                correctParts = listOf("Bầu ơi", "thương lấy", "bí cùng", "tuy rằng khác giống", "nhưng chung một giàn"),
                description = "Lời khuyên nhủ sâu sắc về tình nghĩa đồng bào, tương thân tương ái. Dù khác biệt về nguồn gốc hay vùng miền, nhưng cùng sống trên một đất nước thì phải biết yêu thương, đùm bọc lẫn nhau trong lúc khó khăn."
            ),
            CaDaoVerse(
                id = "nhieu_dieu",
                correctParts = listOf("Nhiễu điều", "phủ lấy", "giá gương", "người trong một nước", "phải thương nhau cùng"),
                description = "Hình ảnh tấm vải đỏ (nhiễu điều) che chở cho giá gương tượng trưng cho tấm lòng bọc lót, đoàn kết của nhân dân Việt Nam, đề cao truyền thống tương trợ và nghĩa tình gia đình, xã hội."
            ),
            CaDaoVerse(
                id = "la_lanh",
                correctParts = listOf("Lá lành", "đùm lá rách", "lá rách ít", "đùm lá rách nhiều"),
                description = "Bài học đạo lý nhân văn giản dị của người Việt về sự tương trợ. Những người có cuộc sống tốt đẹp hơn giúp đỡ người khó khăn, và ngay cả những người nghèo khó cũng sẻ chia, an ủi lẫn nhau."
            ),
            CaDaoVerse(
                id = "uong_nuoc",
                correctParts = listOf("Uống nước", "nhớ nguồn", "ăn quả", "nhớ kẻ", "trồng cây"),
                description = "Đạo lý tri ân truyền thống sâu sắc của người Việt. Nhắc nhở thế hệ sau luôn ghi nhớ công ơn dưỡng dục của cha mẹ, công lao dựng nước của tổ tiên và người đi trước."
            ),
            CaDaoVerse(
                id = "mot_cay",
                correctParts = listOf("Một cây", "làm chẳng", "nên non", "ba cây chụm lại", "nên hòn núi cao"),
                description = "Bài học về sức mạnh tập thể và sự đoàn kết vô song. Nhấn mạnh một cá nhân riêng lẻ sẽ khó hoàn thành đại sự, nhưng sự đồng lòng nhất trí của nhiều người sẽ kiến tạo nên kỳ tích."
            )
        )
    }

    var currentVerseIndex by remember { mutableStateOf(0) }
    val currentVerse = verses[currentVerseIndex]

    // Danh sách các mảnh câu bị xáo trộn để người chơi chọn
    val shuffledParts = remember { mutableStateListOf<String>() }
    // Danh sách các mảnh câu đã chọn của người chơi
    val userParts = remember { mutableStateListOf<String>() }

    var isVictory by remember { mutableStateOf(false) }

    fun loadVerse() {
        userParts.clear()
        shuffledParts.clear()
        shuffledParts.addAll(verses[currentVerseIndex].correctParts.shuffled())
        isVictory = false
    }

    LaunchedEffect(currentVerseIndex) {
        loadVerse()
    }

    // Thưởng thắng
    LaunchedEffect(isVictory) {
        if (isVictory) {
            val winKey = "cadao_win_credited_${currentVerse.id}"
            if (prefs.getHighScore(winKey) == 0) {
                prefs.addXpAndCoins(100, 20)
                // Đồng thời tự động mở khóa thẻ Ca dao tục ngữ trong Bảo tàng!
                val collectionSet = prefs.unlockedCollectionIds.toMutableSet().apply { add("ca_dao_tuc_ngu") }
                prefs.unlockedCollectionIds = collectionSet
                prefs.saveScore(winKey, 1)
            }
        }
    }

    fun selectPart(part: String) {
        if (isVictory) return
        audioManager.playClick()
        userParts.add(part)
        shuffledParts.remove(part)

        // Kiểm tra lắp ráp
        if (userParts.size == currentVerse.correctParts.size) {
            val isCorrect = userParts.zip(currentVerse.correctParts).all { it.first == it.second }
            if (isCorrect) {
                isVictory = true
            } else {
                audioManager.playError()
                // Sai, trả lại toàn bộ các mảnh
                loadVerse()
            }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CA DAO TỤC NGỮ PUZZLE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            audioManager.playClick()
                            onBack()
                        }) {
                            Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        Text(
                            text = "Màn ${currentVerseIndex + 1}/${verses.size} 🏆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                    text = "Hãy chạm chọn các dải câu lục bát theo đúng trật tự ý nghĩa ca dao.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Khu vực lắp ráp câu thơ của user
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNormal.copy(alpha = 0.8f))
                        .border(1.5.dp, OutlineBrown, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📜 CÂU THƠ CỦA BẠN:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )

                    userParts.forEach { part ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, BambooGreen, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = part,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BambooGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Các mảnh ghép xào trộn nằm dưới để chọn
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🧩 CHỌN MẢNH GHÉP:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EarthyBrown
                    )

                    shuffledParts.forEach { part ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, OutlineBrown, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectPart(part)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = part,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EarthyBrown
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nút đặt lại nhanh
                TextButton(
                    onClick = {
                        audioManager.playClick()
                        loadVerse()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Đặt lại ván chơi", fontWeight = FontWeight.Bold, color = FlagRed)
                }
            }
        }

        // Hộp thoại thắng câu ca dao & Hiển thị nguồn gốc nghĩa sâu sắc
        if (isVictory) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            audioManager.playClick()
                            if (currentVerseIndex < verses.size - 1) {
                                currentVerseIndex++
                            } else {
                                currentVerseIndex = 0
                            }
                            loadVerse()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (currentVerseIndex < verses.size - 1) "Câu Tiếp Theo" else "Chơi Lại Từ Đầu", fontWeight = FontWeight.Bold)
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
                        "🎉 GHÉP CA DAO HOÀN TẤT!",
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
                        Text(
                            text = "“" + currentVerse.correctParts.joinToString(" ") + "”",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlagRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentVerse.description,
                            fontSize = 14.sp,
                            color = EarthyBrown,
                            textAlign = TextAlign.Justify,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "+100 XP, +20 Tiền vàng & Đã mở khóa thẻ 'Ca Dao Tục Ngữ' trong Bảo Tàng!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BambooGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                containerColor = SurfaceNormal,
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(24.dp))
            )
        }
    }
}
