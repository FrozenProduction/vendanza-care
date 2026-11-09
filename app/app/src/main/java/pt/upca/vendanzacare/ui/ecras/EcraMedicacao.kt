package pt.upca.vendanzacare.ui.ecras

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import pt.upca.vendanzacare.dominio.Dicionario
import pt.upca.vendanzacare.dominio.Medicamento
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.dominio.Sobreposicao
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.BotaoRedondo
import pt.upca.vendanzacare.ui.componentes.BotaoSecundario
import pt.upca.vendanzacare.ui.componentes.CaixaDestaque
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.ChipHora
import pt.upca.vendanzacare.ui.componentes.FotoDaEmbalagem
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.NotaPrivacidade
import pt.upca.vendanzacare.ui.componentes.TextoVazio
import pt.upca.vendanzacare.ui.componentes.TituloEcra
import pt.upca.vendanzacare.ui.tema.AvisoFundo
import pt.upca.vendanzacare.ui.tema.AvisoTexto
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.TomAzul
import pt.upca.vendanzacare.ui.tema.textoGrande
import pt.upca.vendanzacare.ui.tema.tomDe

/**
 * A medicine's warning line: the other entry it overlaps with, and the ingredient they share.
 * [principio] is normalised (as [Sobreposicao.principio]); [Dicionario.legivel] gives the accented spelling.
 */
private data class Repeticao(val outro: String, val principio: String) {
    /** "Paracetamol 1 g" already says "paracetamol"; repeating it in the sentence reads like a glitch. */
    val nomeJaDiz: Boolean get() = Regras.normalizar(outro).startsWith(principio)
}

/**
 * The list Ana and D. Maria take to every appointment. Tapping a card edits it; "Partilhar" hands the list to a
 * professional as text (no account, no server, no export file left behind).
 *
 * The duplicate-ingredient warning lives **on the cards it concerns** ("Igual a Ben-u-ron: os dois têm
 * paracetamol"), with a one-sentence banner on top. The old design put every pair in one paragraph above the
 * list; at large text that paragraph filled the screen and hid the medicines it was talking about.
 */
