package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Global Audio Manager for Eazy Solve.
 * Provides synthesized background music (ambient zen chords) and sound effects
 * with real-time volume controls for SFX and Music backed by DataStore.
 */
object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 22050

    var soundEnabled: Boolean = true
        set(value) {
            field = value
        }

    var soundVolume: Float = 0.8f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
        }

    var musicEnabled: Boolean = true
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                if (value && isAppForeground) {
                    startBackgroundMusic()
                } else {
                    stopBackgroundMusic()
                }
            }
        }

    var musicVolume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            updateMusicTrackVolume()
        }

    private var isAppForeground: Boolean = true
    private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Background Music engine
    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null
    private var isBgmPlaying: Boolean = false

    fun updateSettings(
        soundEnabled: Boolean,
        soundVolume: Float,
        musicEnabled: Boolean,
        musicVolume: Float
    ) {
        this.soundEnabled = soundEnabled
        this.soundVolume = soundVolume
        this.musicVolume = musicVolume
        this.musicEnabled = musicEnabled
    }

    fun onAppResume() {
        isAppForeground = true
        if (musicEnabled) {
            startBackgroundMusic()
        }
    }

    fun onAppPause() {
        isAppForeground = false
        stopBackgroundMusic()
    }

    private fun updateMusicTrackVolume() {
        try {
            bgmTrack?.setVolume(musicVolume * 0.4f)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting music track volume", e)
        }
    }

    // --- Background Music (Zen Ambient Chord Progression) ---

    @Synchronized
    fun startBackgroundMusic() {
        if (!musicEnabled || !isAppForeground || isBgmPlaying) return
        isBgmPlaying = true

        bgmJob?.cancel()
        bgmJob = audioScope.launch {
            try {
                // Chord progression frequencies (Hz):
                // Cmaj7 (C4, E4, G4, B4), Fmaj7 (F3, A3, C4, E4), Am7 (A3, C4, E4, G4), Gsus4 (G3, C4, D4, G4)
                val progressions = listOf(
                    listOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
                    listOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
                    listOf(220.00, 261.63, 329.63, 392.00), // Am7
                    listOf(196.00, 261.63, 293.66, 392.00)  // Gsus4
                )

                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(SAMPLE_RATE * 2)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack(
                    audioAttributes,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                bgmTrack = track
                track.setVolume(musicVolume * 0.4f)
                track.play()

                val chordDurationSec = 3.5
                val chordSamples = (SAMPLE_RATE * chordDurationSec).toInt()
                var chordIdx = 0

                while (isActive && isBgmPlaying && musicEnabled) {
                    val currentChord = progressions[chordIdx % progressions.size]
                    chordIdx++

                    val buffer = ShortArray(chordSamples)
                    for (i in 0 until chordSamples) {
                        val t = i.toDouble() / SAMPLE_RATE
                        // Envelope: gentle attack, sustain, soft decay
                        val attackTime = 0.6
                        val releaseTime = 0.8
                        val env = when {
                            t < attackTime -> (t / attackTime)
                            t > chordDurationSec - releaseTime -> ((chordDurationSec - t) / releaseTime).coerceAtLeast(0.0)
                            else -> 1.0
                        }

                        // Warm pad synthesis with fundamental + warm overtone
                        var wave = 0.0
                        for (freq in currentChord) {
                            wave += 0.4 * sin(2.0 * PI * freq * t)
                            wave += 0.15 * sin(2.0 * PI * (freq * 2.0) * t) // octave harmonic
                            wave += 0.05 * sin(2.0 * PI * (freq * 0.5) * t) // sub harmonic
                        }
                        wave = (wave / currentChord.size) * env

                        // Soft saturation
                        val sample = (wave * 0.8 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        buffer[i] = sample.toShort()
                    }

                    if (isActive && isBgmPlaying) {
                        track.write(buffer, 0, buffer.size)
                    }
                    delay(50)
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.d(TAG, "BGM loop completed or cancelled: ${e.message}")
            } finally {
                bgmTrack = null
                isBgmPlaying = false
            }
        }
    }

    @Synchronized
    fun stopBackgroundMusic() {
        isBgmPlaying = false
        bgmJob?.cancel()
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
        } catch (e: Exception) {
            // Safe release
        }
        bgmTrack = null
    }

    // --- Sound Effects Synthesis ---

    fun playTap() {
        if (!soundEnabled || soundVolume <= 0f) return
        playSynthesizedTone(durationSec = 0.04) { t ->
            val env = exp(-t * 80.0)
            sin(2.0 * PI * 800.0 * t) * env
        }
    }

    fun playBubblePop() {
        if (!soundEnabled || soundVolume <= 0f) return
        playSynthesizedTone(durationSec = 0.08) { t ->
            val env = exp(-t * 40.0)
            val freq = 280.0 + (t / 0.08) * 600.0 // Rising frequency pop
            sin(2.0 * PI * freq * t) * env
        }
    }

    fun playMatchSuccess() {
        if (!soundEnabled || soundVolume <= 0f) return
        // Ascending harmonic triad (C5 - E5 - G5)
        playSynthesizedTone(durationSec = 0.28) { t ->
            val env = exp(-t * 12.0)
            val note = when {
                t < 0.08 -> 523.25 // C5
                t < 0.16 -> 659.25 // E5
                else -> 783.99     // G5
            }
            (sin(2.0 * PI * note * t) + 0.3 * sin(2.0 * PI * note * 2.0 * t)) * env
        }
    }

    fun playLevelComplete() {
        if (!soundEnabled || soundVolume <= 0f) return
        // Joyful fanfare arpeggio (C5 -> E5 -> G5 -> C6)
        playSynthesizedTone(durationSec = 0.55) { t ->
            val env = exp(-t * 5.0)
            val note = when {
                t < 0.10 -> 523.25 // C5
                t < 0.20 -> 659.25 // E5
                t < 0.30 -> 783.99 // G5
                else -> 1046.50    // C6
            }
            (sin(2.0 * PI * note * t) + 0.4 * sin(2.0 * PI * note * 2.0 * t)) * env
        }
    }

    fun playWrongMove() {
        if (!soundEnabled || soundVolume <= 0f) return
        // Soft double buzz
        playSynthesizedTone(durationSec = 0.14) { t ->
            val env = exp(-t * 22.0)
            val freq = if (t < 0.07) 180.0 else 140.0
            (sin(2.0 * PI * freq * t) + 0.5 * sin(2.0 * PI * freq * 1.5 * t)) * env
        }
    }

    fun playHint() {
        if (!soundEnabled || soundVolume <= 0f) return
        // Sparkling shimmer (C6 -> E6 -> G6 -> B6)
        playSynthesizedTone(durationSec = 0.32) { t ->
            val env = exp(-t * 10.0)
            val note = when {
                t < 0.07 -> 1046.50 // C6
                t < 0.14 -> 1318.51 // E6
                t < 0.21 -> 1567.98 // G6
                else -> 1975.53     // B6
            }
            (sin(2.0 * PI * note * t) + 0.3 * sin(2.0 * PI * note * 2.0 * t)) * env
        }
    }

    fun playCoinReward() {
        if (!soundEnabled || soundVolume <= 0f) return
        // Bright metallic bell ring (B5 -> E6 double ping)
        playSynthesizedTone(durationSec = 0.25) { t ->
            val env = exp(-t * 14.0)
            val note = if (t < 0.08) 987.77 else 1318.51
            (sin(2.0 * PI * note * t) + 0.6 * sin(2.0 * PI * (note * 2.5) * t)) * env
        }
    }

    private fun playSynthesizedTone(durationSec: Double, generator: (Double) -> Double) {
        audioScope.launch {
            try {
                val totalSamples = (SAMPLE_RATE * durationSec).toInt()
                val buffer = ShortArray(totalSamples)
                val effectiveVol = soundVolume * 0.9f

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val wave = generator(t) * effectiveVol
                    val sample = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = sample.toShort()
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack(
                    audioAttributes,
                    audioFormat,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                track.write(buffer, 0, buffer.size)
                track.play()

                delay((durationSec * 1000).toLong() + 50)
                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.d(TAG, "SFX playback failed: ${e.message}")
            }
        }
    }
}
