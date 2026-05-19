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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptScreen(
    customerCode: String,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biên Nhận Thanh Toán", style = MaterialTheme.typography.titleMedium) },
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
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // TỜ BIÊN NHẬN (Thiết kế giống hình)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    DashedDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Địa chỉ: Ấp 6, Xã Châu Pha, Thành Phố Hồ Chí Minh, Việt Nam",
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Điện thoại: 0254 389 4894 - 0865379119", fontSize = 11.sp, color = Color.Black)
                        Text("Số: 00025746", fontSize = 11.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "BIÊN NHẬN THANH TOÁN TIỀN NƯỚC",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "(Liên 2: Giao khách hàng)",
                        fontSize = 11.sp,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer Info
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Tên khách hàng", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(0.3f))
                        Text(
                            text = "PHẠM THỊ LỆ TRANG", // MOCK
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Địa chỉ", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(0.3f))
                        Text(
                            text = "QL 51, Phường Phú Mỹ, TP Hồ Chí Minh", // MOCK
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "(51-PTLT)",
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    // Info Grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            ReceiptGridRow("Mã khách hàng", customerCode, isLabelRed = true)
                            ReceiptGridRow("Mã số thuế", "")
                            ReceiptGridRow("Tính từ ngày", "")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            ReceiptGridRow("Thời gian SD", "")
                            ReceiptGridRow("Số hộ SD", "1")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Đến ngày", fontSize = 11.sp, color = Color.Black)
                                Text("Kỳ: 04/2026", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }

                    // Table 1
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(modifier = Modifier.border(1.dp, Color.Black)) {
                        Row(modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.Black).padding(4.dp)) {
                            Text("Chỉ số mới", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("Chỉ số cũ", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("Khối lượng nước tiêu thụ (m3)", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Text("1989", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("1956", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("33", fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
                        }
                    }

                    // Table 2
                    Column(modifier = Modifier.border(1.dp, Color.Black)) {
                        Row(modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.Black).padding(4.dp)) {
                            Text("Mức sử dụng (m3)", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("Đơn giá (đồng)", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("Thành tiền (đồng)", fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Text("10\n10\n13", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                            Text("9.400\n12.600\n13.500", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                            Text("94.000\n126.000\n175.500", fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f), lineHeight = 18.sp)
                        }
                    }

                    // Bottom section
                    Row(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black)) {
                        Column(
                            modifier = Modifier.weight(1.8f).border(0.5.dp, Color.Black).padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ReceiptGridRow("Tiền nước tính thuế:", "395.500")
                            ReceiptGridRow("Thuế GTGT (5%):", "19.775")
                            ReceiptGridRow("Phí BVMT (10%):", "39.550")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng số tiền thanh toán:", fontSize = 11.sp, color = Color.Black)
                                Text("454.825", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Bằng chữ:", fontSize = 11.sp, color = Color.Black, modifier = Modifier.width(60.dp))
                                Text(
                                    "Bốn trăm năm mươi bốn ngàn tám trăm hai mươi lăm đồng", 
                                    fontSize = 11.sp, 
                                    color = Color.Black,
                                    lineHeight = 14.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Mã tra cứu: 02604822557",
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Giám đốc", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(40.dp))
                            // Placeholder for signature
                            Box(modifier = Modifier.height(30.dp).fillMaxWidth().padding(horizontal = 8.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // draw a fake signature line
                                    drawLine(Color.Gray, Offset(0f, size.height/2), Offset(size.width, size.height/2))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Phan Thanh Hải", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Text(
                        text = "http://toctienltd.vn/tra-cuu",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                Text("🖨 In Biên Nhận")
            }

            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Hoàn tất, trở về màn hình ghi")
            }
        }
    }
}

@Composable
fun ReceiptGridRow(label: String, value: String, isLabelRed: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = label, fontSize = 11.sp, color = if (isLabelRed) Color(0xFFD32F2F) else Color.Black)
        Text(text = value, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun InfoRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}


