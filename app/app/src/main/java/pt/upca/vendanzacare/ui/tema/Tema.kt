package pt.upca.vendanzacare.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = AzulVendanza,
    onPrimary = Color.White,
    primaryContainer = TomAzul.fundo,
    onPrimaryContainer = Color(0xFF06355C),
    secondary = TurquesaCuidado,
    onSecondary = Color.White,
    secondaryContainer = TomTurquesa.fundo,
    onSecondaryContainer = TurquesaEscuro,
    tertiary = TurquesaEscuro,
    onTertiary = Color.White,
    background = Areia,
    onBackground = TextoPrincipal,
    surface = Papel,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFEFEBE3),
    onSurfaceVariant = TextoSecundario,
    surfaceContainerHigh = Papel,
    surfaceContainerHighest = Color(0xFFEFEBE3),
    outline = Color(0xFF6E747A), // 4.2:1 on Areia: text-field borders need ≥ 3:1
    outlineVariant = Linha,
    error = EmergenciaTexto,
    onError = Color.White,
    errorContainer = EmergenciaFundo,
    onErrorContainer = EmergenciaTexto,
)

/** Generous radii: the soft, rounded shapes are most of what makes the app look friendly rather than clinical. */
private val Formas = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Cap for the combined text scale. WCAG 2.1 1.4.4, which DL 83/2018 points to, asks for 200 %; past that, on a
 * 360 dp phone one warning box fills the screen. At system 1.3× with the app at 1.6× the total was 2.08× and
 * the medication list disappeared below the warning.
 */
const val ESCALA_MAXIMA = 2f

/** From this combined scale on, two-column layouts fall back to one column (see `textoGrande()`). */
const val ESCALA_UMA_COLUNA = 1.3f

/**
 * @param escala multiplier applied **on top of** the system font scale (1.0 / 1.3 / 1.6, chosen in
 * Definições); the product is capped at [ESCALA_MAXIMA].
 */
@Composable
fun TemaVendanzaCare(escala: Float = 1f, conteudo: @Composable () -> Unit) {
    val densidade = LocalDensity.current
    val total = (densidade.fontScale * escala).coerceAtMost(maxOf(ESCALA_MAXIMA, densidade.fontScale))
    CompositionLocalProvider(
        LocalDensity provides Density(densidade.density, total),
    ) {
        MaterialTheme(colorScheme = EsquemaClaro, typography = Tipografia, shapes = Formas, content = conteudo)
    }
}

/** True when text is big enough that side-by-side layouts would squeeze words onto one-word lines. */
@Composable
fun textoGrande(): Boolean = LocalDensity.current.fontScale >= ESCALA_UMA_COLUNA
