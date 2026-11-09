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
import pt.upca.vendanzacare.dominio.Regras

/** Turns a dose alarm into a notification. Registered in the manifest, not exported. */
class AlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nome = intent.getStringExtra(Lembretes.EXTRA_NOME) ?: return
        val dose = intent.getStringExtra(Lembretes.EXTRA_DOSE).orEmpty()
        val nota = intent.getStringExtra(Lembretes.EXTRA_NOTA).orEmpty()
        val hora = intent.getIntExtra(Lembretes.EXTRA_HORA, -1)

        val detalhe = listOf(dose, nota)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifBlank { "Está na hora da sua medicação." }

        val abrir = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // The box photo, when there is one, as the notification's picture: recognising the box is faster than
        // reading its name, and for some people it is the only way. Looked up by id; old alarms (armed before
        // the id was added) have none and simply show no picture until they re-arm.
        val id = intent.getStringExtra(Lembretes.EXTRA_ID)
        val foto = id?.let { i -> runCatching { MedicacaoStore(context).listar().firstOrNull { it.id == i }?.foto }.getOrNull() }
        val miniatura = Fotos.miniatura(context, foto)

        val notificacao = NotificationCompat.Builder(context, Lembretes.CANAL)
            // the brand's one-colour symbol (brand/mark/…-uma-cor-branco.png), the status-bar silhouette Android asks for
            .setSmallIcon(R.drawable.ic_stat_marca)
            .setColor(COR_MARCA)
            .setLargeIcon(miniatura)
            .setContentTitle("Está na hora: $nome")
            .setContentText(if (hora >= 0) "${Regras.horaCurta(hora)} · $detalhe" else detalhe)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalhe))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(abrir)
            .build()

        // Android 13+ needs POST_NOTIFICATIONS at runtime; before that it is granted at install. Checked here
        // rather than trusting the system to drop the notification: a receiver must never crash, and the check
        // is also what makes the intent explicit to a reader (and to lint).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            runCatching { NotificationManagerCompat.from(context).notify(nome.hashCode(), notificacao) }
        }

        // Each alarm fires once. Re-arm from the stored list (not from this intent) so tomorrow's dose rings even
        // if the app is never opened, and a medicine deleted in the meantime is not brought back.
        runCatching { Lembretes.reagendar(context, MedicacaoStore(context).listar()) }
    }

    private companion object {
        /** Turquesa Cuidado, #00786B — tints the symbol and the app name in the notification shade. */
        const val COR_MARCA = 0xFF00786B.toInt()
    }
}
