package com.example.appghichiso.di

import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.model.Road
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear

/** Singleton nhẹ để truyền dữ liệu giữa các màn hình mà không cần nav-args phức tạp */
class AppStateHolder {
    var selectedRoad: Road? = null
    var selectedCustomer: Customer? = null
    /** Danh sách khách hàng của tuyến hiện tại – dùng để điều hướng trong MeterReadingScreen */
    var customerList: List<Customer> = emptyList()
    val recordedCustomerCodes: MutableSet<String> = mutableSetOf()
    /** Kỳ ghi chỉ số – được set lại sau khi đăng nhập */
    var billingMonth: Int = currentMonth()
    var billingYear: Int  = currentYear()
}
