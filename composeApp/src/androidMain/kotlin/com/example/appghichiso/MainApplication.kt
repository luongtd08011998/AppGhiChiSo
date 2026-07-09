package com.example.appghichiso

import android.app.Application
import com.example.appghichiso.di.appModule
import com.example.appghichiso.di.printerModule
import com.example.appghichiso.printer.PrinterHub
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            modules(appModule())
            modules(printerModule())
        }
        // Eager-init PrinterHub: singleton Koin là lazy, phải resolve để `PrinterHub.instance`
        // (companion) dùng được ngay khi UI compose (PrintHost ở App root).
        GlobalContext.get().get<PrinterHub>()
    }
}

