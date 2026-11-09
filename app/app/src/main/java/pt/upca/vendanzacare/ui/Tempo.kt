package pt.upca.vendanzacare.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Calendar

/** Minutes since midnight, refreshed every half minute so "próxima toma" does not go stale while open. */
@Composable
fun minutoAgora(): Int {
    var minuto by remember { mutableStateOf(minutoDoDia()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            minuto = minutoDoDia()
        }
    }
    return minuto
}

fun minutoDoDia(): Int {
    val agora = Calendar.getInstance()
    return agora.get(Calendar.HOUR_OF_DAY) * 60 + agora.get(Calendar.MINUTE)
}
