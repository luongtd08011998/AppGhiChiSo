package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.appghichiso.util.currentDateString
import com.example.appghichiso.di.AppStateHolder
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterNoticeScreen(
    customerCode: String,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val appStateHolder = koinInject<AppStateHolder>()
    var isConfirming by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giấy Báo Tiền Nước", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Nền xám nhạt để nổi bật tờ giấy báo
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TỜ GIẤY BÁO TIỀN NƯỚC (Thiết kế giống hình)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp), // Vuông vức như tờ giấy
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Text(
                        text = "TẬP ĐOÀN HẢI CHÂU VIỆT NAM",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "CÔNG TY TNHH CẤP NƯỚC TÓC TIÊN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F), // Màu đỏ
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Địa chỉ: Ấp 6, Xã Châu Pha, Thành Phố Hồ Chí Minh, Việt Nam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "Điện thoại: 0254 389 4894 - 0865379119",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "Số: 00025746",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "GIẤY BÁO TIỀN NƯỚC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer Info
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text("Tên khách hàng: ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.35f))
                        Text(
                            text = "PHẠM THỊ LỆ TRANG", // MOCK
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text("Địa chỉ: ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.35f))
                        Text(
                            text = "QL 51, Phường Phú Mỹ, TP Hồ Chí Minh", // MOCK
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ReceiptRow("Mã khách hàng", customerCode, labelColor = Color(0xFFD32F2F))
                    ReceiptRow("Kỳ thanh toán", "04/2026")
                    ReceiptRow("Thời gian SD", "")
                    ReceiptRow("Chỉ số mới", "1989")
                    ReceiptRow("Chỉ số cũ", "1956")
                    ReceiptRow("Khối lượng nước tiêu thụ (m3)", "33")
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Tổng số tiền thanh toán:", fontSize = 13.sp, color = Color.Black)
                        Text(
                            text = "454.825", // MOCK
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val formattedDate = remember { currentDateString() }
                    
                    Text("Ngày gửi giấy báo: $formattedDate", fontSize = 13.sp, color = Color.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Điện thoại thu ngân viên:", fontSize = 13.sp, color = Color.Black)
                        Text(
                            text = "0906985359", // MOCK
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ghi chú: Mời Quý khách hàng thanh toán trong thời hạn 7 ngày kể từ ngày gửi giấy báo. Hóa đơn điện tử phát hành tại http://toctienltd.vn.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "Liên hệ : 0254 3894894 - 0865379119",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // QR Placeholder
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("QR Code", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "(51-PTLT)",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mã tra cứu: 02604822557",
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Kính báo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "http://toctienltd.vn/tra-cuu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }

            // ACTION BUTTONS
            Button(
                onClick = { /* TODO: Implement Bluetooth Printing */ },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))
            ) {
                Text("🖨 In Giấy Báo (Máy in Bluetooth)")
            }

            Button(
                onClick = {
                    isConfirming = true
                    coroutineScope.launch {
                        // Mock API Call delay
                        delay(1000)
                        isConfirming = false
                        appStateHolder.paidCustomerCodes.add(customerCode)
                        onPaymentSuccess()
                    }
                },
                enabled = !isConfirming,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Xác nhận đã thu tiền", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, labelColor: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = label, 
            fontSize = 13.sp, 
            color = labelColor, 
            modifier = Modifier.weight(0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.3f)
        )
    }
}

@Composable
fun DashedDivider(
    color: Color = Color.Red,
    thickness: Float = 1f,
    intervals: FloatArray = floatArrayOf(10f, 10f)
) {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = thickness,
            pathEffect = PathEffect.dashPathEffect(intervals, 0f)
        )
    }
}
