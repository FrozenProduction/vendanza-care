package pt.upca.vendanzacare.ui

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Opens the dialer with the number already typed. ACTION_DIAL needs no permission and cannot place a call
 * by itself — the person still presses the green button, which is the point.
 */
fun discar(context: Context, numero: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$numero".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
