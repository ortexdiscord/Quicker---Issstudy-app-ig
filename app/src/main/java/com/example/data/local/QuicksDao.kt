package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuicksDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, isUrgent DESC, dueTimestamp ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    // Calendar
    @Query("SELECT * FROM calendar_events ORDER BY dateString ASC, startTime ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEvent): Long

    @Delete
    suspend fun deleteCalendarEvent(event: CalendarEvent)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEvent)

    // Notes
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem): Long

    @Delete
    suspend fun deleteNote(note: NoteItem)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convoId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convoId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    // Chat Channels
    @Query("SELECT * FROM chat_channels ORDER BY lastMessageTime DESC")
    fun getAllChannels(): Flow<List<ChatChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChatChannel)

    @Update
    suspend fun updateChannel(channel: ChatChannel)

    @Query("UPDATE chat_channels SET lastMessage = :lastMessage, lastMessageTime = :time WHERE id = :channelId")
    suspend fun updateLastMessage(channelId: String, lastMessage: String, time: Long)

    @Query("DELETE FROM chat_channels WHERE id = :channelId")
    suspend fun deleteChannelById(channelId: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :convoId")
    suspend fun deleteMessagesByConvoId(convoId: String)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession): Long

    // Recaps
    @Query("SELECT * FROM recaps ORDER BY timestamp DESC")
    fun getAllRecaps(): Flow<List<RecapItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecap(recap: RecapItem): Long
}
