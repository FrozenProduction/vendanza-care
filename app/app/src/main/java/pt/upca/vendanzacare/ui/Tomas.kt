package pt.upca.vendanzacare.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import pt.upca.vendanzacare.dados.RegistoTomas
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.lembretes.Lembretes

/**
 * The doses marked taken, as Compose state. Reading [de] inside a composable subscribes it: when a mark
 * changes — a tap here, or "Já tomei" on the notification while the app is open — that composable redraws.
 */
@Stable
class Tomas internal constructor(private val contexto: Context, private val registo: RegistoTomas) {

    private var versao by mutableIntStateOf(0)

    fun de(dia: String): Set<String> {
        versao // the read is what subscribes the caller
        return registo.tomadas(dia)
    }

    /** Marks or unmarks [hora] for each medicine id, and clears their notifications if they are still up. */
    fun marcar(dia: String, ids: List<String>, hora: Int, tomada: Boolean) {
        registo.marcar(dia, ids.map { Regras.chaveToma(it, hora) }, tomada)
        if (tomada) ids.forEach { Lembretes.fecharNotificacao(contexto, it, hora) }
    }

    internal fun mudou() {
        versao++
    }
}

@Composable
fun rememberTomas(): Tomas {
    val contexto = LocalContext.current.applicationContext
    val registo = remember { RegistoTomas(contexto) }
    val tomas = remember { Tomas(contexto, registo) }
    DisposableEffect(registo) {
        val parar = registo.observar { tomas.mudou() }
        onDispose { parar() }
    }
    return tomas
}
