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
    single { PrinterHub.init(get<PrinterRepository>()) }
}
