package pt.upca.vendanzacare.ui.componentes

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.R
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.LocalLeitor
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.discar
import pt.upca.vendanzacare.ui.tema.AvisoFundo
import pt.upca.vendanzacare.ui.tema.AvisoTexto
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.EmergenciaFundo
import pt.upca.vendanzacare.ui.tema.EmergenciaTexto
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.Tom
import pt.upca.vendanzacare.ui.tema.TomAzul

/** Side margin of every screen. One value, so edges line up from tab to tab. */
val MARGEM = 20.dp

/** Minimum height of anything tappable: above the 48 dp guideline, for hands with tremor. */
val ALVO = 56.dp

// ---- superfícies ------------------------------------------------------------

/**
 * The basic white card: large radius, a hairline instead of a shadow (shadows render inconsistently on the old
 * phones this app targets, and a border keeps the edge visible in direct sunlight).
 */
@Composable
fun Cartao(
    modifier: Modifier = Modifier,
    cor: Color = Papel,
    aoTocar: (() -> Unit)? = null,
    preenchimento: PaddingValues = PaddingValues(18.dp),
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val borda = if (cor == Papel) BorderStroke(1.dp, Linha) else null
    val corpo: @Composable () -> Unit = { Column(Modifier.padding(preenchimento), content = conteudo) }
    if (aoTocar != null) {
        // a tappable card sinks slightly under the finger, like the buttons
        val interacao = remember { MutableInteractionSource() }
        val premido by interacao.collectIsPressedAsState()
        val escala by animateFloatAsState(if (premido) 0.985f else 1f, label = "escala")
        Surface(
            onClick = aoTocar,
            interactionSource = interacao,
            modifier = modifier
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = cor,
            border = borda,
            content = corpo,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = cor,
            border = borda,
            content = corpo,
        )
    }
}

/** A coloured disc with a line icon in it — the visual anchor of every row and tile. */
@Composable
fun IconeEmCirculo(icone: ImageVector, tom: Tom, modifier: Modifier = Modifier, tamanho: Dp = 52.dp) {
    Box(
        modifier = modifier
            .size(tamanho)
            .background(tom.fundo, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = null, tint = tom.tinta, modifier = Modifier.size(tamanho * 0.5f))
    }
}

