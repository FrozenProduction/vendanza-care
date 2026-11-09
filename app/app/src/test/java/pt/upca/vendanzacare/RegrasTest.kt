package pt.upca.vendanzacare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.upca.vendanzacare.dominio.Dicionario
import pt.upca.vendanzacare.dominio.Medicamento
import pt.upca.vendanzacare.dominio.Regras

/**
 * The three decisions that can hurt someone: which dose is next, whether a typed time is real, and whether two
 * medicines share an active ingredient. All pure, all here, all run by `testDebugUnitTest` in the same build.
 */
class RegrasTest {

    @Test
    fun `le as formas de escrever uma hora`() {
        assertEquals(480, Regras.minutosDoDia("8"))
        assertEquals(480, Regras.minutosDoDia("08:00"))
        assertEquals(510, Regras.minutosDoDia("8h30"))
        assertEquals(870, Regras.minutosDoDia("14.30"))
        assertEquals(480, Regras.minutosDoDia("0800"))
        assertEquals(0, Regras.minutosDoDia("0:00"))
        assertNull(Regras.minutosDoDia("24:00"))
        assertNull(Regras.minutosDoDia("8:75"))
        assertNull(Regras.minutosDoDia("tomar"))
        assertNull(Regras.minutosDoDia(""))
    }

    @Test
    fun `le a lista de horas e guarda as invalidas`() {
        val lidas = Regras.lerHoras("08:00, 14:00, 20:00")
        assertEquals(listOf(480, 840, 1200), lidas.horas)
        assertTrue(lidas.tudoValido)

        val comErro = Regras.lerHoras("08:00, tomar, 20:00")
        assertEquals(listOf(480, 1200), comErro.horas)
        assertEquals(listOf("tomar"), comErro.invalidos)

        assertEquals(emptyList<Int>(), Regras.lerHoras("").horas)
    }

    @Test
    fun `horas repetidas contam uma vez e ficam ordenadas`() {
        assertEquals(listOf(480, 1200), Regras.lerHoras("20:00 08:00 08:00").horas)
    }

    @Test
    fun `a proxima toma segue o relogio`() {
        val medicamentos = listOf(
            Medicamento(id = "a", nome = "Paracetamol", dose = "1 comprimido", horas = listOf(480, 1200)),
        )

        val antes = Regras.proximaToma(medicamentos, 7 * 60)
        assertEquals(480, antes?.hora)
        assertEquals(false, antes?.amanha)

        val exatamente = Regras.proximaToma(medicamentos, 8 * 60)
        assertEquals(480, exatamente?.hora)

        val depois = Regras.proximaToma(medicamentos, 9 * 60)
        assertEquals(1200, depois?.hora)

        val amanha = Regras.proximaToma(medicamentos, 21 * 60)
        assertEquals(480, amanha?.hora)
        assertEquals(true, amanha?.amanha)
    }

    @Test
    fun `sem medicamentos nao ha proxima toma`() {
        assertNull(Regras.proximaToma(emptyList(), 600))
        assertNull(Regras.proximaToma(listOf(Medicamento(id = "a", nome = "X")), 600))
    }

    @Test
    fun `minutos ate a toma passam para o dia seguinte`() {
        assertEquals(60, Regras.minutosAte(7 * 60, 8 * 60))
        assertEquals(0, Regras.minutosAte(8 * 60, 8 * 60))
        assertEquals(11 * 60, Regras.minutosAte(21 * 60, 8 * 60))
    }

    @Test
    fun `avisa quando dois medicamentos partilham principio ativo`() {
        val iguais = listOf(
            Medicamento(id = "a", nome = "Metformina 500 mg", horas = listOf(480)),
            Medicamento(id = "b", nome = "metformina", horas = listOf(1200)),
        )
        assertEquals(1, Regras.sobreponiveis(iguais).size)

        val diferentes = listOf(
            Medicamento(id = "a", nome = "Paracetamol"),
            Medicamento(id = "b", nome = "Ibuprofeno"),
        )
        assertEquals(0, Regras.sobreponiveis(diferentes).size)

        val combinado = listOf(
            Medicamento(id = "a", nome = "Metformina + Sitagliptina"),
            Medicamento(id = "b", nome = "Sitagliptina 100 mg"),
        )
        assertEquals(1, Regras.sobreponiveis(combinado).size)
    }

    @Test
    fun `duas entradas do mesmo medicamento contam como sobreposicao`() {
        val repetido = listOf(
            Medicamento(id = "a", nome = "Paracetamol"),
            Medicamento(id = "b", nome = "Paracetamol"),
        )
        assertEquals(1, Regras.sobreponiveis(repetido).size)
    }

