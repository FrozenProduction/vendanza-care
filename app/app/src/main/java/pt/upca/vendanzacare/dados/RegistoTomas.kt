package pt.upca.vendanzacare.dados

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Which doses were marked taken, per day — by "Já tomei" on the notification or a tap on the home screen.
 *
 * One string set per day (`2026-11-09` → `{"<id>@480", …}`, keys from `Regras.chaveToma`), private
 * SharedPreferences, nothing leaves the phone. Only the last [DIAS_GUARDADOS] days are kept: this is a
 * "did I already take it?" aid, not a clinical adherence record, and it should not grow into one silently.
 */
class RegistoTomas(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FICHEIRO, Context.MODE_PRIVATE)

    fun tomadas(dia: String): Set<String> = HashSet(prefs.getStringSet(dia, emptySet()).orEmpty())

    fun marcar(dia: String, chaves: Collection<String>, tomada: Boolean) {
        val atual = tomadas(dia)
        val novo = if (tomada) atual + chaves else atual - chaves.toSet()
        val edicao = prefs.edit()
        // ISO dates sort as text, so "older than the cut-off" is a string comparison
        val corte = dia(-DIAS_GUARDADOS)
        prefs.all.keys.filter { it < corte }.forEach { edicao.remove(it) }
        if (novo.isEmpty()) edicao.remove(dia) else edicao.putStringSet(dia, HashSet(novo))
        edicao.apply()
    }

    /**
     * Calls [aoMudar] on the main thread whenever a mark changes, including from the notification action while
     * the app is open. Returns the function that stops listening. SharedPreferences keeps listeners weakly,
     * so the returned closure is also what keeps this one alive.
     */
    fun observar(aoMudar: () -> Unit): () -> Unit {
        val ouvinte = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> aoMudar() }
        prefs.registerOnSharedPreferenceChangeListener(ouvinte)
        return { prefs.unregisterOnSharedPreferenceChangeListener(ouvinte) }
    }

    companion object {
        private const val FICHEIRO = "vendanzacare_tomas"
        private const val DIAS_GUARDADOS = 14

        /** Today, or [deslocamento] days from it, as `yyyy-MM-dd` in the phone's own time zone (as the alarms are). */
        fun dia(deslocamento: Int = 0): String {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, deslocamento) }
            return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(c.time)
        }
    }
}
