package com.example.appghichiso.utils

import io.ktor.http.encodeURLParameter

/**
 * Xây dựng URL VietQR Quick Link để tạo ảnh QR thanh toán chuyển khoản.
 *
 * @param amount Số tiền thanh toán (VNĐ)
 * @param custCode Mã khách hàng
 * @param yearMonth Kỳ thanh toán (định dạng yyyyMM, VD: "202506")
 * @param invNumber Số hóa đơn
 * @return URL ảnh PNG chứa mã QR thanh toán
 */
fun buildVietQrUrl(amount: Double, custCode: String, yearMonth: String, invNumber: String): String {
    val bankId = "970415" // VietinBank
    val accountNo = "113601145666"
    val template = "tY8MBU9"
    val accountName = "CONG TY TNHH CAP NUOC TOC TIEN"

    val amountLong = amount.toLong()
    val addInfo = "CN.TOCTIEN $custCode $yearMonth $invNumber"

    return buildString {
        append("https://img.vietqr.io/image/")
        append(bankId)
        append("-")
        append(accountNo)
        append("-")
        append(template)
        append(".png?")
        append("amount=")
        append(amountLong)
        append("&addInfo=")
        append(addInfo.encodeURLParameter(spaceToPlus = false))
        append("&accountName=")
        append(accountName.encodeURLParameter(spaceToPlus = false))
    }
}
