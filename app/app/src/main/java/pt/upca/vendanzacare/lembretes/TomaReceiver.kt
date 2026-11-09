package pt.upca.vendanzacare.lembretes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pt.upca.vendanzacare.dados.RegistoTomas
import pt.upca.vendanzacare.dominio.Regras

/**
 * The two buttons on a dose notification. Not exported: only our own PendingIntents reach it.
 *
 * - **Já tomei** records the dose for its day and closes the notification. The home screen, if open, updates
 *   by itself (it listens to [RegistoTomas]).
 * - **Daqui a 10 min** closes it and rings the same dose again in 10 minutes.
 *
 * Neither opens the app: answering from the notification shade is the whole point.
 */
class TomaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(Lembretes.EXTRA_ID) ?: return
        val hora = intent.getIntExtra(Lembretes.EXTRA_HORA, -1)
        if (hora < 0) return
        val dia = intent.getStringExtra(Lembretes.EXTRA_DIA) ?: RegistoTomas.dia()

        when (intent.action) {
            ACAO_TOMEI -> runCatching {
                RegistoTomas(context).marcar(dia, listOf(Regras.chaveToma(id, hora)), tomada = true)
            }
            ACAO_ADIAR -> runCatching { Lembretes.adiar(context, intent, dia) }
            else -> return
        }
        Lembretes.fecharNotificacao(context, id, hora)
    }

    companion object {
        const val ACAO_TOMEI = "pt.upca.vendanzacare.TOMEI"
        const val ACAO_ADIAR = "pt.upca.vendanzacare.ADIAR"
    }
}
