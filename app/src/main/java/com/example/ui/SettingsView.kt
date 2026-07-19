package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    viewModel: MasjidViewModel,
    onBack: () -> Unit
) {
    val currentName by viewModel.masjidName.collectAsState()
    val currentAddress by viewModel.masjidAddress.collectAsState()
    val themePref by viewModel.themePreference.collectAsState()

    var nameInput by remember { mutableStateOf(currentName) }
    var addressInput by remember { mutableStateOf(currentAddress) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Masjid", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(successMessage ?: "", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { successMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }
            }

            // SECTION 1: PROFIL MASJID
            Text(
                "Profil Masjid",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Masjid") },
                        leadingIcon = { Icon(Icons.Default.Mosque, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Alamat Masjid") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            viewModel.updateMasjidName(nameInput)
                            viewModel.updateMasjidAddress(addressInput)
                            successMessage = "Profil masjid berhasil diperbarui!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 2: TEMA APLIKASI
            Text(
                "Tampilan & Tema",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Pilih Tema Aplikasi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                    listOf(
                        "SYSTEM" to "Sesuai Sistem (Default)",
                        "LIGHT" to "Mode Terang (Light Mode)",
                        "DARK" to "Mode Gelap (Dark Mode)"
                    ).forEach { (value, label) ->
                        val isSelected = themePref == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateThemePreference(value) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateThemePreference(value) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = when (value) {
                                    "LIGHT" -> Icons.Default.LightMode
                                    "DARK" -> Icons.Default.DarkMode
                                    else -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = null,
                                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SECTION 3: TENTANG APLIKASI
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sigma Masjid AI v1.2",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sistem Informasi Manajemen Masjid Berbasis AI",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
