package com.example.appghichiso.data.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.utils.formatCurrencyForPrint
import com.example.appghichiso.utils.getCurrentDateString
import com.example.appghichiso.utils.formatApiDate

/**
 * Chiều rộng in thực tế: 48mm @ 203dpi = 384px.
 * Font size tính theo DPI: 1mm = 203/25.4 ≈ 8px.
 * SMALL=22px(2.7mm), BODY=26px(3.2mm), HEAD=28px(3.5mm), TITLE=38px(4.7mm)
 */
private const val PRINT_WIDTH_PX = 384
private const val PADDING = 6

// Font sizes tương đương mm thực tế trên giấy 203dpi
private const val FS_SMALL  = 19f   // ~2.4mm
private const val FS_BODY   = 22f   // ~2.7mm
private const val FS_HEAD   = 21f   // ~2.6mm bold
private const val FS_TITLE  = 32f   // ~4.0mm bold

/**
 * Vẽ chứng từ thành [Bitmap] trắng-đen để in (ESC/POS raster). Tiếng Việt có dấu được đảm bảo
 * vì dùng Paint Canvas thuần (không phụ thuộc font máy in).
 */
object ReceiptBitmapRenderer {

    /** Render giấy báo tiền nước (có QR bitmap nếu được truyền vào). */
    fun renderInvoice(invoice: InvoiceDto, qrBitmap: Bitmap? = null): Bitmap {
        val lines = buildList {
            add(Line("TẬP ĐOÀN HẢI CHÂU VIỆT NAM", Align.CENTER, Size.HEAD))
            add(Line("CÔNG TY TNHH CẤP NƯỚC TÓC TIÊN", Align.CENTER, Size.HEAD))
            add(Line("Ấp Tóc Tiên 1, xã Châu Pha, TP Hồ Chí Minh", Align.CENTER, Size.SMALL))
            add(Line("ĐT: 02543 894 894 - 0865379119", Align.CENTER, Size.SMALL))
            addBlank()
            add(Line("Hóa đơn: ${invoice.invNumber ?: ".........."}", Align.CENTER, Size.BODY, bold = true))
            addBlank()
            add(Line("GIẤY BÁO TIỀN NƯỚC", Align.CENTER, Size.TITLE, bold = true))
            addBlank()
            addKv("Tên KH", invoice.custName ?: "")
            addKv("Địa chỉ", invoice.custAddress ?: "")
            addKv("Mã KH", invoice.custCode ?: "")
            val ym = invoice.yearMonth ?: ""
            val formattedYm = if (ym.length == 6) "${ym.substring(4, 6)}/${ym.substring(0, 4)}" else ym
            addKv("Kỳ thanh toán", formattedYm)
            addKv("Chỉ số mới", "${invoice.newIndex}")
            addKv("Chỉ số cũ", "${invoice.oldIndex}")
            val consumption = invoice.newIndex - invoice.oldIndex
            addKv("KL tiêu thụ (m³)", "$consumption")
            addBlank()
            addKv("TỔNG TIỀN", "${formatCurrencyForPrint(invoice.totalAmount)} đ", bold = true)
            addBlank()
            addKv("Ngày gửi giấy báo", getCurrentDateString())
            invoice.empPhone?.takeIf { it.isNotBlank() }?.let { addKv("ĐT thu ngân", it) }
            addBlank()
            add(Line("Thanh toán trong 7 ngày kể từ ngày gửi.", Align.LEFT, Size.SMALL))
            add(Line("Tra cứu HĐĐT: http://toctienltd.vn", Align.LEFT, Size.SMALL))
            addBlank()
            add(Line("THANH TOÁN TRỰC TUYẾN", Align.CENTER, Size.BODY, bold = true))
            add(Line("Bước 1: Mở app ngân hàng hoặc ví điện tử của bạn.", Align.LEFT, Size.BODY))
            add(Line("Bước 2: Chọn mục \"Thanh toán hóa đơn - Nước\".", Align.LEFT, Size.BODY))
            add(Line("Bước 3: Tìm từ khóa \"Cấp nước Tóc Tiên\" và nhập mã \"${invoice.custCode ?: ""}\" để thanh toán.", Align.LEFT, Size.BODY))
            // TODO: tạm ẩn mã QR giấy báo tiền nước
//            if (qrBitmap != null) {
//                addBlank()
//                add(Line("HOẶC QUÉT MÃ QR", Align.CENTER, Size.BODY, bold = true))
//                addQr()
//                add(Line("Mở app ngân hàng → Quét mã QR", Align.CENTER, Size.SMALL))
//            }
        }
        return composeToBitmap(lines, qrBitmap)
    }

