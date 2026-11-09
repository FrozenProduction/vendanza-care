package pt.upca.vendanzacare.ui.tema

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand palette — every value measured in uni/Projeto-50-10/brand/README.md.
// Colours are never the only signal: severity also carries an icon, a shape and a label.
val AzulVendanza = Color(0xFF0B5FA5)      // 6.57:1 on white
val TurquesaCuidado = Color(0xFF00786B)   // 5.39:1 on white
val TurquesaEscuro = Color(0xFF006056)    // 7.14:1 on white
val AzulNoite = Color(0xFF0B1A2B)         // 17.55:1 on white

// Surfaces. Warm paper instead of the old cool grey: it reads less "hospital form" and lets the white cards
// stand out without shadows.
val Areia = Color(0xFFF5F2EC)
val Papel = Color(0xFFFFFFFF)
val Linha = Color(0xFFE4DED3)

val TextoPrincipal = Color(0xFF15212B)    // 14.6:1 on Areia
val TextoSecundario = Color(0xFF4D5A63)   // 6.3:1 on Areia, 7.1:1 on white

// Semantic tokens. Urgency keeps the pair the prototype settled on (#FFDAD6 / #8C1D18).
val EmergenciaFundo = Color(0xFFFDE3DF)
val EmergenciaTexto = Color(0xFF8C1D18)   // 7.5:1 on EmergenciaFundo
val AvisoFundo = Color(0xFFFFF1D0)
val AvisoTexto = Color(0xFF6B4A00)        // 7.2:1 on AvisoFundo

/** The one gradient in the app: the "próxima toma" card. White text holds ≥ 6.5:1 at both ends. */
val GradienteToma = Brush.linearGradient(listOf(Color(0xFF0B5FA5), Color(0xFF00695E)))

/**
 * Soft tint + strong ink, one pair per topic. Each ink colour holds ≥ 5.5:1 on its own tint and ≥ 6.5:1 on white, so
 * an icon or a title in that colour stays readable wherever it lands.
 */
data class Tom(val fundo: Color, val tinta: Color)

val TomAzul = Tom(Color(0xFFE2EDF8), Color(0xFF0B5FA5))
val TomTurquesa = Tom(Color(0xFFDCF0EB), Color(0xFF00685C))
val TomAmbar = Tom(Color(0xFFFBEDD3), Color(0xFF7D5200))
val TomCoral = Tom(Color(0xFFFBE3DB), Color(0xFF9E3B24))
val TomLilas = Tom(Color(0xFFECE6F6), Color(0xFF5A3E96))
val TomVerde = Tom(Color(0xFFE2F0DD), Color(0xFF2D6A29))

val Tons = listOf(TomAzul, TomTurquesa, TomAmbar, TomCoral, TomLilas, TomVerde)

/** A stable colour per medicine, so "the orange one" stays orange after a restart. */
fun tomDe(chave: String): Tom = Tons[Math.floorMod(chave.hashCode(), Tons.size)]
