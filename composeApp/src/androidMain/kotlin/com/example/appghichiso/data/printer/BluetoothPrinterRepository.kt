package com.example.appghichiso.data.printer

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.domain.model.PrinterDevice
import com.example.appghichiso.printer.PrinterRepository
// TODO: Bỏ comment khi tính năng tự động gạch qua QR đã hoàn thiện
// import com.example.appghichiso.utils.VietQrUrlFetcher
// import com.example.appghichiso.utils.buildVietQrUrl
import com.russhwolf.settings.Settings

/**
 * Implementation Android cho [PrinterRepository]. Dùng thư viện DantSu ESCPOS-ThermalPrinter-Android.
 *
 * Vì tiếng Việt được render thành bitmap (Canvas), ta không phụ thuộc charset máy in.
 * Ảnh chứng từ có thể cao hơn 256px (giới hạn printFormattedText) nên in theo mảnh 256px
 * dùng [EscPosPrinter.bitmapToBytes] + [BluetoothConnection.send].
 */
class BluetoothPrinterRepository(
    private val appContext: Context,
    private val settings: Settings
) : PrinterRepository {

    private val keySavedPrinter = "printer_saved_address"
    private val keySavedName = "printer_saved_name"

    override fun hasPermission(): Boolean {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }
        return perms.all {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun bondedDevices(): List<PrinterDevice> {
        if (!hasPermission()) return emptyList()
        val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = manager?.adapter ?: return emptyList()
        return try {
            adapter.bondedDevices
                ?.filter { it != null }
                ?.map { PrinterDevice(it.name ?: "Máy in không tên", it.address) }
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    override fun getSavedPrinter(): PrinterDevice? {
        val address = settings.getString(keySavedPrinter, "").ifBlank { null } ?: return null
        val name = settings.getString(keySavedName, "").ifBlank { "Máy in" }
        return PrinterDevice(name, address)
    }

    override fun savePrinter(device: PrinterDevice) {
        settings.putString(keySavedPrinter, device.address)
        settings.putString(keySavedName, device.name)
    }

    override fun clearSavedPrinter() {
        settings.remove(keySavedPrinter)
        settings.remove(keySavedName)
    }

    override suspend fun printInvoice(device: PrinterDevice, invoice: InvoiceDto): Result<Unit> = runCatching {
        // TODO: Tạm ẩn QR - chờ hoàn thiện tính năng tự động gạch khi KH chuyển khoản qua QR
        /*
        val qrUrl = buildVietQrUrl(
            amount = invoice.totalAmount,
            custCode = invoice.custCode ?: "",
            yearMonth = invoice.yearMonth ?: "",
            invNumber = invoice.invNumber ?: ""
        )
        val qrBitmap = VietQrUrlFetcher.fetch(qrUrl) // có thể null nếu không có mạng
        val bitmap = ReceiptBitmapRenderer.renderInvoice(invoice, qrBitmap)
        */
        val bitmap = ReceiptBitmapRenderer.renderInvoice(invoice, null)
        printBitmap(device, bitmap)
    }

    override suspend fun printReceipt(device: PrinterDevice, receipt: ReceiptDto): Result<Unit> = runCatching {
        val bitmap = ReceiptBitmapRenderer.renderReceipt(receipt)
        printBitmap(device, bitmap)
    }

    /**
     * In bitmap chứng từ: tạo kết nối Bluetooth trực tiếp từ địa chỉ MAC (không phụ thuộc
     * BluetoothPrintersConnections.list vốn hay bỏ sót máy in nhiệt), tạo EscPosPrinter,
     * chia bitmap thành các mảnh 256px chiều cao rồi gửi lệnh raster (GS v 0).
     */
    private fun printBitmap(device: PrinterDevice, bitmap: Bitmap) {
        val connection = createConnection(device.address)
            ?: error("Không mở được kết nối tới máy in '${device.name}'. Hãy pair lại máy in trong Cài đặt Bluetooth.")
        try {
            connection.connect()
            // Lấy khổ in 48mm @ 203dpi = 384px
            val printerWidthPx = 384

            val finalWidth = minOf(bitmap.width, printerWidthPx)
            val x = if (bitmap.width > printerWidthPx) (bitmap.width - printerWidthPx) / 2 else 0
            val cropped = Bitmap.createBitmap(bitmap, x, 0, finalWidth, bitmap.height)

            // Encode toàn bộ bitmap thành 1 lệnh GS v 0 duy nhất (chịu được chiều cao tới 65535px)
            // Bỏ qua printer.bitmapToBytes() của thư viện vì nó có giới hạn 256px gây lỗi in thu nhỏ 1/3
            val rasterBytes = bitmapToRasterBytesFast(cropped)
            
            // Khởi tạo máy in (ESC @)
            connection.write(byteArrayOf(0x1B, 0x40))
            // Căn giữa (ESC a 1)
            connection.write(byteArrayOf(0x1B, 0x61, 0x01))
            // In ảnh
            connection.write(rasterBytes)
            // Feed giấy 5 dòng
            connection.write(byteArrayOf(0x1B, 0x64, 0x05))
            
            connection.send()
        } catch (e: Exception) {
            throw RuntimeException("Không kết nối được tới '${device.name}'. Kiểm tra máy in đã BẬT, đủ pin và nằm gần điện thoại rồi thử lại.")
        } finally {
            connection.disconnect()
        }
    }

    private fun bitmapToRasterBytesFast(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        
        val dataSize = widthBytes * height
        val bytes = ByteArray(8 + dataSize)
        
        // Lệnh GS v 0 (In ảnh raster)
        bytes[0] = 0x1D
        bytes[1] = 0x76
        bytes[2] = 0x30
        bytes[3] = 0x00
        bytes[4] = (widthBytes and 0xFF).toByte()
        bytes[5] = ((widthBytes shr 8) and 0xFF).toByte()
        bytes[6] = (height and 0xFF).toByte()
        bytes[7] = ((height shr 8) and 0xFF).toByte()
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var index = 8
        for (y in 0 until height) {
            for (xByte in 0 until widthBytes) {
                var b = 0
                for (bBit in 0..7) {
                    val x = xByte * 8 + bBit
                    if (x < width) {
                        val color = pixels[y * width + x]
                        val a = (color shr 24) and 0xFF
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val bl = color and 0xFF
                        // Chuyển sang thang độ xám
                        val luminance = (r * 299 + g * 587 + bl * 114) / 1000
                        // Nếu điểm ảnh tối và không trong suốt -> in màu đen (bit 1)
                        if (a > 128 && luminance < 128) {
                            b = b or (1 shl (7 - bBit))
                        }
                    }
                }
                bytes[index++] = b.toByte()
            }
        }
        return bytes
    }

    /**
     * Tạo [BluetoothConnection] DantSu trực tiếp từ địa chỉ MAC. Bypass `BluetoothPrintersConnections`
     * (vốn bị bỏ sót nhiều máy in nhiệt đã pair — issue #80 của thư viện).
     */
    private fun createConnection(address: String): BluetoothConnection? {
        if (!hasPermission()) return null
        return try {
            val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val device = manager?.adapter?.getRemoteDevice(address) ?: return null
            BluetoothConnection(device)
        } catch (e: Exception) {
            null
        }
    }
}
