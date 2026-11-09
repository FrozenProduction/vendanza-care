package pt.upca.vendanzacare.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.upca.vendanzacare.dados.Conteudo
import pt.upca.vendanzacare.dados.Fotos
import pt.upca.vendanzacare.dados.MedicacaoStore
import pt.upca.vendanzacare.dados.Preferencias
import pt.upca.vendanzacare.dominio.Medicamento
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.lembretes.Lembretes
import pt.upca.vendanzacare.ui.ecras.EcraDefinicoes
import pt.upca.vendanzacare.ui.ecras.EcraInicio
import pt.upca.vendanzacare.ui.ecras.EcraMedicacao
import pt.upca.vendanzacare.ui.ecras.EcraMedicamento
import pt.upca.vendanzacare.ui.ecras.EcraModulo
import pt.upca.vendanzacare.ui.ecras.EcraModulos
import pt.upca.vendanzacare.ui.ecras.EcraPerguntas
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TemaVendanzaCare
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.TomAzul

/**
 * Every screen the app has. The objects double as routes, so a typo is a compile error, not a blank screen.
 * `companion object` reads them lazily on purpose: a `val base = listOf(Inicio.rota, …)` would initialise
 * before the objects exist and crash with `ExceptionInInitializerError` on first navigation.
 */
sealed class Ecra(val rota: String) {
    data object Inicio : Ecra("inicio")
    data object Aprender : Ecra("aprender")
    data object Medicacao : Ecra("medicacao")
    data object Definicoes : Ecra("definicoes")
    data object Perguntas : Ecra("perguntas")

    data object Modulo : Ecra("modulo/{id}") {
        fun caminho(id: String) = "modulo/$id"
    }

    data object Medicamento : Ecra("medicamento/{id}") {
        fun caminho(id: String) = "medicamento/$id"
    }

    companion object {
        val base: List<String> by lazy {
            listOf(Inicio.rota, Aprender.rota, Medicacao.rota, Definicoes.rota)
        }
    }
}

private const val ENTRAR_MS = 180
private const val SAIR_MS = 120

private const val ARG_ID = "id"
private const val NOVO = "novo"

