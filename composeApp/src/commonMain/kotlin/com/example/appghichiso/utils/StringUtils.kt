package com.example.appghichiso.utils

/**
 * Loại bỏ dấu tiếng Việt để phục vụ tìm kiếm không dấu.
 * Hỗ trợ cả ký tự dựng sẵn (precomposed) và ký tự tổ hợp (decomposed).
 */
fun String.removeVietnameseDiacritics(): String {
    val builder = StringBuilder(this.length)
    for (char in this) {
        val replacement = when (char) {
            'à', 'á', 'ả', 'ã', 'ạ', 'ă', 'ằ', 'ắ', 'ẳ', 'ẵ', 'ặ', 'â', 'ầ', 'ấ', 'ẩ', 'ẫ', 'ậ' -> 'a'
            'À', 'Á', 'Ả', 'Ã', 'Ạ', 'Ă', 'Ằ', 'Ắ', 'Ẳ', 'Ẵ', 'Ặ', 'Â', 'Ầ', 'Ấ', 'Ẩ', 'Ẫ', 'Ậ' -> 'A'
            'đ' -> 'd'
            'Đ' -> 'D'
            'è', 'é', 'ẻ', 'ẽ', 'ẹ', 'ê', 'ề', 'ế', 'ể', 'ễ', 'ệ' -> 'e'
            'È', 'É', 'Ẻ', 'Ẽ', 'Ẹ', 'Ê', 'Ề', 'Ế', 'Ể', 'Ễ', 'Ệ' -> 'E'
            'ì', 'í', 'ỉ', 'ĩ', 'ị' -> 'i'
            'Ì', 'Í', 'Ỉ', 'Ĩ', 'Ị' -> 'I'
            'ò', 'ó', 'ỏ', 'õ', 'ọ', 'ô', 'ồ', 'ố', 'ổ', 'ỗ', 'ộ', 'ơ', 'ờ', 'ớ', 'ở', 'ỡ', 'ợ' -> 'o'
            'Ò', 'Ó', 'Ỏ', 'Õ', 'Ọ', 'Ô', 'Ồ', 'Ố', 'Ổ', 'Ỗ', 'Ộ', 'Ơ', 'Ờ', 'Ớ', 'Ở', 'Ỡ', 'Ợ' -> 'O'
            'ù', 'ú', 'ủ', 'ũ', 'ụ', 'ư', 'ừ', 'ứ', 'ử', 'ữ', 'ự' -> 'u'
            'Ù', 'Ú', 'Ủ', 'Ũ', 'Ụ', 'Ư', 'Ừ', 'Ứ', 'Ử', 'Ữ', 'Ự' -> 'U'
            'ỳ', 'ý', 'ỷ', 'ỹ', 'ỵ' -> 'y'
            'Ỳ', 'Ý', 'Ỷ', 'Ỹ', 'Ỵ' -> 'Y'
            in '\u0300'..'\u036f', in '\u1dc0'..'\u1dff', in '\u20d0'..'\u20ff', in '\ufe20'..'\ufe2f' -> null
            else -> char
        }
        if (replacement != null) {
            builder.append(replacement)
        }
    }
    return builder.toString()
}

/**
 * Kiểm tra chuỗi nguồn có chứa query tìm kiếm hay không.
 * Hỗ trợ tìm kiếm không phân biệt hoa thường và không dấu tiếng Việt.
 */
fun String?.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    if (this.isNullOrBlank()) return false

    val trimmedQuery = query.trim()
    if (this.contains(trimmedQuery, ignoreCase = true)) return true

    val normalizedSource = this.removeVietnameseDiacritics()
    val normalizedQuery = trimmedQuery.removeVietnameseDiacritics()

    return normalizedSource.contains(normalizedQuery, ignoreCase = true)
}
