package pt.upca.vendanzacare.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.upca.vendanzacare.R

/**
 * **Atkinson Hyperlegible Next** — drawn by the Braille Institute for readers with low vision (SIL OFL 1.1).
 *
 * It replaced Nunito in the November UI pass. Nunito is warm but its rounded forms blur at small sizes, and
 * its `l`, `I` and `1` are near-identical — on a medication screen "1 comprimido" and "l comprimido" must
 * never be confused. Atkinson gives every one of those glyphs a distinct shape (tailed `l`, serifed `I`,
 * slashed-looking `0`) while staying modern and friendly.
 *
 * Four static weights (400/600/700/800), Latin + Latin-1, 46 KB each, from Google Fonts. Static files on
 * purpose: variable-font axes are ignored below API 26, and minSdk is 24.
 *
 * Nunito (the brand face) is no longer bundled: the only place it appeared, the "VendanzaCare" wordmark, is now
 * the official lockup image (`drawable-nodpi/marca_horizontal.png`), so the brand is drawn, not re-typeset.
 */
val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_semibold, FontWeight.SemiBold),
    Font(R.font.atkinson_bold, FontWeight.Bold),
    Font(R.font.atkinson_extrabold, FontWeight.ExtraBold),
)

/** @param espaco tracking as a fraction of the size (-0.01 = -1 %). */
private fun estilo(tamanho: Int, linha: Int, peso: FontWeight, espaco: Double = 0.0) = TextStyle(
    fontFamily = Atkinson,
    fontWeight = peso,
    fontSize = tamanho.sp,
    lineHeight = linha.sp,
    // in sp, not em: Material's text field animates its label between our styles and its own defaults,
    // which are in sp, and lerping em with sp throws IllegalArgumentException (crashed Definições)
    letterSpacing = (espaco * tamanho).sp,
)

/**
 * One notch above Material 3 defaults, everywhere. The audience reads with effort at arm's length, and body
 * text below 18 sp was the first thing to fail in testing.
 *
 * Screens must never write a literal `sp` — they use these styles, so the Definições text-size control scales
 * all of them. Headings are tight (-1 %) and heavy; body text is loose (1.55 line height) and regular.
 */
val Tipografia = Typography(
    displayLarge = estilo(56, 60, FontWeight.ExtraBold, -0.02),
    displayMedium = estilo(48, 52, FontWeight.ExtraBold, -0.02),
    displaySmall = estilo(36, 42, FontWeight.ExtraBold, -0.015),
    headlineLarge = estilo(34, 40, FontWeight.ExtraBold, -0.01),
    headlineMedium = estilo(30, 36, FontWeight.ExtraBold, -0.01),
    headlineSmall = estilo(25, 31, FontWeight.Bold, -0.01),
    titleLarge = estilo(22, 28, FontWeight.Bold),
    titleMedium = estilo(20, 26, FontWeight.Bold),
    titleSmall = estilo(18, 24, FontWeight.SemiBold),
    bodyLarge = estilo(19, 29, FontWeight.Normal),
    bodyMedium = estilo(17, 25, FontWeight.Normal),
    // every slot is set: an unset one falls back to Roboto (the text-field label floats into bodySmall)
    bodySmall = estilo(15, 21, FontWeight.Normal),
    labelLarge = estilo(18, 22, FontWeight.Bold),
    labelMedium = estilo(15, 19, FontWeight.SemiBold, 0.01),
    labelSmall = estilo(14, 18, FontWeight.Bold, 0.04),
)
