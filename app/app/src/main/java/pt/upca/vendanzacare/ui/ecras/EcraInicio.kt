package pt.upca.vendanzacare.ui.ecras

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.dados.RegistoTomas
import pt.upca.vendanzacare.dominio.Medicamento
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.Tomas
import pt.upca.vendanzacare.ui.componentes.ALVO
import pt.upca.vendanzacare.ui.componentes.BotaoContorno
import pt.upca.vendanzacare.ui.componentes.BarraDaMarca
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.Etiqueta
import pt.upca.vendanzacare.ui.componentes.Faixa112
import pt.upca.vendanzacare.ui.componentes.FotoDaEmbalagem
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.TituloSeccao
import pt.upca.vendanzacare.ui.minutoAgora
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.GradienteToma
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.Tom
import pt.upca.vendanzacare.ui.tema.TomAzul
import pt.upca.vendanzacare.ui.tema.TomLilas
import pt.upca.vendanzacare.ui.tema.TomTurquesa
import pt.upca.vendanzacare.ui.tema.TomVerde
import pt.upca.vendanzacare.ui.tema.textoGrande

/**
 * The first screen answers one question: what do I do now?
 *
 * Top to bottom: who and when (greeting, date) → the dose to take, as the one coloured card → the emergency band →
 * the rest of today → two doors the tab bar does not have (Guias is also a tab, but it is the app's core
 * content and earns a door; Perguntas has no tab).
 *
 * The coloured card asks "Já tomei?" for a dose that is due and not yet marked (up to [Regras.JANELA_EM_FALTA]
 * minutes after its time), and otherwise shows the next one. Marks come from here or from the notification.
 */
