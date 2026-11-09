package pt.upca.vendanzacare.dominio

import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Every decision the app makes about a patient's data lives here, as pure functions.
 *
 * No Android import on purpose: this file is exercised by JVM tests in `app/src/test` that run in the same
 * Gradle build as the APK. A bug here shows a wrong medicine at the wrong time — it is the part that has to
 * be tested, not the part that looks nice.
 */
object Regras {

    const val MINUTOS_NO_DIA = 24 * 60

    // ---------------------------------------------------------------- horas

    /** Result of reading what someone typed into the time field. */
    data class HorasLidas(val horas: List<Int>, val invalidos: List<String>) {
        val tudoValido: Boolean get() = invalidos.isEmpty() && horas.isNotEmpty()
    }

    /** Accepts `8`, `08:00`, `8h`, `8h30`, `14.30`, `0800`. Returns minutes of day, or null. */
    fun minutosDoDia(token: String): Int? {
        var t = token.trim().lowercase(Locale.ROOT)
        if (t.isEmpty()) return null
        if (t.endsWith("h")) t = t.dropLast(1)
        // "8h30" is how people write eight-thirty here; without this line the reminder would vanish silently
        t = t.replace('h', ':').replace('.', ':')
        if (t.isEmpty()) return null

        val partes = t.split(':')
        if (partes.size == 1) {
            val d = partes[0]
            if (d.isEmpty() || !d.all { it.isDigit() }) return null
            return if (d.length == 4) {
                val h = d.substring(0, 2).toIntOrNull() ?: return null
                val m = d.substring(2, 4).toIntOrNull() ?: return null
                valida(h, m)
            } else {
                valida(d.toIntOrNull() ?: return null, 0)
            }
        }
        if (partes.size != 2) return null
        val hs = partes[0]
        val ms = partes[1]
        if (hs.isEmpty() || ms.isEmpty()) return null
        if (!hs.all { it.isDigit() } || !ms.all { it.isDigit() }) return null
        return valida(hs.toIntOrNull() ?: return null, ms.toIntOrNull() ?: return null)
    }

    private fun valida(h: Int, m: Int): Int? =
        if (h in 0..23 && m in 0..59) h * 60 + m else null

