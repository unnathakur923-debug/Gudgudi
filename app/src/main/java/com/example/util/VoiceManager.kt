package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    var isMuted: Boolean = false
    private var isReady: Boolean = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val hindiLocale = Locale("hi", "IN")
            val result = tts?.setLanguage(hindiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallback = tts?.setLanguage(Locale("hi"))
                if (fallback == TextToSpeech.LANG_MISSING_DATA || fallback == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
            }
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.02f)
            isReady = true
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (isMuted || !isReady) return
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "Gudgudi_Hindi_TTS_${System.currentTimeMillis()}")
    }

    private fun getHindiNumberWord(num: Int): String {
        return when (num) {
            1 -> "ek"
            2 -> "do"
            3 -> "teen"
            4 -> "char"
            5 -> "paanch"
            6 -> "chhe"
            else -> "$num"
        }
    }

    /**
     * Plays acoustic dice tumbling/rattle sound on casino table felt.
     */
    fun playDiceTumbleSound() {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 22050
                val durationMs = 650
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                val impactPoints = listOf(0, 70, 150, 245, 350, 450, 540, 600)
                for (impactMs in impactPoints) {
                    val startIdx = (impactMs * sampleRate) / 1000
                    val impactSamples = (sampleRate * 0.035).toInt()
                    val freq = 680.0 + (impactPoints.indexOf(impactMs) * 110.0)
                    for (i in 0 until impactSamples) {
                        val idx = startIdx + i
                        if (idx < numSamples) {
                            val decay = exp(-i.toDouble() / (sampleRate * 0.007))
                            val sample = sin(2.0 * PI * i * freq / sampleRate) * decay
                            buffer[idx] = (sample * Short.MAX_VALUE * 0.85).toInt().toShort()
                        }
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(durationMs.toLong() + 50L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Plays authentic crowd clapping / applause sound bursts.
     */
    fun playApplauseSound() {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 22050
                val durationMs = 1400
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                val rng = Random()
                val totalClaps = 85
                for (c in 0 until totalClaps) {
                    val clapTimeMs = rng.nextInt(durationMs - 60)
                    val startIdx = (clapTimeMs * sampleRate) / 1000
                    val clapDuration = (sampleRate * 0.025).toInt()
                    val freq = 1200.0 + rng.nextInt(750)
                    for (i in 0 until clapDuration) {
                        val idx = startIdx + i
                        if (idx < numSamples) {
                            val decay = exp(-i.toDouble() / (sampleRate * 0.005))
                            val noise = (rng.nextDouble() * 2.0 - 1.0) * 0.65
                            val tone = sin(2.0 * PI * i * freq / sampleRate) * 0.35
                            val sample = (noise + tone) * decay
                            val current = buffer[idx].toInt()
                            val combined = current + (sample * Short.MAX_VALUE * 0.32).toInt()
                            buffer[idx] = combined.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(durationMs.toLong() + 50L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Hindi voice announcements for all game phases.
     */
    fun speakBettingOpen(seconds: Int = 30) {
        speak("Bazi shuru ho chuki hai! $seconds second bache hain, apna lucky number chuniye!")
    }

    fun speakDiceRolling() {
        playDiceTumbleSound()
        speak("Dice ghoom raha hai! Kismat ka pansa palatne wala hai!")
    }

    fun speakDiceResultWithAccounting(winningNumber: Int, isUserWin: Boolean, rewardCoins: Long) {
        playApplauseSound()
        val numWord = getHindiNumberWord(winningNumber)
        val resultAnnouncement = if (isUserWin) {
            "Taaliyan! Badhai ho, aap jeet gaye! Pansa number $numWord par ruka hai! 15 pratishat admin commission katne ke baad aapko $rewardCoins coins mile hain!"
        } else {
            "Pansa number $numWord par ruka hai! 15 pratishat admin commission katne ke baad hisab screen par dikha diya gaya hai."
        }
        speak(resultAnnouncement)
    }

    fun speakDiceNumberSelected(number: Int) {
        val numWord = getHindiNumberWord(number)
        speak("Aapne number $numWord chuna hai.")
    }

    fun speakTimeWarning(seconds: Int) {
        when (seconds) {
            15 -> speak("Pandrah second bache hain!")
            5 -> speak("Paanch second bache hain, jaldi apna number chuniye!")
            else -> speak("$seconds second bache hain!")
        }
    }

    fun speakCheer(type: String) {
        when (type) {
            "applause" -> {
                playApplauseSound()
                speak("Zordaar taaliyan! Shandar jeet!")
            }
            "gudgudi" -> speak("Gudgudi Gaming Zindabad! Shandar khel!")
            "shabaash" -> speak("Shabaash! Agla round shuru hone wala hai!")
            "status" -> speak("Table par bazi lagi hui hai!")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
