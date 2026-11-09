package pt.upca.vendanzacare.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * On-device text-to-speech. Offline, free, no audio files to ship — D. Maria's "áudio em todos os ecrãs"
 * without adding a megabyte of recordings that ULSBE has not supplied yet.
 *
 * Initialisation is asynchronous, so a request made before the engine is ready is queued, not dropped.
 */
class Leitor(context: Context) {

    private var motor: TextToSpeech? = null
    private var pendente: String? = null
    private var iniciado = false

    /** False when the device has no usable voice — the UI then says so instead of showing a dead button. */
    var disponivel by mutableStateOf(true)
        private set

    /**
     * Key of what is being read right now, or null. Lets the "Ouvir" button that started the reading turn into
     * "Parar" — an older person who cannot find how to stop the voice stops trusting the button.
     */
    var aFalar by mutableStateOf<String?>(null)
        private set

    private var pendenteChave: String? = null
    private val principal = Handler(Looper.getMainLooper())

    init {
        motor = TextToSpeech(
            context.applicationContext,
            TextToSpeech.OnInitListener { estado ->
                iniciado = true
                val tts = motor
                if (estado != TextToSpeech.SUCCESS || tts == null) {
                    disponivel = false
                } else {
                    val pt = tts.setLanguage(Locale.forLanguageTag("pt-PT"))
                    val falhouPt = pt == TextToSpeech.LANG_MISSING_DATA || pt == TextToSpeech.LANG_NOT_SUPPORTED
                    disponivel = if (falhouPt) {
                        // no European Portuguese voice installed: read with the system voice rather than stay silent
                        val sistema = tts.setLanguage(Locale.getDefault())
                        sistema != TextToSpeech.LANG_MISSING_DATA &&
                            sistema != TextToSpeech.LANG_NOT_SUPPORTED
                    } else {
                        true
                    }
                    tts.setSpeechRate(0.92f)
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) = Unit
                        override fun onDone(id: String?) = terminou(id)
                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?) = terminou(id)
                        override fun onStop(id: String?, interrompido: Boolean) = terminou(id)
                    })
                    val texto = pendente
                    val chave = pendenteChave
                    pendente = null
                    pendenteChave = null
                    if (texto != null) falar(texto, chave ?: texto)
                }
            },
        )
    }

    /** @param chave identifies the button that asked, so only that one shows "Parar". */
    fun falar(texto: String, chave: String = texto) {
        if (texto.isBlank()) return
        val tts = motor
        aFalar = chave
        if (tts == null || !iniciado) {
            pendente = texto
            pendenteChave = chave
            return
        }
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, chave)
    }

    fun parar() {
        pendente = null
        pendenteChave = null
        aFalar = null
        motor?.stop()
    }

    // progress callbacks arrive on the TTS binder thread; Compose state is written on the main thread
    private fun terminou(id: String?) {
        principal.post { if (aFalar == id) aFalar = null }
    }

    fun fechar() {
        pendente = null
        aFalar = null
        motor?.stop()
        motor?.shutdown()
        motor = null
    }
}

val LocalLeitor = staticCompositionLocalOf<Leitor> { error("Leitor não fornecido") }

@Composable
fun rememberLeitor(): Leitor {
    val contexto = LocalContext.current
    val leitor = remember { Leitor(contexto) }
    DisposableEffect(leitor) {
        onDispose { leitor.fechar() }
    }
    return leitor
}
