package com.example.appghichiso.domain.model

data class Customer(
    val customerId: Int,
    val contractCode: String,
    val contractSerial: String,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val previousIndex: Int,
    val currentIndex: Int,
    val month: Int,
    val year: Int,
    val roadCode: String,
    val roadName: String,
    val roadOrder: Int,
    val priceSchemaName: String,
    val invoiceId: Long? = null
) {
    /** true nếu đã ghi chỉ số tháng này (từ server hoặc phiên làm việc hiện tại) */
    val isRecorded: Boolean get() = currentIndex > 0
    /** true nếu có hóa đơn (invoice ID từ get-invoices-by-road) */
    val hasInvoice: Boolean get() = invoiceId != null
}

