package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Finance
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldSecondary
import java.text.SimpleDateFormat
import java.util.*

val ChartColors = listOf(
    Color(0xFF10B981), // Emerald Green
    Color(0xFFD4AF37), // Gold/Amber
    Color(0xFF3B82F6), // Blue
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFFF97316), // Orange
    Color(0xFF06B6D4), // Cyan
    Color(0xFF14B8A6), // Teal
    Color(0xFF6366F1)  // Indigo
)

@Composable
fun FinancialSummaryChart(finances: List<Finance>, modifier: Modifier = Modifier) {
    if (finances.isEmpty()) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = EmeraldPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Visualisasi Data Kas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bagan tren, distribusi, dan analisis kas akan muncul di sini secara otomatis setelah Anda menambahkan beberapa transaksi.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf("TREN") } // "TREN", "DISTRIBUSI", "TOP"
    var distributionType by remember { mutableStateOf("PEMASUKAN") } // "PEMASUKAN" or "PENGELUARAN"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analisis Keuangan Real-time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Visualisasi arus kas & mutasi kas masjid",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Real-time Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair("TREN", "Tren Kas"),
                    Pair("DISTRIBUSI", "Distribusi"),
                    Pair("TOP", "Top Kategori")
                ).forEach { (tabId, tabName) ->
                    val isSelected = selectedTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                            .clickable { selectedTab = tabId }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart Content based on tab
            when (selectedTab) {
                "TREN" -> {
                    TrendLineChart(finances = finances)
                }
                "DISTRIBUSI" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Switch Pemasukan vs Pengeluaran
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (distributionType == "PEMASUKAN") Color(0xFF22C55E).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { distributionType = "PEMASUKAN" }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Pemasukan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (distributionType == "PEMASUKAN") Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (distributionType == "PENGELUARAN") Color(0xFFEF4444).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { distributionType = "PENGELUARAN" }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Pengeluaran",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (distributionType == "PENGELUARAN") Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        DonutCategoryChart(finances = finances, type = distributionType)
                    }
                }
                "TOP" -> {
                    TopCategoryBars(finances = finances)
                }
            }
        }
    }
}