@Composable
fun EcraMedicacao(
    medicamentos: List<Medicamento>,
    sobreponiveis: List<Sobreposicao>,
    nome: String,
    nascimento: String,
    aoAdicionar: () -> Unit,
    aoAbrir: (String) -> Unit,
) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    var avisosLigados by remember { mutableStateOf(notificacoesLigadas(contexto)) }
    // the fix happens in Android's settings, outside the app; re-check while the warning is on screen so it
    // disappears by itself when the person comes back
    LaunchedEffect(avisosLigados) {
        while (!avisosLigados) {
            delay(1_500L)
            avisosLigados = notificacoesLigadas(contexto)
        }
    }

    val repeticoes: Map<String, List<Repeticao>> = remember(sobreponiveis) {
        sobreponiveis
            .flatMap { s ->
                listOf(s.a.id to Repeticao(s.b.nome, s.principio), s.b.id to Repeticao(s.a.nome, s.principio))
            }
            .groupBy({ it.first }, { it.second })
    }

    fun partilhar() {
        val cabecalho = buildString {
            append(t.aMinhaMedicacao)
            if (nome.isNotBlank()) {
                append("\n").append(nome.trim())
                if (nascimento.isNotBlank()) append(" · ").append(Regras.dataCurta(nascimento))
            }
        }
        val texto = Regras.textoDaLista(medicamentos, cabecalho = cabecalho)
        if (texto.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        runCatching { contexto.startActivity(Intent.createChooser(intent, t.partilharLista)) }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = MARGEM, end = MARGEM, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TituloEcra(
                    titulo = t.aMinhaMedicacao,
                    subtitulo = if (medicamentos.isEmpty()) null else t.contagemMedicamentos(medicamentos.size),
                    lateral = 0.dp,
                    acao = if (medicamentos.isEmpty()) null else {
                        {
                            if (textoGrande()) {
                                BotaoRedondo(Icones.Partilhar, t.partilharLista, ::partilhar, fundo = TomAzul.fundo, tinta = AzulVendanza)
                            } else {
                                BotaoSecundario(t.partilhar, ::partilhar, modifier = Modifier, icone = Icones.Partilhar)
                            }
                        }
                    },
                )
            }

            if (medicamentos.isNotEmpty() && !avisosLigados) {
                item {
                    CaixaDestaque(
                        texto = t.lembretesDesligados,
                        icone = Icones.Sino,
                        acao = t.ligarNotificacoes to { abrirDefinicoesDeNotificacoes(contexto) },
                    )
                }
            }

            if (sobreponiveis.isNotEmpty()) {
                item { CaixaDestaque(titulo = t.duplicacaoTitulo, texto = t.sobreponiveisConselho) }
            }

            if (medicamentos.isEmpty()) {
                item {
                    TextoVazio(
                        titulo = t.listaVaziaTitulo,
                        detalhe = t.listaVaziaTexto,
                        icone = Icones.Comprimido,
                        acao = { BotaoPrincipal(t.adicionarMedicamento, aoAdicionar, icone = Icones.Mais) },
                    )
                }
            }

            items(medicamentos, key = { it.id }) { medicamento ->
                CartaoMedicamento(
                    medicamento = medicamento,
                    repeticoes = repeticoes[medicamento.id].orEmpty(),
                    aoAbrir = { aoAbrir(medicamento.id) },
                )
            }

            item { NotaPrivacidade(t.listaSoTelemovel, Modifier.padding(top = 8.dp)) }
        }

        if (medicamentos.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = aoAdicionar,
                icon = { Icon(Icones.Mais, contentDescription = null) },
                text = { Text(t.adicionarMedicamento, style = MaterialTheme.typography.labelLarge) },
                shape = CircleShape,
                containerColor = AzulVendanza,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MARGEM)
                    .heightIn(min = 60.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CartaoMedicamento(medicamento: Medicamento, repeticoes: List<Repeticao>, aoAbrir: () -> Unit) {
    val t = LocalTextos.current
    val tom = tomDe(medicamento.id)
    Cartao(aoTocar = aoAbrir, preenchimento = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            if (medicamento.foto != null) {
                FotoDaEmbalagem(uri = medicamento.foto, modifier = Modifier.size(60.dp))
            } else {
                IconeEmCirculo(Icones.Comprimido, tom, tamanho = 56.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(medicamento.nome, style = MaterialTheme.typography.titleMedium, color = AzulNoite)
                if (medicamento.dose.isNotBlank()) {
                    Text(medicamento.dose, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                }
            }
            Icon(
                Icones.Seguinte,
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        Spacer(Modifier.height(12.dp))
        if (medicamento.horas.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icones.Sino, contentDescription = null, tint = AvisoTexto, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(t.semHoras, style = MaterialTheme.typography.bodyMedium, color = AvisoTexto)
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                medicamento.horas.forEach { ChipHora(it, tom = tom) }
            }
        }

        if (medicamento.nota.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icones.Informacao,
                    contentDescription = null,
                    tint = TextoSecundario,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(medicamento.nota, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
        }

        for (r in repeticoes) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AvisoFundo, MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icones.Alerta,
                    contentDescription = null,
                    tint = AvisoTexto,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (r.nomeJaDiz) t.igualADe(r.outro) else t.mesmoPrincipioDe(r.outro, Dicionario.legivel(r.principio)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AvisoTexto,
                )
            }
        }
    }
}

private fun notificacoesLigadas(contexto: Context): Boolean =
    runCatching { NotificationManagerCompat.from(contexto).areNotificationsEnabled() }.getOrDefault(false)

/** Straight to this app's notification switch (API 26+), or its App info page on Android 7. */
private fun abrirDefinicoesDeNotificacoes(contexto: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
    }
    runCatching { contexto.startActivity(intent) }
}
