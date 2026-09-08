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

    suspend fun insertStudySession(session: StudySession) = dao.insertStudySession(session)
    suspend fun insertRecap(recap: RecapItem) = dao.insertRecap(recap)

    suspend fun populateInitialDataIfEmpty() {
        val existingTasks = dao.getAllTasks().first()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        if (existingTasks.isEmpty()) {
            // Seed Tasks
            dao.insertTask(
                TaskItem(
                    title = "Submit Quantum Physics Problem Set 4",
                    subject = "Physics",
                    dueTimestamp = System.currentTimeMillis() + 3 * 3600 * 1000,
                    isCompleted = false,
                    isUrgent = true,
                    priority = "HIGH"
                )
            )
            dao.insertTask(
                TaskItem(
                    title = "Review Graph Traversal (BFS/DFS) algorithms",
                    subject = "Algorithms",
                    dueTimestamp = System.currentTimeMillis() + 24 * 3600 * 1000,
                    isCompleted = false,
                    isUrgent = false,
                    priority = "MEDIUM"
                )
            )
            dao.insertTask(
                TaskItem(
                    title = "Read Organic Chemistry Chapter 8: Stereochemistry",
                    subject = "Chemistry",
                    dueTimestamp = System.currentTimeMillis() + 48 * 3600 * 1000,
                    isCompleted = true,
                    isUrgent = false,
                    priority = "LOW"
                )
            )

            // Seed Calendar Events
            dao.insertCalendarEvent(
                CalendarEvent(
                    title = "Physics 301 Lecture: Wave Mechanics",
                    subject = "Physics",
                    startTime = "09:00 AM",
                    endTime = "10:30 AM",
                    dateString = todayStr,
                    source = "Google",
                    isAiGenerated = false
                )
            )
            dao.insertCalendarEvent(
                CalendarEvent(
                    title = "Algorithms Lab: Dynamic Programming",
                    subject = "Computer Science",
                    startTime = "11:00 AM",
                    endTime = "12:30 PM",
                    dateString = todayStr,
                    source = "Outlook",
                    isAiGenerated = false
                )
            )
            dao.insertCalendarEvent(
                CalendarEvent(
                    title = "Deep Focus: Study Session (Lock In)",
                    subject = "General",
                    startTime = "03:00 PM",
                    endTime = "04:30 PM",
                    dateString = todayStr,
                    source = "Quicks",
                    isAiGenerated = true
                )
            )

            // Seed Notes
            dao.insertNote(
                NoteItem(
                    title = "Maxwell's Equations & Wave Equation",
                    content = "1. Gauss's Law for Electricity\n2. Gauss's Law for Magnetism\n3. Faraday's Law of Induction\n4. Ampere's Circuital Law with Maxwell's addition.\nKey takeaway: Electromagnetic waves propagate at speed c = 1/sqrt(mu_0 * eps_0).",
                    subject = "Physics",
                    preset = "Lecture",
                    dateString = todayStr
                )
            )
            dao.insertNote(
                NoteItem(
                    title = "Daily Reflection & Brain Dump",
                    content = "Today feeling energetic. Need to lock in on dynamic programming memoization vs tabulation before Friday quiz.",
                    subject = "Personal",
                    preset = "Diary",
                    dateString = todayStr
                )
            )

            // Seed Chat Channels (WhatsApp 1-on-1 + Discord Servers/Channels)
            dao.insertChannel(
                ChatChannel(
                    id = "quicker_ai",
                    serverName = null,
                    channelName = "Quicker AI",
                    displayName = "Quicker AI (Study Companion)",
                    isDirectMessage = true,
                    avatarName = "ai",
                    lastMessage = "Ready to study! Tap 'Quiz Me' or send your lecture notes.",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 5,
                    unreadCount = 1
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "dm_alif",
                    serverName = null,
                    channelName = "Alif Papi 2",
                    displayName = "Alif Papi 2",
                    isDirectMessage = true,
                    avatarName = "user_1",
                    lastMessage = "Voice call",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 20,
                    unreadCount = 0,
                    callType = "voice"
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "dm_yez",
                    serverName = null,
                    channelName = "Yez",
                    displayName = "Yez",
                    isDirectMessage = true,
                    avatarName = "user_2",
                    lastMessage = "Good to hear",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 50,
                    unreadCount = 2
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "dm_angelo",
                    serverName = null,
                    channelName = "Angelo",
                    displayName = "Angelo",
                    isDirectMessage = true,
                    avatarName = "user_3",
                    lastMessage = "Video call",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 3600 * 4,
                    unreadCount = 0,
                    callType = "video"
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "group_g7",
                    serverName = "Study Cohort",
                    channelName = "G - 7 Group",
                    displayName = "G - 7 Group",
                    isDirectMessage = false,
                    avatarName = "group_1",
                    lastMessage = "~ Kazi Alyna: Didn't know you have one",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 3600 * 10,
                    unreadCount = 7
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "group_sseg7",
                    serverName = "SSE Batch",
                    channelName = "SSE G-7 (Unofficial)",
                    displayName = "SSE G-7 (Unofficial)",
                    isDirectMessage = false,
                    avatarName = "group_2",
                    lastMessage = "Yez: I got 17 marks, without looking at ma...",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 3600 * 24,
                    unreadCount = 0
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "dm_sarah",
                    serverName = null,
                    channelName = "Sarah Chen",
                    displayName = "Sarah Chen",
                    isDirectMessage = true,
                    avatarName = "friend_1",
                    lastMessage = "Are we locking in at the library around 4pm?",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 3600 * 28,
                    unreadCount = 1
                )
            )
            dao.insertChannel(
                ChatChannel(
                    id = "physics_study_general",
                    serverName = "Physics 301 Hub",
                    channelName = "general",
                    displayName = "#general",
                    isDirectMessage = false,
                    avatarName = "server_physics",
                    lastMessage = "Alex: Has anyone checked problem 3 on harmonic oscillators?",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 3600 * 48,
                    unreadCount = 0
                )
            )

            // Seed Sample Messages for conversations
            dao.insertMessage(
                ChatMessage(
                    conversationId = "dm_alif",
                    senderId = "alif",
                    senderName = "Alif Papi 2",
                    isFromUser = false,
                    text = "Hey, did you get the lecture slides from yesterday?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 35
                )
            )
            dao.insertMessage(
                ChatMessage(
                    conversationId = "dm_alif",
                    senderId = "user",
                    senderName = "Me",
                    isFromUser = true,
                    text = "Yeah, I saved them into my Quicks Notes. Calling you now to go over them.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 25
                )
            )

            dao.insertMessage(
                ChatMessage(
                    conversationId = "dm_yez",
                    senderId = "yez",
                    senderName = "Yez",
                    isFromUser = false,
                    text = "Did the professor mention if the quiz is open book?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                )
            )
            dao.insertMessage(
                ChatMessage(
                    conversationId = "dm_yez",
                    senderId = "user",
                    senderName = "Me",
                    isFromUser = true,
                    text = "No, closed book but one cheat sheet allowed!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 55
                )
            )
            dao.insertMessage(
                ChatMessage(
                    conversationId = "dm_yez",
                    senderId = "yez",
                    senderName = "Yez",
                    isFromUser = false,
                    text = "Good to hear",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 50
                )
            )

            dao.insertMessage(
                ChatMessage(
                    conversationId = "group_g7",
                    senderId = "aly",
                    senderName = "Kazi Alyna",
                    isFromUser = false,
                    text = "Didn't know you have one formula sheet already prepared!",
                    timestamp = System.currentTimeMillis() - 1000 * 3600 * 10
                )
            )

            // Seed Welcome Message in Quicker AI
            dao.insertMessage(
                ChatMessage(
                    conversationId = "quicker_ai",
                    senderId = "ai",
                    senderName = "Quicker AI",
                    isFromUser = false,
                    text = "Welcome to Quicker! ⚡ I am your personal AI study assistant. You can send me study notes, questions, or tap any quick tool below: Quiz, Summary, Translate, or Schedule.",
                    messageType = "TEXT"
                )
            )

            // Seed a sample Quiz Message
            dao.insertMessage(
                ChatMessage(
                    conversationId = "quicker_ai",
                    senderId = "ai",
                    senderName = "Quicker AI",
                    isFromUser = false,
                    text = "Here is a quick diagnostic quiz on Physics:",
                    messageType = "QUIZ",
                    quizQuestion = "What is the SI unit of magnetic flux density?",
                    quizOptions = "Tesla (T),Weber (Wb),Henry (H),Gauss (G)",
                    quizCorrectIndex = 0,
                    quizSelectedOption = -1
                )
            )

            // Seed sample past recap
            dao.insertRecap(
                RecapItem(
                    dateString = todayStr,
                    summaryText = "Solid progress today! Completed 2 high-priority tasks and finished 45 minutes of focused Lock In study on Wave Mechanics. Keep up the momentum!",
                    tasksCompleted = 2,
                    studyMinutes = 45,
                    notesCount = 2
                )
            )
        }
    }
}