    @Test
    fun `a marca e o principio ativo sao o mesmo medicamento`() {
        // the double dose the warning exists for: the box says Ben-u-ron, the discharge note says paracetamol
        val lista = listOf(
            Medicamento(id = "a", nome = "Paracetamol 1g", horas = listOf(480)),
            Medicamento(id = "b", nome = "Ben-u-ron 500 mg", horas = listOf(480)),
        )
        val avisos = Regras.sobreponiveis(lista)
        assertEquals(1, avisos.size)
        assertEquals("paracetamol", avisos.single().principio)

        // however the brand is typed
        assertEquals(listOf("paracetamol"), Regras.principiosAtivos("ben u ron"))
        assertEquals(listOf("paracetamol"), Regras.principiosAtivos("BENURON 1 g"))
    }

    @Test
    fun `uma marca combinada avisa sobre cada componente`() {
        val lista = listOf(
            Medicamento(id = "a", nome = "Janumet"),
            Medicamento(id = "b", nome = "Metformina 850 mg"),
        )
        assertEquals("metformina", Regras.sobreponiveis(lista).single().principio)
    }

    @Test
    fun `nomes que comecam pela mesma palavra nao sao o mesmo principio`() {
        val acidos = listOf(
            Medicamento(id = "a", nome = "Ácido fólico 5 mg"),
            Medicamento(id = "b", nome = "Ácido acetilsalicílico 100 mg"),
        )
        assertEquals(0, Regras.sobreponiveis(acidos).size)

        val vitaminas = listOf(
            Medicamento(id = "a", nome = "Vitamina D"),
            Medicamento(id = "b", nome = "Vitamina B12"),
        )
        assertEquals(0, Regras.sobreponiveis(vitaminas).size)
    }

    @Test
    fun `aspirina e acido acetilsalicilico sao o mesmo`() {
        val lista = listOf(
            Medicamento(id = "a", nome = "Aspirina"),
            Medicamento(id = "b", nome = "Ácido acetilsalicílico 100 mg"),
        )
        val aviso = Regras.sobreponiveis(lista).single()
        assertEquals("acido acetilsalicilico", aviso.principio)
        assertEquals("ácido acetilsalicílico", Dicionario.legivel(aviso.principio))
    }

    @Test
    fun `o laboratorio e o sal nao mudam o principio ativo`() {
        val lista = listOf(
            Medicamento(id = "a", nome = "Metformina Generis 1000 mg"),
            Medicamento(id = "b", nome = "Cloridrato de metformina 850 mg"),
        )
        assertEquals("metformina", Regras.sobreponiveis(lista).single().principio)
    }

    @Test
    fun `o texto da lista tem nome dose e horas`() {
        val texto = Regras.textoDaLista(
            listOf(Medicamento(id = "a", nome = "Paracetamol", dose = "1 comprimido", horas = listOf(480, 1200))),
        )
        assertTrue(texto.contains("Paracetamol"))
        assertTrue(texto.contains("1 comprimido"))
        assertTrue(texto.contains("08:00"))
        assertTrue(texto.contains("20:00"))
        assertEquals("", Regras.textoDaLista(emptyList()))
    }

    @Test
    fun `hora curta tem sempre dois digitos`() {
        assertEquals("00:00", Regras.horaCurta(0))
        assertEquals("08:05", Regras.horaCurta(485))
        assertEquals("23:59", Regras.horaCurta(1439))
    }

    @Test
    fun `a data guardada em iso aparece em formato europeu`() {
        assertEquals("12/03/1948", Regras.dataCurta("1948-03-12"))
        assertEquals("01/01/2026", Regras.dataCurta("2026-01-01"))
        // a human may type a single-digit month; that is still the same day
        assertEquals("12/03/1948", Regras.dataCurta("1948-3-12"))
        // impossible dates are handed back untouched instead of silently rolling over to another day
        assertEquals("1948-13-45", Regras.dataCurta("1948-13-45"))
        assertEquals("", Regras.dataCurta(""))
    }

    @Test
    fun `o cabecalho da lista leva o nome e a data quando existem`() {
        val meds = listOf(Medicamento(id = "a", nome = "Paracetamol", horas = listOf(480)))
        val texto = Regras.textoDaLista(meds, cabecalho = "A minha medicação\nMaria · 12/03/1948")
        assertTrue(texto.startsWith("A minha medicação"))
        assertTrue(texto.contains("Maria · 12/03/1948"))
        assertTrue(texto.contains("Paracetamol"))
    }
}
