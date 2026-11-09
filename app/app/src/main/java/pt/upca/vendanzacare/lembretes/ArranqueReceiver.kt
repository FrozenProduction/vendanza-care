package pt.upca.vendanzacare.lembretes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pt.upca.vendanzacare.dados.MedicacaoStore

/**
 * Re-arms every dose alarm when the system forgets them: after a reboot, after an app update, and when the
 * clock or time zone changes (summer time, a phone that was off for days). Without this, the reminders only
 * came back the next time the person opened the app — and the people who most need them open it least.
 *
 * Only reacts to the system broadcasts declared in the manifest; not exported, so no other app can reach it.
 */
class ArranqueReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in ACOES) return
        runCatching { Lembretes.reagendar(context, MedicacaoStore(context).listar()) }
    }

    private companion object {
        val ACOES = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
