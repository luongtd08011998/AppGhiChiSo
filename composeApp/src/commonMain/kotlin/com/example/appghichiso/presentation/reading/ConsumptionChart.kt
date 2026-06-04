package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

/* ──────────────────────────────────────────────────────────────────────────
 *  ConsumptionHistoryChart  — wrapper card (Loading / Error / Success)
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun ConsumptionHistoryChart(historyState: HistoryState) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("📊  Lịch sử tiêu thụ nước")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            when (historyState) {
                is HistoryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
                is HistoryState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Không thể tải lịch sử tiêu thụ",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is HistoryState.Success -> {
                    val points = historyState.points
                    if (points.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chưa có dữ liệu lịch sử",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        ConsumptionBarChart(points = points)
                    }
                }
                is HistoryState.Idle -> { /* chưa load */ }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  ConsumptionBarChart  — Canvas bar chart
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun ConsumptionBarChart(points: List<ConsumptionPoint>) {
    val barColor   = Color(0xFF1565C0)
    val labelColor = Color(0xFF455A64)
    val valueColor = Color(0xFF0D47A1)
    val textMeasurer = rememberTextMeasurer()
    val maxConsumption = points.maxOfOrNull { it.consumption }?.coerceAtLeast(1) ?: 1

    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 8.sp, color = valueColor, fontWeight = FontWeight.Bold)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val chartHeight  = size.height - 36.dp.toPx()
        val chartWidth   = size.width
        val barCount     = points.size
        val totalGap     = chartWidth * 0.25f
        val barWidth     = (chartWidth - totalGap) / barCount
        val gap          = totalGap / (barCount + 1)

        points.forEachIndexed { i, point ->
            val barH   = (point.consumption.toFloat() / maxConsumption) * chartHeight
            val left   = gap + i * (barWidth + gap)
            val top    = chartHeight - barH
            val right  = left + barWidth
            val bottom = chartHeight

            /* Bar */
            drawRoundRect(
                color        = barColor.copy(alpha = 0.85f),
                topLeft      = Offset(left, top),
                size         = Size(barWidth, barH),
                cornerRadius = CornerRadius(6f, 6f)
            )

            /* Month label below bar */
            val labelLayout = textMeasurer.measure(point.label, labelStyle)
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    x = left + (barWidth - labelLayout.size.width) / 2,
                    y = chartHeight + 4.dp.toPx()
                )
            )

            /* Value above bar */
            if (point.consumption > 0) {
                val valueLayout = textMeasurer.measure("${point.consumption}", valueStyle)
                drawText(
                    textLayoutResult = valueLayout,
                    topLeft = Offset(
                        x = left + (barWidth - valueLayout.size.width) / 2,
                        y = (top - valueLayout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }
        }

        /* Baseline */
        drawLine(
            color       = labelColor.copy(alpha = 0.4f),
            start       = Offset(0f, chartHeight),
            end         = Offset(chartWidth, chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    Spacer(Modifier.height(4.dp))
    Text(
        "Đơn vị: m³  •  ${points.size} tháng gần nhất",
        style    = MaterialTheme.typography.labelSmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  Shared small composables
 * ────────────────────────────────────────────────────────────────────────── */

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier  = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(0.58f)
        )
    }
}
