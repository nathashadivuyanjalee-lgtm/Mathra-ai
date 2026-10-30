package com.foss.aihub.ui.mathra

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Text-to-speech wrapper: reads Sinhala with the Sinhala voice and English with the English voice. */
class MathraVoice(context: Context) {
    private var tts: TextToSpeech? = null
    private val main = Handler(Looper.getMainLooper())
    private val queue = ArrayDeque<Pair<Boolean, String>>()
    private val sinhalaLocale = Locale("si", "LK")

    var ready by mutableStateOf(false)
        private set
    var speaking by mutableStateOf(false)
        private set

    /** true when the phone has no Sinhala voice installed (user can install it from the banner). */
    var sinhalaMissing by mutableStateOf(false)

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        main.post { speaking = true }
                    }

                    override fun onDone(utteranceId: String?) {
                        main.post { next() }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        main.post { next() }
                    }
                })
                ready = true
                sinhalaMissing = !isAvailable(sinhalaLocale)
            }
        }
    }

    private fun isAvailable(locale: Locale): Boolean {
        val r = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        return r >= TextToSpeech.LANG_AVAILABLE
    }

    fun speak(raw: String) {
        if (!ready) return
        stop()
        val clean = MathraBot.cleanForSpeech(raw)
        if (clean.isBlank()) return
        queue.addAll(MathraBot.splitByScript(clean))
        next()
    }

    private fun next() {
        val t = tts ?: return
        val item = queue.removeFirstOrNull()
        if (item == null) {
            speaking = false
            return
        }
        val (sinhala, text) = item
        val res = t.setLanguage(if (sinhala) sinhalaLocale else Locale.US)
        if (res < TextToSpeech.LANG_AVAILABLE) {
            if (sinhala) sinhalaMissing = true
            next()
            return
        }
        if (sinhala) sinhalaMissing = false
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mathra_" + System.nanoTime())
    }

    fun stop() {
        queue.clear()
        tts?.stop()
        speaking = false
    }

    fun shutdown() {
        queue.clear()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
