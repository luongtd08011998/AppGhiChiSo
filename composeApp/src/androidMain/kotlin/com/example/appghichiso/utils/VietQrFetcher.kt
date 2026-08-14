package com.example.appghichiso.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tải ảnh PNG từ [url] (img.vietqr.io) và trả về [Bitmap].
 * Dùng HttpURLConnection thuần — không cần thêm thư viện.
 *
 * Ném ngoại lệ nếu tải thất bại (caller bọc bằng runCatching).
 */
fun fetchVietQrBitmap(url: String): Bitmap {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 8_000
    conn.readTimeout    = 8_000
    conn.requestMethod  = "GET"
    conn.connect()
    return conn.inputStream.use { stream ->
        BitmapFactory.decodeStream(stream)
            ?: error("Không giải mã được ảnh QR từ: $url")
    }
}
