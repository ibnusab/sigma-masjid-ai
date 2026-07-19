package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "jamaah")
data class Jamaah(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val category: String, // "Warga", "Donatur", "Pengurus", "Musafir"
    val address: String,
    val qrCode: String,
    val joinDate: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "finance")
data class Finance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "PEMASUKAN", "PENGELUARAN"
    val amount: Double,
    val category: String, // "Infaq", "Sedekah", "Zakat Mal", "Zakat Fitrah", "Donasi", "Wakaf", "Operasional", "Maintenance", "Lainnya"
    val notes: String,
    val date: Long = System.currentTimeMillis(),
    val donorName: String = "Hamba Allah"
) : Serializable

@Entity(tableName = "agenda")
data class Agenda(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val time: String,
    val speaker: String = "",
    val category: String = "Umum" // "Kajian", "Sholat Jumat", "Ramadhan", "Qurban", "Rapat DKM"
) : Serializable

@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Int,
    val status: String, // "Baik", "Rusak", "Sedang Diperbaiki"
    val location: String,
    val lastChecked: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "library")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val category: String, // "Fikih", "Hadits", "Tafsir", "Sejarah", "Akhlak", "Umum"
    val isBorrowed: Boolean = false,
    val borrowerName: String = "",
    val borrowDate: Long = 0
) : Serializable

@Entity(tableName = "petugas")
data class Petugas(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // e.g., "Khatib", "Imam", "Muadzin" for Jumat, or "Subuh", "Dzuhur", etc., for Rawatib
    val category: String, // "Jumat" or "Rawatib" or "Lainnya"
    val name: String, // e.g., "Ustadz Dr. H. Anwar Abbas"
    val detail: String = "" // Additional description or helper text
) : Serializable

@Entity(tableName = "announcement")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: Long = System.currentTimeMillis(),
    val category: String = "Umum" // "Zakat", "Kajian", "Sosial", etc.
) : Serializable

@Entity(tableName = "chat_messages")
data class DbChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "prayer_schedule")
data class PrayerSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val time: String,
    val imam: String = "-",
    val muadzin: String = "-"
) : Serializable

