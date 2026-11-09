package pt.upca.vendanzacare.ui.ecras

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.dominio.Pergunta
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.BotaoContorno
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.Cabecalho
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.TituloSeccao
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.TomLilas

/**
 * The "lista de perguntas pronta a escolher" from the brief. The person ticks what worries them and hands the
 * list over as text — no account, no print driver, no PDF library.
 *
 * The whole row is the tap target (not just the 20 dp box), and the share button sits in a bar pinned to the
 * bottom as soon as one question is ticked, so it never scrolls out of reach.
 */
@Composable
fun EcraPerguntas(
    perguntas: List<Pergunta>,
    escolhidas: Set<String>,
    aoAlternar: (String) -> Unit,
    aoLimpar: () -> Unit,
    aoVoltar: () -> Unit,
) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    val grupos = perguntas.groupBy { it.tema }

    fun partilhar() {
        val texto = perguntas.filter { it.id in escolhidas }
            .joinToString(separator = "\n", prefix = "${t.perguntasProfissionais}:\n\n") { "• ${it.texto}" }
        if (texto.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        runCatching { contexto.startActivity(Intent.createChooser(intent, t.partilhar)) }
    }

    Column(Modifier.fillMaxSize()) {
        Cabecalho(titulo = t.perguntasProfissionais, aoVoltar = aoVoltar)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = MARGEM, end = MARGEM, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(t.perguntasIntro, style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
            }
            for ((tema, lista) in grupos) {
                item(key = "tema-$tema") { TituloSeccao(tema, Modifier.padding(top = 10.dp)) }
                item(key = "lista-$tema") {
                    Cartao(preenchimento = PaddingValues(6.dp)) {
                        lista.forEachIndexed { i, pergunta ->
                            if (i > 0) HorizontalDivider(color = Linha, modifier = Modifier.padding(horizontal = 14.dp))
                            LinhaPergunta(
                                texto = pergunta.texto,
                                escolhida = pergunta.id in escolhidas,
                                aoAlternar = { aoAlternar(pergunta.id) },
                            )
                        }
                    }
                }
            }
        }

        if (escolhidas.isNotEmpty()) {
            Surface(color = Papel) {
                Column {
                    HorizontalDivider(color = Linha)
                    Column(Modifier.padding(start = MARGEM, end = MARGEM, top = 12.dp, bottom = 12.dp)) {
                        BotaoPrincipal(
                            texto = t.contagemPerguntas(escolhidas.size),
                            aoTocar = { partilhar() },
                            icone = Icones.Partilhar,
                        )
                        Spacer(Modifier.height(8.dp))
                        BotaoContorno(t.limparLista, aoLimpar, icone = Icones.Fechar, cor = TextoSecundario)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaPergunta(texto: String, escolhida: Boolean, aoAlternar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (escolhida) TomLilas.fundo else Color.Transparent)
            .toggleable(value = escolhida, role = Role.Checkbox, onValueChange = { aoAlternar() })
            .heightIn(min = 60.dp)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = if (escolhida) TomLilas.tinta else Papel,
            border = if (escolhida) null else BorderStroke(2.dp, TextoSecundario),
            modifier = Modifier.size(28.dp),
        ) {
            if (escolhida) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icones.Visto, contentDescription = null, tint = Papel, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = AzulNoite, modifier = Modifier.weight(1f))
    }
}
