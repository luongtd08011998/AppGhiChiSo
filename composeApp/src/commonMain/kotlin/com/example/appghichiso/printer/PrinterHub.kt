package com.example.appghichiso.printer

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.domain.model.PrinterDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Trung tâm điều phối việc in. Singleton toàn cục ([instance]) để các lambda `onPrint`
 * trong UI có thể gọi trực tiếp mà không cần inject vào ViewModel (giữ nguyên code VM cũ).
 *
 * Vòng đời: [instance] được gán khi khởi tạo từ Koin (xem `printerModule()` ở androidMain/iosMain).
 */
class PrinterHub(
    private val repository: PrinterRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<PrintingState>(PrintingState.Idle)
    val state: StateFlow<PrintingState> = _state.asStateFlow()

    private val _showPicker = MutableStateFlow(false)
    val showPicker: StateFlow<Boolean> = _showPicker.asStateFlow()

    private val _bondedPrinters = MutableStateFlow<List<PrinterDevice>>(emptyList())
    val bondedPrinters: StateFlow<List<PrinterDevice>> = _bondedPrinters.asStateFlow()

    /** Lệnh in đang chờ (do chưa có máy đã chọn) — sẽ thực thi ngay khi chọn xong máy. */
    private var pendingInvoice: InvoiceDto? = null
    private var pendingReceipt: ReceiptDto? = null

    // ---- API gọi từ UI ----

    fun requestPrintInvoice(invoice: InvoiceDto) {
        if (!ensurePermission()) return
        if (!hasSelectedPrinter()) {
            pendingInvoice = invoice
            pendingReceipt = null
            openPicker()
            return
        }
        launchPrint { repository.printInvoice(repository.getSavedPrinter()!!, invoice) }
    }

    fun requestPrintReceipt(receipt: ReceiptDto) {
        if (!ensurePermission()) return
        if (!hasSelectedPrinter()) {
            pendingReceipt = receipt
            pendingInvoice = null
            openPicker()
            return
        }
        launchPrint { repository.printReceipt(repository.getSavedPrinter()!!, receipt) }
    }

    fun openPicker() {
        _bondedPrinters.value = repository.bondedDevices()
        _showPicker.value = true
    }

    /** Bỏ máy in đã chọn và mở lại hộp chọn máy (dùng khi muốn đổi máy in). */
    fun changePrinter() {
        repository.clearSavedPrinter()
        _state.value = PrintingState.Idle
        _bondedPrinters.value = repository.bondedDevices()
        _showPicker.value = true
    }

    fun selectPrinter(device: PrinterDevice) {
        repository.savePrinter(device)
        _showPicker.value = false
        val inv = pendingInvoice
        val rec = pendingReceipt
        pendingInvoice = null
        pendingReceipt = null
        when {
            inv != null -> launchPrint { repository.printInvoice(device, inv) }
            rec != null -> launchPrint { repository.printReceipt(device, rec) }
        }
    }

    fun dismissPicker() {
        _showPicker.value = false
        pendingInvoice = null
        pendingReceipt = null
        if (_state.value is PrintingState.NeedPermission) {
            _state.value = PrintingState.Idle
        }
    }

    /** Gọi lại sau khi user cấp quyền Bluetooth runtime (Android 12+). */
    fun onPermissionGranted() {
        if (_state.value is PrintingState.NeedPermission) {
            _state.value = PrintingState.Idle
            val inv = pendingInvoice
            val rec = pendingReceipt
            when {
                inv != null -> requestPrintInvoice(inv)
                rec != null -> requestPrintReceipt(rec)
            }
        }
    }

    fun reset() {
        _state.value = PrintingState.Idle
    }

    // ---- nội bộ ----

    private fun ensurePermission(): Boolean {
        if (repository.hasPermission()) return true
        // Giữ lại lệnh chờ để chạy lại sau khi cấp quyền
        _state.value = PrintingState.NeedPermission
        return false
    }

    private fun hasSelectedPrinter(): Boolean = repository.getSavedPrinter() != null

    private fun launchPrint(action: suspend () -> Result<Unit>) {
        scope.launch {
            _state.value = PrintingState.Connecting
            val result = action()
            _state.value = result.fold(
                onSuccess = { PrintingState.Success },
                onFailure = { PrintingState.Error(it.message ?: "Lỗi không xác định khi in") }
            )
        }
    }

    companion object {
        @Volatile
        private var _instance: PrinterHub? = null

        val instance: PrinterHub
            get() = _instance ?: error("PrinterHub chưa được khởi tạo. Gọi PrinterHub.init(repo) khi app start.")

        fun init(repository: PrinterRepository): PrinterHub {
            val hub = PrinterHub(repository)
            _instance = hub
            return hub
        }
    }
}
