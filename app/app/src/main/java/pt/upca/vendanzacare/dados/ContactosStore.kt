package pt.upca.vendanzacare.dados

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import pt.upca.vendanzacare.dominio.Contacto

/**
 * The person's own emergency contacts, as one JSON string in private SharedPreferences — same shape and same
 * rules as [MedicacaoStore]: no backup, no network, in the order they were added.
 */
class ContactosStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("vendanzacare_contactos", Context.MODE_PRIVATE)

    fun listar(): List<Contacto> {
        val bruto = prefs.getString(CHAVE, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(bruto)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Contacto(id = o.getString("id"), nome = o.getString("nome"), numero = o.getString("numero"))
            }
        }.getOrElse { emptyList() }
    }

    /** Replaces the contact with the same id in place, or adds it at the end. */
    fun guardar(contacto: Contacto) {
        val lista = listar()
        val novo = if (lista.any { it.id == contacto.id }) {
            lista.map { if (it.id == contacto.id) contacto else it }
        } else {
            lista + contacto
        }
        gravar(novo)
    }

    fun apagar(id: String) = gravar(listar().filterNot { it.id == id })

    private fun gravar(lista: List<Contacto>) {
        val array = JSONArray()
        for (c in lista) {
            array.put(JSONObject().apply {
                put("id", c.id)
                put("nome", c.nome)
                put("numero", c.numero)
            })
        }
        prefs.edit().putString(CHAVE, array.toString()).apply()
    }

    private companion object {
        const val CHAVE = "lista"
    }
}
