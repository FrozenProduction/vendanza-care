package pt.upca.vendanzacare.ui.ecras

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.dominio.Contacto
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.BotaoPrincipal
import pt.upca.vendanzacare.ui.componentes.BotaoRedondo
import pt.upca.vendanzacare.ui.componentes.BotaoSecundario
import pt.upca.vendanzacare.ui.componentes.Cabecalho
import pt.upca.vendanzacare.ui.componentes.CaixaDestaque
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.NotaPrivacidade
import pt.upca.vendanzacare.ui.componentes.TituloSeccao
import pt.upca.vendanzacare.ui.discar
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.EmergenciaFundo
import pt.upca.vendanzacare.ui.tema.EmergenciaTexto
import pt.upca.vendanzacare.ui.tema.Linha
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.Tom
import pt.upca.vendanzacare.ui.tema.TomAmbar
import pt.upca.vendanzacare.ui.tema.TomAzul
import pt.upca.vendanzacare.ui.tema.TomTurquesa
import java.util.UUID

/**
 * Emergency numbers, in two groups:
 *
 * - **National**, fixed: 112 (fire brigade, ambulance and police all come through it), SNS 24 and the Centro de
 *   Informação Antivenenos — the one to call after taking too much of a medicine, which is this app's subject.
 * - **The person's own**: the local fire brigade, a relative, the health centre. Local numbers differ from town
 *   to town, so the app does not guess them; the person (or whoever sets up the phone) adds them, typed or
 *   picked from the phone's contacts.
 *
 * Every number opens the dialer pre-filled (no CALL_PHONE permission); picking a contact uses the system
 * picker, which hands over that one number — the app never reads the address book (no READ_CONTACTS).
 */
@Composable
fun EcraContactos(
    contactos: List<Contacto>,
    aoGuardar: (Contacto) -> Unit,
    aoApagar: (String) -> Unit,
    aoVoltar: () -> Unit,
) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    var aEditar by remember { mutableStateOf<Contacto?>(null) }
    var semAgenda by remember { mutableStateOf(false) }

    val escolher = rememberLauncherForActivityResult(EscolherTelefone()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val lido = lerTelefone(contexto, uri)
        if (lido == null) {
            semAgenda = true
        } else {
            semAgenda = false
            aEditar = Contacto(id = "", nome = lido.first, numero = lido.second)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Cabecalho(titulo = t.contactosUrgencia, aoVoltar = aoVoltar)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = MARGEM, end = MARGEM, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(t.contactosIntro, style = MaterialTheme.typography.bodyLarge, color = TextoSecundario) }

            item { TituloSeccao(t.numerosNacionais, Modifier.padding(top = 8.dp)) }
            item {
                LinhaContacto(
                    titulo = "${t.emergencia} · 112",
                    numero = "112",
                    detalhe = t.emergenciaTexto,
                    icone = Icones.Telefone,
                    tom = Tom(Papel, EmergenciaTexto),
                    cor = EmergenciaFundo,
                )
            }
            item {
                LinhaContacto(
                    titulo = t.sns24,
                    numero = "808 24 24 24",
                    detalhe = t.sns24Texto,
                    icone = Icones.Conversa,
                    tom = TomAzul,
                )
            }
            item {
                LinhaContacto(
                    titulo = t.antivenenos,
                    numero = "800 250 250",
                    detalhe = t.antivenenosTexto,
                    icone = Icones.Frasco,
                    tom = TomAmbar,
                )
            }

            item { TituloSeccao(t.osMeusContactos, Modifier.padding(top = 12.dp)) }
            if (contactos.isEmpty()) {
                item { Text(t.contactosVazio, style = MaterialTheme.typography.bodyLarge, color = TextoSecundario) }
            }
            items(contactos, key = { it.id }) { c ->
                LinhaContacto(
                    titulo = c.nome,
                    numero = c.numero,
                    icone = Icones.Pessoa,
                    tom = TomTurquesa,
                    aoEditar = { aEditar = c },
                )
            }
            if (semAgenda) {
                item { CaixaDestaque(t.semContactos) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    BotaoPrincipal(
                        texto = t.adicionarContacto,
                        aoTocar = { aEditar = Contacto(id = "", nome = "", numero = "") },
                        icone = Icones.Mais,
                    )
                    BotaoSecundario(
                        texto = t.doTelemovel,
                        aoTocar = { runCatching { escolher.launch(Unit) }.onFailure { semAgenda = true } },
                        icone = Icones.Pessoa,
                    )
                }
            }
            item { NotaPrivacidade(t.contactosSoTelemovel) }
        }
    }

    aEditar?.let { inicial ->
        DialogoContacto(
            inicial = inicial,
            aoGuardar = {
                aoGuardar(it.copy(id = it.id.ifBlank { UUID.randomUUID().toString() }))
                aEditar = null
            },
            aoApagar = {
                aoApagar(inicial.id)
                aEditar = null
            },
            aoFechar = { aEditar = null },
        )
    }
}

