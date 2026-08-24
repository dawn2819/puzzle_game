package com.example.puzzlegame.audio

import android.content.Context
import android.media.AudioManager as AndroidAudioManager
import android.media.ToneGenerator

class AudioManager(context: Context) {
    private val androidAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AndroidAudioManager
    private var toneGenerator: ToneGenerator? = null
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            initToneGenerator()
        }

    init {
        initToneGenerator()
    }

    private fun initToneGenerator() {
        try {
            toneGenerator?.release()
            // Ánh xạ âm lượng (0 - 100) cho ToneGenerator
            val toneVolume = (volume * 100).toInt()
            toneGenerator = ToneGenerator(AndroidAudioManager.STREAM_MUSIC, toneVolume)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playClick() {
        if (volume <= 0f) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playSuccess() {
        if (volume <= 0f) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playError() {
        if (volume <= 0f) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
