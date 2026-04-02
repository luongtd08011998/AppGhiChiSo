package com.example.appghichiso.data.local

import com.russhwolf.settings.Settings
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CredentialsStorage(private val settings: Settings) {

    fun save(username: String, password: String, rememberMe: Boolean) {
        settings.putString(KEY_USERNAME, username)
        settings.putBoolean(KEY_REMEMBER_ME, rememberMe)
        if (rememberMe) {
            settings.putString(KEY_PASSWORD, password)
        } else {
            settings.remove(KEY_PASSWORD) // không lưu mật khẩu
        }
    }

    fun saveMonthYear(month: Int, year: Int) {
        settings.putInt(KEY_MONTH, month)
        settings.putInt(KEY_YEAR, year)
    }

    fun getCredentials(): Pair<String, String>? {
        val username = settings.getStringOrNull(KEY_USERNAME) ?: return null
        val password = settings.getStringOrNull(KEY_PASSWORD) ?: return null
        return username to password
    }

    fun getSavedUsername(): String? = settings.getStringOrNull(KEY_USERNAME)

    fun getSavedMonthYear(): Pair<Int, Int>? {
        val month = settings.getIntOrNull(KEY_MONTH) ?: return null
        val year  = settings.getIntOrNull(KEY_YEAR)  ?: return null
        return month to year
    }

    fun isRememberMe(): Boolean = settings.getBooleanOrNull(KEY_REMEMBER_ME) ?: false

    @OptIn(ExperimentalEncodingApi::class)
    fun getBasicAuthHeader(): String? {
        val (username, password) = getCredentials() ?: return null
        val encoded = Base64.encode("$username:$password".encodeToByteArray())
        return "Basic $encoded"
    }

    fun clear() {
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_PASSWORD)
        settings.remove(KEY_REMEMBER_ME)
    }

    /** isLoggedIn = true chỉ khi người dùng chọn "Ghi nhớ mật khẩu" */
    fun isLoggedIn(): Boolean =
        isRememberMe() &&
        settings.getStringOrNull(KEY_USERNAME) != null &&
        settings.getStringOrNull(KEY_PASSWORD) != null

    companion object {
        private const val KEY_USERNAME   = "cred_username"
        private const val KEY_PASSWORD   = "cred_password"
        private const val KEY_REMEMBER_ME = "cred_remember_me"
        private const val KEY_MONTH      = "billing_month"
        private const val KEY_YEAR       = "billing_year"
    }
}
