package pt.upca.vendanzacare

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pt.upca.vendanzacare.lembretes.Lembretes
import pt.upca.vendanzacare.ui.AppVendanzaCare

/**
 * One activity, one Compose tree. There is no second entry point on purpose: everything the app can do is a
 * route inside [AppVendanzaCare].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is light-only, so the bar icons must be dark even when the phone is in dark mode —
        // the default (auto) style draws white icons on our light background and the clock disappears.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        Lembretes.criarCanal(this)
        setContent { AppVendanzaCare() }
    }
}
