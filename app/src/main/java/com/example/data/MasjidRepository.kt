package com.example.data

import kotlinx.coroutines.flow.Flow

class MasjidRepository(private val dao: MasjidDao) {
    // --- Jamaah ---
    val allJamaah: Flow<List<Jamaah>> = dao.getAllJamaah()
    suspend fun insertJamaah(jamaah: Jamaah) = dao.insertJamaah(jamaah)
    suspend fun deleteJamaahById(id: Int) = dao.deleteJamaahById(id)

    // --- Finance ---
    val allFinance: Flow<List<Finance>> = dao.getAllFinance()
    suspend fun insertFinance(finance: Finance) = dao.insertFinance(finance)
    suspend fun deleteFinanceById(id: Int) = dao.deleteFinanceById(id)

    // --- Agenda ---
    val allAgenda: Flow<List<Agenda>> = dao.getAllAgenda()
    suspend fun insertAgenda(agenda: Agenda) = dao.insertAgenda(agenda)
    suspend fun deleteAgendaById(id: Int) = dao.deleteAgendaById(id)

    // --- Inventory ---
    val allInventory: Flow<List<InventoryItem>> = dao.getAllInventory()
    suspend fun insertInventory(item: InventoryItem) = dao.insertInventory(item)
    suspend fun deleteInventoryById(id: Int) = dao.deleteInventoryById(id)

    // --- Books ---
    val allBooks: Flow<List<Book>> = dao.getAllBooks()
    suspend fun insertBook(book: Book) = dao.insertBook(book)
    suspend fun updateBook(book: Book) = dao.updateBook(book)
    suspend fun deleteBookById(id: Int) = dao.deleteBookById(id)

    // --- Petugas ---
    val allPetugas: Flow<List<Petugas>> = dao.getAllPetugas()
    suspend fun insertPetugas(petugas: Petugas) = dao.insertPetugas(petugas)
    suspend fun deletePetugasById(id: Int) = dao.deletePetugasById(id)

    // --- Announcement ---
    val allAnnouncements: Flow<List<Announcement>> = dao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement) = dao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncementById(id: Int) = dao.deleteAnnouncementById(id)

    // --- Chat Messages ---
    val allChatMessages: Flow<List<DbChatMessage>> = dao.getAllChatMessages()
    suspend fun insertChatMessage(message: DbChatMessage) = dao.insertChatMessage(message)
    suspend fun clearChatMessages() = dao.clearChatMessages()

    // --- Prayer Schedule ---
    val allPrayerSchedules: Flow<List<PrayerSchedule>> = dao.getAllPrayerSchedules()
    suspend fun insertPrayerSchedule(schedule: PrayerSchedule) = dao.insertPrayerSchedule(schedule)
    suspend fun deletePrayerScheduleById(id: Int) = dao.deletePrayerScheduleById(id)
}
