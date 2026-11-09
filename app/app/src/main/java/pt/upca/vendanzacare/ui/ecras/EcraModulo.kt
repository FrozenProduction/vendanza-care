package pt.upca.vendanzacare.ui.ecras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.dominio.Modulo
import pt.upca.vendanzacare.dominio.Seccao
import pt.upca.vendanzacare.ui.LocalLeitor
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.BotaoOuvir
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.BotaoRedondo
import pt.upca.vendanzacare.ui.componentes.CaixaDestaque
import pt.upca.vendanzacare.ui.componentes.Cabecalho
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.TextoVazio
import pt.upca.vendanzacare.ui.componentes.iconeDoModulo
import pt.upca.vendanzacare.ui.componentes.tomDoModulo
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.Tom

/**
 * One topic. A coloured header (the tile's colour, so the person knows they landed where they tapped) with a
 * "listen to all of it" button, then one numbered card per section, each with its own listen button.
 */
@Composable
fun EcraModulo(modulo: Modulo?, aoVoltar: () -> Unit, aoAbrirPerguntas: () -> Unit) {
    val leitor = LocalLeitor.current
    val t = LocalTextos.current
    // leaving the screen must stop the voice: audio that follows you into another screen is a defect here
    DisposableEffect(modulo?.id) {
        onDispose { leitor.parar() }
    }

    if (modulo == null) {
        Column(Modifier.fillMaxWidth()) {
            Cabecalho(titulo = t.tema, aoVoltar = aoVoltar)
            TextoVazio(
                titulo = t.temaNaoEncontrado,
                detalhe = t.temaNaoEncontradoTexto,
                modifier = Modifier.padding(MARGEM),
            )
        }
        return
    }

    val tom = tomDoModulo(modulo.icone)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Capa(modulo, tom, aoVoltar) }

        itemsIndexed(modulo.seccoes) { i, seccao ->
            SeccaoView(
                numero = i + 1,
                seccao = seccao,
                chave = "${modulo.id}-$i",
                tom = tom,
                modifier = Modifier.padding(horizontal = MARGEM),
            )
        }

        if (modulo.accao == "perguntas") {
            item {
                BotaoPrincipal(
                    texto = t.escolherPerguntas,
                    aoTocar = aoAbrirPerguntas,
                    icone = Icones.Pergunta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MARGEM, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun Capa(modulo: Modulo, tom: Tom, aoVoltar: () -> Unit) {
    val t = LocalTextos.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tom.fundo, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(start = 14.dp, end = MARGEM, top = 10.dp, bottom = 24.dp),
    ) {
        BotaoRedondo(icone = Icones.Voltar, descricao = t.voltar, aoTocar = aoVoltar)
        Column(Modifier.padding(start = 6.dp, top = 16.dp)) {
            IconeEmCirculo(iconeDoModulo(modulo.icone), Tom(Papel, tom.tinta), tamanho = 72.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                modulo.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoite,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(6.dp))
            Text(modulo.resumo, style = MaterialTheme.typography.bodyLarge, color = AzulNoite)
            Spacer(Modifier.height(18.dp))
            BotaoOuvir(
                texto = textoDoModulo(modulo),
                chave = "${modulo.id}-tudo",
                rotulo = t.ouvirTudo,
                tom = Tom(Papel, tom.tinta),
            )
        }
    }
}

@Composable
private fun SeccaoView(numero: Int, seccao: Seccao, chave: String, tom: Tom, modifier: Modifier = Modifier) {
    Cartao(modifier = modifier, preenchimento = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tom.fundo, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("$numero", style = MaterialTheme.typography.labelLarge, color = tom.tinta)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                seccao.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = AzulNoite,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
                    .semantics { heading() },
            )
        }
        Spacer(Modifier.height(12.dp))

        if (seccao.destaque) {
            CaixaDestaque(seccao.texto)
        } else {
            Text(seccao.texto, style = MaterialTheme.typography.bodyLarge, color = AzulNoite)
        }

        if (seccao.pontos.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (ponto in seccao.pontos) {
                    Row {
                        Box(
                            Modifier
                                .padding(top = 11.dp)
                                .size(8.dp)
                                .background(tom.tinta, CircleShape),
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(ponto, style = MaterialTheme.typography.bodyLarge, color = AzulNoite, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BotaoOuvir(texto = textoParaOuvir(seccao), chave = chave, tom = tom)
    }
}

/** What the phone reads aloud: the title, the body and the bullets, in the order they appear. */
private fun textoParaOuvir(seccao: Seccao): String = buildString {
    append(seccao.titulo).append(". ")
    append(seccao.texto)
    for (ponto in seccao.pontos) append(" ").append(ponto).append(".")
}

private fun textoDoModulo(modulo: Modulo): String = buildString {
    append(modulo.titulo).append(". ").append(modulo.resumo).append(" ")
    for (s in modulo.seccoes) append(textoParaOuvir(s)).append(" ")
}
