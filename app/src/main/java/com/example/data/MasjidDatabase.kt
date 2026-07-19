package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Jamaah::class, Finance::class, Agenda::class, InventoryItem::class, Book::class, Petugas::class, Announcement::class, DbChatMessage::class, PrayerSchedule::class],
    version = 4,
    exportSchema = false
)
abstract class MasjidDatabase : RoomDatabase() {
    abstract fun masjidDao(): MasjidDao

    companion object {
        @Volatile
        private var INSTANCE: MasjidDatabase? = null

        fun getDatabase(context: Context): MasjidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasjidDatabase::class.java,
                    "sigma_masjid_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