@Composable
fun EcraInicio(
    medicamentos: List<Medicamento>,
    nome: String,
    numeroTemas: Int,
    aoAbrirModulos: () -> Unit,
    aoAbrirMedicacao: () -> Unit,
    aoAbrirPerguntas: () -> Unit,
    aoAdicionarMedicamento: () -> Unit,
    aoAbrirContactos: () -> Unit,
    tomas: Tomas,
) {
    val t = LocalTextos.current
    val agora = minutoAgora()
    val dia = remember(agora) { RegistoTomas.dia() }
    val tomadas = tomas.de(dia)
    val emFalta = Regras.emFalta(medicamentos, agora, tomadas)
    val proxima = Regras.proximaToma(medicamentos, agora, tomadas)
    val faltam = proxima?.let { Regras.minutosAte(agora, it.hora) } ?: 0
    val quando = when {
        proxima == null -> ""
        proxima.amanha -> t.amanha
        faltam == 0 -> t.agora
        faltam < 60 -> t.faltamMinutos(faltam)
        else -> t.faltamHorasMinutos(faltam / 60, faltam % 60)
    }
    val saudacao = t.saudacao(agora)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = MARGEM, end = MARGEM, top = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { BarraDaMarca() }

        item {
            Column(Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                Text(
                    text = if (nome.isBlank()) "$saudacao." else "$saudacao, ${nome.trim().substringBefore(' ')}.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoite,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    Regras.dataDeHoje(System.currentTimeMillis(), locale = t.locale),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            when {
                emFalta != null -> {
                    val (hora, lista) = emFalta
                    val passaram = agora - hora
                    CartaoToma(
                        medicamentos = lista,
                        hora = hora,
                        quando = when {
                            passaram == 0 -> t.agora
                            passaram < 60 -> t.passaramMinutos(passaram)
                            else -> t.passaramHorasMinutos(passaram / 60, passaram % 60)
                        },
                        aoAbrirMedicacao = aoAbrirMedicacao,
                        aoTomar = { tomas.marcar(dia, lista.map { it.id }, hora, tomada = true) },
                    )
                }
                proxima != null -> CartaoToma(
                    medicamentos = medicamentos.filter {
                        proxima.hora in it.horas &&
                            (proxima.amanha || Regras.chaveToma(it.id, proxima.hora) !in tomadas)
                    },
                    hora = proxima.hora,
                    quando = quando,
                    aoAbrirMedicacao = aoAbrirMedicacao,
                )
                medicamentos.isNotEmpty() -> CartaoConvite(
                    titulo = t.semHoras,
                    texto = t.horasAjuda,
                    botao = t.verMedicacao,
                    icone = Icones.Relogio,
                    aoTocar = aoAbrirMedicacao,
                )
                else -> CartaoConvite(
                    titulo = t.semMedicacaoTitulo,
                    texto = t.semMedicacaoTexto,
                    botao = t.adicionarALista,
                    icone = Icones.Comprimido,
                    aoTocar = aoAdicionarMedicamento,
                )
            }
        }

        item { Faixa112(aoMaisContactos = aoAbrirContactos) }

        val hoje = tomasDeHoje(medicamentos)
        if (hoje.isNotEmpty()) {
            item {
                Column {
                    TituloSeccao(t.tomasDeHoje)
                    Text(t.agendaAjuda, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                }
            }
            item {
                AgendaDeHoje(
                    tomas = hoje,
                    agora = agora,
                    proxima = proxima?.takeIf { !it.amanha }?.hora,
                    tomadas = tomadas,
                    aoMarcar = { m, hora, tomada -> tomas.marcar(dia, listOf(m.id), hora, tomada) },
                )
            }
        }

        item {
            val guias: @Composable (Modifier) -> Unit = { m ->
                Porta(t.guias, t.contagemTemas(numeroTemas), Icones.Guias, TomTurquesa, aoAbrirModulos, m)
            }
            val perguntas: @Composable (Modifier) -> Unit = { m ->
                Porta(t.perguntasProfissionais, t.prepararConsulta, Icones.Pergunta, TomLilas, aoAbrirPerguntas, m)
            }
            if (textoGrande()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    guias(Modifier)
                    perguntas(Modifier)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    guias(Modifier.weight(1f))
                    perguntas(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Every dose time today, with what is taken at each. */
private fun tomasDeHoje(medicamentos: List<Medicamento>): List<Pair<Int, List<Medicamento>>> =
    medicamentos
        .flatMap { m -> m.horas.map { it to m } }
        .groupBy({ it.first }, { it.second })
        .toSortedMap()
        .map { (hora, lista) -> hora to lista }

/**
 * The hero. Big time, how long until (or since) it, and every medicine due then — two pills at 08:00 are one
 * moment for the person, not two cards. With [aoTomar] it is a due dose: "Está na hora" and a "Já tomei"
 * button that marks all of them; the list below still marks them one by one.
 */
@Composable
private fun CartaoToma(
    medicamentos: List<Medicamento>,
    hora: Int,
    quando: String,
    aoAbrirMedicacao: () -> Unit,
    aoTomar: (() -> Unit)? = null,
) {
    val t = LocalTextos.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(GradienteToma)
            .padding(22.dp),
    ) {
        Etiqueta(
            if (aoTomar != null) t.estaNaHora else t.proximaToma,
            fundo = Color.White.copy(alpha = 0.18f),
            tinta = Color.White,
        )
        Spacer(Modifier.height(10.dp))
        Text(Regras.horaCurta(hora), style = MaterialTheme.typography.displayMedium, color = Color.White)
        Text(quando, style = MaterialTheme.typography.bodyLarge, color = Color.White)

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (m in medicamentos.take(4)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (m.foto != null) {
                        FotoDaEmbalagem(uri = m.foto, modifier = Modifier.size(48.dp), forma = CircleShape)
                    } else {
                        IconeEmCirculo(Icones.Comprimido, Tom(Color.White.copy(alpha = 0.18f), Color.White), tamanho = 48.dp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(m.nome, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        if (m.dose.isNotBlank()) {
                            Text(m.dose, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        if (aoTomar != null) {
            BotaoPrincipal(texto = t.jaTomei, aoTocar = aoTomar, icone = Icones.Visto, fundo = Papel, tinta = AzulVendanza)
            Spacer(Modifier.height(8.dp))
            BotaoContorno(texto = t.verMedicacao, aoTocar = aoAbrirMedicacao, cor = Color.White)
        } else {
            BotaoPrincipal(
                texto = t.verMedicacao,
                aoTocar = aoAbrirMedicacao,
                fundo = Papel,
                tinta = AzulVendanza,
                seta = true,
            )
        }
    }
}

/** What the hero becomes when there is nothing to take: a short why and one button. */
@Composable
private fun CartaoConvite(titulo: String, texto: String, botao: String, icone: ImageVector, aoTocar: () -> Unit) {
    Cartao(preenchimento = PaddingValues(22.dp)) {
        IconeEmCirculo(icone, TomAzul, tamanho = 60.dp)
        Spacer(Modifier.height(14.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = AzulNoite)
        Spacer(Modifier.height(6.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        val adicionar = icone == Icones.Comprimido
        BotaoPrincipal(texto = botao, aoTocar = aoTocar, icone = Icones.Mais.takeIf { adicionar }, seta = !adicionar)
    }
}

/**
 * Today as a list of times, one row per medicine, each a checkbox: tap = taken, tap again = not. Past times
 * are in grey (never faded below 6:1 — "past" is not "unreadable"), the next one is tinted and labelled.
 * A tick means the person said so (here or on the notification) — the app never ticks on its own.
 */
@Composable
private fun AgendaDeHoje(
    tomas: List<Pair<Int, List<Medicamento>>>,
    agora: Int,
    proxima: Int?,
    tomadas: Set<String>,
    aoMarcar: (Medicamento, Int, Boolean) -> Unit,
) {
    val t = LocalTextos.current
    Cartao(preenchimento = PaddingValues(vertical = 8.dp, horizontal = 8.dp)) {
        tomas.forEachIndexed { i, (hora, lista) ->
            val passou = hora < agora
            val eProxima = hora == proxima
            if (i > 0 && !eProxima && tomas.getOrNull(i - 1)?.first != proxima) {
                HorizontalDivider(color = Linha, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (eProxima) TomAzul.fundo else Color.Transparent),
            ) {
                lista.forEachIndexed { j, m ->
                    val tomada = Regras.chaveToma(m.id, hora) in tomadas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .toggleable(value = tomada, role = Role.Checkbox, onValueChange = { aoMarcar(m, hora, it) })
                            .semantics { stateDescription = if (tomada) t.tomada else t.porTomar }
                            .heightIn(min = ALVO)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (j == 0) Regras.horaCurta(hora) else "",
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                eProxima -> AzulVendanza
                                passou -> TextoSecundario
                                else -> AzulNoite
                            },
                            modifier = Modifier.widthIn(min = 64.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                m.nome,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (eProxima && !tomada) FontWeight.Bold else FontWeight.Normal,
                                color = if (passou || tomada) TextoSecundario else AzulNoite,
                            )
                            if (tomada) {
                                Text(t.tomada, style = MaterialTheme.typography.labelMedium, color = TomVerde.tinta)
                            } else if (eProxima && j == 0) {
                                // under the name, not beside it: beside, it broke "Paracetamol" in two
                                Spacer(Modifier.height(4.dp))
                                Etiqueta(t.proxima, fundo = AzulVendanza, tinta = Color.White)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Caixa(tomada)
                    }
                }
            }
        }
    }
}

/** The tick box of a dose: green with a tick when taken, an outlined circle when not. */
@Composable
private fun Caixa(tomada: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (tomada) TomVerde.tinta else Papel, CircleShape)
            .then(if (tomada) Modifier else Modifier.border(2.dp, TextoSecundario, CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        if (tomada) Icon(Icones.Visto, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

/** A coloured door to a section. */
@Composable
private fun Porta(
    titulo: String,
    detalhe: String,
    icone: ImageVector,
    tom: Tom,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Cartao(modifier = modifier, cor = tom.fundo, aoTocar = aoTocar, preenchimento = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            IconeEmCirculo(icone, Tom(Papel, tom.tinta), tamanho = 52.dp)
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(32.dp)
                    .background(Papel.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icones.Seguinte, contentDescription = null, tint = tom.tinta, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNoite)
        Spacer(Modifier.height(2.dp))
        Text(detalhe, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    }
}
