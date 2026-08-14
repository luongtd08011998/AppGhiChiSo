package com.example.appghichiso.utils

/**
 * Sinh URL ảnh mã QR VietQR theo chuẩn NAPAS/VietQR.
 *
 * Định dạng URL:
 *   https://img.vietqr.io/image/{bankId}-{accountNo}-{template}.png
 *       ?amount={amount}
 *       &addInfo={encodedAddInfo}
 *       &accountName={encodedAccountName}
 *
 * Tham khảo: https://www.vietqr.io/danh-sach-api/generate-qr-code/
 *
 * ---
 * Cách dùng:
 * ```kotlin
 * val url = buildVietQrUrl(
 *     amount      = invoice.totalAmount,
 *     custCode    = invoice.custCode ?: "",
 *     yearMonth   = invoice.yearMonth ?: "",
 *     invNumber   = invoice.invNumber ?: ""
 * )
 * // Truyền url vào AsyncImage hoặc coil ImageRequest
 * ```
 */

/** BIN BIDV theo chuẩn NAPAS. */
private const val BIDV_BIN = "970418"

/**
 * Số tài khoản BIDV của Công ty TNHH Cấp Nước Tóc Tiên (dùng chung toàn công ty).
 */
private const val COMPANY_ACCOUNT_NO = "7628894894"   // STK BIDV Cty TNHH Cấp Nước Tóc Tiên

/** Tên tài khoản hiển thị trên app ngân hàng của khách khi quét QR. */
private const val COMPANY_ACCOUNT_NAME = "CTY TNHH CAP NUOC TOC TIEN"

/**
 * Template QR được chọn.
 * - "compact"  → QR nhỏ, có logo ngân hàng (không phải vuong — dễ bị méo khi scale)
 * - "compact2" → nhỏ hơn
 * - "print"    → có logo ngân hàng (dùng khi hiển thị màn hình)
 * - "qr_only"  → chỉ QR thuần, ảnh vuông, không có gì thêm (không bị méo khi in nhiệt)
 */
private const val QR_TEMPLATE = "qr_only"

/** Tiền tố tài khoản định danh (Virtual Account) thu hộ qua BIDV. */
const val VA_PREFIX = "V3TTP"

/**
 * Sinh mã tài khoản định danh (STK ảo) theo cấu trúc: V3TTP + ID hóa đơn.
 */
fun getVirtualAccount(invoiceId: Long): String = "$VA_PREFIX$invoiceId"

/**
 * Sinh URL ảnh VietQR nhúng sẵn thông tin thanh toán.
 *
 * @param invoiceId   ID bản ghi hoá đơn trong DB (dùng để sinh mã VA: V3TTP{invoiceId}).
 * @param amount      Số tiền cần thanh toán (VND).
 * @param custCode    Mã khách hàng — nhúng vào nội dung chuyển khoản để BE định danh.
 * @param yearMonth   Kỳ thanh toán dạng "YYYYMM", vd: "202506".
 * @param invNumber   Số hóa đơn, vd: "HĐ-001234".
 * @param accountNo   Số tài khoản nhận (mặc định sinh theo mã VA [getVirtualAccount]).
 * @return URL ảnh PNG mã VietQR, sẵn sàng truyền vào coil [AsyncImage].
 */
fun buildVietQrUrl(
    invoiceId: Long,
    amount: Double,
    custCode: String,
    yearMonth: String,
    invNumber: String = "",
    accountNo: String = getVirtualAccount(invoiceId)
): String {
    val amountLong = amount.toLong()

    // Nội dung chuyển khoản: BE dùng custCode + kỳ để gạch nợ tự động
    // Định dạng: "CN.TOCTIEN {custCode} {yearMonth} {invNumber}"
    // — Tuân thủ thoả thuận với BIDV (xem mục B2 tài liệu tích hợp)
    val addInfo = buildString {
        append("CN.TOCTIEN")
        if (custCode.isNotBlank()) append(" $custCode")
        if (yearMonth.isNotBlank()) append(" $yearMonth")
        if (invNumber.isNotBlank()) append(" $invNumber")
    }

    val encodedAddInfo   = addInfo.encodeForUrl()
    val encodedAccName   = COMPANY_ACCOUNT_NAME.encodeForUrl()

    return buildString {
        append("https://img.vietqr.io/image/")
        append(BIDV_BIN)
        append("-")
        append(accountNo)
        append("-")
        append(QR_TEMPLATE)
        append(".png")
        append("?amount=")
        append(amountLong)
        append("&addInfo=")
        append(encodedAddInfo)
        append("&accountName=")
        append(encodedAccName)
    }
}

/**
 * Encode chuỗi cho URL query parameter (thay space thành %20, giữ ký tự an toàn).
 * Hàm nhẹ, không cần thư viện bên ngoài, tương thích KMP (commonMain).
 */
private fun String.encodeForUrl(): String {
    val safe = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
    val sb = StringBuilder()
    val bytes = this.encodeToByteArray()
    for (b in bytes) {
        val c = b.toInt() and 0xFF
        if (c < 128 && safe.contains(c.toChar())) {
            sb.append(c.toChar())
        } else {
            sb.append('%')
            sb.append(c.toString(16).uppercase().padStart(2, '0'))
        }
    }
    return sb.toString()
}
