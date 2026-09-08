package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.CodeVerificationManager
import com.example.data.firebase.CloudSyncState
import com.example.data.firebase.FirebaseManager
import com.example.audio.LofiAudioEngine
import com.example.data.api.AiService
import com.example.data.api.QuizResult
import com.example.data.local.CalendarEvent
import com.example.data.local.ChatChannel
import com.example.data.local.ChatMessage
import com.example.data.local.NoteItem
import com.example.data.local.QuicksDatabase
import com.example.data.local.RecapItem
import com.example.data.local.StudySession
import com.example.data.local.TaskItem
import com.example.data.local.UserSessionManager
import com.example.data.repository.QuicksRepository
import com.example.speech.VoiceAction
import com.example.speech.VoiceCommandHelper
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QuicksNavTab {
    HOME,
    RECAP,
    CALENDAR,
    LOCK_IN,
    NOTES,
    CHAT
}

enum class SettingsTab {
    ACCOUNT,
    FIREBASE,
    THEME,
    PRIVACY,
    API_KEYS,
    CREDITS
}

class QuicksViewModel(application: Application) : AndroidViewModel(application) {

    private val db = QuicksDatabase.getDatabase(application)
    val repository = QuicksRepository(db.quicksDao())
    val sessionManager = UserSessionManager(application)
    val aiService = AiService(application)
    val audioEngine = LofiAudioEngine()
    val voiceHelper = VoiceCommandHelper(application)
    val codeVerificationManager = CodeVerificationManager(application)
    val firebaseManager = FirebaseManager(application)

    val cloudSyncState: StateFlow<CloudSyncState> = firebaseManager.syncState
    val firebaseUser = firebaseManager.currentUser

    // Navigation & Modals
    private val _currentTab = MutableStateFlow(QuicksNavTab.HOME)
    val currentTab: StateFlow<QuicksNavTab> = _currentTab.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _selectedSettingsTab = MutableStateFlow(SettingsTab.ACCOUNT)
    val selectedSettingsTab: StateFlow<SettingsTab> = _selectedSettingsTab.asStateFlow()

    // Data Flows from Room
    val tasks = repository.allTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val calendarEvents = repository.allCalendarEvents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val notes = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val channels = repository.allChannels.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val recaps = repository.allRecaps.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Active Chat & Messages
    private val _selectedConversationId = MutableStateFlow("quicker_ai")
    val selectedConversationId: StateFlow<String> = _selectedConversationId.asStateFlow()

    // When null, ChatScreen displays the WhatsApp-style Chats List. When set, displays Conversation Detail.
    private val _activeChatConvoId = MutableStateFlow<String?>(null)
    val activeChatConvoId: StateFlow<String?> = _activeChatConvoId.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

    private var messagesJob: Job? = null

    // Notes Presets
    private val _customPresets = MutableStateFlow(listOf("Diary", "Music", "Lecture", "Lab", "Formulas", "Exam Prep"))
    val customPresets: StateFlow<List<String>> = _customPresets.asStateFlow()

    // Lock In Focus State
    private val _lockInTotalSeconds = MutableStateFlow(25 * 60)
    val lockInTotalSeconds: StateFlow<Int> = _lockInTotalSeconds.asStateFlow()

    private val _lockInRemainingSeconds = MutableStateFlow(25 * 60)
    val lockInRemainingSeconds: StateFlow<Int> = _lockInRemainingSeconds.asStateFlow()

    private val _isLockInRunning = MutableStateFlow(false)
    val isLockInRunning: StateFlow<Boolean> = _isLockInRunning.asStateFlow()

    private val _isLockInImmersive = MutableStateFlow(false)
    val isLockInImmersive: StateFlow<Boolean> = _isLockInImmersive.asStateFlow()

    private var lockInJob: Job? = null

    // Calendar Sync State
    private val _isGoogleCalendarSynced = MutableStateFlow(true)
    val isGoogleCalendarSynced: StateFlow<Boolean> = _isGoogleCalendarSynced.asStateFlow()

    private val _isOutlookCalendarSynced = MutableStateFlow(true)
    val isOutlookCalendarSynced: StateFlow<Boolean> = _isOutlookCalendarSynced.asStateFlow()

