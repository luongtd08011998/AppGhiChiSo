package com.example.appghichiso.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tải ảnh QR VietQR từ URL (img.vietqr.io) và decode thành [Bitmap].
 * Trả về null nếu không có mạng hoặc tải lỗi (lúc đó giấy báo vẫn in, chỉ thiếu QR).
 */
object VietQrUrlFetcher {
    suspend fun fetch(url: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            connection.use {
                if (it.responseCode in 200..299) {
                    it.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } else {
                    null
                }
            }
        }.getOrNull()
    }
}

private inline fun <T> HttpURLConnection.use(block: (HttpURLConnection) -> T): T {
    try {
        return block(this)
    } finally {
        disconnect()
    }
}
