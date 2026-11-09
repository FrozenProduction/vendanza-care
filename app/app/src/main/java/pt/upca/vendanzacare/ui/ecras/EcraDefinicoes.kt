package pt.upca.vendanzacare.ui.ecras

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.R
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.LocalTextos
import pt.upca.vendanzacare.ui.componentes.Cartao
import pt.upca.vendanzacare.ui.componentes.IconeEmCirculo
import pt.upca.vendanzacare.ui.componentes.Icones
import pt.upca.vendanzacare.ui.componentes.MARGEM
import pt.upca.vendanzacare.ui.componentes.NotaPrivacidade
import pt.upca.vendanzacare.ui.componentes.TituloEcra
import pt.upca.vendanzacare.ui.componentes.TituloSeccao
import pt.upca.vendanzacare.ui.tema.AzulNoite
import pt.upca.vendanzacare.ui.tema.AzulVendanza
import pt.upca.vendanzacare.ui.tema.Papel
import pt.upca.vendanzacare.ui.tema.TextoSecundario
import pt.upca.vendanzacare.ui.tema.TomAzul
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Definições replaced Ajuda. The emergency contacts did not move here — they are already on Início, one tap
 * from anywhere the person lands, and duplicating the red band in two tabs makes neither of them special.
 *
 * Everything on this screen is stored locally: name and date of birth exist so the app can greet the person
 * and print a proper header on the medication list they hand to a professional.
 *
 * Choices are radio rows inside a card, each showing what it does ("Aa" at that size), instead of the old
 * stack of full-width buttons where the selected one was told apart only by fill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraDefinicoes(
    escala: Float,
    aoMudarEscala: (Float) -> Unit,
    idioma: String,
    aoMudarIdioma: (String) -> Unit,
    nome: String,
    aoMudarNome: (String) -> Unit,
    nascimento: String,
    aoMudarNascimento: (String) -> Unit,
) {
    val t = LocalTextos.current
    val contexto = LocalContext.current
    var calendarioAberto by remember { mutableStateOf(false) }

    val versao = remember {
        runCatching {
            contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = MARGEM, end = MARGEM, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TituloEcra(t.definicoes, lateral = 0.dp)

        // ---- perfil -------------------------------------------------------
        TituloSeccao(t.perfil)
        Cartao {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(nome)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    if (nome.isBlank()) {
                        // nothing typed yet: say what the name is for instead of echoing the field's label
                        Text(t.oSeuNomeAjuda, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    } else {
                        Text(nome, style = MaterialTheme.typography.titleLarge, color = AzulNoite)
                    }
                    if (nascimento.isNotBlank()) {
                        Text(Regras.dataCurta(nascimento), style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = nome,
                onValueChange = aoMudarNome,
                label = { Text(t.oSeuNome) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulVendanza, focusedLabelColor = AzulVendanza),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = { calendarioAberto = true },
                shape = MaterialTheme.shapes.small,
                color = Papel,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icones.Calendario, contentDescription = null, tint = AzulVendanza)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.dataNascimento, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        Text(
                            if (nascimento.isBlank()) t.escolherData else Regras.dataCurta(nascimento),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AzulNoite,
                        )
                    }
                    Icon(Icones.Seguinte, contentDescription = null, tint = TextoSecundario)
                }
            }
        }

        // ---- texto --------------------------------------------------------
        TituloSeccao(t.tamanhoTexto, Modifier.padding(top = 8.dp))
        Text(t.exemploTexto, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
        val tamanhos = listOf(
            Triple(1f, t.normal, MaterialTheme.typography.bodyMedium),
            Triple(1.3f, t.grande, MaterialTheme.typography.titleLarge),
            Triple(1.6f, t.muitoGrande, MaterialTheme.typography.headlineMedium),
        )
        GrupoEscolha {
            for ((valor, rotulo, amostra) in tamanhos) {
                LinhaEscolha(
                    selecionado = escala == valor,
                    rotulo = rotulo,
                    aoTocar = { aoMudarEscala(valor) },
                    inicio = { Amostra("Aa", amostra) },
                )
            }
        }

        // ---- idioma -------------------------------------------------------
        TituloSeccao(t.idiomaTitulo, Modifier.padding(top = 8.dp))
        GrupoEscolha {
            LinhaEscolha(idioma == "pt", t.portugues, { aoMudarIdioma("pt") }) {
                Amostra("PT", MaterialTheme.typography.labelLarge)
            }
            LinhaEscolha(idioma == "en", t.ingles, { aoMudarIdioma("en") }) {
                Amostra("EN", MaterialTheme.typography.labelLarge)
            }
        }

        // ---- sobre --------------------------------------------------------
        TituloSeccao(t.sobre, Modifier.padding(top = 8.dp))
        Cartao {
            // the vertical lockup, as the manual places it on a cover; its white ground matches the card
            Image(
                painter = painterResource(R.drawable.marca_vertical),
                contentDescription = "VendanzaCare",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(132.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(t.sobreTexto, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            Spacer(Modifier.height(12.dp))
            Text(String.format(t.versao, versao), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        }
        NotaPrivacidade(t.listaSoTelemovel, Modifier.padding(top = 4.dp))
    }

    if (calendarioAberto) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = nascimento.let { isoParaMillis(it) },
            yearRange = 1910..2026,
        )
        DatePickerDialog(
            onDismissRequest = { calendarioAberto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { aoMudarNascimento(millisParaIso(it)) }
                    calendarioAberto = false
                }) { Text(t.guardar) }
            },
            dismissButton = {
                TextButton(onClick = { calendarioAberto = false }) { Text(t.cancelar) }
            },
        ) {
            DatePicker(state = estado)
        }
    }
}

/** Initials when there is a name, a person outline when there is not. */
@Composable
private fun Avatar(nome: String) {
    val iniciais = nome.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .let { p -> (p.firstOrNull()?.take(1).orEmpty() + if (p.size > 1) p.last().take(1) else "").uppercase() }
    if (iniciais.isBlank()) {
        IconeEmCirculo(Icones.Pessoa, TomAzul, tamanho = 64.dp)
    } else {
        Box(
            Modifier
                .size(64.dp)
                .background(AzulVendanza, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(iniciais, style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

@Composable
private fun GrupoEscolha(conteudo: @Composable () -> Unit) {
    Cartao(preenchimento = PaddingValues(6.dp)) {
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(2.dp)) { conteudo() }
    }
}

@Composable
private fun Amostra(texto: String, estilo: TextStyle) {
    Box(Modifier.widthIn(min = 56.dp), contentAlignment = Alignment.CenterStart) {
        Text(texto, style = estilo, color = AzulNoite)
    }
}

/** A radio row: what it is on the left, the name, and a radio dot. The whole row is the target. */
@Composable
private fun LinhaEscolha(
    selecionado: Boolean,
    rotulo: String,
    aoTocar: () -> Unit,
    inicio: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (selecionado) TomAzul.fundo else Color.Transparent)
            .selectable(selected = selecionado, role = Role.RadioButton, onClick = aoTocar)
            .heightIn(min = 64.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        inicio()
        Spacer(Modifier.width(10.dp))
        Text(
            rotulo,
            style = MaterialTheme.typography.titleSmall,
            color = if (selecionado) AzulVendanza else AzulNoite,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .size(26.dp)
                .background(if (selecionado) AzulVendanza else Papel, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selecionado) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(Papel, CircleShape),
                )
            } else {
                Surface(shape = CircleShape, color = Papel, border = BorderStroke(2.dp, TextoSecundario), modifier = Modifier.size(26.dp)) {}
            }
        }
    }
}

// ---- datas: ISO no disco, europeu no ecrã ---------------------------------
// java.time needs API 26 or desugaring; SimpleDateFormat + a UTC Calendar works on minSdk 24 with no extra
// dependency. The picker hands back UTC midnight, so the conversion has to be UTC on both sides or the date
// shifts by a day.

private fun isoParaMillis(iso: String): Long? {
    if (iso.isBlank()) return null
    return runCatching {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        formato.parse(iso)?.time
    }.getOrNull()
}

private fun millisParaIso(millis: Long): String {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millis))
}
