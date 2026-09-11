package com.example.data.ai

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AiMemoryEntry(
    val id: String,
    val key: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AiMemoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("quicks_ai_memory_store", Context.MODE_PRIVATE)

    private val _memories = MutableStateFlow<List<AiMemoryEntry>>(loadMemories())
    val memories: StateFlow<List<AiMemoryEntry>> = _memories

    private fun loadMemories(): List<AiMemoryEntry> {
        val raw = prefs.getString("memories_set", null)
        if (raw.isNullOrBlank()) {
            return listOf(
                AiMemoryEntry("mem_1", "Study Goal", "Mastering Physics, Calculus, and Computer Science algorithms"),
                AiMemoryEntry("mem_2", "Learning Style", "Prefers concise intuitive reasoning, first-principles explanations, and Socratic hints"),
                AiMemoryEntry("mem_3", "Current Focus", "Locking in for semester examinations and test problem practice")
            )
        }
        return try {
            raw.split("|||").mapNotNull { entryStr ->
                val parts = entryStr.split(":::")
                if (parts.size >= 3) {
                    AiMemoryEntry(parts[0], parts[1], parts[2], parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis())
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveMemories(list: List<AiMemoryEntry>) {
        val serialized = list.joinToString("|||") { "${it.id}:::${it.key}:::${it.detail}:::${it.timestamp}" }
        prefs.edit().putString("memories_set", serialized).apply()
        _memories.value = list
    }

    fun addMemory(key: String, detail: String) {
        val newEntry = AiMemoryEntry(
            id = "mem_${System.currentTimeMillis()}",
            key = key.trim(),
            detail = detail.trim()
        )
        val updated = _memories.value.filterNot { it.key.equals(key.trim(), ignoreCase = true) } + newEntry
        saveMemories(updated)
    }

    fun deleteMemory(id: String) {
        val updated = _memories.value.filterNot { it.id == id }
        saveMemories(updated)
    }

    fun clearAllMemories() {
        saveMemories(emptyList())
    }

    fun getMemorySystemPromptBlock(): String {
        val current = _memories.value
        if (current.isEmpty()) return ""
        val bulletPoints = current.joinToString("\n") { "- ${it.key}: ${it.detail}" }
        return "\n\n[USER CONTEXT & MEMORY FROM PREVIOUS SESSIONS]\n$bulletPoints\nUse this memory to tailor your explanations, tone, and pacing without unnecessarily repeating it back to the student."
    }
}
