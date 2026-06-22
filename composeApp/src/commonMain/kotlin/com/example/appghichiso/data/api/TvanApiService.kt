package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.PayCashResponse
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.api.dto.TvanPublishResponse
import com.example.appghichiso.session.SessionManager
import com.example.appghichiso.util.Logger
import com.example.appghichiso.util.currentTimestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val TAG = "TvanApiService"
private const val TVAN_URL = "http://toctienltd.vn/cm-portlet/api"
private val json = Json { ignoreUnknownKeys = true; isLenient = true }

@OptIn(ExperimentalEncodingApi::class)
class TvanApiService(
    private val client: HttpClient,
    private val sessionManager: SessionManager
) {


    suspend fun getToPublishList(ym: String, rc: String, cc: String, pn: Int = 0): List<InvoiceDto> {
        val timestamp = currentTimestamp()
        val response = client.get("$TVAN_URL/to_publish_list?ym=$ym&rc=$rc&cc=$cc&pn=$pn&t=$timestamp") {

            header("Cache-Control", "no-cache, no-store, must-revalidate")
            header("Pragma", "no-cache")
        }
        return parseInvoiceList(response.bodyAsText()).map {
            if (it.custCode == null) it.copy(custCode = cc) else it
        }
    }

    suspend fun getDebtList(ym: String, rc: String, cc: String, pn: Int = 0): List<InvoiceDto> {
        val timestamp = currentTimestamp()
        val response = client.get("$TVAN_URL/debt_list?ym=$ym&rc=$rc&cc=$cc&pn=$pn&t=$timestamp") {

            header("Cache-Control", "no-cache, no-store, must-revalidate")
            header("Pragma", "no-cache")
        }
        return parseInvoiceList(response.bodyAsText()).map {
            if (it.custCode == null) it.copy(custCode = cc) else it
        }
    }

    suspend fun publishToTvan(ids: List<Long>): TvanPublishResponse {
        val jsonArray = ids.joinToString(prefix = "[", postfix = "]")
        val response = client.post("$TVAN_URL/publish_to_tvan") {

            contentType(ContentType.Application.Json)
            setBody(jsonArray)
        }
        return try {
            json.decodeFromString<TvanPublishResponse>(response.bodyAsText())
        } catch (e: Exception) {
            Logger.w(TAG, e) { "publishToTvan: failed to parse response" }
            TvanPublishResponse(retCode = "ERR", retMsg = "Parsing Error", result = "")
        }
    }

    suspend fun payCash(id: Long): PayCashResponse {
        // Tài liệu: curl -d "781457" ... truyền raw string
        val response = client.post("$TVAN_URL/pay_cash") {

            contentType(ContentType.Application.Json)
            setBody(id.toString())
        }
        return try {
            json.decodeFromString<PayCashResponse>(response.bodyAsText())
        } catch (e: Exception) {
            Logger.w(TAG, e) { "payCash: failed to parse response" }
            PayCashResponse(retCode = "ERR", retMsg = "Parsing Error", result = "")
        }
    }

    suspend fun getPaidList(ym: String, rc: String, cc: String, pn: Int = 0): List<InvoiceDto> {
        val timestamp = currentTimestamp()
        val response = client.get("$TVAN_URL/paid_list?ym=$ym&rc=$rc&cc=$cc&pn=$pn&t=$timestamp") {

            header("Cache-Control", "no-cache, no-store, must-revalidate")
            header("Pragma", "no-cache")
        }
        return parseInvoiceList(response.bodyAsText()).map {
            if (it.custCode == null) it.copy(custCode = cc) else it
        }
    }

    suspend fun getReceipt(id: Long): ReceiptDto? {
        val response = client.get("$TVAN_URL/receipt/$id") {

        }
        return parseReceipt(response.bodyAsText())
    }

    // --- Parsers ---
    private fun parseInvoiceList(text: String): List<InvoiceDto> {
        if (text.isBlank()) return emptyList()
        return json.decodeFromString<List<InvoiceDto>>(text)
    }

    private fun parseReceipt(text: String): ReceiptDto? {
        val trimmed = text.trim()
        if (trimmed.startsWith("{")) {
            return try {
                json.decodeFromString<ReceiptDto>(trimmed)
            } catch (e: Exception) {
                Logger.w(TAG, e) { "parseReceipt: failed to parse JSON object" }
                null
            }
        }

        val map = text.lines().associate { line ->
            val parts = line.split("=", limit = 2)
            (parts.getOrNull(0)?.trim() ?: "") to (parts.getOrNull(1)?.trim() ?: "")
        }.filterKeys { it.isNotEmpty() }

        if (!map.containsKey("id") && !map.containsKey("invNumber")) return null

        return ReceiptDto(
            id = map["id"]?.toLongOrNull() ?: 0,
            invNumber = map["invNumber"],
            custCode = map["custCode"] ?: "",
            custName = map["custName"] ?: "",
            custAddress = map["custAddress"] ?: "",
            custTaxCode = map["custTaxCode"]?.takeIf { it != "null" },
            numOfHouseHold = map["numOfHouseHold"]?.toIntOrNull() ?: 1,
            timeToUsedFrom = map["timeToUsedFrom"] ?: "",
            timeToUsedTo = map["timeToUsedTo"] ?: "",
            period = map["period"] ?: "",
            oldIndex = map["oldIndex"]?.toLongOrNull() ?: 0,
            newIndex = map["newIndex"]?.toLongOrNull() ?: 0,
            volumn0 = map["Vol0"] ?: map["volumn0"]?.takeIf { it != "null" },
            volumn1 = map["Vol1"] ?: map["volumn1"]?.takeIf { it != "null" },
            volumn2 = map["Vol2"] ?: map["volumn2"]?.takeIf { it != "null" },
            volumn3 = map["Vol3"] ?: map["volumn3"]?.takeIf { it != "null" },
            price0 = map["Pr0"] ?: map["price0"]?.takeIf { it != "null" },
            price1 = map["Pr1"] ?: map["price1"]?.takeIf { it != "null" },
            price2 = map["Pr2"] ?: map["price2"]?.takeIf { it != "null" },
            price3 = map["Pr3"] ?: map["price3"]?.takeIf { it != "null" },
            amount0 = map["Amn0"] ?: map["amount0"]?.takeIf { it != "null" },
            amount1 = map["Amn1"] ?: map["amount1"]?.takeIf { it != "null" },
            amount2 = map["Amn2"] ?: map["amount2"]?.takeIf { it != "null" },
            amount3 = map["Amn3"] ?: map["amount3"]?.takeIf { it != "null" },
            amount = map["amount"] ?: "",
            taxFee = map["taxFee"] ?: "",
            envFee = map["envFee"] ?: "",
            totalAmount = map["totalAmount"] ?: "",
            totalAmountInWord = map["totalAmountInWord"] ?: "",
            lookupCode = map["LookupCode"] ?: map["lookupCode"] ?: ""
        )
    }
}
