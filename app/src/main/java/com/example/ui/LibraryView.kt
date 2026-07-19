package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.Book
import com.example.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryView(
    viewModel: MasjidViewModel,
    onBack: () -> Unit
) {
    val books by viewModel.bookList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showBorrowDialog by remember { mutableStateOf<Book?>(null) }
    var editingBook by remember { mutableStateOf<Book?>(null) }

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fikih") }
    var borrowerName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perpustakaan Masjid", fontWeight = FontWeight.Bold, color = Color.White) },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Buku")
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
            if (books.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada koleksi buku", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(books) { book ->
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
                                        text = book.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Karya: ${book.author} • ${book.category}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    if (book.isBorrowed) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Dipinjam: ${book.borrowerName}",
                                                    color = Color(0xFFDC2626),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Sejak: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(book.borrowDate))}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Tersedia",
                                                color = Color(0xFF22C55E),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                Row {
                                    if (book.isBorrowed) {
                                        Button(
                                            onClick = { viewModel.returnBook(book) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Kembalikan", fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = { showBorrowDialog = book },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Pinjam", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { editingBook = book }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                    }
                                    IconButton(onClick = { viewModel.deleteBook(book.id) }, modifier = Modifier.size(36.dp)) {
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
                    title = { Text("Tambah Koleksi Buku", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Judul Buku") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = author,
                                onValueChange = { author = it },
                                label = { Text("Nama Penulis / Mushannif") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fikih", "Hadits", "Tafsir", "Sejarah", "Umum").forEach { cat ->
                                    val selected = category == cat
                                    FilterChip(
                                        selected = selected,
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
                                if (title.isNotEmpty() && author.isNotEmpty()) {
                                    viewModel.addBook(
                                        title = title,
                                        author = author,
                                        category = category
                                    )
                                    title = ""
                                    author = ""
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

            if (showBorrowDialog != null) {
                AlertDialog(
                    onDismissRequest = { showBorrowDialog = null },
                    title = { Text("Form Peminjaman Buku", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Buku yang dipinjam: ${showBorrowDialog?.title}")
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = borrowerName,
                                onValueChange = { borrowerName = it },
                                label = { Text("Nama Peminjam") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (borrowerName.isNotEmpty()) {
                                    viewModel.borrowBook(showBorrowDialog!!, borrowerName)
                                    borrowerName = ""
                                    showBorrowDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBorrowDialog = null }) {
                            Text("Batal")
                        }
                    }
                )
            }

            if (editingBook != null) {
                val currentBook = editingBook!!
                var editTitle by remember(currentBook) { mutableStateOf(currentBook.title) }
                var editAuthor by remember(currentBook) { mutableStateOf(currentBook.author) }
                var editCategory by remember(currentBook) { mutableStateOf(currentBook.category) }

                AlertDialog(
                    onDismissRequest = { editingBook = null },
                    title = { Text("Edit Koleksi Buku", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Judul Buku") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editAuthor,
                                onValueChange = { editAuthor = it },
                                label = { Text("Nama Penulis / Mushannif") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kategori", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fikih", "Hadits", "Tafsir", "Sejarah", "Umum").forEach { cat ->
                                    val selected = editCategory == cat
                                    FilterChip(
                                        selected = selected,
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
                                if (editTitle.isNotEmpty() && editAuthor.isNotEmpty()) {
                                    viewModel.updateBook(
                                        currentBook.copy(
                                            title = editTitle,
                                            author = editAuthor,
                                            category = editCategory
                                        )
                                    )
                                    editingBook = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingBook = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
