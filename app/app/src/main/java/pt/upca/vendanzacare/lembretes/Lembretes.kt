package pt.upca.vendanzacare.lembretes

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import pt.upca.vendanzacare.R
import pt.upca.vendanzacare.dominio.Medicamento
import java.util.Calendar

/**
 * Dose reminders, one alarm per (medicine, time).
 *
 * Each alarm fires once, so the chain is kept alive in three places (architecture.md §10):
 *  - when the app opens and after every edit;
 *  - in [AlarmeReceiver], right after each dose notification — this is what makes tomorrow ring;
 *  - in [ArranqueReceiver], after reboot, update, or a clock / time-zone change.
 *
 * Deliberate ceiling: `setAndAllowWhileIdle` is inexact — under Doze the notification can arrive a few minutes
 * late. Exact alarms need SCHEDULE_EXACT_ALARM, which Play restricts. Upgrade path: that permission.
 */
object Lembretes {

    const val CANAL = "tomas"
    private const val PREFS = "vendanzacare_alarmes"
    private const val CHAVE = "pedidos"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gestor = context.getSystemService(NotificationManager::class.java) ?: return
        if (gestor.getNotificationChannel(CANAL) != null) return

        val som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val canal = NotificationChannel(
            CANAL,
            context.getString(R.string.canal_tomas),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.canal_tomas_descricao)
            enableVibration(true)
            setSound(som, atributos)
        }
        gestor.createNotificationChannel(canal)
    }

    /** Cancels everything armed last time and arms the current list. Safe to call on every launch. */
    fun reagendar(context: Context, medicamentos: List<Medicamento>) {
        criarCanal(context)
        val gestor = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        for (codigo in prefs.getStringSet(CHAVE, emptySet()).orEmpty()) {
            val antigo = codigo.toIntOrNull() ?: continue
            val pendente = pendente(context, antigo, intentVazio(context))
            gestor.cancel(pendente)
            pendente.cancel()
        }

        val novos = mutableSetOf<String>()
        val agora = System.currentTimeMillis()
        for (m in medicamentos) {
            for (hora in m.horas) {
                val codigo = pedidoId(m.id, hora)
                val pendente = pendente(context, codigo, intentCom(context, m, hora))
                val disparo = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hora / 60)
                    set(Calendar.MINUTE, hora % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (disparo.timeInMillis <= agora) disparo.add(Calendar.DAY_OF_YEAR, 1)
                gestor.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.timeInMillis, pendente)
                novos += codigo.toString()
            }
        }
        prefs.edit().putStringSet(CHAVE, novos).apply()
    }

    private fun pedidoId(idMedicamento: String, hora: Int): Int = "$idMedicamento-$hora".hashCode()

    /** One notification per (medicine, time), so "Já tomei" closes exactly the one it was pressed on. */
    fun idNotificacao(idMedicamento: String, hora: Int): Int = "aviso-$idMedicamento-$hora".hashCode()

    /** Removes the dose notification if it is still on screen — marked taken in the app, nothing left to ask. */
    fun fecharNotificacao(context: Context, idMedicamento: String, hora: Int) {
        runCatching { NotificationManagerCompat.from(context).cancel(idNotificacao(idMedicamento, hora)) }
    }

    /**
     * "Daqui a 10 min": rings the same dose again later, for the same [dia]. Its own request code, so the
     * daily alarm for that dose is untouched, and re-snoozing just moves this one.
     */
    fun adiar(context: Context, original: Intent, dia: String, minutos: Int = ADIAR_MINUTOS) {
        val gestor = context.getSystemService(AlarmManager::class.java) ?: return
        val id = original.getStringExtra(EXTRA_ID) ?: return
        val hora = original.getIntExtra(EXTRA_HORA, -1)
        val intent = Intent(context, AlarmeReceiver::class.java).apply {
            putExtras(original)
            action = null
            putExtra(EXTRA_DIA, dia)
        }
        val pendente = pendente(context, "adiar-$id-$hora".hashCode(), intent)
        gestor.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + minutos * 60_000L,
            pendente,
        )
    }

    private fun intentVazio(context: Context) = Intent(context, AlarmeReceiver::class.java)

    private fun intentCom(context: Context, m: Medicamento, hora: Int) =
        Intent(context, AlarmeReceiver::class.java).apply {
            putExtra(EXTRA_ID, m.id)
            putExtra(EXTRA_NOME, m.nome)
            putExtra(EXTRA_DOSE, m.dose)
            putExtra(EXTRA_NOTA, m.nota)
            putExtra(EXTRA_HORA, hora)
        }

    private fun pendente(context: Context, codigo: Int, intent: Intent) =
        android.app.PendingIntent.getBroadcast(
            context,
            codigo,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

    const val EXTRA_ID = "id"
    const val EXTRA_NOME = "nome"
    const val EXTRA_DOSE = "dose"
    const val EXTRA_NOTA = "nota"
    const val EXTRA_HORA = "hora"
    /** Set only on a snoozed alarm: the day the dose belongs to, which may no longer be today. */
    const val EXTRA_DIA = "dia"
    const val ADIAR_MINUTOS = 10
}
