package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto

@Composable
fun InvoicePaperDialog(
    invoice: InvoiceDto,
    onDismiss: () -> Unit,
    onPayCash: () -> Unit,
    onPrint: () -> Unit,
    isLoading: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Giấy báo content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text("TẬP ĐOÀN HẢI CHÂU VIỆT NAM", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    Text("CÔNG TY TNHH CẤP NƯỚC TÓC TIÊN", fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    Text("Địa chỉ: Ấp 6, xã Châu Pha, TP Hồ Chí Minh", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Điện thoại: 0254 389 4894 - 0865379119", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                        Text("Số: ${invoice.invNumber ?: ".........."}", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "GIẤY BÁO TIỀN NƯỚC", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.align(Alignment.CenterHorizontally), 
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    BillRow("Tên khách hàng", invoice.custName ?: "")
                    BillRow("Địa chỉ", invoice.custAddress ?: "")
                    BillRow("Mã khách hàng", invoice.custCode ?: "", isLabelRed = true)
                    
                    val ym = invoice.yearMonth ?: ""
                    val formattedYm = if (ym.length == 6) "${ym.substring(4, 6)}/${ym.substring(0, 4)}" else ym
                    BillRow("Kỳ thanh toán", formattedYm)
                    
                    BillRow("Thời gian SD", "")
                    BillRow("Chỉ số mới", "${invoice.newIndex}")
                    BillRow("Chỉ số cũ", "${invoice.oldIndex}")
                    val consumption = invoice.newIndex - invoice.oldIndex
                    BillRow("Khối lượng nước tiêu thụ (m3)", "$consumption")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    BillRow("Tổng số tiền thanh toán:", formatCurrency(invoice.totalAmount), isValueBold = true)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    val currentDate = com.example.appghichiso.utils.getCurrentDateString()
                    BillRow("Ngày gửi giấy báo:", currentDate)
                    BillRow("Điện thoại thu ngân viên:", invoice.empPhone ?: "")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Ghi chú: Mời Quý khách hàng thanh toán trong thời hạn 7 ngày kể từ ngày gửi giấy báo. Hóa đơn điện tử phát hành tại http://toctienltd.vn.\nLiên hệ: 0254 3894894 - 0865379119",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Buttons
                HorizontalDivider(color = Color.LightGray)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPayCash,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Màu xanh lá
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Thu Tiền", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPrint,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("In Giấy")
                        }
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Đóng")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, isLabelRed: Boolean = false, isValueBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLabelRed) Color.Red else Color.Black,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isValueBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val amountLong = amount.toLong()
    if (amountLong == 0L) return "0"
    val stringBuilder = StringBuilder(amountLong.toString())
    var i = stringBuilder.length - 3
    while (i > 0) {
        stringBuilder.insert(i, ".")
        i -= 3
    }
    return stringBuilder.toString()
}

@Composable
fun ReceiptDialog(
    receipt: ReceiptDto,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header
                    Text("TẬP ĐOÀN HẢI CHÂU VIỆT NAM", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    Text("CÔNG TY TNHH CẤP NƯỚC TÓC TIÊN", fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    Text("Địa chỉ: Ấp 6, xã Châu Pha, TP Hồ Chí Minh", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Điện thoại: 0254 389 4894", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                        Text("Số: ${receipt.invNumber ?: ".........."}", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "BIÊN NHẬN THANH TOÁN TIỀN NƯỚC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "(Liên 2: Giao khách hàng)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Thông tin khách hàng — dùng Column dọc thay vì Row ngang chật
                    ReceiptInfoRow("Tên khách hàng", receipt.custName.uppercase())
                    ReceiptInfoRow("Địa chỉ", receipt.custAddress)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Thông tin chi tiết — 2 cột nhưng dùng weight rộng hơn
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            ReceiptInfoRow("Mã KH", receipt.custCode, labelColor = Color.Red)
                            Spacer(Modifier.height(4.dp))
                            ReceiptInfoRow("Mã số thuế", receipt.custTaxCode ?: "")
                            Spacer(Modifier.height(4.dp))
                            ReceiptInfoRow("Tính từ ngày", receipt.timeToUsedFrom)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ReceiptInfoRow("Số hộ SD", "${receipt.numOfHouseHold}")
                            Spacer(Modifier.height(4.dp))
                            ReceiptInfoRow("Đến ngày", receipt.timeToUsedTo)
                            Spacer(Modifier.height(4.dp))
                            ReceiptInfoRow("Kỳ", receipt.period)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Bảng 1: Chỉ số
                    Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black)) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Text("Chỉ số mới", modifier = Modifier.weight(0.25f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            Text("Chỉ số cũ", modifier = Modifier.weight(0.25f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            Text("KL nước TT (m³)", modifier = Modifier.weight(0.5f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(color = Color.Black, thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Text("${receipt.newIndex}", modifier = Modifier.weight(0.25f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            Text("${receipt.oldIndex}", modifier = Modifier.weight(0.25f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            val consumption = receipt.newIndex - receipt.oldIndex
                            Text("$consumption", modifier = Modifier.weight(0.5f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        // Bảng 2: Thành tiền
                        HorizontalDivider(color = Color.Black, thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Text("Mức SD (m³)", modifier = Modifier.weight(0.33f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            Text("Đơn giá", modifier = Modifier.weight(0.33f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                            Text("Thành tiền", modifier = Modifier.weight(0.34f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                        }
                        val levels = listOf(
                            Triple(receipt.volumn0, receipt.price0, receipt.amount0),
                            Triple(receipt.volumn1, receipt.price1, receipt.amount1),
                            Triple(receipt.volumn2, receipt.price2, receipt.amount2),
                            Triple(receipt.volumn3, receipt.price3, receipt.amount3)
                        )
                        levels.forEach { (vol, price, amount) ->
                            if (vol != null && vol != "null" && vol.isNotBlank() && vol != "0") {
                                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Text(vol, modifier = Modifier.weight(0.33f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                                    Text(price ?: "", modifier = Modifier.weight(0.33f).padding(4.dp), textAlign = TextAlign.Center, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
                                    Text(amount ?: "", modifier = Modifier.weight(0.34f).padding(4.dp), textAlign = TextAlign.End, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    ReceiptInfoRow("Tiền nước tính thuế", receipt.amount)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReceiptInfoRow("Thuế GTGT (5%)", receipt.taxFee)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReceiptInfoRow("Phí BVMT (10%)", receipt.envFee)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng số tiền thanh toán:", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                        Text(receipt.totalAmount, fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Bằng chữ: ", color = Color.Black, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text(receipt.totalAmountInWord, color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ReceiptInfoRow("Mã tra cứu", receipt.lookupCode)

                    Spacer(modifier = Modifier.height(16.dp))
                    // Chữ ký
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Người nộp tiền\n\n(Ký, họ tên)", style = MaterialTheme.typography.bodySmall, color = Color.Black, textAlign = TextAlign.Center)
                        Text("Thu ngân\n\n(Ký, họ tên)", style = MaterialTheme.typography.bodySmall, color = Color.Black, textAlign = TextAlign.Center)
                    }
                }

                HorizontalDivider(color = Color.LightGray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPrint,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("In Biên Nhận")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String, labelColor: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = labelColor, style = MaterialTheme.typography.bodySmall)
        Text(value, color = Color.Black, style = MaterialTheme.typography.bodySmall)
    }
}

