package com.example.appghichiso.util

/**
 * Lightweight KMP logger — ghi ra stdout (logcat trên Android, console trên iOS/desktop).
 * Có thể swap sang thư viện logging đầy đủ sau (e.g. Kermit, Napier) mà không cần đổi call-site.
 */
object Logger {

    /** Warning: thường dùng cho lỗi có thể phục hồi (parse fail, network hiccup, …). */
    fun w(tag: String, throwable: Throwable? = null, message: () -> String) {
        println("W/$tag: ${message()}")
        throwable?.let { println(it.stackTraceToString()) }
    }

    /** Error: lỗi nghiêm trọng, nên quan tâm ngay. */
    fun e(tag: String, throwable: Throwable? = null, message: () -> String) {
        println("E/$tag: ${message()}")
        throwable?.let { println(it.stackTraceToString()) }
    }

    /** Debug: chỉ dùng trong quá trình phát triển. */
    fun d(tag: String, message: () -> String) {
        println("D/$tag: ${message()}")
    }
}
