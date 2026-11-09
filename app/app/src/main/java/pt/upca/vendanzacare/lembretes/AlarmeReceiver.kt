package pt.upca.vendanzacare.lembretes

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import pt.upca.vendanzacare.MainActivity
import pt.upca.vendanzacare.R
import pt.upca.vendanzacare.dados.Fotos
import pt.upca.vendanzacare.dados.MedicacaoStore
import pt.upca.vendanzacare.dados.Preferencias
import pt.upca.vendanzacare.dados.RegistoTomas
import pt.upca.vendanzacare.dominio.Regras
import pt.upca.vendanzacare.ui.minutoDoDia
import pt.upca.vendanzacare.ui.textosDe

/** Turns a dose alarm into a notification. Registered in the manifest, not exported. */
class AlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nome = intent.getStringExtra(Lembretes.EXTRA_NOME) ?: return
        val dose = intent.getStringExtra(Lembretes.EXTRA_DOSE).orEmpty()
        val nota = intent.getStringExtra(Lembretes.EXTRA_NOTA).orEmpty()
        val hora = intent.getIntExtra(Lembretes.EXTRA_HORA, -1)
        val id = intent.getStringExtra(Lembretes.EXTRA_ID)
        val t = textosDe(runCatching { Preferencias(context).idioma }.getOrDefault("pt"))

        // the day this dose belongs to: given on a snoozed alarm, otherwise today — or yesterday when a late
        // alarm crossed midnight
        val dia = intent.getStringExtra(Lembretes.EXTRA_DIA)
            ?: RegistoTomas.dia(if (hora >= 0) Regras.diaDoAlarme(minutoDoDia(), hora) else 0)
        val registo = RegistoTomas(context)
        val jaTomada = id != null && hora >= 0 &&
            runCatching { Regras.chaveToma(id, hora) in registo.tomadas(dia) }.getOrDefault(false)

        val detalhe = listOf(dose, nota)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifBlank { t.notificacaoTexto }

        val abrir = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // The box photo, when there is one, as the notification's picture: recognising the box is faster than
        // reading its name, and for some people it is the only way. Looked up by id; old alarms (armed before
        // the id was added) have none and simply show no picture until they re-arm.
        val foto = id?.let { i -> runCatching { MedicacaoStore(context).listar().firstOrNull { it.id == i }?.foto }.getOrNull() }
        val miniatura = Fotos.miniatura(context, foto)

        val construtor = NotificationCompat.Builder(context, Lembretes.CANAL)
            // the brand's one-colour symbol (brand/mark/…-uma-cor-branco.png), the status-bar silhouette Android asks for
            .setSmallIcon(R.drawable.ic_stat_marca)
            .setColor(COR_MARCA)
            .setLargeIcon(miniatura)
            .setContentTitle(t.tituloNotificacao(nome))
            .setContentText(if (hora >= 0) "${Regras.horaCurta(hora)} · $detalhe" else detalhe)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalhe))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(abrir)

        // "Já tomei" first: it is the answer to the question the notification asks. Old alarms armed before
        // ids existed have no id and get no buttons — they still ring, and re-arm with ids right after.
        if (id != null && hora >= 0) {
            construtor
                .addAction(R.drawable.ic_stat_marca, t.jaTomei, acao(context, intent, TomaReceiver.ACAO_TOMEI, id, hora, dia))
                .addAction(R.drawable.ic_stat_marca, t.daquiA10, acao(context, intent, TomaReceiver.ACAO_ADIAR, id, hora, dia))
        }
        val notificacao = construtor.build()
        val numero = if (id != null && hora >= 0) Lembretes.idNotificacao(id, hora) else nome.hashCode()

        // Android 13+ needs POST_NOTIFICATIONS at runtime; before that it is granted at install. Checked here
        // rather than trusting the system to drop the notification: a receiver must never crash, and the check
        // is also what makes the intent explicit to a reader (and to lint).
        // marked taken already (in the app, ahead of time): nothing to ask, stay quiet
        if (!jaTomada && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED)
        ) {
            runCatching { NotificationManagerCompat.from(context).notify(numero, notificacao) }
        }

        // Each alarm fires once. Re-arm from the stored list (not from this intent) so tomorrow's dose rings even
        // if the app is never opened, and a medicine deleted in the meantime is not brought back.
        runCatching { Lembretes.reagendar(context, MedicacaoStore(context).listar()) }
    }

    /** A button's broadcast to [TomaReceiver], carrying the dose and its day. One request code per dose and button. */
    private fun acao(context: Context, alarme: Intent, acao: String, id: String, hora: Int, dia: String): PendingIntent {
        val intent = Intent(context, TomaReceiver::class.java).apply {
            this.action = acao
            putExtras(alarme)
            putExtra(Lembretes.EXTRA_DIA, dia)
        }
        return PendingIntent.getBroadcast(
            context,
            "$acao-$id-$hora".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        /** Turquesa Cuidado, #00786B — tints the symbol and the app name in the notification shade. */
        const val COR_MARCA = 0xFF00786B.toInt()
    }
}
