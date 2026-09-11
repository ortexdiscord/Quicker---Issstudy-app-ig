package com.example.data.repository

import com.example.data.local.CalendarEvent
import com.example.data.local.ChatChannel
import com.example.data.local.ChatMessage
import com.example.data.local.NoteItem
import com.example.data.local.QuicksDao
import com.example.data.local.RecapItem
import com.example.data.local.StudySession
import com.example.data.local.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuicksRepository(private val dao: QuicksDao) {

    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allCalendarEvents: Flow<List<CalendarEvent>> = dao.getAllCalendarEvents()
    val allNotes: Flow<List<NoteItem>> = dao.getAllNotes()
    val allChannels: Flow<List<ChatChannel>> = dao.getAllChannels()
    val allStudySessions: Flow<List<StudySession>> = dao.getAllStudySessions()
    val allRecaps: Flow<List<RecapItem>> = dao.getAllRecaps()

    fun getMessagesForConversation(convoId: String): Flow<List<ChatMessage>> =
        dao.getMessagesForConversation(convoId)

    suspend fun insertTask(task: TaskItem) = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)

    suspend fun insertCalendarEvent(event: CalendarEvent) = dao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEvent) = dao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEvent) = dao.deleteCalendarEvent(event)

    suspend fun insertNote(note: NoteItem) = dao.insertNote(note)
    suspend fun deleteNote(note: NoteItem) = dao.deleteNote(note)

    suspend fun insertMessage(message: ChatMessage) = dao.insertMessage(message)
    suspend fun updateMessage(message: ChatMessage) = dao.updateMessage(message)

    suspend fun insertChannel(channel: ChatChannel) = dao.insertChannel(channel)
    suspend fun updateChannel(channel: ChatChannel) = dao.updateChannel(channel)
    suspend fun updateLastMessage(channelId: String, lastMessage: String, time: Long) = dao.updateLastMessage(channelId, lastMessage, time)
    suspend fun deleteChannel(channelId: String) {
        dao.deleteChannelById(channelId)
        dao.deleteMessagesByConvoId(channelId)
    }

    suspend fun cleanUpLegacyFakeChats() {
        val legacyIds = listOf("dm_alif", "dm_yez", "dm_angelo", "group_g7", "group_sseg7", "dm_sarah", "physics_study_general")
        for (id in legacyIds) {
            dao.deleteChannelById(id)
            dao.deleteMessagesByConvoId(id)
        }

        // Purge previous fake seed tasks and notes so user has 100% clean authentic data
        try {
            val currentTasks = dao.getAllTasks().first()
            currentTasks.filter {
                it.title.contains("Quantum Physics Problem Set 4") ||
                it.title.contains("Review Graph Traversal") ||
                it.title.contains("Stereochemistry")
            }.forEach { dao.deleteTask(it) }

            val currentNotes = dao.getAllNotes().first()
            currentNotes.filter {
                it.title.contains("Maxwell's Equations") ||
                it.title.contains("Daily Reflection & Brain Dump")
            }.forEach { dao.deleteNote(it) }

            val currentEvents = dao.getAllCalendarEvents().first()
            currentEvents.filter {
                it.title.contains("Wave Mechanics") ||
                it.title.contains("Algorithms Lab") ||
                it.title.contains("Deep Focus: Study Session")
            }.forEach { dao.deleteCalendarEvent(it) }
        } catch (_: Exception) {}
    }

    suspend fun insertStudySession(session: StudySession) = dao.insertStudySession(session)
    suspend fun insertRecap(recap: RecapItem) = dao.insertRecap(recap)

    suspend fun populateInitialDataIfEmpty() {
        // Clean up any legacy fake chats and fake placeholder data
        cleanUpLegacyFakeChats()

        val existingChannels = dao.getAllChannels().first()
        if (existingChannels.none { it.id == "quicker_ai" }) {
            // Only initialize the official Quicker AI companion channel
            dao.insertChannel(
                ChatChannel(
                    id = "quicker_ai",
                    serverName = null,
                    channelName = "Quicker AI",
                    displayName = "Quicker AI (Study Companion)",
                    isDirectMessage = true,
                    avatarName = "ai",
                    lastMessage = "Welcome to Quicks! Ask me anything, or attach notes and PDFs.",
                    lastMessageTime = System.currentTimeMillis(),
                    unreadCount = 0
                )
            )

            dao.insertMessage(
                ChatMessage(
                    conversationId = "quicker_ai",
                    senderId = "ai",
                    senderName = "Quicker AI",
                    isFromUser = false,
                    text = "Welcome to **Quicks**! ⚡ I am your AI study companion.\n\nChoose between **Smart**, **Expert**, or **Master** mode. You can attach PDFs, images, record voice prompts, or explore our new **Study Cast Studio**!",
                    messageType = "TEXT"
                )
            )
        }
    }
}