@Composable
fun TrendLineChart(finances: List<Finance>) {
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    // Group finances by date, sort by timestamp ascending
    val sortedFinances = finances.sortedBy { it.date }
    val aggregatedData = remember(finances) {
        val dailyMap = sortedFinances.groupBy {
            dateFormat.format(Date(it.date))
        }
        
        // Let's build consecutive cumulative data or daily totals.
        // For visual impact, let's plot the total daily incomes and daily expenses.
        dailyMap.map { (dateStr, items) ->
            val income = items.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
            val expense = items.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
            Triple(dateStr, income, expense)
        }.takeLast(7) // Display the last 7 active transaction days
    }

    if (aggregatedData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada data tren", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column {
        // Legendary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp, 3.dp).background(Color(0xFF22C55E)))
                Text("Pemasukan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp, 3.dp).background(Color(0xFFEF4444)))
                Text("Pengeluaran", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val maxVal = remember(aggregatedData) {
            val maxIncome = aggregatedData.maxOfOrNull { it.second } ?: 0.0
            val maxExpense = aggregatedData.maxOfOrNull { it.third } ?: 0.0
            val max = Math.max(maxIncome, maxExpense)
            if (max == 0.0) 100000.0 else max * 1.15 // Add 15% head room
        }

        val density = LocalDensity.current
        val labelPaint = remember {
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = with(density) { 10.sp.toPx() }
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingLeft = 50.dp.toPx()
                val paddingRight = 10.dp.toPx()
                val paddingTop = 15.dp.toPx()
                val paddingBottom = 25.dp.toPx()

                val chartWidth = width - paddingLeft - paddingRight
                val chartHeight = height - paddingTop - paddingBottom

                // Draw horizontal grid lines (Y-axis)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (chartHeight * i / gridLines)
                    val gridValue = maxVal * (gridLines - i) / gridLines
                    
                    // Grid line
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        start = Offset(paddingLeft, y),
                        end = Offset(width - paddingRight, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Y Label
                    val formattedVal = if (gridValue >= 1_000_000) {
                        String.format("%.1fJ", gridValue / 1_000_000.0)
                    } else if (gridValue >= 1_000) {
                        String.format("%.0fK", gridValue / 1_000.0)
                    } else {
                        String.format("%.0f", gridValue)
                    }
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        formattedVal,
                        paddingLeft - 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        labelPaint
                    )
                }

                if (aggregatedData.size >= 1) {
                    val pointsCount = aggregatedData.size
                    val stepX = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth

                    val incomePoints = mutableListOf<Offset>()
                    val expensePoints = mutableListOf<Offset>()

                    // Calculate point offsets
                    for (i in 0 until pointsCount) {
                        val x = paddingLeft + (i * stepX)
                        
                        val incY = paddingTop + chartHeight - ((aggregatedData[i].second / maxVal) * chartHeight).toFloat()
                        val expY = paddingTop + chartHeight - ((aggregatedData[i].third / maxVal) * chartHeight).toFloat()

                        incomePoints.add(Offset(x, incY))
                        expensePoints.add(Offset(x, expY))

                        // Draw X Labels
                        drawContext.canvas.nativeCanvas.drawText(
                            aggregatedData[i].first,
                            x,
                            height - 6.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = with(density) { 9.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }

                    // Draw Income Line & Gradient Fill
                    if (incomePoints.size > 1) {
                        val incPath = Path().apply {
                            moveTo(incomePoints[0].x, incomePoints[0].y)
                            for (i in 1 until incomePoints.size) {
                                lineTo(incomePoints[i].x, incomePoints[i].y)
                            }
                        }
                        
                        // Fill Gradient
                        val incFillPath = Path().apply {
                            addPath(incPath)
                            lineTo(incomePoints.last().x, paddingTop + chartHeight)
                            lineTo(incomePoints.first().x, paddingTop + chartHeight)
                            close()
                        }
                        drawPath(
                            path = incFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF22C55E).copy(alpha = 0.2f), Color.Transparent)
                            )
                        )

                        // Stroke
                        drawPath(
                            path = incPath,
                            color = Color(0xFF22C55E),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw Expense Line & Gradient Fill
                    if (expensePoints.size > 1) {
                        val expPath = Path().apply {
                            moveTo(expensePoints[0].x, expensePoints[0].y)
                            for (i in 1 until expensePoints.size) {
                                lineTo(expensePoints[i].x, expensePoints[i].y)
                            }
                        }

                        // Fill Gradient
                        val expFillPath = Path().apply {
                            addPath(expPath)
                            lineTo(expensePoints.last().x, paddingTop + chartHeight)
                            lineTo(expensePoints.first().x, paddingTop + chartHeight)
                            close()
                        }
                        drawPath(
                            path = expFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444).copy(alpha = 0.15f), Color.Transparent)
                            )
                        )

                        // Stroke
                        drawPath(
                            path = expPath,
                            color = Color(0xFFEF4444),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw glowing point dots
                    incomePoints.forEach { pt ->
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = Color(0xFF22C55E), radius = 3.dp.toPx(), center = pt)
                    }

                    expensePoints.forEach { pt ->
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = Color(0xFFEF4444), radius = 3.dp.toPx(), center = pt)
                    }
                }
            }
        }
        
        if (aggregatedData.size < 2) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Catatan: Tambahkan transaksi di tanggal yang berbeda untuk melihat dinamika kurva tren.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DonutCategoryChart(finances: List<Finance>, type: String) {
    val filteredFinances = remember(finances, type) {
        finances.filter { it.type == type }
    }

    val categoryTotals = remember(filteredFinances) {
        filteredFinances.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalAmount = remember(categoryTotals) {
        categoryTotals.sumOf { it.second }
    }

    if (totalAmount == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Tidak ada mutasi ${type.lowercase()} untuk saat ini",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Donut Chart Drawing (Canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizeMin = size.minDimension
                val strokeW = 16.dp.toPx()
                val radius = (sizeMin - strokeW) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                var currentStartAngle = -90f

                categoryTotals.forEachIndexed { index, (_, amount) ->
                    val sweepAngle = ((amount / totalAmount) * 360f).toFloat()
                    val color = ChartColors[index % ChartColors.size]

                    drawArc(
                        color = color,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Butt),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                    )

                    currentStartAngle += sweepAngle
                }
            }

            // Center Text inside Donut
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Kas",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (totalAmount >= 1_000_000_000) {
                        String.format("Rp %.2fM", totalAmount / 1_000_000_000.0)
                    } else if (totalAmount >= 1_000_000) {
                        String.format("Rp %.1fJT", totalAmount / 1_000_000.0)
                    } else {
                        String.format("Rp %,.0f", totalAmount)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EmeraldDark
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Legend / Distribution List
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categoryTotals.take(5).forEachIndexed { index, (category, amount) ->
                val color = ChartColors[index % ChartColors.size]
                val percentage = (amount / totalAmount) * 100.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = String.format("%.1f%%", percentage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (categoryTotals.size > 5) {
                Text(
                    text = "+ ${categoryTotals.size - 5} kategori lainnya",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
        }
    }
}

@Composable
fun TopCategoryBars(finances: List<Finance>) {
    // Combine Pemasukan and Pengeluaran into top overall categories or just display absolute values
    val categoryTotals = remember(finances) {
        finances.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }

    val maxAmount = remember(categoryTotals) {
        categoryTotals.maxOfOrNull { it.second } ?: 1.0
    }

    if (categoryTotals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada data kategori", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Kategori Terbesar (Volume Transaksi)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        categoryTotals.forEachIndexed { index, (category, amount) ->
            val color = ChartColors[index % ChartColors.size]
            val progress = (amount / maxAmount).toFloat()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rp ${String.format("%,.0f", amount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }

                // Custom animated progress bar
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 800),
                    label = "ProgressBar"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
