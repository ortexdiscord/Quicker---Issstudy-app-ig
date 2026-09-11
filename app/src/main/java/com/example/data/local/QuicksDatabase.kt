package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskItem::class,
        CalendarEvent::class,
        NoteItem::class,
        ChatMessage::class,
        ChatChannel::class,
        StudySession::class,
        RecapItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuicksDatabase : RoomDatabase() {
    abstract fun quicksDao(): QuicksDao

    companion object {
        @Volatile
        private var INSTANCE: QuicksDatabase? = null

        fun getDatabase(context: Context): QuicksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuicksDatabase::class.java,
                    "quicks_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
