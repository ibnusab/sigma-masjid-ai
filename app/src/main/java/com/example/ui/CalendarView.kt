package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Agenda
import com.example.data.Finance
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    viewModel: MasjidViewModel,
    onBack: () -> Unit
) {
    val agendaList by viewModel.agendaList.collectAsState()
    val financeList by viewModel.financeList.collectAsState()

    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))

    // Helper functions for matching dates
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    fun isSameDay(timeInMillis: Long, cal: Calendar): Boolean {
        val checkCal = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        return isSameDay(checkCal, cal)
    }

    // Days grid preparation
    val year = calendarMonth.get(Calendar.YEAR)
    val month = calendarMonth.get(Calendar.MONTH)
    
    val gridDays = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDay = cal.get(Calendar.DAY_OF_WEEK) // 1: Sunday, 2: Monday...
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val list = mutableListOf<Calendar?>()
        // Fill empty days before first day of the month
        // We assume week starts on Sunday (1) to Saturday (7)
        for (i in 1 until firstDay) {
            list.add(null)
        }
        // Fill actual days
        for (day in 1..maxDays) {
            val dCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            list.add(dCal)
        }
        list
    }

    // Current selected date info
    val filteredAgenda = agendaList.filter { isSameDay(it.date, selectedDate) }
    val filteredFinance = financeList.filter { isSameDay(it.date, selectedDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender Aktivitas & Keuangan", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Switcher Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            calendarMonth = (calendarMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                        }
                        
                        Text(
                            text = monthFormat.format(calendarMonth.time),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = EmeraldPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(onClick = {
                            calendarMonth = (calendarMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Weekday Titles
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val days = listOf("Ahd", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                        days.forEach { dayTitle ->
                            Text(
                                text = dayTitle,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (dayTitle == "Ahd") Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Month Days Grid
                    val chunks = gridDays.chunked(7)
                    chunks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            week.forEach { dayCal ->
                                if (dayCal == null) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val isSelected = isSameDay(dayCal, selectedDate)
                                    val isToday = isSameDay(dayCal, Calendar.getInstance())
                                    
                                    val hasAgenda = agendaList.any { isSameDay(it.date, dayCal) }
                                    val hasFinance = financeList.any { isSameDay(it.date, dayCal) }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> EmeraldPrimary
                                                    isToday -> EmeraldPrimary.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDate = dayCal }
                                            .testTag("day_${dayCal.get(Calendar.DAY_OF_MONTH)}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> EmeraldPrimary
                                                    dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> Color.Red
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            
                                            // Badges/Indicators below text
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (hasAgenda) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else GoldSecondary)
                                                    )
                                                }
                                                if (hasFinance) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else Color(0xFF22C55E))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Pad the end of the week if necessary
                            if (week.size < 7) {
                                for (i in week.size until 7) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }
            
            // Selected Day Header / Title
            Text(
                text = dateFormat.format(selectedDate.time),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = EmeraldPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            // List of Events on Selected Day
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Agenda Section
                item {
                    Text(
                        text = "📅 Agenda Kegiatan (${filteredAgenda.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                if (filteredAgenda.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "Tidak ada kegiatan terjadwal hari ini",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(filteredAgenda) { ag ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            ag.category,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ag.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ag.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (ag.description.isNotEmpty()) {
                                    Text(ag.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                if (ag.speaker.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoldSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(ag.speaker, fontSize = 11.sp, color = GoldSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Finance Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💵 Laporan Keuangan (${filteredFinance.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                if (filteredFinance.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "Tidak ada laporan keuangan hari ini",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(filteredFinance) { fn ->
                        val isIncome = fn.type == "PEMASUKAN"
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isIncome) Color(0xFF22C55E).copy(alpha = 0.12f)
                                                    else Color(0xFFDC2626).copy(alpha = 0.12f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                fn.category,
                                                color = if (isIncome) Color(0xFF15803D) else Color(0xFFB91C1C),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(fn.donorName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(fn.notes, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                
                                Text(
                                    text = "${if (isIncome) "+" else "-"} Rp ${NumberFormat.getInstance(Locale("id", "ID")).format(fn.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isIncome) Color(0xFF22C55E) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
