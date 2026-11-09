package pt.upca.vendanzacare.dados

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import pt.upca.vendanzacare.dominio.Medicamento

/**
 * The medication list, stored as one JSON string in private SharedPreferences.
 *
 * Only this class knows the storage format. Swapping in Room later means rewriting this file and nothing else
 * — the screens talk in `List<Medicamento>`. Nothing here ever leaves the phone (no backup, no network).
 */
class MedicacaoStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("vendanzacare_medicacao", Context.MODE_PRIVATE)

    fun listar(): List<Medicamento> {
        val bruto = prefs.getString(CHAVE, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(bruto)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                val horas = o.optJSONArray("horas")
                Medicamento(
                    id = o.getString("id"),
                    nome = o.getString("nome"),
                    dose = o.optString("dose"),
                    horas = if (horas == null) emptyList() else (0 until horas.length()).map { horas.getInt(it) },
                    nota = o.optString("nota"),
                    foto = if (o.isNull("foto")) null else o.optString("foto").ifBlank { null },
                )
            }
        }.getOrElse { emptyList() }
    }

    fun guardar(medicamento: Medicamento) {
        val lista = listar().filterNot { it.id == medicamento.id } + medicamento
        gravar(lista)
    }

    fun apagar(id: String) = gravar(listar().filterNot { it.id == id })

    private fun gravar(lista: List<Medicamento>) {
        val array = JSONArray()
        for (m in lista) {
            array.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("nome", m.nome)
                    put("dose", m.dose)
                    put("horas", JSONArray(m.horas))
                    put("nota", m.nota)
                    put("foto", m.foto ?: JSONObject.NULL)
                },
            )
        }
        prefs.edit().putString(CHAVE, array.toString()).apply()
    }

    private companion object {
        const val CHAVE = "lista"
    }
}
