package pt.upca.vendanzacare.dominio

/**
 * Brand name → active ingredient(s), for the "princípios ativos sobreponíveis" warning.
 *
 * Why it exists: people keep their list by the name on the box. D. Maria writes «Ben-u-ron», the hospital
 * writes «Paracetamol» — without this table the app saw two different medicines and stayed silent about a
 * double dose, which is the exact error the ULSBE brief names ("genéricos com nomes diferentes").
 *
 * **This is a seed, not a source.** It covers common brands on the Portuguese market so the rule can be tested
 * and demonstrated. The real table comes from Infarmed's medicine database and must be validated by ULSBE
 * before release (architecture.md §12). Keys are written as on the box; they are normalised on load.
 *
 * No Android import: used by [Regras] and exercised by JVM tests.
 */
object Dicionario {

    private val SEMENTE: Map<String, List<String>> = mapOf(
        "Ben-u-ron" to listOf("paracetamol"),
        "Panadol" to listOf("paracetamol"),
        "Dafalgan" to listOf("paracetamol"),
        "Brufen" to listOf("ibuprofeno"),
        "Nurofen" to listOf("ibuprofeno"),
        "Aspirina" to listOf("ácido acetilsalicílico"),
        "Cartia" to listOf("ácido acetilsalicílico"),
        "Tromalyt" to listOf("ácido acetilsalicílico"),
        "Glucophage" to listOf("metformina"),
        "Risidon" to listOf("metformina"),
        "Januvia" to listOf("sitagliptina"),
        "Janumet" to listOf("sitagliptina", "metformina"),
        "Galvus" to listOf("vildagliptina"),
        "Eucreas" to listOf("vildagliptina", "metformina"),
        "Lasix" to listOf("furosemida"),
        "Zyloric" to listOf("alopurinol"),
        "Zocor" to listOf("sinvastatina"),
        "Zarator" to listOf("atorvastatina"),
        "Lipitor" to listOf("atorvastatina"),
        "Crestor" to listOf("rosuvastatina"),
        "Concor" to listOf("bisoprolol"),
        "Norvasc" to listOf("amlodipina"),
        "Cozaar" to listOf("losartan"),
        "Diovan" to listOf("valsartan"),
        "Coversyl" to listOf("perindopril"),
        "Triatec" to listOf("ramipril"),
        "Lexotan" to listOf("bromazepam"),
        "Xanax" to listOf("alprazolam"),
        "Stilnox" to listOf("zolpidem"),
        "Voltaren" to listOf("diclofenac"),
        "Tramal" to listOf("tramadol"),
        "Zaldiar" to listOf("tramadol", "paracetamol"),
        "Losec" to listOf("omeprazol"),
        "Nexium" to listOf("esomeprazol"),
        "Eutirox" to listOf("levotiroxina"),
        "Xarelto" to listOf("rivaroxabano"),
        "Eliquis" to listOf("apixabano"),
        "Pradaxa" to listOf("dabigatrano"),
        "Varfine" to listOf("varfarina"),
        "Sintrom" to listOf("acenocumarol"),
        "Plavix" to listOf("clopidogrel"),
        "Lyrica" to listOf("pregabalina"),
        "Neurontin" to listOf("gabapentina"),
        "Aricept" to listOf("donepezilo"),
        "Ebixa" to listOf("memantina"),
        "Zoloft" to listOf("sertralina"),
        "Cipralex" to listOf("escitalopram"),
        "Seroquel" to listOf("quetiapina"),
        "Risperdal" to listOf("risperidona"),
        "Ventilan" to listOf("salbutamol"),
        "Fluimucil" to listOf("acetilcisteína"),
        "Clamoxyl" to listOf("amoxicilina"),
        "Augmentin" to listOf("amoxicilina", "ácido clavulânico"),
        "Nolotil" to listOf("metamizol"),
    )

    /** "benuron" → ["paracetamol"]; keys and values both normalised (lower case, no accents, no hyphens). */
    private val MARCAS: Map<String, List<String>> =
        SEMENTE.entries.associate { (marca, principios) -> chave(marca) to principios.map(Regras::normalizar) }

    /** Normalised ingredient → the spelling a person should read ("acido acetilsalicilico" → "ácido …"). */
    private val LEGIVEL: Map<String, String> =
        SEMENTE.values.flatten().associateBy(Regras::normalizar)

    /** Brand lookup on already-normalised words joined together: "ben u ron" and "ben-u-ron" both work. */
    fun marca(palavrasNormalizadas: List<String>): List<String>? {
        for (n in minOf(3, palavrasNormalizadas.size) downTo 1) {
            MARCAS[chave(palavrasNormalizadas.take(n).joinToString(""))]?.let { return it }
        }
        return null
    }

    /** The accented form for display, or the input unchanged when the table does not know it. */
    fun legivel(principioNormalizado: String): String = LEGIVEL[principioNormalizado] ?: principioNormalizado

    private fun chave(texto: String): String = Regras.normalizar(texto).filter { it.isLetterOrDigit() }
}