/** One number as a card: tap anywhere to call. Own contacts also get an edit button. */
@Composable
private fun LinhaContacto(
    titulo: String,
    numero: String,
    icone: ImageVector,
    tom: Tom,
    detalhe: String? = null,
    cor: Color = Papel,
    aoEditar: (() -> Unit)? = null,
) {
    val contexto = LocalContext.current
    val t = LocalTextos.current
    Cartao(cor = cor, aoTocar = { discar(contexto, Regras.numeroParaMarcar(numero)) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconeEmCirculo(icone, tom, tamanho = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNoite)
                Text(numero, style = MaterialTheme.typography.titleLarge, color = if (cor == Papel) AzulVendanza else EmergenciaTexto)
                if (detalhe != null) {
                    Text(detalhe, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                }
            }
            Spacer(Modifier.width(10.dp))
            if (aoEditar != null) {
                BotaoRedondo(Icones.Lapis, t.editarContacto, aoEditar)
            } else {
                Surface(
                    shape = CircleShape,
                    color = if (cor == Papel) tom.fundo else Papel,
                    contentColor = tom.tinta,
                    modifier = Modifier.heightIn(min = 44.dp),
                ) {
                    Icon(
                        Icones.Telefone,
                        contentDescription = t.ligarParaDe(titulo),
                        modifier = Modifier.padding(11.dp),
                    )
                }
            }
        }
    }
}

/** New or edit. Suggestions fill the name in one tap ("Bombeiros"), the number field opens the phone keypad. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogoContacto(
    inicial: Contacto,
    aoGuardar: (Contacto) -> Unit,
    aoApagar: () -> Unit,
    aoFechar: () -> Unit,
) {
    val t = LocalTextos.current
    val novo = inicial.id.isBlank()
    var nome by remember { mutableStateOf(inicial.nome) }
    var numero by remember { mutableStateOf(inicial.numero) }
    var mostrarErro by remember { mutableStateOf(false) }
    val cores = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulVendanza, focusedLabelColor = AzulVendanza)

    AlertDialog(
        onDismissRequest = aoFechar,
        containerColor = Papel,
        title = {
            Text(if (novo) t.novoContacto else t.editarContacto, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (novo) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (s in t.sugestoesContacto) {
                            val escolhida = nome == s
                            Surface(
                                onClick = { nome = s },
                                shape = MaterialTheme.shapes.small,
                                color = if (escolhida) TomTurquesa.tinta else Papel,
                                contentColor = if (escolhida) Papel else AzulNoite,
                                border = if (escolhida) null else BorderStroke(1.dp, Linha),
                            ) {
                                Text(
                                    s,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text(t.nomeContacto) },
                    supportingText = { Text(t.nomeContactoAjuda, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = MaterialTheme.shapes.small,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = cores,
                    modifier = Modifier.fillMaxWidth(),
                )
                val erro = mostrarErro && !Regras.numeroValido(numero)
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text(t.numero) },
                    supportingText = {
                        Text(if (erro) t.erroNumero else t.numeroAjuda, style = MaterialTheme.typography.bodySmall)
                    },
                    isError = erro,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    shape = MaterialTheme.shapes.small,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = cores,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!novo) {
                    TextButton(onClick = aoApagar) {
                        Text(t.apagarContacto, color = EmergenciaTexto, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (Regras.numeroValido(numero)) {
                    aoGuardar(inicial.copy(nome = nome.trim().ifBlank { numero.trim() }, numero = numero.trim()))
                } else {
                    mostrarErro = true
                }
            }) { Text(t.guardar, style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text(t.cancelar, style = MaterialTheme.typography.labelLarge) }
        },
    )
}

/** The system's "pick a phone number" screen. Returns one data row the app may read once, or null. */
private class EscolherTelefone : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK).setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data.takeIf { resultCode == Activity.RESULT_OK }
}

/** Name and number of the picked row; null if the picker handed back something unreadable. */
private fun lerTelefone(contexto: Context, uri: Uri): Pair<String, String>? = runCatching {
    val colunas = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    )
    contexto.contentResolver.query(uri, colunas, null, null, null)?.use { c ->
        if (!c.moveToFirst()) return@use null
        val numero = c.getString(1).orEmpty()
        if (numero.isBlank()) null else c.getString(0).orEmpty() to numero
    }
}.getOrNull()
