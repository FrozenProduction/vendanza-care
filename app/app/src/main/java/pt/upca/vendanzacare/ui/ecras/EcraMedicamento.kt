package pt.upca.vendanzacare.ui.ecras

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.upca.vendanzacare.dados.Fotos
import pt.upca.vendanzacare.dominio.Medicamento
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.BotaoContorno
import pt.upca.vendanzacare.ui.componentes.BotaoGrande
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.BotaoSecundario
import pt.upca.vendanzacare.ui.componentes.Cabecalho
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.ChipHora
import pt.upca.vendanzacare.ui.componentes.FotoDaEmbalagem
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.TituloSeccao
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.EmergenciaTexto
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.textoGrande

/**
 * Add / edit one medicine. Only the name is mandatory: someone who does not know the times yet can still keep
 * the list, and the list then says out loud that there are no reminders.
 *
 * Times are **tapped, not typed**. Four moments of the day cover most prescriptions ("de manhã e ao jantar");
 * anything else goes through the time picker's keyboard mode (two big number fields — the clock dial is hard to
 * hit with a tremor). The free-text field this replaced accepted "8h30", "0830" and "14.30", and still produced
 * errors people could not fix by themselves.
 *
 * The **box photo comes first**: the person photographs the box with the phone's own camera, then copies the name
 * and dose printed on it into the fields below — the photo stays on screen while they type. Nothing reads the
 * photo; it is only kept (privately, see `dados/Fotos.kt`) and shown on the card and in the reminder. That is
 * the "ler o medicamento" step without any recognition, which is planned separately (architecture.md §12.10).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EcraMedicamento(
    medicamento: Medicamento?,
    aoGuardar: (Medicamento) -> Unit,
    aoApagar: (String) -> Unit,
    aoVoltar: () -> Unit,
) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    val chave = medicamento?.id ?: "novo"

    var nome by rememberSaveable(chave) { mutableStateOf(medicamento?.nome.orEmpty()) }
    var dose by rememberSaveable(chave) { mutableStateOf(medicamento?.dose.orEmpty()) }
    // minutes of the day as "480,1200": a String survives rotation in a Bundle without a custom Saver
    var horasGuardadas by rememberSaveable(chave) { mutableStateOf(medicamento?.horas.orEmpty().joinToString(",")) }
    var nota by rememberSaveable(chave) { mutableStateOf(medicamento?.nota.orEmpty()) }
    // empty string = no photo. A nullable String is not a saveable Bundle value and would fail on rotation.
    var foto by rememberSaveable(chave) { mutableStateOf(medicamento?.foto.orEmpty()) }
    var erro by rememberSaveable(chave) { mutableStateOf("") }
    var confirmarApagar by remember { mutableStateOf(false) }
    var escolherHora by remember { mutableStateOf(false) }
    var aPreparar by remember { mutableStateOf(false) }
    // the file the camera app is writing into — saveable, because the system may kill this app while the
    // camera is open, and the result must still land in the right place when it comes back
    var captura by rememberSaveable(chave) { mutableStateOf("") }
    val escopo = rememberCoroutineScope()

    val horas = horasGuardadas.split(',').mapNotNull { it.toIntOrNull() }.distinct().sorted()
    fun mudarHoras(novas: List<Int>) {
        horasGuardadas = novas.distinct().sorted().joinToString(",")
    }

    /** Copies the picture into the app's folder, straightened and shrunk, off the main thread. */
    fun guardarFoto(origem: Uri) {
        aPreparar = true
        escopo.launch {
            val nova = withContext(Dispatchers.IO) { Fotos.importar(contexto, origem) }
            if (nova != null) foto = nova else erro = t.fotoFalhou
            aPreparar = false
        }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tirada ->
        val destino = captura.toUri()
        captura = ""
        if (tirada) guardarFoto(destino) else Fotos.apagarSeNossa(contexto, destino.toString())
    }
    fun tirarFotografia() {
        val destino = Fotos.novoDestino(contexto)
        captura = destino.toString()
        try {
            camara.launch(destino)
        } catch (e: ActivityNotFoundException) {
            Fotos.apagarSeNossa(contexto, captura)
            captura = ""
            erro = t.semCamara
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) guardarFoto(uri)
    }
    fun escolherDaGaleria() {
        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val momentos = listOf(8 * 60 to t.manha, 13 * 60 to t.almoco, 20 * 60 to t.jantar, 22 * 60 to t.deitar)
    val outras = horas.filter { h -> momentos.none { it.first == h } }

    Column(Modifier.fillMaxWidth()) {
        Cabecalho(titulo = if (medicamento == null) t.novoMedicamento else t.editar, aoVoltar = aoVoltar)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = MARGEM, end = MARGEM, top = 6.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ---- a caixa ------------------------------------------------------
            TituloSeccao(t.fotografia)
            SeccaoFotografia(
                foto = foto,
                aPreparar = aPreparar,
                aoTirar = ::tirarFotografia,
                aoEscolher = ::escolherDaGaleria,
                aoRemover = { foto = "" },
            )

            // ---- o quê --------------------------------------------------------
            TituloSeccao(t.oQueDizACaixa)
            Cartao {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Campo(nome, { nome = it; erro = "" }, t.nome, t.nomeAjuda, palavras = true)
                    Campo(dose, { dose = it }, t.dose, t.doseAjuda)
                    Campo(nota, { nota = it }, t.nota, t.notaAjuda)
                }
            }

            // ---- quando -------------------------------------------------------
            TituloSeccao(t.horas)
            Text(t.horasAjuda, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            FlowRow(
                maxItemsInEachRow = if (textoGrande()) 1 else 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                for ((hora, rotulo) in momentos) {
                    val ligado = hora in horas
                    Momento(
                        rotulo = rotulo,
                        hora = hora,
                        ligado = ligado,
                        aoTocar = { mudarHoras(if (ligado) horas - hora else horas + hora) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (outras.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (h in outras) ChipHora(h, aoRemover = { mudarHoras(horas - h) })
                }
            }
            BotaoSecundario(t.outraHora, { escolherHora = true }, icone = Icones.Relogio)

            if (erro.isNotEmpty()) {
                Text(erro, style = MaterialTheme.typography.bodyLarge, color = EmergenciaTexto)
            }

            BotaoPrincipal(
                texto = t.guardar,
                icone = Icones.Visto,
                aoTocar = {
                    val nomeLimpo = nome.trim()
                    if (nomeLimpo.length < 2) {
                        erro = t.erroNome
                    } else {
                        aoGuardar(
                            Medicamento(
                                id = medicamento?.id ?: UUID.randomUUID().toString(),
                                nome = nomeLimpo,
                                dose = dose.trim(),
                                horas = horas,
                                nota = nota.trim(),
                                foto = foto.ifBlank { null },
                            ),
                        )
                        aoVoltar()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            if (medicamento != null) {
                BotaoContorno(
                    texto = t.apagarMedicamento,
                    aoTocar = { confirmarApagar = true },
                    icone = Icones.Lixo,
                    cor = EmergenciaTexto,
                )
            }
        }
    }

    if (escolherHora) {
        val estado = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { escolherHora = false },
            containerColor = Papel,
            title = { Text(t.escolherHora, style = MaterialTheme.typography.titleLarge) },
            text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimeInput(state = estado) } },
            confirmButton = {
                TextButton(onClick = {
                    mudarHoras(horas + (estado.hour * 60 + estado.minute))
                    escolherHora = false
                }) { Text(t.ok, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { escolherHora = false }) { Text(t.cancelar, style = MaterialTheme.typography.labelLarge) }
            },
        )
    }

    if (confirmarApagar && medicamento != null) {
        AlertDialog(
            onDismissRequest = { confirmarApagar = false },
            containerColor = Papel,
            icon = { Icon(Icones.Lixo, contentDescription = null, tint = EmergenciaTexto) },
            title = { Text(t.apagarTituloDe(medicamento.nome), style = MaterialTheme.typography.titleLarge) },
            text = { Text(t.apagarTexto, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarApagar = false
                    aoApagar(medicamento.id)
                    aoVoltar()
                }) { Text(t.apagar, color = EmergenciaTexto, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarApagar = false }) { Text(t.cancelar, style = MaterialTheme.typography.labelLarge) }
            },
        )
    }
}

/**
 * No photo yet: two big tiles, the camera first and filled. With a photo: the photo itself, large enough to
 * read the box from while typing, then "take another" / "from the gallery" / "remove".
 */
@Composable
private fun SeccaoFotografia(
    foto: String,
    aPreparar: Boolean,
    aoTirar: () -> Unit,
    aoEscolher: () -> Unit,
    aoRemover: () -> Unit,
) {
    val t = LocalTextos.current
    val grande = textoGrande()
    when {
        aPreparar -> Cartao {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = AzulVendanza, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Text(t.aGuardarFoto, style = MaterialTheme.typography.bodyLarge, color = AzulNoite)
            }
        }

        foto.isBlank() -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(t.fotografiaAjuda, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            val camara: @Composable (Modifier) -> Unit = { m ->
                BotaoGrande(t.tirarFotografia, Icones.Fotografia, aoTirar, m, detalhe = t.comACamara, destaque = true)
            }
            val galeria: @Composable (Modifier) -> Unit = { m ->
                BotaoGrande(t.daGaleria, Icones.Imagem, aoEscolher, m, detalhe = t.fotoQueJaTem)
            }
            if (grande) {
                camara(Modifier.fillMaxWidth())
                galeria(Modifier.fillMaxWidth())
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    camara(Modifier.weight(1f))
                    galeria(Modifier.weight(1f))
                }
            }
        }

        else -> Cartao(preenchimento = PaddingValues(10.dp)) {
            FotoDaEmbalagem(
                uri = foto,
                forma = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
            Spacer(Modifier.height(10.dp))
            val botoes: @Composable (Modifier) -> Unit = { m ->
                BotaoSecundario(t.tirarOutra, aoTirar, m, icone = Icones.Fotografia)
            }
            if (grande) {
                botoes(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                BotaoSecundario(t.daGaleria, aoEscolher, Modifier.fillMaxWidth(), icone = Icones.Imagem)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    botoes(Modifier.weight(1f))
                    BotaoSecundario(t.daGaleria, aoEscolher, Modifier.weight(1f), icone = Icones.Imagem)
                }
            }
            Spacer(Modifier.height(8.dp))
            BotaoContorno(t.removerFotografia, aoRemover, icone = Icones.Fechar, cor = TextoSecundario)
        }
    }
}

@Composable
private fun Campo(valor: String, aoMudar: (String) -> Unit, rotulo: String, ajuda: String, palavras: Boolean = false) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        supportingText = { Text(ajuda, style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(
            capitalization = if (palavras) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulVendanza,
            focusedLabelColor = AzulVendanza,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** One moment of the day as a big toggle: "Manhã · 08:00". Selected = blue fill and a tick. */
@Composable
private fun Momento(rotulo: String, hora: Int, ligado: Boolean, aoTocar: () -> Unit, modifier: Modifier = Modifier) {
    // the toggleable Surface overload: TalkBack announces "Manhã, 08:00, selecionado / não selecionado"
    Surface(
        checked = ligado,
        onCheckedChange = { aoTocar() },
        shape = MaterialTheme.shapes.medium,
        color = if (ligado) AzulVendanza else Papel,
        contentColor = if (ligado) Papel else AzulNoite,
        border = if (ligado) null else BorderStroke(1.dp, Linha),
        modifier = modifier.heightIn(min = 64.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Papel,
                border = if (ligado) null else BorderStroke(2.dp, TextoSecundario),
                modifier = Modifier.size(26.dp),
            ) {
                if (ligado) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icones.Visto, contentDescription = null, tint = AzulVendanza, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(rotulo, style = MaterialTheme.typography.titleSmall)
                Text(Regras.horaCurta(hora), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
