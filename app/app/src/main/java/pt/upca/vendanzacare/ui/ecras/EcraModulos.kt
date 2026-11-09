package pt.upca.vendanzacare.ui.ecras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.dominio.Modulo
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.TextoVazio
import pt.upca.vendanzacare.ui.componentes.TituloEcra
import pt.upca.vendanzacare.ui.componentes.iconeDoModulo
import pt.upca.vendanzacare.ui.componentes.tomDoModulo
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.Tom
import pt.upca.vendanzacare.ui.tema.textoGrande

/**
 * The twelve topics as coloured tiles, two per row: each has its own picture and colour, so "the green one with
 * the drop" is findable again without reading. With large text the grid drops to one column of wide rows —
 * two columns at 1.6× squeeze "Comunicação" onto three lines.
 */
@Composable
fun EcraModulos(modulos: List<Modulo>, aoAbrir: (String) -> Unit) {
    val t = LocalTextos.current
    val umaColuna = textoGrande()

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (umaColuna) 1 else 2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = MARGEM, end = MARGEM, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TituloEcra(
                titulo = t.guiasTitulo,
                subtitulo = t.guiasIntro,
                lateral = 0.dp, // the grid already pads the sides
            )
        }
        if (modulos.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextoVazio(titulo = t.conteudoIndisponivel, detalhe = t.conteudoIndisponivelTexto)
            }
        }
        items(modulos, key = { it.id }) { modulo ->
            val tom = tomDoModulo(modulo.icone)
            if (umaColuna) {
                LinhaTema(modulo.titulo, iconeDoModulo(modulo.icone), tom) { aoAbrir(modulo.id) }
            } else {
                MosaicoTema(modulo.titulo, iconeDoModulo(modulo.icone), tom) { aoAbrir(modulo.id) }
            }
        }
    }
}

@Composable
private fun MosaicoTema(titulo: String, icone: ImageVector, tom: Tom, aoTocar: () -> Unit) {
    Cartao(
        cor = tom.fundo,
        aoTocar = aoTocar,
        modifier = Modifier.heightIn(min = 150.dp),
        preenchimento = PaddingValues(16.dp),
    ) {
        IconeEmCirculo(icone, Tom(Papel, tom.tinta), tamanho = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNoite)
    }
}

@Composable
private fun LinhaTema(titulo: String, icone: ImageVector, tom: Tom, aoTocar: () -> Unit) {
    Cartao(cor = tom.fundo, aoTocar = aoTocar, preenchimento = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconeEmCirculo(icone, Tom(Papel, tom.tinta), tamanho = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNoite)
            }
            Icon(Icones.Seguinte, contentDescription = null, tint = tom.tinta)
        }
    }
}
