package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryView(
    viewModel: MasjidViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.inventoryList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Baik") }
    var location by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventaris Masjid", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Aset")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada data inventaris", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
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
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Jumlah: ${item.quantity} unit • Lokasi: ${item.location}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Status Badge
                                    val badgeColor = when (item.status) {
                                        "Baik" -> Color(0xFF22C55E)
                                        "Rusak" -> Color(0xFFDC2626)
                                        else -> Color(0xFFF59E0B)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.status,
                                            color = badgeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Dicek: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.lastChecked))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingItem = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                    }
                                    IconButton(onClick = { viewModel.deleteInventory(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Tambah Aset Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Aset") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Jumlah") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Lokasi Penyimpanan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kondisi Barang", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Baik", "Perlu Perbaikan", "Rusak").forEach { cond ->
                                    val selected = status == cond
                                    FilterChip(
                                        selected = selected,
                                        onClick = { status = cond },
                                        label = { Text(cond) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && quantity.isNotEmpty()) {
                                    viewModel.addInventory(
                                        name = name,
                                        quantity = quantity.toIntOrNull() ?: 1,
                                        status = status,
                                        location = location.ifEmpty { "Gudang" }
                                    )
                                    name = ""
                                    quantity = ""
                                    location = ""
                                    status = "Baik"
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

            if (editingItem != null) {
                val currentItem = editingItem!!
                var editName by remember(currentItem) { mutableStateOf(currentItem.name) }
                var editQuantity by remember(currentItem) { mutableStateOf(currentItem.quantity.toString()) }
                var editLocation by remember(currentItem) { mutableStateOf(currentItem.location) }
                var editStatus by remember(currentItem) { mutableStateOf(currentItem.status) }

                AlertDialog(
                    onDismissRequest = { editingItem = null },
                    title = { Text("Edit Data Aset", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nama Aset") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editQuantity,
                                onValueChange = { editQuantity = it },
                                label = { Text("Jumlah") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editLocation,
                                onValueChange = { editLocation = it },
                                label = { Text("Lokasi Penyimpanan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kondisi Barang", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Baik", "Perlu Perbaikan", "Rusak").forEach { cond ->
                                    val selected = editStatus == cond
                                    FilterChip(
                                        selected = selected,
                                        onClick = { editStatus = cond },
                                        label = { Text(cond) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isNotEmpty() && editQuantity.isNotEmpty()) {
                                    viewModel.updateInventory(
                                        currentItem.copy(
                                            name = editName,
                                            quantity = editQuantity.toIntOrNull() ?: 1,
                                            status = editStatus,
                                            location = editLocation.ifEmpty { "Gudang" },
                                            lastChecked = System.currentTimeMillis()
                                        )
                                    )
                                    editingItem = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingItem = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
