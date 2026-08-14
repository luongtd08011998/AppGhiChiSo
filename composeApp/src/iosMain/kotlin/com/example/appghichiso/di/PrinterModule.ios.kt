package com.example.appghichiso.di

import com.example.appghichiso.data.printer.NoopPrinterRepository
import com.example.appghichiso.printer.PrinterHub
import com.example.appghichiso.printer.PrinterRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Module in cho iOS (no-op). Build được nhưng tính năng in chưa hỗ trợ.
 */
fun printerModule(): Module = module {
    single<PrinterRepository> { NoopPrinterRepository() }
    // QR BIDV được tải trực tiếp trong BluetoothPrinterRepository — không cần PaybillApiService nữa
    single { PrinterHub.init(get<PrinterRepository>()) }
}
