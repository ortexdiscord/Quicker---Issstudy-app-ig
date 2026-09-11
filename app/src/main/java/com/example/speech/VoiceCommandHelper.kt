package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

sealed class VoiceAction {
    object PauseSession : VoiceAction()
    object ResumeSession : VoiceAction()
    object StopMusic : VoiceAction()
    object PlayMusic : VoiceAction()
    object NextTrack : VoiceAction()
    data class AddMinutes(val minutes: Int) : VoiceAction()
    data class SetTimer(val minutes: Int) : VoiceAction()
    data class Unknown(val rawText: String) : VoiceAction()
}

class VoiceCommandHelper(private val context: Context) {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _lastHeardText = MutableStateFlow("")
    val lastHeardText: StateFlow<String> = _lastHeardText

    private var speechRecognizer: SpeechRecognizer? = null

    fun parseCommand(raw: String): VoiceAction {
        val clean = raw.lowercase(Locale.ROOT).trim()
        return when {
            clean.contains("stop music") || clean.contains("pause music") || clean.contains("mute music") -> {
                VoiceAction.StopMusic
            }
            clean.contains("play music") || clean.contains("start music") -> {
                VoiceAction.PlayMusic
            }
            clean.contains("next track") || clean.contains("next song") || clean.contains("shuffle") -> {
                VoiceAction.NextTrack
            }
            clean.contains("pause") || clean.contains("freeze") || clean.contains("hold on") -> {
                VoiceAction.PauseSession
            }
            clean.contains("resume") || clean.contains("continue") || clean.contains("start") -> {
                VoiceAction.ResumeSession
            }
            clean.contains("add 5") || clean.contains("plus 5") -> {
                VoiceAction.AddMinutes(5)
            }
            clean.contains("add 10") || clean.contains("plus 10") -> {
                VoiceAction.AddMinutes(10)
            }
            clean.contains("add 15") -> {
                VoiceAction.AddMinutes(15)
            }
            clean.contains("25") -> {
                VoiceAction.SetTimer(25)
            }
            clean.contains("45") -> {
                VoiceAction.SetTimer(45)
            }
            clean.contains("50") -> {
                VoiceAction.SetTimer(50)
            }
            clean.contains("60") || clean.contains("1 hour") -> {
                VoiceAction.SetTimer(60)
            }
            else -> {
                val digits = clean.filter { it.isDigit() }
                if (digits.isNotEmpty()) {
                    val num = digits.toIntOrNull()
                    if (num != null && num in 1..180) {
                        VoiceAction.SetTimer(num)
                    } else {
                        VoiceAction.Unknown(raw)
                    }
                } else {
                    VoiceAction.Unknown(raw)
                }
            }
        }
    }

    fun startListening(onResult: (VoiceAction) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not supported on this device")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        onError("Voice recognition error: $error")
                    }
                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _lastHeardText.value = text
                        if (text.isNotBlank()) {
                            onResult(parseCommand(text))
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'Pause', 'Stop music', 'Add 5 minutes', etc.")
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError(e.message ?: "Could not start voice recognition")
        }
    }

    fun startSpeechToText(onText: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not supported on this device")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        onError("Speech recognition error: $error")
                    }
                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _lastHeardText.value = text
                        if (text.isNotBlank()) {
                            onText(text)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question or note...")
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError(e.message ?: "Could not start microphone voice input")
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }
}
