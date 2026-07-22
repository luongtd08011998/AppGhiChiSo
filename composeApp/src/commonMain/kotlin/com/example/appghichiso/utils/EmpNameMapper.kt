package com.example.appghichiso.utils

/**
 * Bảng tra cứu: username đăng nhập → họ tên đầy đủ của thu ngân.
 * Username được ghép từ: [tên] + [chữ cái đầu họ] + [chữ cái đầu tên lót]
 * Ví dụ: Lâm Thị Vân → vanlt
 *
 * Trường hợp đặc biệt (username không theo quy tắc chung):
 * - huongptt → Nguyễn Thị Vân
 */
private val EMP_NAME_MAP: Map<String, String> = mapOf(
    // username (phần trước @) → Họ tên đầy đủ
    "loanttb"  to "Trần Thị Bích Loan",
    "thanhdt"  to "Đào Thị Thanh",
    "minhdtt"  to "Đặng Thị Tuệ Minh",
    "vanlt"    to "Lâm Thị Vân",
    "huongptt" to "Nguyễn Thị Vân",   // trường hợp đặc biệt
)

/**
 * Chuyển username đăng nhập thành họ tên đầy đủ để hiển thị trên biên nhận.
 * - Nếu username có dạng "vanlt@toctienltd.vn" thì tách lấy phần trước "@".
 * - Nếu không tìm thấy trong bảng tra cứu, trả về chính [username] gốc.
 */
fun resolveEmpName(username: String): String {
    val key = username.substringBefore("@").lowercase().trim()
    return EMP_NAME_MAP[key] ?: username
}
