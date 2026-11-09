package pt.upca.vendanzacare.dados

import android.content.Context
import org.json.JSONObject
import pt.upca.vendanzacare.dominio.Modulo
import pt.upca.vendanzacare.dominio.Pergunta
import pt.upca.vendanzacare.dominio.Seccao

/** Everything the app knows from the bundled files. Read once, at start. */
data class ListaConteudo(
    val modulos: List<Modulo> = emptyList(),
    val perguntas: List<Pergunta> = emptyList(),
) {
    fun modulo(id: String): Modulo? = modulos.firstOrNull { it.id == id }
}

/**
 * Reads `assets/conteudo.json` and `assets/perguntas.json`.
 *
 * A broken file must not crash the app on a patient's phone: the affected list comes back empty and the
 * screen says so. It still fails loudly in CI, because the build's unit tests assert the real files parse.
 */
object Conteudo {

    fun ler(context: Context): ListaConteudo = ListaConteudo(
        modulos = runCatching { lerModulos(texto(context, "conteudo.json")) }.getOrElse { emptyList() },
        perguntas = runCatching { lerPerguntas(texto(context, "perguntas.json")) }.getOrElse { emptyList() },
    )

    private fun texto(context: Context, nome: String): String =
        context.assets.open(nome).bufferedReader().use { it.readText() }

    private fun lerModulos(json: String): List<Modulo> {
        val lista = JSONObject(json).optJSONArray("modulos") ?: return emptyList()
        return (0 until lista.length()).map { i ->
            val o = lista.getJSONObject(i)
            val seccoes = o.optJSONArray("seccoes")
            Modulo(
                id = o.getString("id"),
                titulo = o.getString("titulo"),
                resumo = o.optString("resumo"),
                icone = o.optString("icone", "info"),
                accao = o.optString("accao").ifBlank { null },
                seccoes = if (seccoes == null) emptyList() else (0 until seccoes.length()).map { j ->
                    val s = seccoes.getJSONObject(j)
                    val pontos = s.optJSONArray("pontos")
                    Seccao(
                        titulo = s.getString("titulo"),
                        texto = s.optString("texto"),
                        pontos = if (pontos == null) emptyList() else (0 until pontos.length()).map { pontos.getString(it) },
                        destaque = s.optBoolean("destaque", false),
                    )
                },
            )
        }
    }

    private fun lerPerguntas(json: String): List<Pergunta> {
        val lista = JSONObject(json).optJSONArray("perguntas") ?: return emptyList()
        return (0 until lista.length()).map { i ->
            val o = lista.getJSONObject(i)
            Pergunta(
                id = o.getString("id"),
                texto = o.getString("texto"),
                tema = o.optString("tema", "Geral"),
            )
        }
    }
}
