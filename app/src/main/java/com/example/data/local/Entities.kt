package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val dueTimestamp: Long,
    val isCompleted: Boolean = false,
    val isUrgent: Boolean = false,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val startTime: String,
    val endTime: String,
    val dateString: String,
    val source: String = "Quicks", // "Google", "Outlook", "Quicks"
    val isAiGenerated: Boolean = false
)

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String,
    val preset: String, // "Diary", "Music", "Lecture", "Lab", "Formulas", etc.
    val imagePath1: String? = null,
    val imagePath2: String? = null,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val extractedOcrText: String? = null,
    val aiSummary: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val isFromUser: Boolean,
    val text: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT", // "TEXT", "IMAGE", "QUIZ", "SCHEDULE", "SOURCE"
    val quizQuestion: String? = null,
    val quizOptions: String? = null, // comma-separated or json
    val quizCorrectIndex: Int = -1,
    val quizSelectedOption: Int = -1,
    val replyToText: String? = null,
    val fileName: String? = null,
    val fileUri: String? = null,
    val fileType: String? = null, // "pdf", "image", "doc"
    val extractedSourceText: String? = null,
    val isEncrypted: Boolean = true,
    val aiModeUsed: String? = null // "Smart" or "Expert"
)

@Entity(tableName = "chat_channels")
data class ChatChannel(
    @PrimaryKey val id: String,
    val serverName: String? = null,
    val channelName: String,
    val displayName: String,
    val isDirectMessage: Boolean = true,
    val avatarName: String = "default",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val callType: String? = null // "voice", "video", or null
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val subject: String,
    val musicTrackName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recaps")
data class RecapItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val summaryText: String,
    val tasksCompleted: Int,
    val studyMinutes: Int,
    val notesCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
