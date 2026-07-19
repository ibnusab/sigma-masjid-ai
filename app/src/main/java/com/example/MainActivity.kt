package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import android.app.DatePickerDialog
import androidx.compose.ui.window.Dialog
import android.app.TimePickerDialog
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

data class IslamicCity(val name: String, val latitude: Double, val longitude: Double)

val indonesianCities = listOf(
    IslamicCity("Purbalingga", -7.3875, 109.3625),
    IslamicCity("Jakarta", -6.2088, 106.8456),
    IslamicCity("Surabaya", -7.2575, 112.7521),
    IslamicCity("Bandung", -6.9175, 107.6191),
    IslamicCity("Yogyakarta", -7.7956, 110.3695),
    IslamicCity("Medan", 3.5952, 98.6722),
    IslamicCity("Makassar", -5.1477, 119.4327),
    IslamicCity("Banda Aceh", 5.5483, 95.3238),
    IslamicCity("Balikpapan", -1.2654, 116.8312),
    IslamicCity("Jayapura", -2.5916, 140.6690)
)

fun calculateQiblaDirection(latitude: Double, longitude: Double): Double {
    val latKaaba = Math.toRadians(21.4225)
    val lonKaaba = Math.toRadians(39.8262)
    val latRad = Math.toRadians(latitude)
    val lonRad = Math.toRadians(longitude)
    
    val deltaLon = lonKaaba - lonRad
    val y = Math.sin(deltaLon) * Math.cos(latKaaba)
    val x = Math.cos(latRad) * Math.sin(latKaaba) - Math.sin(latRad) * Math.cos(latKaaba) * Math.cos(deltaLon)
    
    val bearingRad = Math.atan2(y, x)
    return (Math.toDegrees(bearingRad) + 360.0) % 360.0
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MasjidViewModel = viewModel()
            val themePref by viewModel.themePreference.collectAsState()
            val isDark = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDark) {
                MainAppShell(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: MasjidViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Manage dynamic screens/sub-views (Quran, Inventory, Library)
    var currentSubView by remember { mutableStateOf<String?>(null) }
    
    // Modern Back-press behavior handling
    BackHandler(enabled = true) {
        if (currentSubView != null) {
            currentSubView = null
        } else if (currentTab != 0) {
            viewModel.setTab(0)
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        Dialog(onDismissRequest = { showExitDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Konfirmasi Keluar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    val appName = context.getString(R.string.app_name)
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari aplikasi $appName?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showExitDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Batal", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                (context as? android.app.Activity)?.finish()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
    
    if (currentSubView != null) {
        when (currentSubView) {
            "quran" -> QuranView { currentSubView = null }
            "inventory" -> InventoryView(viewModel) { currentSubView = null }
            "library" -> LibraryView(viewModel) { currentSubView = null }
            "calendar" -> CalendarView(viewModel) { currentSubView = null }
            "settings" -> SettingsView(viewModel) { currentSubView = null }
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Jamaah") },
                    label = { Text("Jamaah", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Keuangan") },
                    label = { Text("Keuangan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Event, contentDescription = "Agenda") },
                    label = { Text("Agenda", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                    label = { Text("Asisten AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(viewModel) { viewName -> currentSubView = viewName }
                1 -> JamaahScreen(viewModel)
                2 -> KeuanganScreen(viewModel)
                3 -> AgendaScreen(viewModel)
                4 -> AiAssistantScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 🏠 DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: MasjidViewModel, onNavigateToSubView: (String) -> Unit) {
    val jamaah by viewModel.jamaahList.collectAsState()
    val finances by viewModel.financeList.collectAsState()
    val agenda by viewModel.agendaList.collectAsState()
    val masjidName by viewModel.masjidName.collectAsState()
    val masjidAddress by viewModel.masjidAddress.collectAsState()
    val prayerSchedules by viewModel.prayerScheduleList.collectAsState()

    var selectedPrayerId by remember { mutableStateOf<Int?>(null) }

    // State for Qibla Compass
    var selectedQiblaCity by remember { mutableStateOf(indonesianCities[0]) }
    var qiblaCityExpanded by remember { mutableStateOf(false) }
    var qiblaAutoMode by remember { mutableStateOf(true) }
    var manualAzimuth by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var azimuth by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        val sensorEventListener = object : SensorEventListener {
            private var gravity = FloatArray(3)
            private var geomagnetic = FloatArray(3)
            private var hasGravity = false
            private var hasGeomagnetic = false
            
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val headingRad = orientation[0]
                    var headingDeg = Math.toDegrees(headingRad.toDouble()).toFloat()
                    headingDeg = (headingDeg + 360) % 360
                    azimuth = headingDeg
                    isSensorAvailable = true
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                        hasGeomagnetic = true
                    }
                    
                    if (hasGravity && hasGeomagnetic) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(r, orientation)
                            val headingRad = orientation[0]
                            var headingDeg = Math.toDegrees(headingRad.toDouble()).toFloat()
                            headingDeg = (headingDeg + 360) % 360
                            azimuth = headingDeg
                            isSensorAvailable = true
                        }
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            isSensorAvailable = true
        } else {
            var registered = false
            if (accelerometer != null) {
                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                registered = true
            }
            if (magnetometer != null) {
                sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
                registered = true
            }
            isSensorAvailable = registered
        }
        
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    val totalPemasukan = finances.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
    val totalPengeluaran = finances.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
    val saldo = totalPemasukan - totalPengeluaran

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Image Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Sigma Masjid AI Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Settings Button at Top Right of Hero Image Box
                IconButton(
                    onClick = { onNavigateToSubView("settings") },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mosque, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = masjidName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$masjidAddress • Professional Mosque Management",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Real-Time Prayer Times Card
        item {
            val displayPrayers = if (prayerSchedules.isNotEmpty()) {
                prayerSchedules
            } else {
                listOf(
                    PrayerSchedule(id = 1, name = "Subuh", time = "04:45", imam = "Ust. Fauzi", muadzin = "Ust. Bilal"),
                    PrayerSchedule(id = 2, name = "Syuruq", time = "06:05", imam = "-", muadzin = "-"),
                    PrayerSchedule(id = 3, name = "Dzuhur", time = "12:02", imam = "Ust. Yusuf", muadzin = "Ust. Salim"),
                    PrayerSchedule(id = 4, name = "Ashar", time = "15:23", imam = "Ust. Jufri", muadzin = "Ust. Salim"),
                    PrayerSchedule(id = 5, name = "Maghrib", time = "18:10", imam = "Ust. Syakir", muadzin = "Ust. Bilal"),
                    PrayerSchedule(id = 6, name = "Isya", time = "19:22", imam = "Ust. Syakir", muadzin = "Ust. Bilal")
                )
            }
            val currentSelectedPrayer = displayPrayers.find { it.id == selectedPrayerId } ?: displayPrayers.find { it.name == "Dzuhur" } ?: displayPrayers.firstOrNull()

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jadwal Sholat Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EmeraldPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Detail Petugas",
                                color = GoldSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6 Prayer Times Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        displayPrayers.forEach { prayer ->
                            val isSelected = currentSelectedPrayer?.id == prayer.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { selectedPrayerId = prayer.id }
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = prayer.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.time,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (currentSelectedPrayer != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.08f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mosque,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Petugas Sholat ${currentSelectedPrayer.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Imam Sholat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text(currentSelectedPrayer.imam, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Muadzin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text(currentSelectedPrayer.muadzin, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Qibla Compass Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Arah Kiblat Digital",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "Sensor & Kompas Syariah",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // City Selection Dropdown
                        Box {
                            TextButton(
                                onClick = { qiblaCityExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(selectedQiblaCity.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            
                            DropdownMenu(
                                expanded = qiblaCityExpanded,
                                onDismissRequest = { qiblaCityExpanded = false }
                            ) {
                                indonesianCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text("${city.name} (${String.format("%.2f", city.latitude)}°, ${String.format("%.2f", city.longitude)}°)") },
                                        onClick = {
                                            selectedQiblaCity = city
                                            qiblaCityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    val targetQiblaAngle = calculateQiblaDirection(selectedQiblaCity.latitude, selectedQiblaCity.longitude).toFloat()
                    val compassHeading = if (qiblaAutoMode && isSensorAvailable) azimuth else manualAzimuth
                    val relativeAngle = (targetQiblaAngle - compassHeading + 360f) % 360f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // City Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Posisi: ${selectedQiblaCity.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // Qibla Angle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Explore, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sudut Kiblat: ${String.format("%.1f", targetQiblaAngle)}° dari Utara",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // Heading Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSensorAvailable && qiblaAutoMode) Icons.Default.CompassCalibration else Icons.Default.Adjust,
                                    contentDescription = null,
                                    tint = if (isSensorAvailable && qiblaAutoMode) Color(0xFF22C55E) else Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSensorAvailable && qiblaAutoMode) "Sensor HP: ${String.format("%.1f", compassHeading)}°" else "Kompas Manual: ${String.format("%.1f", compassHeading)}°",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Match indication
                            val isAligned = Math.abs(relativeAngle) < 5 || Math.abs(relativeAngle - 360f) < 5
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAligned) Color(0xFF22C55E).copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.05f))
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    text = if (isAligned) "✓ HP sejajar arah Kiblat!" else "Putar HP ${String.format("%.0f", relativeAngle)}° untuk sejajar",
                                    fontSize = 11.sp,
                                    color = if (isAligned) Color(0xFF22C55E) else EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Compass Graphic
                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Glow ring
                                Box(
                                    modifier = Modifier
                                        .size(105.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                )
                                
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    val centerX = size.width / 2
                                    val centerY = size.height / 2
                                    val radius = size.minDimension / 2
                                    
                                    // Dial circle
                                    drawCircle(
                                        color = EmeraldPrimary.copy(alpha = 0.2f),
                                        radius = radius,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = EmeraldPrimary,
                                        radius = radius - 6.dp.toPx(),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                    
                                    // Dial Ticks
                                    rotate(degrees = -compassHeading) {
                                        for (angle in 0 until 360 step 30) {
                                            val isCardinal = angle % 90 == 0
                                            val tickLength = if (isCardinal) 8.dp.toPx() else 4.dp.toPx()
                                            val tickColor = if (isCardinal) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.4f)
                                            val strokeW = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                                            
                                            rotate(degrees = angle.toFloat()) {
                                                drawLine(
                                                    color = tickColor,
                                                    start = Offset(centerX, centerY - radius + 6.dp.toPx()),
                                                    end = Offset(centerX, centerY - radius + 6.dp.toPx() + tickLength),
                                                    strokeWidth = strokeW
                                                )
                                            }
                                        }
                                        
                                        // Needles
                                        val needleWidth = 4.dp.toPx()
                                        val needleLength = radius - 14.dp.toPx()
                                        
                                        // North needle (Red)
                                        val northPath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(centerX, centerY - needleLength)
                                            lineTo(centerX - needleWidth, centerY)
                                            lineTo(centerX + needleWidth, centerY)
                                            close()
                                        }
                                        drawPath(northPath, color = Color(0xFFEF4444))
                                        
                                        // South needle (Slate)
                                        val southPath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(centerX, centerY + needleLength)
                                            lineTo(centerX - needleWidth, centerY)
                                            lineTo(centerX + needleWidth, centerY)
                                            close()
                                        }
                                        drawPath(southPath, color = Color(0xFF94A3B8))
                                    }
                                    
                                    // Qibla Pointer (Gold)
                                    rotate(degrees = targetQiblaAngle - compassHeading) {
                                        val qiblaLength = radius - 8.dp.toPx()
                                        val path = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(centerX, centerY - qiblaLength)
                                            lineTo(centerX - 8.dp.toPx(), centerY - qiblaLength + 12.dp.toPx())
                                            lineTo(centerX + 8.dp.toPx(), centerY - qiblaLength + 12.dp.toPx())
                                            close()
                                        }
                                        drawPath(path, color = GoldSecondary)
                                        
                                        drawLine(
                                            color = GoldSecondary,
                                            start = Offset(centerX, centerY),
                                            end = Offset(centerX, centerY - qiblaLength + 10.dp.toPx()),
                                            strokeWidth = 2.5.dp.toPx()
                                        )
                                    }
                                    
                                    // Center pin
                                    drawCircle(color = EmeraldDark, radius = 4.dp.toPx())
                                    drawCircle(color = Color.White, radius = 1.5.dp.toPx())
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🕋 ARAH KIBLAT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Mode Otomatis (Sensor):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = qiblaAutoMode && isSensorAvailable,
                                onCheckedChange = { 
                                    if (isSensorAvailable) {
                                        qiblaAutoMode = it
                                    } else {
                                        qiblaAutoMode = false
                                    }
                                },
                                enabled = isSensorAvailable,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldPrimary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                        
                        if (!isSensorAvailable) {
                            Text(
                                "⚠️ Sensor HP Tidak Tersedia",
                                fontSize = 9.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (!qiblaAutoMode || !isSensorAvailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Putar Kompas Secara Manual (Gunakan slider untuk menyesuaikan dengan kompas fisik):",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.RotateLeft, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Slider(
                                    value = manualAzimuth,
                                    onValueChange = { manualAzimuth = it },
                                    valueRange = 0f..360f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = EmeraldPrimary,
                                        activeTrackColor = EmeraldPrimary,
                                        inactiveTrackColor = EmeraldPrimary.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${manualAzimuth.toInt()}°",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Access Sub-modules Section
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aplikasi Syariah & Operasional",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 2x2 Grid for Quick Access
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Al-Quran Digital Button
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView("quran") },
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Al-Qur'an", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPrimary)
                                Text("114 Surat & Audio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        // Inventaris Button
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView("inventory") },
                            colors = CardDefaults.cardColors(containerColor = GoldSecondary.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GoldSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Inventaris", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldSecondary)
                                Text("Aset & Maintenance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Perpustakaan Button
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView("library") },
                            colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Perpustakaan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldDark)
                                Text("Log Peminjaman", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        // Kalender Aktivitas & Keuangan Button
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView("calendar") }
                                .testTag("calendar_button"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0EA5E9).copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0EA5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Kalender", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0EA5E9))
                                Text("Agenda & Keuangan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        // Live Statistics & Metrics Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Statistik Real-time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kas Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Saldo Kas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rp ${String.format("%,.0f", saldo)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    // Jamaah Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jumlah Jamaah", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${jamaah.size} Terdaftar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 👥 JAMAAH MANAGEMENT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamaahScreen(viewModel: MasjidViewModel) {
    val jamaahList by viewModel.jamaahList.collectAsState()
    val masjidName by viewModel.masjidName.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedJamaahQr by remember { mutableStateOf<Jamaah?>(null) }
    var editingJamaah by remember { mutableStateOf<Jamaah?>(null) }

    // Form states
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Warga") }

    val categories = listOf("Semua", "Warga", "Donatur", "Pengurus", "Musafir")

    val filteredList = jamaahList.filter {
        val matchesQuery = it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        val matchesCategory = selectedCategory == "Semua" || it.category == selectedCategory
        matchesQuery && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Jamaah", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Jamaah")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari Jamaah...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Scroll Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Jamaah
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada jamaah ditemukan", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { jm ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = jm.name.firstOrNull()?.toString() ?: "J",
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Information details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = jm.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Hub: ${jm.phone} • ${jm.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = jm.address,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }

                                Row {
                                    IconButton(onClick = { selectedJamaahQr = jm }) {
                                        Icon(Icons.Default.QrCode, contentDescription = "ID QR Code", tint = GoldSecondary)
                                    }
                                    IconButton(onClick = { editingJamaah = jm }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                    }
                                    IconButton(onClick = { viewModel.deleteJamaah(jm.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating dialog for Add Jamaah
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Registrasi Jamaah Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Lengkap") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("No. HP / Whatsapp") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Alamat Tinggal") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Jamaah", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Warga", "Donatur", "Pengurus", "Musafir").forEach { cat ->
                                    val isSel = category == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { category = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && phone.isNotEmpty()) {
                                    viewModel.addJamaah(name, phone, category, address)
                                    name = ""
                                    phone = ""
                                    address = ""
                                    category = "Warga"
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Floating dialog for Edit Jamaah
            if (editingJamaah != null) {
                val currentJm = editingJamaah!!
                var editName by remember(currentJm) { mutableStateOf(currentJm.name) }
                var editPhone by remember(currentJm) { mutableStateOf(currentJm.phone) }
                var editAddress by remember(currentJm) { mutableStateOf(currentJm.address) }
                var editCategory by remember(currentJm) { mutableStateOf(currentJm.category) }

                AlertDialog(
                    onDismissRequest = { editingJamaah = null },
                    title = { Text("Edit Data Jamaah", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nama Lengkap") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("No. HP / Whatsapp") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("Alamat Tinggal") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Jamaah", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Warga", "Donatur", "Pengurus", "Musafir").forEach { cat ->
                                    val isSel = editCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isNotEmpty() && editPhone.isNotEmpty()) {
                                    viewModel.updateJamaah(
                                        currentJm.copy(
                                            name = editName,
                                            phone = editPhone,
                                            category = editCategory,
                                            address = editAddress
                                        )
                                    )
                                    editingJamaah = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingJamaah = null }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // QR Code ID Dialog Display
            if (selectedJamaahQr != null) {
                val jm = selectedJamaahQr!!
                AlertDialog(
                    onDismissRequest = { selectedJamaahQr = null },
                    confirmButton = {
                        Button(
                            onClick = { selectedJamaahQr = null },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Tutup")
                        }
                    },
                    title = { Text("Kartu Anggota Jamaah", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(EmeraldPrimary.copy(alpha = 0.05f), EmeraldDark.copy(alpha = 0.1f))
                                    )
                                )
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(20.dp)
                        ) {
                            Text(
                                jm.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = EmeraldPrimary
                            )
                            Text(
                                jm.category.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // High-Fidelity Canvas-drawn QR Code
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val size = this.size.width
                                    val steps = 8
                                    val stepSize = size / steps
                                    
                                    // Draw custom abstract QR code block alignment
                                    for (i in 0 until steps) {
                                        for (j in 0 until steps) {
                                            // Make corner anchors solid like QR Code standard
                                            val isAnchor = (i < 3 && j < 3) || (i >= steps - 3 && j < 3) || (i < 3 && j >= steps - 3)
                                            val isRandomBlock = (i + j) % 3 == 0 || (i * j) % 2 == 0
                                            
                                            if (isAnchor || (!isAnchor && isRandomBlock)) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(i * stepSize, j * stepSize),
                                                    size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "ID: ${jm.qrCode}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                masjidName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 💰 FINANCIAL MANAGEMENT & ZAKAT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeuanganScreen(viewModel: MasjidViewModel) {
    val finances by viewModel.financeList.collectAsState()
    var isZakatMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFinance by remember { mutableStateOf<Finance?>(null) }

    // Dialog state
    var type by remember { mutableStateOf("PEMASUKAN") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Infaq") }
    var donorName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val totalPemasukan = finances.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
    val totalPengeluaran = finances.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
    val saldo = totalPemasukan - totalPengeluaran

    // Enhanced Zakat State
    var zakatType by remember { mutableStateOf("MAAL") } // "MAAL" or "FITRAH"
    var isMaalInputDetailed by remember { mutableStateOf(false) }
    
    // Zakat Mal detailed fields
    var simpleWealthInput by remember { mutableStateOf("") }
    var savingsWealth by remember { mutableStateOf("") }
    var goldWealth by remember { mutableStateOf("") }
    var investmentWealth by remember { mutableStateOf("") }
    var receivablesWealth by remember { mutableStateOf("") }
    var debtsVal by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("1200000") } // Default Rp 1.2M / gram
    
    // Zakat Fitrah detailed fields
    var fitrahPersons by remember { mutableStateOf("1") }
    var ricePrice by remember { mutableStateOf("15000") } // Default Rp 15,000 / kg
    
    // Calculated results
    var calculatedZakatMaal by remember { mutableStateOf<Double?>(null) }
    var isNishabMet by remember { mutableStateOf(false) }
    var netWealthCalculated by remember { mutableStateOf(0.0) }
    
    var calculatedFitrahRice by remember { mutableStateOf<Double?>(null) }
    var calculatedFitrahCash by remember { mutableStateOf<Double?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keuangan & Zakat", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        },
        floatingActionButton = {
            if (!isZakatMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = "Tambah Transaksi")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Ledger summary card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Saldo Kas Masjid", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(
                        "Rp ${String.format("%,.0f", saldo)}",
                        color = GoldSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Pemasukan", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("Rp ${String.format("%,.0f", totalPemasukan)}", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Pengeluaran", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("Rp ${String.format("%,.0f", totalPengeluaran)}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab toggler: Buku Kas vs Zakat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isZakatMode = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isZakatMode) EmeraldPrimary else Color.Transparent,
                        contentColor = if (!isZakatMode) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Buku Kas Masjid")
                }
                Button(
                    onClick = { isZakatMode = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isZakatMode) EmeraldPrimary else Color.Transparent,
                        contentColor = if (isZakatMode) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Kalkulator Zakat")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isZakatMode) {
                // Buku Kas Ledger List
                if (finances.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada mutasi keuangan", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            FinancialSummaryChart(finances = finances)
                        }
                        items(finances) { fn ->
                            val isIncome = fn.type == "PEMASUKAN"
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fn.notes,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Kat: ${fn.category} • Dari: ${fn.donorName}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(fn.date)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${if (isIncome) "+" else "-"} Rp ${String.format("%,.0f", fn.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isIncome) Color(0xFF22C55E) else Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { editingFinance = fn }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                        }
                                        IconButton(onClick = { viewModel.deleteFinance(fn.id) }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Enhanced Zakat Calculator View
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Zakat Category Switch (Maal vs Fitrah)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { 
                                    zakatType = "MAAL" 
                                    // Reset calculation results
                                    calculatedZakatMaal = null
                                    calculatedFitrahRice = null
                                    calculatedFitrahCash = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (zakatType == "MAAL") GoldSecondary else Color.Transparent,
                                    contentColor = if (zakatType == "MAAL") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zakat Maal (Harta)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { 
                                    zakatType = "FITRAH" 
                                    // Reset calculation results
                                    calculatedZakatMaal = null
                                    calculatedFitrahRice = null
                                    calculatedFitrahCash = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (zakatType == "FITRAH") GoldSecondary else Color.Transparent,
                                    contentColor = if (zakatType == "FITRAH") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zakat Fitrah (Jiwa)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (zakatType == "MAAL") {
                        // ==============================
                        // ZAKAT MAAL SCREEN
                        // ==============================
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Kalkulator Zakat Maal",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = EmeraldPrimary
                                        )
                                        
                                        // Mode switch (Simple vs Detailed)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("Mode Rinci", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Switch(
                                                checked = isMaalInputDetailed,
                                                onCheckedChange = { 
                                                    isMaalInputDetailed = it 
                                                    calculatedZakatMaal = null // Reset calculation on toggle
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = EmeraldPrimary
                                                )
                                            )
                                        }
                                    }
                                    
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    if (!isMaalInputDetailed) {
                                        // Simple Input Mode
                                        OutlinedTextField(
                                            value = simpleWealthInput,
                                            onValueChange = { simpleWealthInput = it },
                                            label = { Text("Total Nilai Kekayaan / Harta (Rp)") },
                                            placeholder = { Text("Contoh: 100000000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        // Detailed Input Mode
                                        Text(
                                            "Lengkapi rincian kekayaan Anda selama haul (1 tahun hijriah):",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        OutlinedTextField(
                                            value = savingsWealth,
                                            onValueChange = { savingsWealth = it },
                                            label = { Text("Uang Tunai, Tabungan & Deposito (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", color = EmeraldPrimary) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        OutlinedTextField(
                                            value = goldWealth,
                                            onValueChange = { goldWealth = it },
                                            label = { Text("Emas, Perak & Logam Mulia (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", color = EmeraldPrimary) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        OutlinedTextField(
                                            value = investmentWealth,
                                            onValueChange = { investmentWealth = it },
                                            label = { Text("Saham, Reksa Dana & Investasi (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", color = EmeraldPrimary) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        OutlinedTextField(
                                            value = receivablesWealth,
                                            onValueChange = { receivablesWealth = it },
                                            label = { Text("Piutang Lancar / Tagihan Aktif (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", color = EmeraldPrimary) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        OutlinedTextField(
                                            value = debtsVal,
                                            onValueChange = { debtsVal = it },
                                            label = { Text("Pengurang: Hutang Jatuh Tempo (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = { Text("Rp ", color = Color(0xFFEF4444)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    
                                    // Gold Price Calibration
                                    OutlinedTextField(
                                        value = goldPrice,
                                        onValueChange = { goldPrice = it },
                                        label = { Text("Harga Emas Saat Ini (Rp / gram)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = GoldSecondary) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            val goldVal = goldPrice.toDoubleOrNull() ?: 1200000.0
                                            val nishabLimit = goldVal * 85.0
                                            
                                            val wealth = if (!isMaalInputDetailed) {
                                                simpleWealthInput.toDoubleOrNull() ?: 0.0
                                            } else {
                                                val savings = savingsWealth.toDoubleOrNull() ?: 0.0
                                                val gold = goldWealth.toDoubleOrNull() ?: 0.0
                                                val investment = investmentWealth.toDoubleOrNull() ?: 0.0
                                                val receivables = receivablesWealth.toDoubleOrNull() ?: 0.0
                                                val debts = debtsVal.toDoubleOrNull() ?: 0.0
                                                (savings + gold + investment + receivables) - debts
                                            }
                                            
                                            netWealthCalculated = wealth
                                            isNishabMet = wealth >= nishabLimit
                                            calculatedZakatMaal = if (isNishabMet) wealth * 0.025 else 0.0
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Hitung Zakat Maal", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (calculatedZakatMaal != null) {
                            item {
                                val goldVal = goldPrice.toDoubleOrNull() ?: 1200000.0
                                val nishabLimit = goldVal * 85.0
                                
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isNishabMet) EmeraldPrimary.copy(alpha = 0.05f) else Color(0xFFDC2626).copy(alpha = 0.05f)
                                    ),
                                    border = BorderStroke(1.dp, if (isNishabMet) EmeraldPrimary else Color(0xFFDC2626)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (isNishabMet) "Wajib Membayar Zakat Maal ✓" else "Belum Mencapai Nishab ⚠️",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isNishabMet) EmeraldPrimary else Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Kekayaan Bersih Anda:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Rp ${String.format("%,.0f", netWealthCalculated)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Limit Nishab (85g Emas):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Rp ${String.format("%,.0f", nishabLimit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        if (isNishabMet) {
                                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = EmeraldPrimary.copy(alpha = 0.2f))
                                            Text("Zakat yang Wajib Dikeluarkan (2.5%):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text(
                                                "Rp ${String.format("%,.0f", calculatedZakatMaal)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                color = EmeraldPrimary,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.addFinance(
                                                        type = "PEMASUKAN",
                                                        amount = calculatedZakatMaal!!,
                                                        category = "Zakat Mal",
                                                        notes = "Pembayaran Zakat Maal",
                                                        donorName = "Muzakki"
                                                    )
                                                    calculatedZakatMaal = null
                                                    simpleWealthInput = ""
                                                    savingsWealth = ""
                                                    goldWealth = ""
                                                    investmentWealth = ""
                                                    receivablesWealth = ""
                                                    debtsVal = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Setor Zakat ke Kas Masjid", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Kekayaan Anda belum mencapai nishab (setara harga 85 gram emas saat ini). Anda belum berkewajiban membayar Zakat Maal, namun sangat dianjurkan untuk memperbanyak infaq dan sedekah.",
                                                fontSize = 11.sp,
                                                color = Color(0xFFDC2626),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ==============================
                        // ZAKAT FITRAH SCREEN
                        // ==============================
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Kalkulator Zakat Fitrah",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = EmeraldPrimary
                                    )
                                    
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Text(
                                        "Zakat Fitrah dikeluarkan setiap bulan Ramadhan berupa makanan pokok seberat 2.5 kg (atau 3.5 liter) beras per jiwa, atau berupa uang tunai senilai makanan pokok tersebut.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )

                                    OutlinedTextField(
                                        value = fitrahPersons,
                                        onValueChange = { fitrahPersons = it },
                                        label = { Text("Jumlah Anggota Keluarga / Jiwa") },
                                        placeholder = { Text("Contoh: 4") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = ricePrice,
                                        onValueChange = { ricePrice = it },
                                        label = { Text("Harga Beras Saat Ini (Rp / Kilogram)") },
                                        placeholder = { Text("Contoh: 15000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            val persons = fitrahPersons.toIntOrNull() ?: 1
                                            val rPrice = ricePrice.toDoubleOrNull() ?: 15000.0
                                            
                                            calculatedFitrahRice = persons * 2.5
                                            calculatedFitrahCash = persons * 2.5 * rPrice
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Hitung Zakat Fitrah", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (calculatedFitrahRice != null && calculatedFitrahCash != null) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, EmeraldPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Hasil Perhitungan Zakat Fitrah",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = EmeraldPrimary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // 1. Rice Option
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Pembayaran Beras (Makanan Pokok)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("${String.format("%.1f", calculatedFitrahRice)} kg Beras", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                                                    Text("Setara dengan 3.5 Liter per jiwa", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            // 2. Cash Option
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Payments, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Pembayaran Uang Tunai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("Rp ${String.format("%,.0f", calculatedFitrahCash)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                                                    Text("Berdasarkan harga Rp ${String.format("%,.0f", ricePrice.toDoubleOrNull() ?: 15000.0)} / kg", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = EmeraldPrimary.copy(alpha = 0.2f))

                                        Button(
                                            onClick = {
                                                viewModel.addFinance(
                                                    type = "PEMASUKAN",
                                                    amount = calculatedFitrahCash!!,
                                                    category = "Zakat Fitrah",
                                                    notes = "Zakat Fitrah (${fitrahPersons} Jiwa)",
                                                    donorName = "Muzakki Keluarga"
                                                )
                                                calculatedFitrahRice = null
                                                calculatedFitrahCash = null
                                                fitrahPersons = "1"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Setor Zakat Fitrah (Uang Tunai) ke Masjid", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transaction Add Dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Tambah Mutasi Kas", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Type Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { type = "PEMASUKAN" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type == "PEMASUKAN") Color(0xFF22C55E) else Color.Gray.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text("Pemasukan")
                                }
                                Button(
                                    onClick = { type = "PENGELUARAN" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type == "PENGELUARAN") Color(0xFFDC2626) else Color.Gray.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text("Pengeluaran")
                                }
                            }

                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                label = { Text("Jumlah (Rupiah)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (type == "PEMASUKAN") {
                                OutlinedTextField(
                                    value = donorName,
                                    onValueChange = { donorName = it },
                                    label = { Text("Nama Donatur / Pembayar") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Keterangan / Notes") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cats = if (type == "PEMASUKAN") listOf("Infaq", "Sedekah", "Zakat", "Donasi", "Wakaf") else listOf("Operasional", "Maintenance", "Bantuan Sosial", "Lainnya")
                                cats.forEach { cat ->
                                    val isSel = category == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { category = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amtValue = amount.toDoubleOrNull() ?: 0.0
                                if (amtValue > 0 && notes.isNotEmpty()) {
                                    viewModel.addFinance(
                                        type = type,
                                        amount = amtValue,
                                        category = category,
                                        notes = notes,
                                        donorName = if (type == "PEMASUKAN") donorName.ifEmpty { "Hamba Allah" } else "DKM Al-Hikmah"
                                    )
                                    amount = ""
                                    notes = ""
                                    donorName = ""
                                    category = "Infaq"
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Transaction Edit Dialog
            if (editingFinance != null) {
                val currentFn = editingFinance!!
                var editType by remember(currentFn) { mutableStateOf(currentFn.type) }
                var editAmount by remember(currentFn) { mutableStateOf(currentFn.amount.toString()) }
                var editCategory by remember(currentFn) { mutableStateOf(currentFn.category) }
                var editDonorName by remember(currentFn) { mutableStateOf(currentFn.donorName) }
                var editNotes by remember(currentFn) { mutableStateOf(currentFn.notes) }

                AlertDialog(
                    onDismissRequest = { editingFinance = null },
                    title = { Text("Edit Mutasi Kas", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Type Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        editType = "PEMASUKAN"
                                        if (editCategory != "Infaq" && editCategory != "Sedekah" && editCategory != "Zakat" && editCategory != "Donasi" && editCategory != "Wakaf") {
                                            editCategory = "Infaq"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editType == "PEMASUKAN") Color(0xFF22C55E) else Color.Gray.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text("Pemasukan")
                                }
                                Button(
                                    onClick = { 
                                        editType = "PENGELUARAN"
                                        if (editCategory != "Operasional" && editCategory != "Maintenance" && editCategory != "Bantuan Sosial" && editCategory != "Lainnya") {
                                            editCategory = "Operasional"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editType == "PENGELUARAN") Color(0xFFDC2626) else Color.Gray.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text("Pengeluaran")
                                }
                            }

                            OutlinedTextField(
                                value = editAmount,
                                onValueChange = { editAmount = it },
                                label = { Text("Jumlah (Rupiah)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (editType == "PEMASUKAN") {
                                OutlinedTextField(
                                    value = editDonorName,
                                    onValueChange = { editDonorName = it },
                                    label = { Text("Nama Donatur / Pembayar") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = editNotes,
                                onValueChange = { editNotes = it },
                                label = { Text("Keterangan / Notes") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cats = if (editType == "PEMASUKAN") listOf("Infaq", "Sedekah", "Zakat", "Donasi", "Wakaf") else listOf("Operasional", "Maintenance", "Bantuan Sosial", "Lainnya")
                                cats.forEach { cat ->
                                    val isSel = editCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amtValue = editAmount.toDoubleOrNull() ?: 0.0
                                if (amtValue > 0 && editNotes.isNotEmpty()) {
                                    viewModel.updateFinance(
                                        currentFn.copy(
                                            type = editType,
                                            amount = amtValue,
                                            category = editCategory,
                                            notes = editNotes,
                                            donorName = if (editType == "PEMASUKAN") editDonorName.ifEmpty { "Hamba Allah" } else "DKM Al-Hikmah"
                                        )
                                    )
                                    editingFinance = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingFinance = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 📅 AGENDA & SCHEDULING SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: MasjidViewModel) {
    val agendaList by viewModel.agendaList.collectAsState()
    val petugasList by viewModel.petugasList.collectAsState()
    val announcementList by viewModel.announcementList.collectAsState()
    val prayerScheduleList by viewModel.prayerScheduleList.collectAsState()

    var isOfficerMode by remember { mutableStateOf(0) } // 0: Agenda, 1: Jadwal Sholat, 2: Petugas, 3: Pengumuman
    
    // Add Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddPrayerScheduleDialog by remember { mutableStateOf(false) }
    var showAddPetugasDialog by remember { mutableStateOf(false) }
    var showAddAnnounceDialog by remember { mutableStateOf(false) }

    // Edit Dialog states
    var editingAgenda by remember { mutableStateOf<Agenda?>(null) }
    var editingPrayerSchedule by remember { mutableStateOf<PrayerSchedule?>(null) }
    var editingPetugas by remember { mutableStateOf<Petugas?>(null) }
    var editingAnnounce by remember { mutableStateOf<Announcement?>(null) }

    // Add Prayer Schedule fields
    var prayerNameInput by remember { mutableStateOf("") }
    var prayerTimeInput by remember { mutableStateOf("") }
    var prayerImamInput by remember { mutableStateOf("") }
    var prayerMuadzinInput by remember { mutableStateOf("") }

    // Add Agenda fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var speaker by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kajian") }
    var agendaDateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda & Penjadwalan", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (isOfficerMode) {
                        0 -> showAddDialog = true
                        1 -> {
                            prayerNameInput = ""
                            prayerTimeInput = "00:00"
                            prayerImamInput = ""
                            prayerMuadzinInput = ""
                            showAddPrayerScheduleDialog = true
                        }
                        2 -> showAddPetugasDialog = true
                        3 -> showAddAnnounceDialog = true
                    }
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Screen Section Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Kegiatan", "Jadwal Sholat", "Petugas", "Pengumuman")
                tabs.forEachIndexed { index, title ->
                    Button(
                        onClick = { isOfficerMode = index },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOfficerMode == index) EmeraldPrimary else Color.Transparent,
                            contentColor = if (isOfficerMode == index) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (isOfficerMode) {
                0 -> {
                    // Agenda List Screen
                    if (agendaList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada agenda terencana", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(agendaList) { ag ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(EmeraldPrimary.copy(alpha = 0.15f))
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
                                                Text(
                                                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ag.date)),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = ag.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = ag.description,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldSecondary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(ag.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                
                                                if (ag.speaker.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldSecondary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(ag.speaker, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { editingAgenda = ag }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                            }
                                            IconButton(onClick = { viewModel.deleteAgenda(ag.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Jadwal Sholat (Prayer Schedule) List with CRUD
                    if (prayerScheduleList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada jadwal sholat", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(prayerScheduleList) { prayer ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Schedule, "Jam", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(prayer.name, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 15.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(GoldSecondary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(prayer.time, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GoldSecondary)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Imam Sholat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                    Text(prayer.imam, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Muadzin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                    Text(prayer.muadzin, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                editingPrayerSchedule = prayer
                                                prayerNameInput = prayer.name
                                                prayerTimeInput = prayer.time
                                                prayerImamInput = prayer.imam
                                                prayerMuadzinInput = prayer.muadzin
                                            }) {
                                                Icon(Icons.Default.Edit, "Edit", tint = EmeraldPrimary)
                                            }
                                            IconButton(onClick = { viewModel.deletePrayerSchedule(prayer.id) }) {
                                                Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Petugas Sholat & DKM Schedule List (Fully dynamic from DB)
                    if (petugasList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada jadwal petugas", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Section: Jumat
                            val jumatPetugas = petugasList.filter { it.category == "Jumat" }
                            if (jumatPetugas.isNotEmpty()) {
                                item {
                                    Text(
                                        "Jadwal Petugas Sholat Jumat Ini",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(jumatPetugas) { pet ->
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pet.title, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                                if (pet.detail.isNotEmpty()) {
                                                    Text(pet.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                            Row {
                                                IconButton(onClick = { editingPetugas = pet }) {
                                                    Icon(Icons.Default.Edit, "Edit", tint = EmeraldPrimary)
                                                }
                                                IconButton(onClick = { viewModel.deletePetugas(pet.id) }) {
                                                    Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Rawatib / Harian
                            val rawatibPetugas = petugasList.filter { it.category == "Rawatib" }
                            if (rawatibPetugas.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Jadwal Petugas Harian Rawatib",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(rawatibPetugas) { pet ->
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pet.title, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                                if (pet.detail.isNotEmpty()) {
                                                    Text(pet.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                            Row {
                                                IconButton(onClick = { editingPetugas = pet }) {
                                                    Icon(Icons.Default.Edit, "Edit", tint = EmeraldPrimary)
                                                }
                                                IconButton(onClick = { viewModel.deletePetugas(pet.id) }) {
                                                    Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Lainnya
                            val lainnyaPetugas = petugasList.filter { it.category != "Jumat" && it.category != "Rawatib" }
                            if (lainnyaPetugas.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Petugas Acara Khusus / Lainnya",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(lainnyaPetugas) { pet ->
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(pet.title, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(EmeraldPrimary.copy(alpha = 0.1f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(pet.category, fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(pet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                                if (pet.detail.isNotEmpty()) {
                                                    Text(pet.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                            Row {
                                                IconButton(onClick = { editingPetugas = pet }) {
                                                    Icon(Icons.Default.Edit, "Edit", tint = EmeraldPrimary)
                                                }
                                                IconButton(onClick = { viewModel.deletePetugas(pet.id) }) {
                                                    Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Public Announcements List from DB
                    if (announcementList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada pengumuman aktif", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(announcementList) { ann ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        ann.category,
                                                        color = EmeraldPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ann.date)),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { editingAnnounce = ann },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { viewModel.deleteAnnouncement(ann.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(ann.title, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(ann.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialog for Adding Agenda
            if (showAddDialog) {
                val context = LocalContext.current
                val calendar = Calendar.getInstance().apply { timeInMillis = agendaDateInMillis }
                val datePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            agendaDateInMillis = cal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }

                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Tambah Agenda Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Nama Kegiatan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Deskripsi") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = speaker,
                                onValueChange = { speaker = it },
                                label = { Text("Penceramah / Speaker (Opsional)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Date Selector Field
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                            ) {
                                OutlinedTextField(
                                    value = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date(agendaDateInMillis)),
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Tanggal Kegiatan") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pilih Tanggal"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            // Time Picker Selection Row (Start & End Time)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Waktu Mulai Picker
                                val startHour = startTime.split(":").getOrNull(0)?.toIntOrNull() ?: 8
                                val startMinute = startTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
                                val startTimePickerDialog = remember {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            startTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        startHour,
                                        startMinute,
                                        true
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { startTimePickerDialog.show() }
                                ) {
                                    OutlinedTextField(
                                        value = startTime,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Waktu Mulai") },
                                        trailingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Pilih Jam")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                // Waktu Selesai Picker
                                val endHour = endTime.split(":").getOrNull(0)?.toIntOrNull() ?: 9
                                val endMinute = endTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
                                val endTimePickerDialog = remember {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            endTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        endHour,
                                        endMinute,
                                        true
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { endTimePickerDialog.show() }
                                ) {
                                    OutlinedTextField(
                                        value = endTime,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Waktu Selesai") },
                                        trailingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Pilih Jam")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Kegiatan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Kajian", "Sholat Jumat", "Ramadhan", "Rapat DKM").forEach { cat ->
                                    val isSel = category == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { category = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (title.isNotEmpty()) {
                                    viewModel.addAgenda(
                                        title = title,
                                        description = description,
                                        date = agendaDateInMillis,
                                        time = "$startTime - $endTime",
                                        speaker = speaker,
                                        category = category
                                    )
                                    title = ""
                                    description = ""
                                    speaker = ""
                                    category = "Kajian"
                                    agendaDateInMillis = System.currentTimeMillis()
                                    startTime = "08:00"
                                    endTime = "09:00"
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Editing Agenda
            if (editingAgenda != null) {
                val currentAg = editingAgenda!!
                var editTitle by remember(currentAg) { mutableStateOf(currentAg.title) }
                var editDescription by remember(currentAg) { mutableStateOf(currentAg.description) }
                var editDateInMillis by remember(currentAg) { mutableStateOf(currentAg.date) }
                
                // Parse existing time string e.g. "18:30 - 20:00"
                val timeParts = currentAg.time.split("-")
                var editStartTime by remember(currentAg) { mutableStateOf(timeParts.getOrNull(0)?.trim() ?: "18:30") }
                var editEndTime by remember(currentAg) { mutableStateOf(timeParts.getOrNull(1)?.trim() ?: "20:00") }
                
                var editSpeaker by remember(currentAg) { mutableStateOf(currentAg.speaker) }
                var editCategory by remember(currentAg) { mutableStateOf(currentAg.category) }

                val context = LocalContext.current
                val calendar = Calendar.getInstance().apply { timeInMillis = editDateInMillis }
                val datePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            editDateInMillis = cal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }

                AlertDialog(
                    onDismissRequest = { editingAgenda = null },
                    title = { Text("Edit Agenda", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Nama Kegiatan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { editDescription = it },
                                label = { Text("Deskripsi") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editSpeaker,
                                onValueChange = { editSpeaker = it },
                                label = { Text("Penceramah / Speaker (Opsional)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Date Selector Field
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                            ) {
                                OutlinedTextField(
                                    value = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date(editDateInMillis)),
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Tanggal Kegiatan") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pilih Tanggal"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            // Time Picker Selection Row (Start & End Time)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Waktu Mulai Picker
                                val startHour = editStartTime.split(":").getOrNull(0)?.toIntOrNull() ?: 18
                                val startMinute = editStartTime.split(":").getOrNull(1)?.toIntOrNull() ?: 30
                                val startTimePickerDialog = remember {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            editStartTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        startHour,
                                        startMinute,
                                        true
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { startTimePickerDialog.show() }
                                ) {
                                    OutlinedTextField(
                                        value = editStartTime,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Waktu Mulai") },
                                        trailingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Pilih Jam")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                // Waktu Selesai Picker
                                val endHour = editEndTime.split(":").getOrNull(0)?.toIntOrNull() ?: 20
                                val endMinute = editEndTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
                                val endTimePickerDialog = remember {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            editEndTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        endHour,
                                        endMinute,
                                        true
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { endTimePickerDialog.show() }
                                ) {
                                    OutlinedTextField(
                                        value = editEndTime,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Waktu Selesai") },
                                        trailingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Pilih Jam")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Kegiatan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Kajian", "Sholat Jumat", "Ramadhan", "Rapat DKM").forEach { cat ->
                                    val isSel = editCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editTitle.isNotEmpty()) {
                                    viewModel.updateAgenda(
                                        currentAg.copy(
                                            title = editTitle,
                                            description = editDescription,
                                            date = editDateInMillis,
                                            time = "$editStartTime - $editEndTime",
                                            speaker = editSpeaker,
                                            category = editCategory
                                        )
                                    )
                                    editingAgenda = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingAgenda = null }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Adding Petugas
            if (showAddPetugasDialog) {
                var petName by remember { mutableStateOf("") }
                var petTitle by remember { mutableStateOf("") }
                var petCategory by remember { mutableStateOf("Jumat") }
                var petDetail by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddPetugasDialog = false },
                    title = { Text("Tambah Petugas Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Nama Lengkap Petugas") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = petTitle,
                                onValueChange = { petTitle = it },
                                label = { Text("Jabatan / Peran (contoh: Khatib, Imam, Subuh)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = petDetail,
                                onValueChange = { petDetail = it },
                                label = { Text("Keterangan Tambahan (contoh: Muadzin: Ust. Bilal)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Penjadwalan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Jumat", "Rawatib", "Lainnya").forEach { cat ->
                                    val isSel = petCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { petCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (petName.isNotEmpty() && petTitle.isNotEmpty()) {
                                    viewModel.addPetugas(
                                        title = petTitle,
                                        category = petCategory,
                                        name = petName,
                                        detail = petDetail
                                    )
                                    showAddPetugasDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddPetugasDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Editing Petugas
            if (editingPetugas != null) {
                val currentPet = editingPetugas!!
                var editPetName by remember(currentPet) { mutableStateOf(currentPet.name) }
                var editPetTitle by remember(currentPet) { mutableStateOf(currentPet.title) }
                var editPetCategory by remember(currentPet) { mutableStateOf(currentPet.category) }
                var editPetDetail by remember(currentPet) { mutableStateOf(currentPet.detail) }

                AlertDialog(
                    onDismissRequest = { editingPetugas = null },
                    title = { Text("Edit Petugas", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPetName,
                                onValueChange = { editPetName = it },
                                label = { Text("Nama Lengkap Petugas") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editPetTitle,
                                onValueChange = { editPetTitle = it },
                                label = { Text("Jabatan / Peran") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editPetDetail,
                                onValueChange = { editPetDetail = it },
                                label = { Text("Keterangan Tambahan") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Penjadwalan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Jumat", "Rawatib", "Lainnya").forEach { cat ->
                                    val isSel = editPetCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editPetCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editPetName.isNotEmpty() && editPetTitle.isNotEmpty()) {
                                    viewModel.updatePetugas(
                                        currentPet.copy(
                                            name = editPetName,
                                            title = editPetTitle,
                                            category = editPetCategory,
                                            detail = editPetDetail
                                        )
                                    )
                                    editingPetugas = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingPetugas = null }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Adding Announcement
            if (showAddAnnounceDialog) {
                var annTitle by remember { mutableStateOf("") }
                var annContent by remember { mutableStateOf("") }
                var annCategory by remember { mutableStateOf("Umum") }

                AlertDialog(
                    onDismissRequest = { showAddAnnounceDialog = false },
                    title = { Text("Tambah Pengumuman Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = annTitle,
                                onValueChange = { annTitle = it },
                                label = { Text("Judul Pengumuman") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = annContent,
                                onValueChange = { annContent = it },
                                label = { Text("Isi Pengumuman") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Pengumuman", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Umum", "Kajian", "Zakat", "Sosial").forEach { cat ->
                                    val isSel = annCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { annCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (annTitle.isNotEmpty() && annContent.isNotEmpty()) {
                                    viewModel.addAnnouncement(
                                        title = annTitle,
                                        content = annContent,
                                        category = annCategory
                                    )
                                    showAddAnnounceDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddAnnounceDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Editing Announcement
            if (editingAnnounce != null) {
                val currentAnn = editingAnnounce!!
                var editAnnTitle by remember(currentAnn) { mutableStateOf(currentAnn.title) }
                var editAnnContent by remember(currentAnn) { mutableStateOf(currentAnn.content) }
                var editAnnCategory by remember(currentAnn) { mutableStateOf(currentAnn.category) }

                AlertDialog(
                    onDismissRequest = { editingAnnounce = null },
                    title = { Text("Edit Pengumuman", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editAnnTitle,
                                onValueChange = { editAnnTitle = it },
                                label = { Text("Judul Pengumuman") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editAnnContent,
                                onValueChange = { editAnnContent = it },
                                label = { Text("Isi Pengumuman") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori Pengumuman", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Umum", "Kajian", "Zakat", "Sosial").forEach { cat ->
                                    val isSel = editAnnCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editAnnCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editAnnTitle.isNotEmpty() && editAnnContent.isNotEmpty()) {
                                    viewModel.updateAnnouncement(
                                        currentAnn.copy(
                                            title = editAnnTitle,
                                            content = editAnnContent,
                                            category = editAnnCategory
                                        )
                                    )
                                    editingAnnounce = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingAnnounce = null }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Adding Prayer Schedule
            if (showAddPrayerScheduleDialog) {
                var nameVal by remember { mutableStateOf("") }
                var timeVal by remember { mutableStateOf("12:00") }
                var imamVal by remember { mutableStateOf("-") }
                var muadzinVal by remember { mutableStateOf("-") }

                var isImamManual by remember { mutableStateOf(false) }
                var isMuadzinManual by remember { mutableStateOf(false) }

                var imamExpanded by remember { mutableStateOf(false) }
                var muadzinExpanded by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showAddPrayerScheduleDialog = false },
                    title = { Text("Tambah Jadwal Sholat Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = nameVal,
                                onValueChange = { nameVal = it },
                                label = { Text("Nama Waktu (contoh: Dhuha, Tarawih)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = timeVal,
                                onValueChange = { timeVal = it },
                                label = { Text("Waktu (contoh: 08:30)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Imam Field with Dropdown of Petugas & Manual Fallback
                            if (isImamManual) {
                                OutlinedTextField(
                                    value = imamVal,
                                    onValueChange = { imamVal = it },
                                    label = { Text("Imam Sholat (Manual)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isImamManual = false 
                                            imamVal = "-"
                                        }) {
                                            Icon(Icons.Default.People, "Pilih dari Daftar")
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = if (imamVal == "-") "Pilih Imam..." else imamVal,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Imam Sholat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, "Pilih Imam")
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { imamExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = imamExpanded,
                                        onDismissRequest = { imamExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("- (Tanpa Imam)") },
                                            onClick = {
                                                imamVal = "-"
                                                imamExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✍️ Ketik Manual...") },
                                            onClick = {
                                                isImamManual = true
                                                imamVal = ""
                                                imamExpanded = false
                                            }
                                        )
                                        petugasList.forEach { petugas ->
                                            DropdownMenuItem(
                                                text = { Text("${petugas.name} (${petugas.title})") },
                                                onClick = {
                                                    imamVal = petugas.name
                                                    imamExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Muadzin Field with Dropdown of Petugas & Manual Fallback
                            if (isMuadzinManual) {
                                OutlinedTextField(
                                    value = muadzinVal,
                                    onValueChange = { muadzinVal = it },
                                    label = { Text("Muadzin Sholat (Manual)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isMuadzinManual = false 
                                            muadzinVal = "-"
                                        }) {
                                            Icon(Icons.Default.People, "Pilih dari Daftar")
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = if (muadzinVal == "-") "Pilih Muadzin..." else muadzinVal,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Muadzin Sholat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, "Pilih Muadzin")
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { muadzinExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = muadzinExpanded,
                                        onDismissRequest = { muadzinExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("- (Tanpa Muadzin)") },
                                            onClick = {
                                                muadzinVal = "-"
                                                muadzinExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✍️ Ketik Manual...") },
                                            onClick = {
                                                isMuadzinManual = true
                                                muadzinVal = ""
                                                muadzinExpanded = false
                                            }
                                        )
                                        petugasList.forEach { petugas ->
                                            DropdownMenuItem(
                                                text = { Text("${petugas.name} (${petugas.title})") },
                                                onClick = {
                                                    muadzinVal = petugas.name
                                                    muadzinExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameVal.isNotEmpty() && timeVal.isNotEmpty()) {
                                    viewModel.addPrayerSchedule(
                                        name = nameVal,
                                        time = timeVal,
                                        imam = if (imamVal.isEmpty()) "-" else imamVal,
                                        muadzin = if (muadzinVal.isEmpty()) "-" else muadzinVal
                                    )
                                    showAddPrayerScheduleDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddPrayerScheduleDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog for Editing Prayer Schedule
            if (editingPrayerSchedule != null) {
                val currentPs = editingPrayerSchedule!!
                var editNameVal by remember(currentPs) { mutableStateOf(currentPs.name) }
                var editTimeVal by remember(currentPs) { mutableStateOf(currentPs.time) }
                var editImamVal by remember(currentPs) { mutableStateOf(currentPs.imam) }
                var editMuadzinVal by remember(currentPs) { mutableStateOf(currentPs.muadzin) }

                val isImamInList = petugasList.any { it.name == currentPs.imam } || currentPs.imam == "-"
                val isMuadzinInList = petugasList.any { it.name == currentPs.muadzin } || currentPs.muadzin == "-"

                var isImamManual by remember(currentPs) { mutableStateOf(!isImamInList) }
                var isMuadzinManual by remember(currentPs) { mutableStateOf(!isMuadzinInList) }

                var imamExpanded by remember { mutableStateOf(false) }
                var muadzinExpanded by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { editingPrayerSchedule = null },
                    title = { Text("Edit Jadwal & Petugas Sholat", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editNameVal,
                                onValueChange = { editNameVal = it },
                                label = { Text("Nama Waktu (contoh: Subuh)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editTimeVal,
                                onValueChange = { editTimeVal = it },
                                label = { Text("Waktu (contoh: 04:45)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Imam selection/input for editing
                            if (isImamManual) {
                                OutlinedTextField(
                                    value = editImamVal,
                                    onValueChange = { editImamVal = it },
                                    label = { Text("Imam Sholat (Manual)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isImamManual = false 
                                            editImamVal = "-"
                                        }) {
                                            Icon(Icons.Default.People, "Pilih dari Daftar")
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = if (editImamVal == "-") "Pilih Imam..." else editImamVal,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Imam Sholat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, "Pilih Imam")
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { imamExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = imamExpanded,
                                        onDismissRequest = { imamExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("- (Tanpa Imam)") },
                                            onClick = {
                                                editImamVal = "-"
                                                imamExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✍️ Ketik Manual...") },
                                            onClick = {
                                                isImamManual = true
                                                editImamVal = ""
                                                imamExpanded = false
                                            }
                                        )
                                        petugasList.forEach { petugas ->
                                            DropdownMenuItem(
                                                text = { Text("${petugas.name} (${petugas.title})") },
                                                onClick = {
                                                    editImamVal = petugas.name
                                                    imamExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Muadzin selection/input for editing
                            if (isMuadzinManual) {
                                OutlinedTextField(
                                    value = editMuadzinVal,
                                    onValueChange = { editMuadzinVal = it },
                                    label = { Text("Muadzin Sholat (Manual)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isMuadzinManual = false 
                                            editMuadzinVal = "-"
                                        }) {
                                            Icon(Icons.Default.People, "Pilih dari Daftar")
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = if (editMuadzinVal == "-") "Pilih Muadzin..." else editMuadzinVal,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Muadzin Sholat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, "Pilih Muadzin")
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { muadzinExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = muadzinExpanded,
                                        onDismissRequest = { muadzinExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("- (Tanpa Muadzin)") },
                                            onClick = {
                                                editMuadzinVal = "-"
                                                muadzinExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✍️ Ketik Manual...") },
                                            onClick = {
                                                isMuadzinManual = true
                                                editMuadzinVal = ""
                                                muadzinExpanded = false
                                            }
                                        )
                                        petugasList.forEach { petugas ->
                                            DropdownMenuItem(
                                                text = { Text("${petugas.name} (${petugas.title})") },
                                                onClick = {
                                                    editMuadzinVal = petugas.name
                                                    muadzinExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editNameVal.isNotEmpty() && editTimeVal.isNotEmpty()) {
                                    viewModel.updatePrayerSchedule(
                                        currentPs.copy(
                                            name = editNameVal,
                                            time = editTimeVal,
                                            imam = if (editImamVal.isEmpty()) "-" else editImamVal,
                                            muadzin = if (editMuadzinVal.isEmpty()) "-" else editMuadzinVal
                                        )
                                    )
                                    editingPrayerSchedule = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingPrayerSchedule = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 🤖 GEMINI AI ASSISTANT CHAT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: MasjidViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val aiIsLoading by viewModel.aiIsLoading.collectAsState()
    var userMessageText by remember { mutableStateOf("") }

    val quickActions = listOf(
        "📊 Ringkas Keuangan" to "Ringkas laporan keuangan kas masjid saat ini beserta rekomendasi anggarannya.",
        "📢 Pengumuman Jumat" to "Buat draf teks pengumuman Sholat Jumat yang rapi dan menarik untuk dibacakan oleh pengurus DKM.",
        "✉️ Surat Undangan" to "Buat draf surat undangan resmi dari pengurus DKM kepada warga untuk rapat koordinasi agenda Idul Adha.",
        "🌙 Rekomendasi Ramadhan" to "Berikan ide dan rekomendasi program kegiatan inovatif selama bulan suci Ramadhan untuk jamaah muda."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asisten AI Sigma Masjid", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Chat", tint = Color.White)
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
            // Message List Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(chatHistory) { msg ->
                    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    val containerColor = if (msg.isUser) EmeraldPrimary else MaterialTheme.colorScheme.surface
                    val textColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    val cardShape = if (msg.isUser) {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                    } else {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                    }

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
                        Card(
                            shape = cardShape,
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                    color = if (msg.isUser) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                if (aiIsLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EmeraldPrimary)
                            Text("Sigma AI sedang memproses...", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Quick Actions Chips Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickActions) { (label, prompt) ->
                        ElevatedFilterChip(
                            selected = false,
                            onClick = { viewModel.sendAiMessage(prompt) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = GoldSecondary.copy(alpha = 0.1f),
                                labelColor = EmeraldDark
                            )
                        )
                    }
                }
            }

            // Text Input Bar Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("Tanyakan asisten AI...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                IconButton(
                    onClick = {
                        if (userMessageText.trim().isNotEmpty()) {
                            viewModel.sendAiMessage(userMessageText)
                            userMessageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
