package com.example.appghichiso.di

import com.example.appghichiso.data.printer.BluetoothPrinterRepository
import com.example.appghichiso.printer.PrinterHub
import com.example.appghichiso.printer.PrinterRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Module Koin cho tính năng in (Android). Khởi tạo [PrinterRepository] + [PrinterHub].
 */
fun printerModule(): Module = module {
    single<PrinterRepository> {
        BluetoothPrinterRepository(
            appContext = androidContext(),
            settings = get()
        )
    }
    single { PrinterHub.init(get<PrinterRepository>()) }
}