/** Small pill label ("Próxima", "3 escolhidas"). Never the only carrier of meaning — it sits next to text. */
@Composable
fun Etiqueta(texto: String, fundo: Color, tinta: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = tinta,
        modifier = modifier
            .background(fundo, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

// ---- botões -----------------------------------------------------------------
//
// One family, four kinds, all built on [BaseBotao] so they press, disable and size the same way:
//   BotaoPrincipal  filled, with a darker "key" edge under it — the one action of a screen
//   BotaoSecundario tinted, flat — the alternative next to it
//   BotaoContorno   outline only — quiet or destructive actions ("Apagar medicamento")
//   BotaoGrande     a tile, icon over label — a choice between two ways of doing one thing
//
// Why the key edge: flat buttons are the ones older users most often fail to recognise as tappable. A 3 dp
// darker base reads as "this can be pressed" and, when pressed, the button drops onto it — feedback a person
// with reduced sensitivity in the fingers can see, not only feel.

/** Disabled colours that stay readable (5.7:1). Material's default fades the label to 38 %, below 4.5:1. */
private val DesativadoFundo = Color(0xFFEFEBE3)
private val DesativadoTinta = Color(0xFF4D5A63)

@Composable
private fun BaseBotao(
    aoTocar: () -> Unit,
    modifier: Modifier,
    fundo: Color,
    tinta: Color,
    ativo: Boolean,
    relevo: Boolean = false,
    borda: BorderStroke? = null,
    preenchimento: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    conteudo: @Composable RowScope.() -> Unit,
) {
    val interacao = remember { MutableInteractionSource() }
    val premido by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(if (premido) 0.97f else 1f, label = "escala")
    val comRelevo = relevo && ativo
    val desce by animateDpAsState(if (premido && comRelevo) RELEVO - 1.dp else 0.dp, label = "desce")
    val corBase = lerp(fundo, Color.Black, 0.3f)

    Box(
        propagateMinConstraints = true,
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .padding(bottom = if (relevo) RELEVO else 0.dp)
            .drawBehind {
                if (comRelevo) {
                    drawRoundRect(
                        color = corBase,
                        topLeft = Offset(0f, RELEVO.toPx()),
                        size = size,
                        cornerRadius = CornerRadius(size.height / 2),
                    )
                }
            },
    ) {
        Surface(
            onClick = aoTocar,
            enabled = ativo,
            shape = CircleShape,
            color = if (ativo) fundo else DesativadoFundo,
            contentColor = if (ativo) tinta else DesativadoTinta,
            border = borda,
            interactionSource = interacao,
            modifier = Modifier
                .offset(y = desce)
                .heightIn(min = ALVO),
        ) {
            Row(
                modifier = Modifier.padding(preenchimento),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = conteudo,
            )
        }
    }
}

private val RELEVO = 3.dp

/**
 * The one filled button a screen has. Pill-shaped, full width by default, with the key edge.
 * [seta] adds a trailing › for buttons that open another screen ("Ver a minha medicação").
 */
@Composable
fun BotaoPrincipal(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icone: ImageVector? = null,
    fundo: Color = MaterialTheme.colorScheme.primary,
    tinta: Color = Color.White,
    ativo: Boolean = true,
    seta: Boolean = false,
) {
    BaseBotao(aoTocar, modifier, fundo, tinta, ativo, relevo = true) { ConteudoBotao(texto, icone, seta) }
}

/** Tinted, flat — for the second action next to a [BotaoPrincipal]. [borda] for use on coloured cards. */
@Composable
fun BotaoSecundario(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icone: ImageVector? = null,
    tom: Tom = TomAzul,
    ativo: Boolean = true,
    seta: Boolean = false,
    borda: Color? = null,
) {
    BaseBotao(
        aoTocar, modifier, tom.fundo, tom.tinta, ativo,
        borda = borda?.let { BorderStroke(1.5.dp, it) },
        preenchimento = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
    ) { ConteudoBotao(texto, icone, seta) }
}

/** Outline only, transparent inside. For quiet actions and, in red, destructive ones. Border ≥ 3:1. */
@Composable
fun BotaoContorno(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icone: ImageVector? = null,
    cor: Color = MaterialTheme.colorScheme.primary,
) {
    BaseBotao(
        aoTocar, modifier, Color.Transparent, cor, ativo = true,
        borda = BorderStroke(1.5.dp, cor),
        preenchimento = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
    ) { ConteudoBotao(texto, icone, seta = false) }
}

@Composable
private fun RowScope.ConteudoBotao(texto: String, icone: ImageVector?, seta: Boolean) {
    if (icone != null) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
    }
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f, fill = false),
    )
    if (seta) {
        Spacer(Modifier.width(8.dp))
        Icon(Icones.Seguinte, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

/**
 * A big square-ish choice: icon in a disc, label under it, optional line under that. Two side by side give
 * an obvious either/or ("Tirar fotografia" / "Escolher da galeria") that a row of pills does not.
 */
@Composable
fun BotaoGrande(
    texto: String,
    icone: ImageVector,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    detalhe: String? = null,
    tom: Tom = TomAzul,
    destaque: Boolean = false,
) {
    val interacao = remember { MutableInteractionSource() }
    val premido by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(if (premido) 0.97f else 1f, label = "escala")
    val fundo = if (destaque) tom.tinta else tom.fundo
    val tinta = if (destaque) Color.White else AzulNoite
    Surface(
        onClick = aoTocar,
        shape = MaterialTheme.shapes.large,
        color = fundo,
        contentColor = tinta,
        interactionSource = interacao,
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .heightIn(min = 132.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            IconeEmCirculo(
                icone,
                if (destaque) Tom(Color.White.copy(alpha = 0.2f), Color.White) else Tom(Papel, tom.tinta),
                tamanho = 56.dp,
            )
            Spacer(Modifier.height(12.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            if (detalhe != null) {
                Text(
                    detalhe,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = if (destaque) Color.White else TextoSecundario,
                )
            }
        }
    }
}

/** Round icon-only button, 52 dp. Always has a spoken label. */
@Composable
fun BotaoRedondo(
    icone: ImageVector,
    descricao: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    fundo: Color = Papel,
    tinta: Color = AzulNoite,
) {
    Surface(
        onClick = aoTocar,
        shape = CircleShape,
        color = fundo,
        contentColor = tinta,
        border = if (fundo == Papel) BorderStroke(1.dp, Linha) else null,
        modifier = modifier.size(52.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icone, contentDescription = descricao, modifier = Modifier.size(24.dp))
        }
    }
}

// ---- cabeçalhos -------------------------------------------------------------

/** Title of a tab screen (Início, Guias, Medicação, Definições): big, with an optional line and action. */
@Composable
fun TituloEcra(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    lateral: Dp = MARGEM,
    acao: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = lateral, end = lateral, top = 20.dp, bottom = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoite,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            if (acao != null) {
                Spacer(Modifier.width(12.dp))
                acao()
            }
        }
        // under the row, not beside the title: next to an action button it wrapped one word per line
        if (subtitulo != null) {
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Header of a pushed screen: round back button + title. Replaces Material's TopAppBar, which ellipsised long titles. */
@Composable
fun Cabecalho(titulo: String, aoVoltar: () -> Unit, modifier: Modifier = Modifier) {
    val t = LocalTextos.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = MARGEM, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotaoRedondo(icone = Icones.Voltar, descricao = t.voltar, aoTocar = aoVoltar)
        Spacer(Modifier.width(14.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = AzulNoite,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
    }
}

/** Section label inside a screen. */
@Composable
fun TituloSeccao(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        color = AzulNoite,
        modifier = modifier
            .padding(top = 8.dp)
            .semantics { heading() },
    )
}

/**
 * The brand line at the top of Início: the official horizontal lockup — symbol + "Vendanza" in navy + "Care"
 * in turquoise, exactly as the brand manual draws it.
 *
 * The delivered `brand/lockup/vendanzacare-horizontal.png` has a white background baked in (a white box on
 * the sand screen), so `drawable-nodpi/marca_horizontal.png` is rebuilt from the two transparent masters
 * (`mark/vendanzacare-simbolo.png` + `typography/vendanzacare-logotipo.png`) placed in the exact boxes they
 * occupy in that lockup — same proportions (1.279 vs 1.278), same V-over-baseline alignment.
 * 26 dp tall ≈ 250 dp wide: above the manual's 180 px minimum for the horizontal logo at every density.
 */
@Composable
fun BarraDaMarca(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.marca_horizontal),
        contentDescription = "VendanzaCare",
        modifier = modifier.height(26.dp),
    )
}

// ---- blocos com significado ----------------------------------------------------

/**
 * The one red element in the app. Deliberately on Início, never buried in a menu: the brief says the app is
 * not for triage, so this is a call button, not a symptom checker. Both numbers open the dialer pre-filled.
 */
@Composable
fun Faixa112(modifier: Modifier = Modifier, aoMaisContactos: (() -> Unit)? = null) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    Cartao(modifier = modifier, cor = EmergenciaFundo) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconeEmCirculo(Icones.Telefone, Tom(Papel, EmergenciaTexto), tamanho = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(t.ajudaAgora, style = MaterialTheme.typography.titleMedium, color = EmergenciaTexto)
                Text(t.ajudaAgoraTexto, style = MaterialTheme.typography.bodyMedium, color = EmergenciaTexto)
            }
        }
        Spacer(Modifier.padding(top = 14.dp))
        BotaoPrincipal(
            texto = t.ligar112,
            aoTocar = { discar(contexto, "112") },
            icone = Icones.Telefone,
            fundo = EmergenciaTexto,
        )
        Spacer(Modifier.padding(top = 8.dp))
        BotaoSecundario(
            texto = t.ligarSns24,
            aoTocar = { discar(contexto, "808242424") },
            tom = Tom(Papel, EmergenciaTexto),
            borda = EmergenciaTexto,
        )
        if (aoMaisContactos != null) {
            Spacer(Modifier.padding(top = 8.dp))
            BotaoContorno(t.outrosContactos, aoMaisContactos, icone = Icones.Pessoa, cor = EmergenciaTexto)
        }
    }
}

