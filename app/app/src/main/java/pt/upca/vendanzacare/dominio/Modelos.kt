package pt.upca.vendanzacare.dominio

/** One block of a module: a title, a short text, and up to a few bullets. */
data class Seccao(
    val titulo: String,
    val texto: String,
    val pontos: List<String> = emptyList(),
    /** Rendered as an alert box — reserved for "call 112" and "stop and ask" content. */
    val destaque: Boolean = false,
)

/** A thematic module. `id` is stable: it is the route argument and the content key. */
data class Modulo(
    val id: String,
    val titulo: String,
    val resumo: String,
    val icone: String,
    val seccoes: List<Seccao>,
    /** Optional extra action rendered at the end of the module — currently only "perguntas". */
    val accao: String? = null,
)

/** A question the person can take to an appointment. */
data class Pergunta(
    val id: String,
    val texto: String,
    val tema: String,
)

/**
 * One medicine in the local list.
 *
 * `horas` are minutes of the day (480 = 08:00) — already parsed and sorted, so no screen ever parses a string.
 * `foto` is a content:// URI of a private copy in the app's own folder (see `dados/Fotos.kt`); it never leaves
 * the phone. Entries saved before that may hold a gallery URI with a persisted grant, which still displays.
 */
data class Medicamento(
    val id: String,
    val nome: String,
    val dose: String = "",
    val horas: List<Int> = emptyList(),
    val nota: String = "",
    val foto: String? = null,
)

/** Two list entries that contain the same active ingredient; `principio` is normalised (see [Dicionario.legivel]). */
data class Sobreposicao(
    val a: Medicamento,
    val b: Medicamento,
    val principio: String,
)

/** The next dose to take, with `amanha` telling the caller which day it belongs to. */
data class Toma(
    val medicamento: Medicamento,
    val hora: Int,
    val amanha: Boolean = false,
)

/**
 * A number the person wants close at hand — the local fire brigade, a relative, the health centre. Stored only
 * on the phone, next to the fixed national numbers (112, SNS 24, Centro Antivenenos) that are not editable.
 */
data class Contacto(
    val id: String,
    val nome: String,
    val numero: String,
)
