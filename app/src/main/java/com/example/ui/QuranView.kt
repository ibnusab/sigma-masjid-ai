package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models matching our App's existing classes ---
data class Surah(
    val number: Int,
    val name: String,
    val arabic: String,
    val translation: String,
    val versesCount: Int,
    val type: String, // "Makkiyah" or "Madaniyah"
    val verses: List<Verse>
)

data class Verse(
    val number: Int,
    val arabic: String,
    val translation: String
)

// --- API Models for equran.id v2 ---
@JsonClass(generateAdapter = true)
data class QuranResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T
)

@JsonClass(generateAdapter = true)
data class ApiSurah(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String
)

@JsonClass(generateAdapter = true)
data class ApiSurahDetail(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String,
    @Json(name = "ayat") val ayat: List<ApiVerse>
)

@JsonClass(generateAdapter = true)
data class ApiVerse(
    @Json(name = "nomorAyat") val nomorAyat: Int,
    @Json(name = "teksArab") val teksArab: String,
    @Json(name = "teksLatin") val teksLatin: String,
    @Json(name = "teksIndonesia") val teksIndonesia: String
)

// --- Retrofit Service ---
interface QuranApiService {
    @GET("surat")
    suspend fun getSurahList(): Response<QuranResponse<List<ApiSurah>>>

    @GET("surat/{nomor}")
    suspend fun getSurahDetail(@Path("nomor") nomor: Int): Response<QuranResponse<ApiSurahDetail>>
}

// --- Retrofit Client ---
object QuranRetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://equran.id/api/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: QuranApiService = retrofit.create(QuranApiService::class.java)
}

// --- UI States ---
sealed interface QuranUiState {
    object Loading : QuranUiState
    data class Success(val surahs: List<Surah>) : QuranUiState
    data class Error(val message: String, val fallback: List<Surah>) : QuranUiState
}

sealed interface SurahDetailUiState {
    object Idle : SurahDetailUiState
    object Loading : SurahDetailUiState
    data class Success(val surahDetail: Surah) : SurahDetailUiState
    data class Error(val message: String, val fallback: Surah) : SurahDetailUiState
}

// --- Mapper Functions ---
fun ApiSurah.toSurah(): Surah {
    return Surah(
        number = this.nomor,
        name = this.namaLatin,
        arabic = this.nama,
        translation = this.arti,
        versesCount = this.jumlahAyat,
        type = if (this.tempatTurun.lowercase() == "mekah") "Makkiyah" else "Madaniyah",
        verses = emptyList()
    )
}

fun ApiSurahDetail.toSurah(): Surah {
    return Surah(
        number = this.nomor,
        name = this.namaLatin,
        arabic = this.nama,
        translation = this.arti,
        versesCount = this.jumlahAyat,
        type = if (this.tempatTurun.lowercase() == "mekah") "Makkiyah" else "Madaniyah",
        verses = this.ayat.map { it.toVerse() }
    )
}