    /** Render biên nhận thanh toán (Liên 2, không QR). */
    fun renderReceipt(receipt: ReceiptDto): Bitmap {
        val lines = buildList {
            add(Line("TẬP ĐOÀN HẢI CHÂU VIỆT NAM", Align.CENTER, Size.HEAD))
            add(Line("CÔNG TY TNHH CẤP NƯỚC TÓC TIÊN", Align.CENTER, Size.HEAD))
            add(Line("Ấp Tóc Tiên 1, xã Châu Pha, TP Hồ Chí Minh", Align.CENTER, Size.SMALL))
            add(Line("ĐT: 02543 894 894", Align.CENTER, Size.SMALL))
            addBlank()
            add(Line("Hóa đơn: ${receipt.invNumber ?: ".........."}", Align.CENTER, Size.BODY, bold = true))
            addBlank()
            add(Line("BIÊN NHẬN THANH TOÁN TIỀN NƯỚC", Align.CENTER, Size.HEAD, bold = true))
            add(Line("(Liên 2: Giao khách hàng)", Align.CENTER, Size.SMALL))
            addBlank()
            addKv("Tên KH", receipt.custName.uppercase())
            addKv("Địa chỉ", receipt.custAddress)
            addKv("Mã KH", receipt.custCode)
            receipt.custTaxCode?.takeIf { it.isNotBlank() }?.let { addKv("Mã số thuế", it) }
            if (receipt.numOfHouseHold > 1) addKv("Số hộ SD", "${receipt.numOfHouseHold}")
            receipt.paymentLineNum?.takeIf { it.isNotBlank() }?.let { addKv("Phiếu thu", it) }
            receipt.paymentLineDate?.takeIf { it.isNotBlank() }?.let { addKv("Ngày phiếu thu", formatApiDate(it)) }
            receipt.period.takeIf { it.isNotBlank() }?.let { addKv("Kỳ", it) }
            addBlank()
            // Bảng chỉ số (3 cột)
            add(Row3("CS mới", "CS cũ", "KL(m³)", header = true))
            val consumption = receipt.newIndex - receipt.oldIndex
            add(Row3("${receipt.newIndex}", "${receipt.oldIndex}", "$consumption"))
            addBlank()
            // Bảng giá 4 mức
            add(Row3("Mức(m³)", "Đơn giá", "Thành tiền", header = true))
            levelsOf(receipt).forEach { (vol, price, amount) ->
                if (!vol.isNullOrBlank() && vol != "0" && vol != "null") {
                    add(Row3(vol, price ?: "", amount ?: ""))
                }
            }
            addBlank()
            addKv("Tiền nước", receipt.amount)
            addKv("Thuế GTGT (5%)", receipt.taxFee)
            addKv("Phí BVMT (10%)", receipt.envFee)
            addKv("TỔNG THANH TOÁN", receipt.totalAmount, bold = true)
            receipt.totalAmountInWord.takeIf { it.isNotBlank() }?.let { addKv("Bằng chữ", it, small = true) }
            addBlank()
            addBlank()
            // Phần ký tên — chuẩn biên nhận Việt Nam
            add(SigRow(
                left  = "Người nộp tiền",
                right = "Thu ngân"
            ))
            addBlank()
            addBlank()
            addBlank()
            addBlank()
            addBlank()
            addBlank()
            addBlank()
            addBlank() // Tổng cộng 8 blank = 80px khoảng trống ký
            addBlank()
            add(SigRow(
                left  = "(Ký, họ tên)",
                right = if (receipt.empName.isNotBlank()) receipt.empName else "(Ký, họ tên)",
                rightBold = receipt.empName.isNotBlank()
            ))
            addBlank()
            add(Line("Cảm ơn Quý khách đã hoàn tất thanh toán tiền nước.", Align.CENTER, Size.SMALL, bold = true))
            add(Line("Công ty TNHH Cấp nước Tóc Tiên hân hạnh được phục vụ Quý khách!", Align.CENTER, Size.SMALL, bold = true))
        }
        return composeToBitmap(lines, null)
    }

    // ---- helper ----

    private fun MutableList<Drawable>.addKv(label: String, value: String, bold: Boolean = false, small: Boolean = false) {
        add(KvLine(label, value, bold, small))
    }

    private fun MutableList<Drawable>.addBlank() {
        add(Blank)
    }

    private fun MutableList<Drawable>.addQr() {
        add(Qr)
    }

    private fun MutableList<Drawable>.add(line: Line) = add(line as Drawable)
    private fun MutableList<Drawable>.add(row: Row3) = add(row as Drawable)

    private fun levelsOf(r: ReceiptDto) = listOf(
        Triple(r.volumn0, r.price0, r.amount0),
        Triple(r.volumn1, r.price1, r.amount1),
        Triple(r.volumn2, r.price2, r.amount2),
        Triple(r.volumn3, r.price3, r.amount3)
    )

