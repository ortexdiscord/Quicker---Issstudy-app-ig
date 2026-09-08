package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

data class LofiTrack(
    val id: Int,
    val title: String,
    val style: String,
    val bpm: Int,
    val baseFrequencies: List<Float>
)

class LofiAudioEngine {

    val tracks: List<LofiTrack> = listOf(
        LofiTrack(1, "Midnight Tokyo Rain", "Rain & Warm Maj7", 68, listOf(349.23f, 440.0f, 523.25f, 659.25f)), // Fmaj7
        LofiTrack(2, "Coffee Shop Study", "Mellow Jazz Chords", 72, listOf(261.63f, 329.63f, 392.00f, 493.88f)), // Cmaj7
        LofiTrack(3, "Deep Lock In Beats", "Chill Sub-Bass & Minor", 65, listOf(220.0f, 261.63f, 329.63f, 392.00f)), // Am7
        LofiTrack(4, "Late Night Library", "Soft Electric Rhodes", 60, listOf(293.66f, 349.23f, 440.0f, 523.25f)), // Dm7
        LofiTrack(5, "Cosmic Lo-Fi", "Airy Ethereal Drift", 75, listOf(392.00f, 493.88f, 587.33f, 698.46f)), // G7
        LofiTrack(6, "Analog Tape Dreams", "Vintage Warm Flutter", 64, listOf(329.63f, 392.00f, 493.88f, 587.33f)), // Em7
        LofiTrack(7, "Study Session #7", "Classic Lo-Fi Groove", 70, listOf(261.63f, 329.63f, 392.00f, 440.0f)), // C6
        LofiTrack(8, "Raindrop Melodies", "Soothing Pentatonic Bells", 66, listOf(392.0f, 440.0f, 523.25f, 659.25f)),
        LofiTrack(9, "Vinyl Warmth Lounge", "Cozy Fireplace Crackle", 74, listOf(349.23f, 392.0f, 440.0f, 523.25f)),
        LofiTrack(10, "Zen Focus Drone", "Deep Alpha Harmonics", 58, listOf(196.0f, 293.66f, 392.0f, 587.33f))
    )

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume

    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun getCurrentTrack(): LofiTrack = tracks[_currentTrackIndex.value]

    fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startSynthesizer()
    }

    fun pause() {
        _isPlaying.value = false
        stopSynthesizer()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % tracks.size
        if (_isPlaying.value) {
            stopSynthesizer()
            startSynthesizer()
        }
    }

    fun previousTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
        if (_isPlaying.value) {
            stopSynthesizer()
            startSynthesizer()
        }
    }

    fun shuffleTrack() {
        val next = (tracks.indices).filter { it != _currentTrackIndex.value }.randomOrNull() ?: 0
        _currentTrackIndex.value = next
        if (_isPlaying.value) {
            stopSynthesizer()
            startSynthesizer()
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        audioTrack?.setVolume(clamped)
    }

    private fun startSynthesizer() {
        synthesisJob?.cancel()
        synthesisJob = scope.launch {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.setVolume(_volume.value)
            track.play()

            val buffer = ShortArray(1024)
            var sampleCounter = 0L
            val currentLofi = getCurrentTrack()
            val chords = currentLofi.baseFrequencies
            val random = Random()

            try {
                while (isActive && _isPlaying.value) {
                    val seconds = sampleCounter.toDouble() / sampleRate
                    // Chord progression changes every 3.5 seconds
                    val chordStep = ((seconds / 3.5).toInt()) % chords.size
                    val chordFreq = chords[chordStep]

                    for (i in buffer.indices) {
                        val t = (sampleCounter + i).toDouble() / sampleRate

                        // 1. Soft Warm Rhodes Tone (fundamental + 2nd + 3rd harmonic)
                        val fundamental = sin(2.0 * Math.PI * chordFreq * t)
                        val harmonic2 = 0.35 * sin(2.0 * Math.PI * (chordFreq * 1.5) * t)
                        val harmonic3 = 0.20 * sin(2.0 * Math.PI * (chordFreq * 2.0) * t)

                        // 2. Gentle vibrato / lofi tape flutter (0.5Hz subtle pitch modulation)
                        val flutter = 1.0 + 0.003 * sin(2.0 * Math.PI * 0.7 * t)

                        // 3. Gentle Vinyl Crackle / Rain ambient noise
                        val rainCrackle = if (random.nextFloat() > 0.985f) (random.nextFloat() - 0.5f) * 0.15f else 0.0f
                        val ambientHiss = (random.nextFloat() - 0.5f) * 0.015f

                        // Combine and apply soft envelope
                        val combined = ((fundamental + harmonic2 + harmonic3) * 0.45 * flutter) + rainCrackle + ambientHiss

                        // Scale to 16-bit PCM Short
                        val sampleVal = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                        buffer[i] = sampleVal
                    }

                    track.write(buffer, 0, buffer.size)
                    sampleCounter += buffer.size
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        }
    }

    private fun stopSynthesizer() {
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }

    fun release() {
        stopSynthesizer()
    }
}
