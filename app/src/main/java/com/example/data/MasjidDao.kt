package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MasjidDao {
    // --- Jamaah ---
    @Query("SELECT * FROM jamaah ORDER BY name ASC")
    fun getAllJamaah(): Flow<List<Jamaah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJamaah(jamaah: Jamaah)

    @Query("DELETE FROM jamaah WHERE id = :id")
    suspend fun deleteJamaahById(id: Int)

    // --- Finance ---
    @Query("SELECT * FROM finance ORDER BY date DESC")
    fun getAllFinance(): Flow<List<Finance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: Finance)

    @Query("DELETE FROM finance WHERE id = :id")
    suspend fun deleteFinanceById(id: Int)

    // --- Agenda ---
    @Query("SELECT * FROM agenda ORDER BY date ASC")
    fun getAllAgenda(): Flow<List<Agenda>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgenda(agenda: Agenda)

    @Query("DELETE FROM agenda WHERE id = :id")
    suspend fun deleteAgendaById(id: Int)

    // --- Inventory ---
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: InventoryItem)

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun deleteInventoryById(id: Int)

    // --- Library (Books) ---
    @Query("SELECT * FROM library ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteBookById(id: Int)

    // --- Petugas ---
    @Query("SELECT * FROM petugas ORDER BY category DESC, id ASC")
    fun getAllPetugas(): Flow<List<Petugas>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetugas(petugas: Petugas)

    @Query("DELETE FROM petugas WHERE id = :id")
    suspend fun deletePetugasById(id: Int)

    // --- Announcement ---
    @Query("SELECT * FROM announcement ORDER BY date DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcement WHERE id = :id")
    suspend fun deleteAnnouncementById(id: Int)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<DbChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: DbChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // --- Prayer Schedule ---
    @Query("SELECT * FROM prayer_schedule ORDER BY time ASC")
    fun getAllPrayerSchedules(): Flow<List<PrayerSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerSchedule(schedule: PrayerSchedule)

    @Query("DELETE FROM prayer_schedule WHERE id = :id")
    suspend fun deletePrayerScheduleById(id: Int)
}
