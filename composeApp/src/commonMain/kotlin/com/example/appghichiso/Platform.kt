package com.example.appghichiso

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getAppVersion(): String