    // AI Generation Loading States
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isGeneratingRecap = MutableStateFlow(false)
    val isGeneratingRecap: StateFlow<Boolean> = _isGeneratingRecap.asStateFlow()

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
            loadConversation(_selectedConversationId.value)
        }
    }

    fun selectTab(tab: QuicksNavTab) {
        _currentTab.value = tab
    }

    fun openSettings(tab: SettingsTab = SettingsTab.ACCOUNT) {
        _selectedSettingsTab.value = tab
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun setSettingsTab(tab: SettingsTab) {
        _selectedSettingsTab.value = tab
    }

    // --- Task Actions ---
    fun addTask(title: String, subject: String, isUrgent: Boolean, priority: String, dueHours: Int = 24) {
        viewModelScope.launch {
            val due = System.currentTimeMillis() + (dueHours * 3600L * 1000L)
            repository.insertTask(
                TaskItem(
                    title = title,
                    subject = subject,
                    dueTimestamp = due,
                    isCompleted = false,
                    isUrgent = isUrgent,
                    priority = priority
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Calendar Actions ---
    fun toggleGoogleSync() {
        _isGoogleCalendarSynced.value = !_isGoogleCalendarSynced.value
    }

    fun toggleOutlookSync() {
        _isOutlookCalendarSynced.value = !_isOutlookCalendarSynced.value
    }

    fun addCalendarEvent(title: String, subject: String, startTime: String, endTime: String, source: String = "Quicks") {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())
            repository.insertCalendarEvent(
                CalendarEvent(
                    title = title,
                    subject = subject,
                    startTime = startTime,
                    endTime = endTime,
                    dateString = today,
                    source = source
                )
            )
        }
    }

    fun rescheduleEvent(event: CalendarEvent, newStartTime: String, newEndTime: String) {
        viewModelScope.launch {
            repository.updateCalendarEvent(event.copy(startTime = newStartTime, endTime = newEndTime))
        }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(event)
        }
    }

    // --- Notes Actions ---
    fun addNote(
        title: String,
        content: String,
        subject: String,
        preset: String,
        imagePath1: String? = null,
        imagePath2: String? = null,
        dateString: String? = null
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateString ?: dateFormat.format(Date())
            repository.insertNote(
                NoteItem(
                    title = title,
                    content = content,
                    subject = subject,
                    preset = preset,
                    imagePath1 = imagePath1,
                    imagePath2 = imagePath2,
                    dateString = date
                )
            )
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addCustomPreset(preset: String) {
        val trimmed = preset.trim()
        if (trimmed.isNotBlank() && !_customPresets.value.contains(trimmed)) {
            _customPresets.value = _customPresets.value + trimmed
        }
    }

    fun forwardNoteToAi(note: NoteItem) {
        viewModelScope.launch {
            selectTab(QuicksNavTab.CHAT)
            openConversation("quicker_ai")
            val prompt = "Here are my notes on ${note.subject} (${note.preset}):\nTitle: ${note.title}\n${note.content}\nPlease summarize the key formulas or concepts and point out any common exam pitfalls."
            sendChatMessage("quicker_ai", prompt, note.imagePath1)
        }
    }

    fun forwardNoteToChat(note: NoteItem, targetConvoId: String) {
        viewModelScope.launch {
            selectTab(QuicksNavTab.CHAT)
            openConversation(targetConvoId)
            val text = "Shared Note [${note.subject} - ${note.preset}]:\n${note.title}\n${note.content}"
            sendChatMessage(targetConvoId, text, note.imagePath1)
        }
    }

    // --- Lock In / Focus Timer Actions ---
    fun setLockInDuration(minutes: Int) {
        val seconds = minutes * 60
        _lockInTotalSeconds.value = seconds
        _lockInRemainingSeconds.value = seconds
    }

    fun addLockInMinutes(minutes: Int) {
        _lockInRemainingSeconds.value = (_lockInRemainingSeconds.value + (minutes * 60)).coerceAtLeast(0)
    }

    fun toggleLockInTimer() {
        if (_isLockInRunning.value) {
            pauseLockIn()
        } else {
            startLockIn()
        }
    }

    fun startLockIn() {
        _isLockInRunning.value = true
        _isLockInImmersive.value = true
        audioEngine.play()

        lockInJob?.cancel()
        lockInJob = viewModelScope.launch {
            while (_isLockInRunning.value && _lockInRemainingSeconds.value > 0) {
                delay(1000)
                _lockInRemainingSeconds.value -= 1
            }
            if (_lockInRemainingSeconds.value <= 0) {
                completeLockIn()
            }
        }
    }

    fun pauseLockIn() {
        _isLockInRunning.value = false
        lockInJob?.cancel()
    }

    fun exitLockInImmersive() {
        _isLockInImmersive.value = false
    }

    fun enterLockInImmersive() {
        _isLockInImmersive.value = true
    }

    private fun completeLockIn() {
        _isLockInRunning.value = false
        lockInJob?.cancel()
        viewModelScope.launch {
            val minutesStudied = (_lockInTotalSeconds.value / 60)
            repository.insertStudySession(
                StudySession(
                    durationMinutes = minutesStudied,
                    subject = "Focused Study",
                    musicTrackName = audioEngine.getCurrentTrack().title
                )
            )
        }
    }

    fun handleVoiceAction(action: VoiceAction) {
        when (action) {
            is VoiceAction.PauseSession -> pauseLockIn()
            is VoiceAction.ResumeSession -> startLockIn()
            is VoiceAction.StopMusic -> audioEngine.pause()
            is VoiceAction.PlayMusic -> audioEngine.play()
            is VoiceAction.NextTrack -> audioEngine.nextTrack()
            is VoiceAction.AddMinutes -> addLockInMinutes(action.minutes)
            is VoiceAction.SetTimer -> setLockInDuration(action.minutes)
            is VoiceAction.Unknown -> {}
        }
    }

    // --- Chat & Channels ---
    fun openConversation(convoId: String) {
        _selectedConversationId.value = convoId
        _activeChatConvoId.value = convoId
        loadConversation(convoId)
    }

    fun closeConversation() {
        _activeChatConvoId.value = null
    }

    fun loadConversation(convoId: String) {
        _selectedConversationId.value = convoId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForConversation(convoId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun createNewChat(name: String, isDirect: Boolean, serverName: String? = null) {
        viewModelScope.launch {
            val id = "chat_${System.currentTimeMillis()}"
            val channel = ChatChannel(
                id = id,
                serverName = serverName,
                channelName = name,
                displayName = name,
                isDirectMessage = isDirect,
                avatarName = if (isDirect) "friend_new" else "group_new",
                lastMessage = "Chat started",
                lastMessageTime = System.currentTimeMillis()
            )
            repository.insertChannel(channel)
            openConversation(id)
        }
    }

    fun sendChatMessage(
        convoId: String,
        text: String,
        imageUri: String? = null,
        replyToText: String? = null,
        isHintRequested: Boolean = false
    ) {
        if (text.isBlank() && imageUri == null) return

        val user = sessionManager.userProfile.value
        viewModelScope.launch {
            val previewText = if (text.isNotBlank()) text else "📷 Photo"
            repository.updateLastMessage(convoId, previewText, System.currentTimeMillis())

            val userMsg = ChatMessage(
                conversationId = convoId,
                senderId = "user",
                senderName = user.name,
                isFromUser = true,
                text = text,
                imageUri = imageUri,
                replyToText = replyToText
            )
            repository.insertMessage(userMsg)

            // If messaging Quicker AI, trigger AI response
            if (convoId == "quicker_ai") {
                triggerQuickerAiResponse(text, imageUri, isHintRequested)
            } else {
                // Friendly simulated peer reply for demo channels
                delay(1000)
                val peerReplies = listOf(
                    "Got it, reviewing this right now!",
                    "Great explanation, thanks for sharing!",
                    "Let's lock in on this before class tomorrow.",
                    "Checked your note, makes total sense."
                )
                val reply = peerReplies.random()
                repository.updateLastMessage(convoId, reply, System.currentTimeMillis())
                repository.insertMessage(
                    ChatMessage(
                        conversationId = convoId,
                        senderId = "peer",
                        senderName = if (convoId.contains("sarah")) "Sarah Chen" else if (convoId.contains("yez")) "Yez" else "Alex",
                        isFromUser = false,
                        text = reply
                    )
                )
            }
        }
    }

    fun requestAiQuiz(topic: String = "Physics") {
        viewModelScope.launch {
            _isAiLoading.value = true
            val quiz = aiService.generateQuiz(topic)
            _isAiLoading.value = false

            repository.insertMessage(
                ChatMessage(
                    conversationId = "quicker_ai",
                    senderId = "ai",
                    senderName = "Quicker AI",
                    isFromUser = false,
                    text = "Here is a custom quiz question for $topic:",
                    messageType = "QUIZ",
                    quizQuestion = quiz.question,
                    quizOptions = quiz.options.joinToString(",,,,"),
                    quizCorrectIndex = quiz.correctIndex,
                    quizSelectedOption = -1
                )
            )
        }
    }

    fun answerQuiz(message: ChatMessage, optionIndex: Int) {
        viewModelScope.launch {
            repository.updateMessage(message.copy(quizSelectedOption = optionIndex))
        }
    }

    fun requestAiSummary(content: String) {
        viewModelScope.launch {
            sendChatMessage("quicker_ai", "Can you summarize this study topic concisely: \"$content\"?")
        }
    }

    fun requestAiTranslation(text: String, targetLanguage: String = "Spanish") {
        viewModelScope.launch {
            sendChatMessage("quicker_ai", "Please translate this study phrase into $targetLanguage with academic precision: \"$text\"")
        }
    }

    fun requestAiScheduleAdvice() {
        viewModelScope.launch {
            val upcoming = calendarEvents.value.take(3)
            val scheduleContext = upcoming.joinToString("; ") { "${it.title} at ${it.startTime}" }
            sendChatMessage(
                "quicker_ai",
                "Based on my calendar ($scheduleContext), suggest an optimal Lock In study block and add it to my schedule."
            )
        }
    }

    private fun triggerQuickerAiResponse(prompt: String, imageUri: String?, isHintMode: Boolean) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val lower = prompt.lowercase()

            // If prompt asks to schedule
            if (lower.contains("schedule") && (lower.contains("add") || lower.contains("lock in") || lower.contains("study block"))) {
                addCalendarEvent("AI Planned: Deep Focus Study Block", "General", "02:00 PM", "03:30 PM", "Quicks")
            }

            val result = aiService.generateResponse(prompt, imageUri, isHintMode)
            _isAiLoading.value = false

            val responseText = result.getOrElse { it.message ?: "Could not complete request." }
            repository.insertMessage(
                ChatMessage(
                    conversationId = "quicker_ai",
                    senderId = "ai",
                    senderName = "Quicker AI",
                    isFromUser = false,
                    text = responseText,
                    messageType = "TEXT"
                )
            )
        }
    }

    // --- Daily Recap ---
    fun generateRecapNow() {
        viewModelScope.launch {
            _isGeneratingRecap.value = true
            val completed = tasks.value.filter { it.isCompleted }
            val currentNotes = notes.value
            val totalMinutes = 45 // default or calculate from study sessions

            val recapText = aiService.generateDailyRecap(completed, currentNotes, totalMinutes)
            _isGeneratingRecap.value = false

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            repository.insertRecap(
                RecapItem(
                    dateString = todayStr,
                    summaryText = recapText,
                    tasksCompleted = completed.size,
                    studyMinutes = totalMinutes,
                    notesCount = currentNotes.size
                )
            )
        }
    }

    fun syncLocalDataToFirebaseCloud() {
        val userEmail = sessionManager.userProfile.value.email
        if (userEmail.isBlank()) return
        viewModelScope.launch {
            val taskList = tasks.value
            val noteList = notes.value
            firebaseManager.syncLocalDataToCloud(
                userEmail = userEmail,
                tasks = taskList,
                notes = noteList,
                sessions = emptyList()
            )
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Quicks", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        voiceHelper.stopListening()
    }
}
