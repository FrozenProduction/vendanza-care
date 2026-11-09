package pt.upca.vendanzacare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A guard on the bundled content, not on the app: the JSON is the product here, and a truncated file would
 * ship as a module list with missing themes. `org.json` is a stub in JVM tests, so this counts structure
 * instead of parsing — enough to catch a half-written file or a module without sections.
 */
class ConteudoAssetsTest {

    private fun assets(nome: String): File {
        val candidatos = listOf(File("src/main/assets/$nome"), File("app/src/main/assets/$nome"))
        return candidatos.first { it.exists() }
    }

    @Test
    fun `os doze modulos estao no ficheiro`() {
        val texto = assets("conteudo.json").readText()
        assertEquals(12, Regex("\"id\":\\s*\"").findAll(texto).count())
        assertTrue(Regex("\"seccoes\"").findAll(texto).count() == 12)
        assertEquals(0, Regex("TODO").findAll(texto).count())
    }

    @Test
    fun `as perguntas estao no ficheiro`() {
        val texto = assets("perguntas.json").readText()
        assertEquals(17, Regex("\"id\":\\s*\"").findAll(texto).count())
        assertTrue(texto.contains("\"tema\""))
    }
}
