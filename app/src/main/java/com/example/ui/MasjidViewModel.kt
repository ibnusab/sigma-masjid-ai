package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MasjidViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MasjidDatabase.getDatabase(application)
    private val repository = MasjidRepository(db.masjidDao())

    // --- SharedPreferences and Settings Settings ---
    private val sharedPrefs = application.getSharedPreferences("masjid_prefs", Context.MODE_PRIVATE)

    private val _masjidName = MutableStateFlow(sharedPrefs.getString("masjid_name", "Masjid Raya Al-Hikmah") ?: "Masjid Raya Al-Hikmah")
    val masjidName = _masjidName.asStateFlow()

    private val _masjidAddress = MutableStateFlow(sharedPrefs.getString("masjid_address", "Jl. Melati No. 12, Jakarta") ?: "Jl. Melati No. 12, Jakarta")
    val masjidAddress = _masjidAddress.asStateFlow()

    private val _themePreference = MutableStateFlow(sharedPrefs.getString("theme_pref", "SYSTEM") ?: "SYSTEM")
    val themePreference = _themePreference.asStateFlow()

    fun updateMasjidName(newName: String) {
        _masjidName.value = newName
        sharedPrefs.edit().putString("masjid_name", newName).apply()
    }

    fun updateMasjidAddress(newAddress: String) {
        _masjidAddress.value = newAddress
        sharedPrefs.edit().putString("masjid_address", newAddress).apply()
    }

    fun updateThemePreference(pref: String) { // "SYSTEM", "LIGHT", "DARK"
        _themePreference.value = pref
        sharedPrefs.edit().putString("theme_pref", pref).apply()
    }

    // --- State Flows from Database ---
    val jamaahList: StateFlow<List<Jamaah>> = repository.allJamaah
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financeList: StateFlow<List<Finance>> = repository.allFinance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agendaList: StateFlow<List<Agenda>> = repository.allAgenda
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryList: StateFlow<List<InventoryItem>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookList: StateFlow<List<Book>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val petugasList: StateFlow<List<Petugas>> = repository.allPetugas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcementList: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerScheduleList: StateFlow<List<PrayerSchedule>> = repository.allPrayerSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State ---
    private val _currentTab = MutableStateFlow(0) // 0: Dashboard, 1: Jamaah, 2: Keuangan, 3: Agenda, 4: AI
    val currentTab = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // --- AI Chat States ---
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

    val chatHistory: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .map { list ->
            if (list.isEmpty()) {
                listOf(
                    ChatMessage(
                        "Assalamualaikum wr. wb. Saya Asisten AI Sigma Masjid. Saya dapat membantu Anda menganalisis laporan keuangan, membuat pengumuman, draf surat resmi, membuat jadwal imam/khotib, dan menjawab pertanyaan seputar operasional masjid. Silakan pilih menu cepat di bawah atau tulis pertanyaan Anda!",
                        isUser = false
                    )
                )
            } else {
                list.map { ChatMessage(it.text, it.isUser, it.timestamp) }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf(
                ChatMessage(
                    "Assalamualaikum wr. wb. Saya Asisten AI Sigma Masjid. Saya dapat membantu Anda menganalisis laporan keuangan, membuat pengumuman, draf surat resmi, membuat jadwal imam/khotib, dan menjawab pertanyaan seputar operasional masjid. Silakan pilih menu cepat di bawah atau tulis pertanyaan Anda!",
                    isUser = false
                )
            )
        )

    private val _aiIsLoading = MutableStateFlow(false)
    val aiIsLoading = _aiIsLoading.asStateFlow()

    // --- Initialize Dummy Data ---
    init {
        viewModelScope.launch {
            // Seed Jamaah
            jamaahList.collect { list ->
                if (list.isEmpty()) {
                    seedDummyData()
                }
            }
        }
    }

    private suspend fun seedDummyData() {
        // Jamaah
        repository.insertJamaah(Jamaah(name = "Ahmad Fauzi", phone = "081234567890", category = "Warga", address = "Jl. Anggrek No. 10", qrCode = "JAM-AHMAD-01"))
        repository.insertJamaah(Jamaah(name = "Budi Santoso", phone = "082345678901", category = "Pengurus", address = "Jl. Melati No. 5", qrCode = "JAM-BUDI-02"))
        repository.insertJamaah(Jamaah(name = "Siti Aminah", phone = "083456789012", category = "Donatur", address = "Jl. Mawar No. 12", qrCode = "JAM-SITI-03"))
        repository.insertJamaah(Jamaah(name = "H. Rachman", phone = "084567890123", category = "Donatur", address = "Jl. Melati No. 40", qrCode = "JAM-RACHMAN-04"))
        repository.insertJamaah(Jamaah(name = "Yusuf Malik", phone = "085678901234", category = "Musafir", address = "Jl. Pemuda No. 4", qrCode = "JAM-YUSUF-05"))

        // Finance
        val today = System.currentTimeMillis()
        repository.insertFinance(Finance(type = "PEMASUKAN", amount = 5000000.0, category = "Donasi", notes = "Donasi renovasi tempat wudhu", date = today - 172800000, donorName = "Siti Aminah"))
        repository.insertFinance(Finance(type = "PEMASUKAN", amount = 2500000.0, category = "Zakat Mal", notes = "Zakat Mal bulanan", date = today - 259200000, donorName = "H. Rachman"))
        repository.insertFinance(Finance(type = "PENGELUARAN", amount = 1200000.0, category = "Operasional", notes = "Pembayaran listrik & air masjid", date = today - 86400000))
        repository.insertFinance(Finance(type = "PEMASUKAN", amount = 1800000.0, category = "Infaq", notes = "Infaq Kotak Amal Sholat Jumat", date = today - 345600000, donorName = "Hamba Allah"))
        repository.insertFinance(Finance(type = "PENGELUARAN", amount = 350000.0, category = "Maintenance", notes = "Perbaikan mikrofon adzan", date = today))
        repository.insertFinance(Finance(type = "PEMASUKAN", amount = 1500000.0, category = "Wakaf", notes = "Wakaf Al-Qur'an baru", date = today - 432000000, donorName = "H. Rachman"))

        // Agenda
        repository.insertAgenda(Agenda(title = "Kajian Akhir Pekan", description = "Pembahasan kitab Riyadhus Shalihin bersama Ustadz Dr. Khalid", date = today + 86400000, time = "18:30 - 20:00", speaker = "Ustadz Dr. Khalid", category = "Kajian"))
        repository.insertAgenda(Agenda(title = "Sholat Jumat & Khutbah", description = "Khutbah Jumat dengan tema Menjaga Amanah di Era Digital", date = today + 172800000, time = "11:45 - 12:45", speaker = "Ustadz Prof. Dr. Hamka", category = "Sholat Jumat"))
        repository.insertAgenda(Agenda(title = "Rapat DKM Bulanan", description = "Rapat rutin pengurus DKM membahas laporan keuangan dan agenda Idul Adha", date = today + 259200000, time = "09:00 - 11:30", speaker = "Ketua DKM Budi Santoso", category = "Rapat DKM"))

        // Inventory
        repository.insertInventory(InventoryItem(name = "Air Conditioner LG", quantity = 6, status = "Baik", location = "Ruang Utama Sholat"))
        repository.insertInventory(InventoryItem(name = "Sound System Mixer Yamaha", quantity = 1, status = "Baik", location = "Ruang Kontrol Sound"))
        repository.insertInventory(InventoryItem(name = "Al-Qur'an Stand Kayu", quantity = 15, status = "Baik", location = "Ruang Utama Sholat"))
        repository.insertInventory(InventoryItem(name = "Vacuum Cleaner Karcher", quantity = 2, status = "Sedang Diperbaiki", location = "Gudang Alat"))

        // Library (Books)
        repository.insertBook(Book(title = "Bulughul Maram", author = "Ibnu Hajar Al-Asqalani", category = "Hadits"))
        repository.insertBook(Book(title = "Riyadhus Shalihin", author = "Imam An-Nawawi", category = "Hadits"))
        repository.insertBook(Book(title = "Tafsir Al-Azhar", author = "Prof. Dr. Hamka", category = "Tafsir", isBorrowed = true, borrowerName = "Ahmad Fauzi", borrowDate = today - 432000000))
        repository.insertBook(Book(title = "Fikih Sunnah", author = "Sayyid Sabiq", category = "Fikih"))

        // Petugas
        repository.insertPetugas(Petugas(title = "Khatib", category = "Jumat", name = "Ustadz Dr. H. Anwar Abbas", detail = "Khutbah Jumat"))
        repository.insertPetugas(Petugas(title = "Imam Sholat", category = "Jumat", name = "Ustadz H. Syakir Daulay", detail = "Sholat Jumat"))
        repository.insertPetugas(Petugas(title = "Muadzin", category = "Jumat", name = "Ustadz Bilal Ramadhan", detail = "Adzan Jumat"))
        repository.insertPetugas(Petugas(title = "Subuh", category = "Rawatib", name = "Ust. Fauzi", detail = "Muadzin: Ust. Bilal"))
        repository.insertPetugas(Petugas(title = "Dzuhur", category = "Rawatib", name = "Ust. Yusuf", detail = "Muadzin: Ust. Salim"))
        repository.insertPetugas(Petugas(title = "Ashar", category = "Rawatib", name = "Ust. Jufri", detail = "Muadzin: Ust. Salim"))
        repository.insertPetugas(Petugas(title = "Maghrib", category = "Rawatib", name = "Ust. Syakir", detail = "Muadzin: Ust. Bilal"))
        repository.insertPetugas(Petugas(title = "Isya", category = "Rawatib", name = "Ust. Syakir", detail = "Muadzin: Ust. Bilal"))

        // Announcements (Pengumuman)
        repository.insertAnnouncement(Announcement(title = "📢 Penerimaan Zakat Fitrah Dibuka", content = "Panitia DKM Al-Hikmah mulai menerima pembayaran Zakat Fitrah berupa Beras 2.5kg / Rp 45.000 mulai hari ini.", date = today, category = "Zakat"))
        repository.insertAnnouncement(Announcement(title = "🕌 Kajian Rutin Sabtu Shubuh", content = "Insya Allah akan diselenggarakan Kajian Ba'da Shubuh membahas 'Tafsir Jalalain' bersama Ust. Dr. Anwar.", date = today - 86400000, category = "Kajian"))
        repository.insertAnnouncement(Announcement(title = "🧹 Kerja Bakti Akbar Masjid", content = "Mengundang seluruh warga sekitar untuk melaksanakan bakti sosial kebersihan masjid mempersiapkan bulan Ramadhan.", date = today - 259200000, category = "Sosial"))

        // Prayer Schedules
        repository.insertPrayerSchedule(PrayerSchedule(name = "Subuh", time = "04:45", imam = "Ust. Fauzi", muadzin = "Ust. Bilal"))
        repository.insertPrayerSchedule(PrayerSchedule(name = "Syuruq", time = "06:05", imam = "-", muadzin = "-"))
        repository.insertPrayerSchedule(PrayerSchedule(name = "Dzuhur", time = "12:02", imam = "Ust. Yusuf", muadzin = "Ust. Salim"))
        repository.insertPrayerSchedule(PrayerSchedule(name = "Ashar", time = "15:23", imam = "Ust. Jufri", muadzin = "Ust. Salim"))
        repository.insertPrayerSchedule(PrayerSchedule(name = "Maghrib", time = "18:10", imam = "Ust. Syakir", muadzin = "Ust. Bilal"))
        repository.insertPrayerSchedule(PrayerSchedule(name = "Isya", time = "19:22", imam = "Ust. Syakir", muadzin = "Ust. Bilal"))
    }

    // --- Repository Operations ---
    fun addJamaah(name: String, phone: String, category: String, address: String) {
        viewModelScope.launch {
            val qr = "JAM-${name.replace(" ", "-").uppercase()}-${(100..999).random()}"
            repository.insertJamaah(Jamaah(name = name, phone = phone, category = category, address = address, qrCode = qr))
        }
    }

    fun updateJamaah(jamaah: Jamaah) {
        viewModelScope.launch {
            repository.insertJamaah(jamaah)
        }
    }

    fun deleteJamaah(id: Int) {
        viewModelScope.launch {
            repository.deleteJamaahById(id)
        }
    }

    fun addFinance(type: String, amount: Double, category: String, notes: String, donorName: String) {
        viewModelScope.launch {
            repository.insertFinance(Finance(type = type, amount = amount, category = category, notes = notes, donorName = donorName))
        }
    }

    fun updateFinance(finance: Finance) {
        viewModelScope.launch {
            repository.insertFinance(finance)
        }
    }

    fun deleteFinance(id: Int) {
        viewModelScope.launch {
            repository.deleteFinanceById(id)
        }
    }

    fun addAgenda(title: String, description: String, date: Long, time: String, speaker: String, category: String) {
        viewModelScope.launch {
            repository.insertAgenda(Agenda(title = title, description = description, date = date, time = time, speaker = speaker, category = category))
        }
    }

    fun updateAgenda(agenda: Agenda) {
        viewModelScope.launch {
            repository.insertAgenda(agenda)
        }
    }

    fun deleteAgenda(id: Int) {
        viewModelScope.launch {
            repository.deleteAgendaById(id)
        }
    }

    fun addInventory(name: String, quantity: Int, status: String, location: String) {
        viewModelScope.launch {
            repository.insertInventory(InventoryItem(name = name, quantity = quantity, status = status, location = location))
        }
    }

    fun updateInventory(item: InventoryItem) {
        viewModelScope.launch {
            repository.insertInventory(item)
        }
    }

    fun deleteInventory(id: Int) {
        viewModelScope.launch {
            repository.deleteInventoryById(id)
        }
    }

    fun addBook(title: String, author: String, category: String) {
        viewModelScope.launch {
            repository.insertBook(Book(title = title, author = author, category = category))
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun borrowBook(book: Book, borrowerName: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(isBorrowed = true, borrowerName = borrowerName, borrowDate = System.currentTimeMillis()))
        }
    }

    fun returnBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book.copy(isBorrowed = false, borrowerName = "", borrowDate = 0))
        }
    }

    fun deleteBook(id: Int) {
        viewModelScope.launch {
            repository.deleteBookById(id)
        }
    }

    // --- Petugas CRUD ---
    fun addPetugas(title: String, category: String, name: String, detail: String) {
        viewModelScope.launch {
            repository.insertPetugas(Petugas(title = title, category = category, name = name, detail = detail))
        }
    }

    fun updatePetugas(petugas: Petugas) {
        viewModelScope.launch {
            repository.insertPetugas(petugas)
        }
    }

    fun deletePetugas(id: Int) {
        viewModelScope.launch {
            repository.deletePetugasById(id)
        }
    }

    // --- Announcement CRUD ---
    fun addAnnouncement(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertAnnouncement(Announcement(title = title, content = content, category = category))
        }
    }

    fun updateAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.insertAnnouncement(announcement)
        }
    }

    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncementById(id)
        }
    }

    // --- Prayer Schedule CRUD ---
    fun addPrayerSchedule(name: String, time: String, imam: String, muadzin: String) {
        viewModelScope.launch {
            repository.insertPrayerSchedule(PrayerSchedule(name = name, time = time, imam = imam, muadzin = muadzin))
        }
    }

    fun updatePrayerSchedule(schedule: PrayerSchedule) {
        viewModelScope.launch {
            repository.insertPrayerSchedule(schedule)
        }
    }

    fun deletePrayerSchedule(id: Int) {
        viewModelScope.launch {
            repository.deletePrayerScheduleById(id)
        }
    }

    // --- Gemini AI Assistant Integration ---
    fun sendAiMessage(messageText: String) {
        _aiIsLoading.value = true

        viewModelScope.launch {
            // Save user message to database
            repository.insertChatMessage(DbChatMessage(text = messageText, isUser = true))

            val finances = financeList.value
            val agenda = agendaList.value
            val jamaah = jamaahList.value

            val totalPemasukan = finances.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
            val totalPengeluaran = finances.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
            val saldo = totalPemasukan - totalPengeluaran

            // Construct system instruction with contextual data of the mosque
            val systemContext = """
                Anda adalah Sigma Masjid AI, sebuah sistem kecerdasan buatan pengelola operasional masjid yang profesional.
                Anda berbicara dalam Bahasa Indonesia yang santun, islami, ramah, dan profesional.
                Saat ini Anda memiliki data operasional Masjid berikut untuk membantu menjawab pertanyaan:
                - Nama Masjid: ${masjidName.value} (Sigma Masjid AI)
                - Alamat Masjid: ${masjidAddress.value}
                - Jumlah Jamaah terdaftar: ${jamaah.size} orang
                - Total Pemasukan Kas: Rp ${String.format("%,.0f", totalPemasukan)}
                - Total Pengeluaran Kas: Rp ${String.format("%,.0f", totalPengeluaran)}
                - Saldo Kas Masjid Saat Ini: Rp ${String.format("%,.0f", saldo)}
                - Jumlah Agenda terjadwal: ${agenda.size} kegiatan
                
                Daftar agenda masjid saat ini:
                ${agenda.joinToString("\n") { "- ${it.title} oleh ${it.speaker} pada ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.date))} jam ${it.time}" }}
                
                Lakukan tugas yang diminta pengurus masjid dengan akurat. Jika diminta membuat surat, proposal, jadwal, anggaran, atau pengumuman, berikan draf yang rapi dan terstruktur menggunakan pemformatan markdown yang indah.
            """.trimIndent()

            val response = GeminiClient.generateContent(messageText, systemContext)
            
            // Save AI response to database
            repository.insertChatMessage(DbChatMessage(text = response, isUser = false))
            _aiIsLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }
}
