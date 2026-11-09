package pt.upca.vendanzacare.dados

import android.content.Context

/**
 * Local preferences: text size, language, and the person's own name and date of birth.
 *
 * The name and birthdate live here because they make the app useful at an appointment — "this is Maria, born
 * 12/03/1948, and this is her list" — and **only** here: no server, no cloud backup, no analytics. That keeps
 * the client's v1 rule intact: nobody collects personal data, the phone just remembers it.
 */
class Preferencias(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("vendanzacare_prefs", Context.MODE_PRIVATE)

    var escalaTexto: Float
        get() = prefs.getFloat(CHAVE_ESCALA, 1f)
        set(valor) = prefs.edit().putFloat(CHAVE_ESCALA, valor).apply()

    /** "pt" or "en". Only the interface follows it — see [pt.upca.vendanzacare.ui.Textos]. */
    var idioma: String
        get() = prefs.getString(CHAVE_IDIOMA, "pt") ?: "pt"
        set(valor) = prefs.edit().putString(CHAVE_IDIOMA, valor).apply()

    var nome: String
        get() = prefs.getString(CHAVE_NOME, "").orEmpty()
        set(valor) = prefs.edit().putString(CHAVE_NOME, valor.trim()).apply()

    /** ISO `yyyy-mm-dd`, or empty. Read once per appointment, never queried — text is enough. */
    var nascimento: String
        get() = prefs.getString(CHAVE_NASCIMENTO, "").orEmpty()
        set(valor) = prefs.edit().putString(CHAVE_NASCIMENTO, valor).apply()

    fun perguntasEscolhidas(): Set<String> = prefs.getStringSet(CHAVE_PERGUNTAS, emptySet()) ?: emptySet()

    fun guardarPerguntas(ids: Set<String>) {
        // a fresh set: SharedPreferences does not copy it, and mutating the returned one is undefined
        prefs.edit().putStringSet(CHAVE_PERGUNTAS, HashSet(ids)).apply()
    }

    private companion object {
        const val CHAVE_ESCALA = "escala_texto"
        const val CHAVE_IDIOMA = "idioma"
        const val CHAVE_NOME = "nome"
        const val CHAVE_NASCIMENTO = "nascimento"
        const val CHAVE_PERGUNTAS = "perguntas"
    }
}