/**
 * "Ouvir" — reads [texto] aloud and becomes "Parar" while it does. [chave] tells this button apart from the
 * others on the screen. Disabled with a reason when the phone has no voice.
 */
@Composable
fun BotaoOuvir(texto: String, chave: String, modifier: Modifier = Modifier, rotulo: String? = null, tom: Tom = TomAzul) {
    val leitor = LocalLeitor.current
    val t = LocalTextos.current
    val aFalar = leitor.aFalar == chave
    BaseBotao(
        aoTocar = { if (aFalar) leitor.parar() else leitor.falar(texto, chave) },
        modifier = modifier,
        fundo = if (aFalar) tom.tinta else tom.fundo,
        tinta = if (aFalar) Color.White else tom.tinta,
        ativo = leitor.disponivel,
        preenchimento = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Icon(if (aFalar) Icones.Parar else Icones.Ouvir, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = when {
                !leitor.disponivel -> t.semVoz
                aFalar -> t.parar
                else -> rotulo ?: t.ouvir
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Amber callout. Red is reserved for the 112 band so the signal keeps its meaning.
 * [titulo] is the one-line gist; [texto] the detail; [acao] an optional fix ("Ligar notificações").
 */
@Composable
fun CaixaDestaque(
    texto: String,
    modifier: Modifier = Modifier,
    titulo: String? = null,
    icone: ImageVector = Icones.Alerta,
    acao: Pair<String, () -> Unit>? = null,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = AvisoFundo, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(16.dp)) {
            Icon(icone, contentDescription = null, tint = AvisoTexto, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (titulo != null) Text(titulo, style = MaterialTheme.typography.titleSmall, color = AvisoTexto)
                Text(texto, style = MaterialTheme.typography.bodyMedium, color = AvisoTexto)
                if (acao != null) {
                    TextButton(
                        onClick = acao.second,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(acao.first, style = MaterialTheme.typography.labelLarge, color = AvisoTexto)
                    }
                }
            }
        }
    }
}

/** A single time of day, as a pill with a clock. With [aoRemover] it gets an ✕ and becomes a removable chip. */
@Composable
fun ChipHora(hora: Int, modifier: Modifier = Modifier, tom: Tom = TomAzul, aoRemover: (() -> Unit)? = null) {
    val t = LocalTextos.current
    val texto = Regras.horaCurta(hora)
    Row(
        modifier = modifier
            .background(tom.fundo, CircleShape)
            .padding(start = 12.dp, end = if (aoRemover == null) 14.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icones.Relogio, contentDescription = null, tint = tom.tinta, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = tom.tinta,
            modifier = Modifier.padding(vertical = 7.dp),
        )
        if (aoRemover != null) {
            Surface(onClick = aoRemover, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icones.Fechar,
                        contentDescription = t.removerHoraDe(texto),
                        tint = tom.tinta,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** Empty state: a picture, what is missing, and what to do next. */
@Composable
fun TextoVazio(
    titulo: String,
    detalhe: String,
    modifier: Modifier = Modifier,
    icone: ImageVector = Icones.Informacao,
    tom: Tom = TomAzul,
    acao: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconeEmCirculo(icone, tom, tamanho = 96.dp)
        Spacer(Modifier.padding(top = 4.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = AzulNoite, textAlign = TextAlign.Center)
        Text(
            detalhe,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (acao != null) {
            Spacer(Modifier.padding(top = 6.dp))
            acao()
        }
    }
}

/** Small reassurance line with a lock: "A lista fica só neste telemóvel." */
@Composable
fun NotaPrivacidade(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icones.Cadeado, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