fun ApiVerse.toVerse(): Verse {
    return Verse(
        number = this.nomorAyat,
        arabic = this.teksArab,
        translation = this.teksIndonesia
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranView(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE) }
    
    // Bookmark State (formatted as "SurahNumber:VerseNumber:SurahName")
    var bookmarkedVerse by remember {
        mutableStateOf(sharedPref.getString("bookmark_key", null))
    }

    // Audio Player State
    val mediaPlayer = remember { MediaPlayer() }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var currentPlayingSurah by remember { mutableStateOf<Surah?>(null) }
    var currentPlayingAudioUrl by remember { mutableStateOf<String?>(null) }

    // Release MediaPlayer when Composable exits
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // Audio Playback Helpers
    val playAudio = { url: String ->
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                isPlayingAudio = true
            }
            mediaPlayer.setOnCompletionListener {
                isPlayingAudio = false
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                Toast.makeText(context, "Gagal memutar audio murottal", Toast.LENGTH_SHORT).show()
                isPlayingAudio = false
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlayingAudio = false
        }
    }

    val toggleAudio = { surah: Surah ->
        val url = String.format("https://cdn.equran.id/audio-full/Misyari-Rasyid-Al-Afasi/%03d.mp3", surah.number)
        if (currentPlayingAudioUrl == url) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPlayingAudio = false
            } else {
                mediaPlayer.start()
                isPlayingAudio = true
            }
        } else {
            currentPlayingAudioUrl = url
            currentPlayingSurah = surah
            playAudio(url)
        }
    }

    // List of Surahs UI State
    var listUiState by remember { mutableStateOf<QuranUiState>(QuranUiState.Loading) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Fetch Surahs List
    LaunchedEffect(refreshTrigger) {
        listUiState = QuranUiState.Loading
        try {
            val response = QuranRetrofitClient.apiService.getSurahList()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    val list = body.data.map { it.toSurah() }
                    listUiState = QuranUiState.Success(list)
                } else {
                    listUiState = QuranUiState.Error("Server mengembalikan kode: ${body?.code}", getDummySurahs())
                }
            } else {
                listUiState = QuranUiState.Error("Koneksi gagal: ${response.code()}", getDummySurahs())
            }
        } catch (e: Exception) {
            listUiState = QuranUiState.Error("Offline. Menampilkan data offline.", getDummySurahs())
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSurahHeader by remember { mutableStateOf<Surah?>(null) }
    var detailUiState by remember { mutableStateOf<SurahDetailUiState>(SurahDetailUiState.Idle) }

    // Fetch Surah Details
    LaunchedEffect(selectedSurahHeader) {
        val header = selectedSurahHeader
        if (header != null) {
            detailUiState = SurahDetailUiState.Loading
            try {
                val response = QuranRetrofitClient.apiService.getSurahDetail(header.number)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        val detailedSurah = body.data.toSurah()
                        detailUiState = SurahDetailUiState.Success(detailedSurah)
                    } else {
                        val fallback = getDummySurahs().find { it.number == header.number } ?: header
                        detailUiState = SurahDetailUiState.Error("Gagal mengambil detail dari server.", fallback)
                    }
                } else {
                    val fallback = getDummySurahs().find { it.number == header.number } ?: header
                    detailUiState = SurahDetailUiState.Error("Error server: ${response.code()}", fallback)
                }
            } catch (e: Exception) {
                val fallback = getDummySurahs().find { it.number == header.number } ?: header
                detailUiState = SurahDetailUiState.Error("Gagal menghubungkan. Menggunakan mode offline.", fallback)
            }
        } else {
            detailUiState = SurahDetailUiState.Idle
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedSurahHeader?.name ?: "Al-Qur'an Digital",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedSurahHeader != null) {
                            selectedSurahHeader = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedSurahHeader == null) {
                // --- SURAH LIST VIEW ---
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        placeholder = { Text("Cari Surah...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            cursorColor = EmeraldPrimary
                        )
                    )

                    // Bookmark Header Card if bookmark exists
                    val currentBookmark = bookmarkedVerse
                    if (currentBookmark != null) {
                        val parts = currentBookmark.split(":")
                        if (parts.size >= 3) {
                            val surahNum = parts[0].toIntOrNull() ?: 1
                            val verseNum = parts[1].toIntOrNull() ?: 1
                            val surahName = parts[2]

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .clickable {
                                        // Open this surah automatically
                                        selectedSurahHeader = Surah(
                                            number = surahNum,
                                            name = surahName,
                                            arabic = "",
                                            translation = "",
                                            versesCount = 0,
                                            type = "",
                                            verses = emptyList()
                                        )
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = GoldSecondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Terakhir Dibaca (Bookmark)",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "QS. $surahName: Ayat $verseNum",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Surah List UI based on loading State
                    when (val state = listUiState) {
                        is QuranUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = EmeraldPrimary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Mengunduh Daftar Surah...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                        is QuranUiState.Success -> {
                            val filteredSurahs = state.surahs.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.translation.contains(searchQuery, ignoreCase = true)
                            }

                            if (filteredSurahs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                    Text("Surah tidak ditemukan", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(filteredSurahs) { surah ->
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedSurahHeader = surah },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Number Box
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(EmeraldPrimary, EmeraldDark)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = surah.number.toString(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                // Name Info
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = surah.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${surah.translation} • ${surah.versesCount} Ayat",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }

                                                // Arabic text
                                                Text(
                                                    text = surah.arabic,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = EmeraldPrimary,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is QuranUiState.Error -> {
                            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                                // Notice bar
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(state.message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("Menampilkan Surah utama mode offline.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Button(
                                            onClick = { refreshTrigger++ },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Coba Lagi", fontSize = 10.sp)
                                        }
                                    }
                                }

                                val filteredSurahs = state.fallback.filter {
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                            it.translation.contains(searchQuery, ignoreCase = true)
                                }

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(filteredSurahs) { surah ->
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedSurahHeader = surah },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(EmeraldPrimary, EmeraldDark)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = surah.number.toString(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = surah.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${surah.translation} • ${surah.versesCount} Ayat",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }

                                                Text(
                                                    text = surah.arabic,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = EmeraldPrimary,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- SURAH DETAIL VIEW ---
                val currentHeader = selectedSurahHeader!!

                when (val detailState = detailUiState) {
                    is SurahDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Mengunduh Ayat-Ayat Al-Qur'an...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                    is SurahDetailUiState.Success -> {
                        val surah = detailState.surahDetail
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(EmeraldPrimary, EmeraldDark)
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = surah.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = surah.translation,
                                        color = GoldSecondary,
                                        fontSize = 16.sp
                                    )
                                    Divider(
                                        color = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .padding(vertical = 12.dp)
                                            .width(150.dp)
                                    )
                                    Text(
                                        text = "${surah.type} • ${surah.versesCount} Ayat",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Real Audio Play Button playing Mishary Rashid Al-Afasy
                                    Button(
                                        onClick = { toggleAudio(surah) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldSecondary,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingAudio && currentPlayingSurah?.number == surah.number) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Murottal"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (isPlayingAudio && currentPlayingSurah?.number == surah.number) "Jeda Murottal" else "Putar Murottal Al-Afasy",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Verses List
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Bismillah for surahs other than At-Tawbah (9) and Al-Fatihah (1)
                                if (surah.number != 1 && surah.number != 9) {
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }

                                items(surah.verses) { verse ->
                                    val verseKey = "${surah.number}:${verse.number}:${surah.name}"
                                    val isBookmarked = bookmarkedVerse == verseKey

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        // Verse Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isBookmarked) EmeraldPrimary.copy(alpha = 0.1f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(EmeraldPrimary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = verse.number.toString(),
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Row {
                                                // Share Button
                                                IconButton(
                                                    onClick = {
                                                        val sendIntent: Intent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            putExtra(Intent.EXTRA_TEXT, "${verse.arabic}\n\nArtinya: \"${verse.translation}\" (QS. ${surah.name}: ${verse.number})")
                                                            type = "text/plain"
                                                        }
                                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                                        context.startActivity(shareIntent)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Copy Button
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("Ayat Al-Quran", "${verse.arabic}\n\nArtinya: \"${verse.translation}\" (QS. ${surah.name}: ${verse.number})")
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "Ayat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin", modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Bookmark Button
                                                IconButton(
                                                    onClick = {
                                                        if (isBookmarked) {
                                                            sharedPref.edit().remove("bookmark_key").apply()
                                                            bookmarkedVerse = null
                                                            Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            sharedPref.edit().putString("bookmark_key", verseKey).apply()
                                                            bookmarkedVerse = verseKey
                                                            Toast.makeText(context, "QS. ${surah.name} ayat ${verse.number} disimpan sebagai Bookmark", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                        contentDescription = "Tandai",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (isBookmarked) GoldSecondary else EmeraldPrimary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Arabic Script
                                        Text(
                                            text = verse.arabic,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth(),
                                            lineHeight = 40.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Translation
                                        Text(
                                            text = verse.translation,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                    is SurahDetailUiState.Error -> {
                        val surah = detailState.fallback
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Notice bar
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(detailState.message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Menggunakan ayat offline jika tersedia.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Button(
                                        onClick = { selectedSurahHeader = currentHeader },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Retry", fontSize = 10.sp)
                                    }
                                }
                            }

                            // Fallback UI Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(EmeraldPrimary, EmeraldDark)
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = surah.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = surah.translation,
                                        color = GoldSecondary,
                                        fontSize = 16.sp
                                    )
                                    Divider(
                                        color = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .padding(vertical = 12.dp)
                                            .width(150.dp)
                                    )
                                    Text(
                                        text = "${surah.type} • ${surah.versesCount} Ayat",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            if (surah.verses.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                    Text("Detail ayat tidak tersedia offline", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (surah.number != 1 && surah.number != 9) {
                                        item {
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    items(surah.verses) { verse ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(EmeraldPrimary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = verse.number.toString(),
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = verse.arabic,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth(),
                                                lineHeight = 40.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = verse.translation,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.fillMaxWidth(),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Global Music Player floating card if playing
            AnimatedVisibility(
                visible = isPlayingAudio,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Mendengarkan Murottal",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                "Surah ${currentPlayingSurah?.name ?: ""}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = {
                            mediaPlayer.pause()
                            isPlayingAudio = false
                        }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        }
                        IconButton(onClick = {
                            mediaPlayer.stop()
                            isPlayingAudio = false
                            currentPlayingAudioUrl = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Generate realistic dummy data for Al-Qur'an (4 main Surahs as robust fallback offline)
private fun getDummySurahs(): List<Surah> {
    return listOf(
        Surah(
            number = 1,
            name = "Al-Fatihah",
            arabic = "الفاتحة",
            translation = "Pembukaan",
            versesCount = 7,
            type = "Makkiyah",
            verses = listOf(
                Verse(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Dengan menyebut nama Allah Yang Maha Pemurah lagi Maha Penyayang."),
                Verse(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Segala puji bagi Allah, Tuhan semesta alam."),
                Verse(3, "الرَّحْمَٰنِ الرَّحِيمِ", "Maha Pemurah lagi Maha Penyayang."),
                Verse(4, "مَالِكِ يَوْمِ الدِّينِ", "Yang menguasai di Hari Pembalasan."),
                Verse(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Hanya Engkaulah yang kami sembah, dan hanya kepada Engkaulah kami meminta pertolongan."),
                Verse(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Tunjukilah kami jalan yang lurus,"),
                Verse(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "(yaitu) Jalan orang-orang yang telah Engkau beri nikmat kepada mereka; bukan (jalan) mereka yang dimurkai dan bukan (pula jalan) mereka yang sesat.")
            )
        ),
        Surah(
            number = 36,
            name = "Yasin",
            arabic = "يس",
            translation = "Yasin",
            versesCount = 5,
            type = "Makkiyah",
            verses = listOf(
                Verse(1, "يسٓ", "Ya Sin."),
                Verse(2, "وَالْقُرْءَانِ الْحَكِيمِ", "Demi Al-Qur'an yang penuh hikmah,"),
                Verse(3, "إlnَّكَ لَمِنَ الْمُرْسَلِينَ", "sesungguhnya kamu salah seorang dari rasul-rasul,"),
                Verse(4, "عَلَىٰ صِرَاطٍ مُسْتَقِيمٍ", "(yang berada) di atas jalan yang lurus,"),
                Verse(5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "(sebagai wahyu) yang diturunkan oleh Yang Maha Perkasa lagi Maha Penyayang.")
            )
        ),
        Surah(
            number = 67,
            name = "Al-Mulk",
            arabic = "الملك",
            translation = "Kerajaan",
            versesCount = 4,
            type = "Makkiyah",
            verses = listOf(
                Verse(1, "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَى_ءٍ قَدِيرٌ", "Maha Suci Allah Yang di tangan-Nyalah segala kerajaan, dan Dia Maha Kuasa atas segala sesuatu,"),
                Verse(2, "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ", "Yang menjadikan mati dan hidup, supaya Dia menguji kamu, siapa di antara kamu yang lebih baik amalnya. Dan Dia Maha Perkasa lagi Maha Pengampun,"),
                Verse(3, "ٱلَّذِى خَلَقَ سَبْعَ سَمَٰوَٰتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلَقِ ٱلرَّحْمَٰنِ مِن تَفَٰوutٍ ۖ فَٱرْجِعِ ٱلْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", "Yang telah menciptakan tujuh langit berlapis-lapis. Kamu sekali-kali tidak melihat pada ciptaan Tuhan Yang Maha Pemurah sesuatu yang tidak seimbang. Maka lihatlah berulang-ulang, adakah kamu lihat sesuatu yang cacat?"),
                Verse(4, "ثُمَّ ٱرْجِعِ ٱلْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ ٱلْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Kemudian pandanglah sekali lagi niscaya penglihatanmu akan kembali kepadamu dengan tidak menemukan sesuatu cacat dan ia pun dalam keadaan payah.")
            )
        ),
        Surah(
            number = 112,
            name = "Al-Ikhlas",
            arabic = "الإخلاص",
            translation = "Ikhlas",
            versesCount = 4,
            type = "Makkiyah",
            verses = listOf(
                Verse(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Katakanlah: 'Dialah Allah, Yang Maha Esa.'"),
                Verse(2, "اللَّهُ الصَّمَدُ", "Allah adalah Tuhan yang bergantung kepada-Nya segala sesuatu."),
                Verse(3, "لَمْ يَلِدْ وَلَمْ يُولَد_", "Dia tiada beranak dan tidak pula diperanakkan,"),
                Verse(4, "وَلَمْ يَكُن لَّهُ كُfُوًا أَحَدٌ", "dan tidak ada seorang pun yang setara dengan Dia.")
            )
        )
    )
}
