package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.SmsUpdateResponse
import com.example.appghichiso.session.SessionManager
import com.example.appghichiso.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val TAG = "SmsApiService"
private const val SMS_URL = "http://toctienltd.vn"

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

@OptIn(ExperimentalEncodingApi::class)
class SmsApiService(
    private val client: HttpClient,
    private val sessionManager: SessionManager
) {

    suspend fun getSms(customerCode: String): String? {
        val response: String = client.get("$SMS_URL/cm-portlet/api/customer/sms/$customerCode").body()
        return response.trim().ifBlank { null }
    }

    suspend fun updateSms(customerCode: String, sms: String): SmsUpdateResponse {
        val response: HttpResponse = client.post("$SMS_URL/cm-portlet/api/customer/sms") {
            contentType(ContentType.Application.Json)
            setBody("""{"code":"$customerCode","sms":"$sms"}""")
        }
        val body = response.bodyAsText()
        return try {
            json.decodeFromString<SmsUpdateResponse>(body)
        } catch (e: Exception) {
            Logger.w(TAG, e) { "updateSms: failed to parse response — body=${body.take(200)}" }
            SmsUpdateResponse(retCode = "PARSE_ERROR", retMsg = body.take(200))
        }
    }

    suspend fun updatePhone(customerCode: String, phone: String): SmsUpdateResponse {
        val response: HttpResponse = client.post("$SMS_URL/cm-portlet/api/customer/phone") {
            contentType(ContentType.Application.Json)
            setBody("""{"code":"$customerCode","phone":"$phone"}""")
        }
        val body = response.bodyAsText()
        return try {
            json.decodeFromString<SmsUpdateResponse>(body)
        } catch (e: Exception) {
            Logger.w(TAG, e) { "updatePhone: failed to parse response — body=${body.take(200)}" }
            SmsUpdateResponse(retCode = "PARSE_ERROR", retMsg = body.take(200))
        }
    }
}