@Composable
fun AppVendanzaCare() {
    val contexto = LocalContext.current
    val leitor = rememberLeitor()
    val prefs = remember { Preferencias(contexto) }
    val store = remember { MedicacaoStore(contexto) }
    val conteudos = remember { Conteudo.ler(contexto) }

    var escala by remember { mutableStateOf(prefs.escalaTexto) }
    var idioma by remember { mutableStateOf(prefs.idioma) }
    var nome by remember { mutableStateOf(prefs.nome) }
    var nascimento by remember { mutableStateOf(prefs.nascimento) }
    var medicamentos by remember { mutableStateOf(store.listar()) }
    var escolhidas by remember { mutableStateOf(prefs.perguntasEscolhidas()) }

    // re-armed on every launch and after every edit; ArranqueReceiver covers reboots (architecture.md §10)
    LaunchedEffect(medicamentos) {
        Lembretes.reagendar(contexto, medicamentos)
        // photos of deleted medicines, replaced photos and captures never saved (dados/Fotos.kt)
        withContext(Dispatchers.IO) { Fotos.limpar(contexto, medicamentos.map { it.foto }) }
    }

    val pedirNotificacoes = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedida = ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedida) pedirNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rota = entrada?.destination?.route

    fun mudarSeparador(destino: String) {
        nav.navigate(destino) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun guardarMedicamento(medicamento: Medicamento) {
        store.guardar(medicamento)
        medicamentos = store.listar()
    }

    fun apagarMedicamento(id: String) {
        store.apagar(id)
        medicamentos = store.listar()
    }

    val textos = textosDe(idioma)

    TemaVendanzaCare(escala = escala) {
        CompositionLocalProvider(
            LocalLeitor provides leitor,
            LocalTextos provides textos,
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (rota != null && rota in Ecra.base) {
                        BarraInferior(rota = rota, textos = textos, aoMudar = { mudarSeparador(it) })
                    }
                },
            ) { margens ->
                // Navigation's default is a 700 ms cross-fade on every change, which reads as "the app is slow".
                // Tabs swap with a quick fade; screens opened on top fade in fast and let the old one go faster.
                NavHost(
                    navController = nav,
                    startDestination = Ecra.Inicio.rota,
                    modifier = Modifier.padding(margens),
                    enterTransition = { fadeIn(tween(ENTRAR_MS)) },
                    exitTransition = { fadeOut(tween(SAIR_MS)) },
                    popEnterTransition = { fadeIn(tween(ENTRAR_MS)) },
                    popExitTransition = { fadeOut(tween(SAIR_MS)) },
                ) {
                    composable(Ecra.Inicio.rota) {
                        EcraInicio(
                            medicamentos = medicamentos,
                            nome = nome,
                            aoAbrirModulos = { mudarSeparador(Ecra.Aprender.rota) },
                            aoAbrirMedicacao = { mudarSeparador(Ecra.Medicacao.rota) },
                            numeroTemas = conteudos.modulos.size,
                            aoAbrirPerguntas = { nav.navigate(Ecra.Perguntas.rota) },
                            aoAdicionarMedicamento = { nav.navigate(Ecra.Medicamento.caminho(NOVO)) },
                        )
                    }

                    composable(Ecra.Aprender.rota) {
                        EcraModulos(
                            modulos = conteudos.modulos,
                            aoAbrir = { nav.navigate(Ecra.Modulo.caminho(it)) },
                        )
                    }

                    composable(Ecra.Modulo.rota) { argumentos ->
                        val id = argumentos.arguments?.getString(ARG_ID).orEmpty()
                        EcraModulo(
                            modulo = conteudos.modulo(id),
                            aoVoltar = { nav.popBackStack() },
                            aoAbrirPerguntas = { nav.navigate(Ecra.Perguntas.rota) },
                        )
                    }

                    composable(Ecra.Medicacao.rota) {
                        EcraMedicacao(
                            medicamentos = medicamentos,
                            sobreponiveis = Regras.sobreponiveis(medicamentos),
                            nome = nome,
                            nascimento = nascimento,
                            aoAdicionar = { nav.navigate(Ecra.Medicamento.caminho(NOVO)) },
                            aoAbrir = { nav.navigate(Ecra.Medicamento.caminho(it)) },
                        )
                    }

                    composable(Ecra.Medicamento.rota) { argumentos ->
                        val id = argumentos.arguments?.getString(ARG_ID).orEmpty()
                        EcraMedicamento(
                            medicamento = if (id == NOVO) null else medicamentos.firstOrNull { it.id == id },
                            aoGuardar = { guardarMedicamento(it) },
                            aoApagar = { apagarMedicamento(it) },
                            aoVoltar = { nav.popBackStack() },
                        )
                    }

                    composable(Ecra.Perguntas.rota) {
                        EcraPerguntas(
                            perguntas = conteudos.perguntas,
                            escolhidas = escolhidas,
                            aoAlternar = { id ->
                                val novas = if (id in escolhidas) escolhidas - id else escolhidas + id
                                escolhidas = novas
                                prefs.guardarPerguntas(novas)
                            },
                            aoLimpar = {
                                escolhidas = emptySet()
                                prefs.guardarPerguntas(emptySet())
                            },
                            aoVoltar = { nav.popBackStack() },
                        )
                    }

                    composable(Ecra.Definicoes.rota) {
                        EcraDefinicoes(
                            escala = escala,
                            aoMudarEscala = {
                                escala = it
                                prefs.escalaTexto = it
                            },
                            idioma = idioma,
                            aoMudarIdioma = {
                                idioma = it
                                prefs.idioma = it
                            },
                            nome = nome,
                            aoMudarNome = {
                                nome = it
                                prefs.nome = it
                            },
                            nascimento = nascimento,
                            aoMudarNascimento = {
                                nascimento = it
                                prefs.nascimento = it
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Four tabs, each with icon **and** word. The earlier icon-only bar saved space, but an "i" in a circle does not
 * say "Aprender" to someone who has never used an app like this, and the usability literature on older adults
 * is consistent that unlabeled icons are the first thing they fail on.
 *
 * Labels never wrap: from large text sizes on they shrink step by step (down to 12 sp) to fit their quarter
 * of the width. TalkBack reads the full word either way, and the icon stays the same size.
 */
@Composable
private fun BarraInferior(rota: String, textos: Textos, aoMudar: (String) -> Unit) {
    val itens = listOf(
        Triple(Ecra.Inicio.rota, Icones.Inicio, textos.inicio),
        Triple(Ecra.Aprender.rota, Icones.Aprender, textos.aprender),
        Triple(Ecra.Medicacao.rota, Icones.Comprimido, textos.medicacao),
        Triple(Ecra.Definicoes.rota, Icones.Ajustes, textos.definicoes),
    )
    // All four labels must be the same size, so the bar caps its own text scale at 1.15× instead of letting
    // each label shrink on its own (at "Muito grande" that gave "Início" big and "Definições" small).
    // The cap only touches these four words; the icons carry the meaning and TalkBack reads the full label.
    val densidade = LocalDensity.current
    Surface(color = Papel) {
        CompositionLocalProvider(
            LocalDensity provides Density(densidade.density, densidade.fontScale.coerceAtMost(ESCALA_BARRA)),
        ) {
            Column {
                HorizontalDivider(color = Linha)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .selectableGroup()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    for ((destino, icone, rotulo) in itens) {
                        ItemBarra(
                            selecionado = rota == destino,
                            icone = icone,
                            rotulo = rotulo,
                            aoTocar = { aoMudar(destino) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private const val ESCALA_BARRA = 1.15f

@Composable
private fun ItemBarra(
    selecionado: Boolean,
    icone: ImageVector,
    rotulo: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cor = if (selecionado) AzulVendanza else TextoSecundario
    val fundo by animateColorAsState(if (selecionado) TomAzul.fundo else Color.Transparent, label = "fundo")
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selecionado, onClick = aoTocar, role = Role.Tab)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 34.dp)
                .background(fundo, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(24.dp))
        }
        BasicText(
            text = rotulo,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium.copy(
                color = cor,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            // sizes are in sp, already multiplied by the font scale: dividing the floor by it keeps the
            // smallest step at ~11 dp on screen whatever the text-size setting
            autoSize = TextAutoSize.StepBased(
                minFontSize = (11f / LocalDensity.current.fontScale).sp,
                maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 2.dp, end = 2.dp),
        )
    }
}
