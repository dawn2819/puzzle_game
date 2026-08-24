package com.example.puzzlegame.data

data class MuseumItem(
    val id: String,
    val title: String,
    val category: String, // "trò chơi", "di sản", "văn học", "ẩm thực"
    val description: String,
    val iconEmoji: String,
    val xpReward: Int = 100,
    val coinCost: Int = 150
)

object MuseumRepository {
    val items = listOf(
        // Trò chơi dân gian
        MuseumItem(
            id = "o_an_quan",
            title = "Ô Ăn Quan",
            category = "trò chơi",
            description = "Trò chơi dân gian trí tuệ quen thuộc của trẻ em Việt Nam, giúp rèn luyện khả năng tính toán, phân bổ và tư duy chiến thuật thông qua việc rải những viên sỏi cuội vào các ô dân và ăn quân của đối phương.",
            iconEmoji = "🏺"
        ),
        MuseumItem(
            id = "co_ganh",
            title = "Cờ Gánh",
            category = "trò chơi",
            description = "Môn cờ dân gian độc đáo xuất xứ từ Quảng Nam. Quân cờ di chuyển trên bàn cờ kẻ ô vuông chéo, ăn quân bằng cách kẹp giữa (gánh) hai quân đối phương hoặc bao vây hoàn toàn (chẹt vây).",
            iconEmoji = "♟️"
        ),
        MuseumItem(
            id = "rong_ran",
            title = "Rồng Rắn Lên Mây",
            category = "trò chơi",
            description = "Trò chơi tập thể truyền thống tái hiện hình ảnh rồng rắn xin thuốc thầy đồ. Trong phiên bản câu đố, người chơi điều khiển đầu rồng Lý mạ vàng đi qua ma trận để tích lũy ngọc và mở cổng làng.",
            iconEmoji = "🐉"
        ),
        MuseumItem(
            id = "nhay_lo_co",
            title = "Nhảy Lò Cò",
            category = "trò chơi",
            description = "Trò chơi vận động của tuổi thơ được tái hiện dưới dạng lưới số logic, yêu cầu người chơi tìm đường đi chuyển động chính xác từ ô 1 tới ô 9, vượt qua bẫy sụt lún và cổng dịch chuyển.",
            iconEmoji = "🏃"
        ),
        
        // Di sản & Biểu tượng
        MuseumItem(
            id = "trong_dong",
            title = "Trống Đồng Đông Sơn",
            category = "di sản",
            description = "Biểu tượng đỉnh cao của nền văn hóa Đông Sơn thời đại đồ đồng. Họa tiết trên mặt trống thể hiện thế giới quan sinh động của người Việt cổ về vũ trụ, nông nghiệp, và các loài chim muông.",
            iconEmoji = "🥁"
        ),
        MuseumItem(
            id = "non_la",
            title = "Nón Lá",
            category = "di sản",
            description = "Vật dụng che mưa che nắng thân thuộc của người phụ nữ Việt Nam, mang tính thẩm mỹ cao và là biểu tượng văn hóa đặc trưng, tượng trưng cho sự dịu dàng, cần cù của con người Việt.",
            iconEmoji = "👒"
        ),
        MuseumItem(
            id = "chua_mot_cot",
            title = "Chùa Một Cột",
            category = "di sản",
            description = "Kiến trúc độc đáo xây dựng vào thời vua Lý Thái Tông, mô phỏng đóa sen nở rộ trên mặt nước, thể hiện triết lý Phật giáo sâu sắc và tài hoa xây dựng mỹ thuật cổ xưa.",
            iconEmoji = "🏯"
        ),
        
        // Văn học dân gian
        MuseumItem(
            id = "ca_dao_tuc_ngu",
            title = "Ca Dao Tục Ngữ",
            category = "văn học",
            description = "Kho tàng văn học truyền miệng phong phú của người Việt, gửi gắm các bài học đạo đức, tình yêu quê hương đất nước, và triết lý sống nhân văn qua những câu lục bát mượt mà.",
            iconEmoji = "📜"
        ),
        
        // Ẩm thực Việt Nam
        MuseumItem(
            id = "pho",
            title = "Phở",
            category = "ẩm thực",
            description = "Món ăn quốc hồn quốc túy của Việt Nam. Sự kết hợp hoàn hảo giữa nước dùng thanh ngọt hầm từ xương bò, bánh phở mềm dai, thịt bò thơm ngon và các loại rau thơm tinh tế.",
            iconEmoji = "🍜"
        ),
        MuseumItem(
            id = "banh_mi",
            title = "Bánh Mì",
            category = "ẩm thực",
            description = "Tinh hoa ẩm thực đường phố Việt Nam, biến tấu từ bánh baguette Pháp với lớp vỏ giòn rụm, ruột mềm, nhân patê thơm béo, thịt nướng, chả lụa và dưa chua thanh mát.",
            iconEmoji = "🥖"
        ),
        MuseumItem(
            id = "banh_chung",
            title = "Bánh Chưng",
            category = "ẩm thực",
            description = "Món ăn ngày Tết cổ truyền gói ghém tấm lòng thành kính dâng lên tổ tiên. Bánh làm từ gạo nếp thơm, đỗ xanh ngọt bùi, thịt mỡ béo ngậy được bọc trong lá dong xanh vuông vắn.",
            iconEmoji = "🫔"
        )
    )
}