    private fun composeToBitmap(items: List<Drawable>, qr: Bitmap?): Bitmap {
        val paintBody  = basePaint(FS_BODY)
        val paintSmall = basePaint(FS_SMALL)
        val paintHead  = basePaint(FS_HEAD,  bold = true)
        val paintTitle = basePaint(FS_TITLE, bold = true)
        val paintRow   = basePaint(FS_SMALL)

        val contentWidth = PRINT_WIDTH_PX - PADDING * 2
        // QR tối đa 480px (= ~80% canvas 576px) → sau scale xuống 384px thành ~320px, đủ to để quét
        val qrSize = if (qr != null) minOf(480, contentWidth) else 0

        // ước lượng chiều cao
        var height = PADDING + 8
        for (item in items) {
            height += when (item) {
                is Line   -> {
                    val p = when (item.size) {
                        Size.SMALL -> if (item.bold) basePaint(FS_SMALL, bold = true) else paintSmall
                        Size.HEAD -> paintHead
                        Size.TITLE -> paintTitle
                        Size.BODY -> if (item.bold) paintHead else paintBody
                    }
                    val w = p.measureText(item.text)
                    val avail = (contentWidth - item.indent * 2).coerceAtLeast(1f)
                    val lines = kotlin.math.ceil((w / avail).toDouble()).toInt().coerceAtLeast(1)
                    lines * (p.textSize + 6).toInt() + 6
                }
                is KvLine -> {
                    val p = basePaint(if (item.small) FS_SMALL else FS_BODY)
                    val labelW = p.measureText("${item.label}: ")
                    val valueW = p.measureText(item.value)
                    val availW = (contentWidth - labelW).coerceAtLeast(1f)
                    val estimatedLines = kotlin.math.ceil((valueW / availW).toDouble()).toInt().coerceAtLeast(1)
                    estimatedLines * (FS_BODY + 6).toInt() + 6
                }
                is Row3   -> (FS_SMALL + 8).toInt()
                is SigRow -> (FS_SMALL + 8).toInt()
                Blank     -> 10
                Qr        -> qrSize + 16
            }
        }
        height += PADDING + 16

        val bitmap = Bitmap.createBitmap(PRINT_WIDTH_PX, height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = PADDING + 24
        for (item in items) {
            when (item) {
                is Line -> {
                    val p = when (item.size) {
                        Size.SMALL -> if (item.bold) basePaint(FS_SMALL, bold = true) else paintSmall
                        Size.HEAD -> paintHead
                        Size.TITLE -> paintTitle
                        Size.BODY -> if (item.bold) paintHead else paintBody
                    }
                    paintSetColor(p, item)
                    val baseline = y.toFloat()
                    val nextY = drawWrappedText(canvas, item.text, PADDING.toFloat() + item.indent, (PRINT_WIDTH_PX - PADDING).toFloat() - item.indent, baseline, p, item.align)
                    y = nextY.toInt() + 4
                }
                is KvLine -> {
                    val p = basePaint(if (item.small) FS_SMALL else FS_BODY, bold = item.bold)
                    val baseline = y.toFloat()
                    canvas.drawText("${item.label}: ", PADDING.toFloat(), baseline, p)
                    val labelW = p.measureText("${item.label}: ")
                    val nextY = drawWrappedText(canvas, item.value, (PADDING + labelW), PADDING + contentWidth.toFloat(), baseline, p)
                    y = nextY.toInt() + 4
                }
                is Row3 -> {
                    val p = if (item.header) basePaint(FS_SMALL, bold = true) else paintRow
                    val c0 = contentWidth / 3f
                    val x0 = PADDING + c0 / 2f
                    val x1 = PADDING + c0 + c0 / 2f
                    val x2 = PADDING + c0 * 2 + c0 / 2f
                    val baseline = y.toFloat()
                    drawCentered(canvas, item.a, x0, baseline, p)
                    drawCentered(canvas, item.b, x1, baseline, p)
                    drawCentered(canvas, item.c, x2, baseline, p)
                    y += (FS_SMALL + 8).toInt()
                }
                is SigRow -> {
                    // Chia đôi trang: trái / phải
                    val midX = PRINT_WIDTH_PX / 2f
                    val baseline = y.toFloat()
                    val pL = basePaint(FS_SMALL, item.leftBold)
                    val pR = basePaint(FS_SMALL, item.rightBold)
                    // Vẽ phần trái: căn giữa nửa trái
                    val leftCenterX = PADDING + (midX - PADDING) / 2f
                    drawCentered(canvas, item.left, leftCenterX, baseline, pL)
                    // Vẽ phần phải: căn giữa nửa phải
                    val rightCenterX = midX + (PRINT_WIDTH_PX - PADDING - midX) / 2f
                    drawCentered(canvas, item.right, rightCenterX, baseline, pR)
                    y += (FS_SMALL + 8).toInt()
                }
                Blank -> y += 10
                Qr -> {
                    if (qr != null) {
                        val left = (PRINT_WIDTH_PX - qrSize) / 2f
                        
                        // Bước 1: Thu phóng QR về đúng kích thước in, bật filter để các nét không bị khuyết
                        val scaledQr = Bitmap.createScaledBitmap(qr, qrSize, qrSize, true)
                        // Bước 2: Ép các nét xám/viền mờ thành đen trắng tuyệt đối (chống dithering)
                        val bwQr = thresholdBitmap(scaledQr)
                        
                        val paint = Paint().apply {
                            isFilterBitmap = false
                            isAntiAlias = false
                            isDither = false
                        }
                        canvas.drawBitmap(bwQr, left, y.toFloat(), paint)
                        y += qrSize + 16
                    }
                }
            }
        }

        return bitmap
    }

    private fun drawWrappedText(canvas: Canvas, text: String, startX: Float, endX: Float, baseline: Float, paint: Paint, align: Align = Align.LEFT): Float {
        if (text.isBlank()) return baseline
        val words = text.split(" ")
        var line = ""
        var curY = baseline
        val maxW = endX - startX
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) <= maxW) {
                line = test
            } else {
                if (line.isNotEmpty()) {
                    when (align) {
                        Align.CENTER    -> drawCentered(canvas, line, startX + maxW / 2f, curY, paint)
                        Align.RIGHT_COL -> drawCentered(canvas, line, startX + maxW * 3f / 4f, curY, paint)
                        Align.LEFT      -> canvas.drawText(line, startX, curY, paint)
                    }
                    curY += paint.textSize + 4
                    line = word
                } else {
                    // từ quá dài, vẽ luôn
                    when (align) {
                        Align.CENTER    -> drawCentered(canvas, word, startX + maxW / 2f, curY, paint)
                        Align.RIGHT_COL -> drawCentered(canvas, word, startX + maxW * 3f / 4f, curY, paint)
                        Align.LEFT      -> canvas.drawText(word, startX, curY, paint)
                    }
                    curY += paint.textSize + 4
                }
            }
        }
        if (line.isNotEmpty()) {
            when (align) {
                Align.CENTER    -> drawCentered(canvas, line, startX + maxW / 2f, curY, paint)
                Align.RIGHT_COL -> drawCentered(canvas, line, startX + maxW * 3f / 4f, curY, paint)
                Align.LEFT      -> canvas.drawText(line, startX, curY, paint)
            }
            curY += paint.textSize + 4
        }
        return curY
    }

    private fun thresholdBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val outBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val gray = (r * 0.299 + g * 0.587 + b * 0.114).toInt()
            pixels[i] = if (gray > 128) Color.WHITE else Color.BLACK
        }
        outBmp.setPixels(pixels, 0, width, 0, 0, width, height)
        return outBmp
    }

    private fun drawCentered(canvas: Canvas, text: String, centerX: Float, baseline: Float, paint: Paint) {
        if (text.isBlank()) return
        val w = paint.measureText(text)
        canvas.drawText(text, centerX - w / 2f, baseline, paint)
    }

    private fun paintSetColor(p: Paint, line: Line) {
        // giữ đen cho mọi dòng (máy in nhiệt đen-trắng)
        p.color = Color.BLACK
    }

    private fun basePaint(size: Float, bold: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        color = Color.BLACK
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun lineHeight(line: Line, body: Paint, small: Paint, head: Paint, title: Paint): Int = when (line.size) {
        Size.SMALL -> small.textSize.toInt()
        Size.HEAD -> head.textSize.toInt()
        Size.TITLE -> title.textSize.toInt()
        Size.BODY -> body.textSize.toInt()
    }
}

// ---- model nội bộ cho layout ----
private enum class Align { LEFT, CENTER, RIGHT_COL }
private enum class Size { SMALL, BODY, HEAD, TITLE }

private sealed interface Drawable
private data class Line(val text: String, val align: Align, val size: Size, val bold: Boolean = false, val indent: Float = 0f) : Drawable
private data class KvLine(val label: String, val value: String, val bold: Boolean, val small: Boolean) : Drawable
private data class Row3(val a: String, val b: String, val c: String, val header: Boolean = false) : Drawable
private data object Blank : Drawable
private data class SigRow(
    val left: String,
    val right: String,
    val leftBold: Boolean = false,
    val rightBold: Boolean = false
) : Drawable
private data object Qr : Drawable