    /**
     * Reads a whole field: `"08:00, 14:00"` → `[480, 840]`.
     *
     * Invalid tokens are returned instead of being dropped silently — the form has to be able to say
     * "«tomar» não é uma hora" rather than quietly saving a medicine with no reminder.
     */
    fun lerHoras(entrada: String): HorasLidas {
        val tokens = entrada.split(',', ';', '\n', '\t', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val horas = mutableListOf<Int>()
        val invalidos = mutableListOf<String>()
        for (t in tokens) {
            val m = minutosDoDia(t)
            if (m == null) invalidos += t else horas += m
        }
        return HorasLidas(horas.distinct().sorted(), invalidos)
    }

    fun horaCurta(minuto: Int): String {
        val h = (minuto / 60) % 24
        val m = minuto % 60
        return String.format(Locale.ROOT, "%02d:%02d", h, m)
    }

    /** Minutes from now until [hora]; wraps to tomorrow when the hour has already passed. */
    fun minutosAte(minutoAgora: Int, hora: Int): Int =
        if (hora >= minutoAgora) hora - minutoAgora else hora + MINUTOS_NO_DIA - minutoAgora

    /**
     * The next dose from now on. Doses already marked taken today ([tomadas], keys from [chaveToma]) are
     * skipped — someone who took the 13:00 pill early is asked about 20:00, not 13:00. Tomorrow starts clean.
     */
    fun proximaToma(medicamentos: List<Medicamento>, minutoAgora: Int, tomadas: Set<String> = emptySet()): Toma? {
        val candidatos = medicamentos.flatMap { m -> m.horas.map { h -> Toma(m, h) } }
        if (candidatos.isEmpty()) return null
        val hoje = candidatos
            .filter { it.hora >= minutoAgora && chaveToma(it.medicamento.id, it.hora) !in tomadas }
            .minByOrNull { it.hora }
        if (hoje != null) return hoje
        return candidatos.minByOrNull { it.hora }?.copy(amanha = true)
    }

    // ---------------------------------------------------------------- tomas registadas

    /** How long a passed dose keeps asking "já tomou?" on the home screen before it becomes just history. */
    const val JANELA_EM_FALTA = 180

    /** One dose of one medicine on one day, as stored by `RegistoTomas`. The day is the key around it. */
    fun chaveToma(idMedicamento: String, hora: Int): String = "$idMedicamento@$hora"

    /**
     * The earliest dose time in the last [janela] minutes that still has something not marked taken, with the
     * medicines missing at that time. Null when everything due recently is ticked off (or nothing was due).
     * Only today: a 23:30 dose is not asked about at 00:15.
     */
    fun emFalta(
        medicamentos: List<Medicamento>,
        minutoAgora: Int,
        tomadas: Set<String>,
        janela: Int = JANELA_EM_FALTA,
    ): Pair<Int, List<Medicamento>>? =
        medicamentos
            .flatMap { m -> m.horas.map { h -> h to m } }
            .filter { (h, m) -> h <= minutoAgora && minutoAgora - h <= janela && chaveToma(m.id, h) !in tomadas }
            .groupBy({ it.first }, { it.second })
            .minByOrNull { it.key }
            ?.let { it.key to it.value }

    /**
     * Which day an alarm for [hora] belongs to, as an offset from today: 0, or -1 when it rang after midnight
     * for last night's dose (alarms are inexact and only ever late, so "now earlier than the dose" means the
     * day turned while it waited).
     */
    fun diaDoAlarme(minutoAgora: Int, hora: Int): Int = if (minutoAgora < hora) -1 else 0

    // ---------------------------------------------------------------- contactos

    /** What gets dialled: digits and a leading +, nothing else ("253 000 000" → "253000000"). */
    fun numeroParaMarcar(numero: String): String {
        val limpo = numero.trim().filter { it.isDigit() || it == '+' }
        return if (limpo.startsWith('+')) "+" + limpo.drop(1).replace("+", "") else limpo.replace("+", "")
    }

    /** At least three digits: 112 is the shortest number anyone needs. */
    fun numeroValido(numero: String): Boolean = numeroParaMarcar(numero).count { it.isDigit() } >= 3

    // ------------------------------------------------------- princípios ativos

    /** A dose or a unit: "500", "1g", "2,5mg", "mg", "comprimidos". Everything from here on is not the name. */
    private val DOSE = Regex("^[0-9]+([.,][0-9]+)?(mg|g|mcg|ug|ml|ui|%)?$")
    private val UNIDADES = setOf(
        "mg", "g", "mcg", "ug", "ml", "ui", "comp", "comprimido", "comprimidos", "capsula", "capsulas",
        "gotas", "saqueta", "saquetas", "xarope", "injetavel",
    )

    /** Salt and filler words: "Cloridrato de metformina" is metformina. */
    private val SAIS = setOf(
        "de", "do", "da", "cloridrato", "dicloridrato", "bromidrato", "sulfato", "maleato", "besilato",
        "tartarato", "fumarato", "succinato", "sodico", "potassico", "calcico", "magnesico",
    )

    /**
     * The active ingredients behind what someone typed, normalised (lower case, no accents).
     *
     * - `"Metformina 500 mg"` → `["metformina"]` — the name stops at the dose.
     * - `"Ácido fólico 5 mg"` → `["acido folico"]` — **all** the name words, not just the first: the old
     *   first-word rule made ácido fólico and ácido acetilsalicílico "the same medicine".
     * - `"Ben-u-ron 500 mg"` → `["paracetamol"]` — brand names go through [Dicionario].
     * - `"Metformina + Sitagliptina"` → both.
     */
    fun principiosAtivos(nome: String): List<String> =
        nome.split('+', '/', ',', ';')
            .flatMap { parte -> principiosDaParte(parte) }
            .distinct()

    private fun principiosDaParte(parte: String): List<String> {
        val palavras = normalizar(parte)
            .replace(Regex("[^a-z0-9%\\s-]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
        if (palavras.isEmpty()) return emptyList()

        Dicionario.marca(palavras)?.let { return it }

        val nome = palavras
            .takeWhile { !DOSE.matches(it) && it !in UNIDADES }
            .filter { p -> p.any { it.isLetter() } && p !in SAIS }
        return if (nome.isEmpty()) emptyList() else listOf(nome.joinToString(" "))
    }

    /**
     * Same ingredient when one name is the start of the other, word by word: "paracetamol" matches
     * "paracetamol generis" (a lab name after the substance is how generics are sold here), but "acido folico"
     * never matches "acido acetilsalicilico". Returns the shorter name — the ingredient itself — or null.
     */
    private fun principioComum(x: String, y: String): String? {
        val a = x.split(' ')
        val b = y.split(' ')
        val (curto, longo) = if (a.size <= b.size) a to b else b to a
        return if (longo.take(curto.size) == curto) curto.joinToString(" ") else null
    }

    /** Lower case, accents removed: "Ácido Fólico" → "acido folico". Shared with [Dicionario]. */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    /**
     * Pairs of entries that share an active ingredient — the "aviso de princípios ativos sobreponíveis" the
     * brief asks for — with the ingredient they share. Two entries of the same drug count, on purpose: that is
     * a double dose waiting to happen.
     */
    fun sobreponiveis(medicamentos: List<Medicamento>): List<Sobreposicao> {
        val principios = medicamentos.map { principiosAtivos(it.nome) }
        val resultado = mutableListOf<Sobreposicao>()
        for (i in medicamentos.indices) {
            for (j in i + 1 until medicamentos.size) {
                val comum = principios[i].firstNotNullOfOrNull { p ->
                    principios[j].firstNotNullOfOrNull { q -> principioComum(p, q) }
                } ?: continue
                resultado += Sobreposicao(medicamentos[i], medicamentos[j], comum)
            }
        }
        return resultado
    }

    /** The list as plain text, for ACTION_SEND to a professional. */
    fun textoDaLista(medicamentos: List<Medicamento>, cabecalho: String = "A minha medicação"): String {
        if (medicamentos.isEmpty()) return ""
        val sb = StringBuilder(cabecalho.trim()).append("\n\n")
        for (m in medicamentos.sortedBy { normalizar(it.nome) }) {
            sb.append("• ").append(m.nome)
            if (m.dose.isNotBlank()) sb.append(" — ").append(m.dose)
            if (m.horas.isNotEmpty()) sb.append(" — ").append(m.horas.joinToString(", ") { horaCurta(it) })
            if (m.nota.isNotBlank()) sb.append(" — ").append(m.nota)
            sb.append('\n')
        }
        return sb.toString().trim()
    }

    /** "Segunda-feira, 26 de outubro" — Lisbon time, European date order, interface language. */
    fun dataDeHoje(millis: Long, locale: String = "pt-PT", zona: String = "Europe/Lisbon"): String {
        val formato = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag(locale))
        formato.timeZone = java.util.TimeZone.getTimeZone(zona)
        return formato.format(Date(millis)).replaceFirstChar { it.uppercase(Locale.forLanguageTag(locale)) }
    }

    /**
     * ISO `yyyy-mm-dd` → `dd/MM/yyyy`. Stored as ISO so it sorts and survives any locale; shown European
     * because that is what a Portuguese professional reads on a printed list.
     */
    fun dataCurta(iso: String): String = runCatching {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
            // strict: a lenient parser turns "1948-13-45" into a real date instead of failing, and this
            // string ends up printed on a list a professional reads
            isLenient = false
        }
        val saida = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        saida.format(entrada.parse(iso) ?: return iso)
    }.getOrDefault(iso)
}
