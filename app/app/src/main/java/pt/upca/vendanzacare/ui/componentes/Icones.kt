package pt.upca.vendanzacare.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import pt.upca.vendanzacare.ui.tema.Tom
import pt.upca.vendanzacare.ui.tema.TomAmbar
import pt.upca.vendanzacare.ui.tema.TomAzul
import pt.upca.vendanzacare.ui.tema.TomCoral
import pt.upca.vendanzacare.ui.tema.TomLilas
import pt.upca.vendanzacare.ui.tema.TomTurquesa
import pt.upca.vendanzacare.ui.tema.TomVerde

/**
 * The app's own icon set: 24 × 24 grid, 2 dp round-capped strokes, drawn for this app.
 *
 * It replaced `material-icons-core`, whose 49 glyphs forced stand-ins that misled ("Falar" was an envelope,
 * "Evitar infeções" a refresh arrow, "Vacinação" a star). Every icon here depicts its subject, and they share
 * one line weight, so the screens read as a single designed thing. Tinting works as with any `Icon`.
 */
object Icones {
    // ---- navegação e ações -------------------------------------------------
    val Inicio by lazy { icone("M3.5 10.5 12 3.5l8.5 7", "M5.5 9v11h13V9", "M10 20v-5h4v5") }
    val Guias by lazy {
        icone(
            "M12 6.5C10 5 7 4.5 3.5 4.5v14c3.5 0 6.5.5 8.5 2 2-1.5 5-2 8.5-2v-14c-3.5 0-6.5.5-8.5 2z",
            "M12 6.5v14",
        )
    }
    val Comprimido by lazy { icone("M7 8.5h10a3.5 3.5 0 0 1 0 7H7a3.5 3.5 0 0 1 0-7z", "M12 8.5v7", rodar = -45f) }
    val Ajustes by lazy {
        icone(
            "M4 7h9", "M17 7h3", "M13 7a2 2 0 1 0 4 0a2 2 0 1 0-4 0z",
            "M4 17h3", "M11 17h9", "M7 17a2 2 0 1 0 4 0a2 2 0 1 0-4 0z",
        )
    }
    val Voltar by lazy { icone("M15 5l-7 7 7 7") }
    val Seguinte by lazy { icone("M9.5 5.5 16 12l-6.5 6.5") }
    val Partilhar by lazy {
        icone("M12 3.5v11", "M7.5 8 12 3.5 16.5 8", "M5 12.5v6a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-6")
    }
    val Telefone by lazy {
        icone(
            "M5 3.5h3.5l1.8 4.5-2.3 1.4a11 11 0 0 0 6.1 6.1l1.4-2.3 4.5 1.8V19a2 2 0 0 1-2 2A16.5 16.5 0 0 1 3 5.5a2 2 0 0 1 2-2z",
        )
    }
    val Ouvir by lazy {
        icone("M4 9.5h3.5L12 5.5v13l-4.5-4H4z", "M15.5 9a4 4 0 0 1 0 6", "M18.5 6.5a7.5 7.5 0 0 1 0 11")
    }
    val Parar by lazy { icone(cheio = "M7 6h10a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1z") }
    val Mais by lazy { icone("M12 5v14", "M5 12h14") }
    val Visto by lazy { icone("M5 12.5l4.5 4.5L19 7.5") }
    val Fechar by lazy { icone("M6.5 6.5l11 11", "M17.5 6.5l-11 11") }
    val Relogio by lazy { icone("M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0z", "M12 7.5V12l3 2") }
    val Lixo by lazy {
        icone("M4.5 7h15", "M9.5 7V4.5h5V7", "M6.5 7l1 13h9l1-13", "M10 11v5.5", "M14 11v5.5")
    }
    val Fotografia by lazy {
        icone(
            "M4 7.5h3l1.5-2.5h7L17 7.5h3a1 1 0 0 1 1 1V19a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8.5a1 1 0 0 1 1-1z",
            "M8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0z",
        )
    }
    val Imagem by lazy {
        icone(
            "M5 4.5h14a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 18V6A1.5 1.5 0 0 1 5 4.5z",
            "M3.5 16.5l5-5 4 4 2.5-2.5 5.5 5.5",
            "M14.5 9a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0-3 0z",
        )
    }
    val Cadeado by lazy {
        icone(
            "M7 11h10a1 1 0 0 1 1 1v7.5a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V12a1 1 0 0 1 1-1z",
            "M8.5 11V8a3.5 3.5 0 0 1 7 0v3",
        )
    }
    val Sino by lazy { icone("M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 20.5a2 2 0 0 0 4 0") }
    val Pergunta by lazy {
        icone(
            "M5 4.5h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-8l-5 3.5v-3.5H5a2 2 0 0 1-2-2v-9a2 2 0 0 1 2-2z",
            "M10 9a2 2 0 1 1 2.8 1.8c-.5.3-.8.7-.8 1.3",
            "M12 14.6v.01",
        )
    }
    val Calendario by lazy {
        icone(
            "M5.5 5.5h13a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1v-13a1 1 0 0 1 1-1z",
            "M4.5 10h15", "M8.5 3.5v4", "M15.5 3.5v4",
        )
    }
    val Lapis by lazy { icone("M4 20h4L19 9l-4-4L4 16z", "M13 7l4 4") }
    val Globo by lazy {
        icone(
            "M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0z", "M3 12h18",
            "M12 3c2.5 2.5 3.5 5.5 3.5 9s-1 6.5-3.5 9c-2.5-2.5-3.5-5.5-3.5-9s1-6.5 3.5-9z",
        )
    }
    val Informacao by lazy { icone("M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0z", "M12 11v5.5", "M12 7.8v.01") }
    val TextoTamanho by lazy { icone("M3 19l5-13 5 13", "M4.8 14.5h6.4", "M14 19l3.5-9 3.5 9", "M15.2 16h4.6") }

    // ---- temas -------------------------------------------------------------
    val Pessoa by lazy { icone("M8 8a4 4 0 1 0 8 0a4 4 0 1 0-8 0z", "M4.5 20.5c.8-3.8 3.9-6 7.5-6s6.7 2.2 7.5 6") }
    val Conversa by lazy {
        icone(
            "M5 4.5h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-8l-5 3.5v-3.5H5a2 2 0 0 1-2-2v-9a2 2 0 0 1 2-2z",
            "M8 9.5h8", "M8 13h5",
        )
    }
    val Balanca by lazy {
        icone(
            "M12 4v16", "M8 20h8", "M5 7h14",
            "M5 7l-3 6.5a3 3 0 0 0 6 0z", "M19 7l-3 6.5a3 3 0 0 0 6 0z",
        )
    }
    val Gota by lazy {
        icone(
            "M12 3.5c3.2 4.2 6 7.4 6 10.8a6 6 0 0 1-12 0c0-3.4 2.8-6.6 6-10.8z",
            "M9.3 14.5a2.7 2.7 0 0 0 2.7 2.7",
        )
    }
    val Cama by lazy {
        icone("M3 6v14", "M3 16h18", "M21 20v-6.5a3 3 0 0 0-3-3h-7.5V16", "M5.5 11.5a2 2 0 1 0 4 0a2 2 0 1 0-4 0z")
    }
    val Alerta by lazy {
        icone(
            "M10.3 4.2 2.6 17.5a2 2 0 0 0 1.7 3h15.4a2 2 0 0 0 1.7-3L13.7 4.2a2 2 0 0 0-3.4 0z",
            "M12 9.5v4.5", "M12 17.2v.01",
        )
    }
    val Frasco by lazy {
        icone(
            "M7.5 3.5h9v3.5h-9z",
            "M6 7h12v12.5a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 6 19.5z",
            "M12 11v6", "M9 14h6",
        )
    }
    val Degraus by lazy { icone("M3.5 20.5h5v-5h5v-5h5v-5h2", "M3.5 20.5h17") }
    val Crianca by lazy {
        icone(
            "M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0z",
            "M9 10.5v.01", "M15 10.5v.01", "M8.8 14.5a4 4 0 0 0 6.4 0",
            "M12 3c-1.5 1.5-1 3 .5 3",
        )
    }
    val Escudo by lazy {
        icone(
            "M12 3 19.5 6v5.5c0 4.6-3.2 8.3-7.5 9.5-4.3-1.2-7.5-4.9-7.5-9.5V6z",
            "M8.8 12.2l2.2 2.2 4.2-4.4",
        )
    }
    val Seringa by lazy {
        icone(
            "M7 9h9v6H7a1 1 0 0 1-1-1v-4a1 1 0 0 1 1-1z",
            "M2.5 12H6", "M2.5 9.5v5", "M9.5 9v2.5", "M12.5 9v2.5", "M16 12h5.5",
            rodar = -45f,
        )
    }
}

/**
 * Module icon + colour. The key comes from `conteudo.json`, so a new module only needs code when it needs a
 * new picture. Unknown key → information icon in blue, never a crash.
 */
fun iconeDoModulo(chave: String): ImageVector = when (chave) {
    "pessoa" -> Icones.Pessoa
    "falar" -> Icones.Conversa
    "direitos" -> Icones.Balanca
    "maos" -> Icones.Gota
    "cirurgia" -> Icones.Cama
    "medicacao" -> Icones.Comprimido
    "erro" -> Icones.Alerta
    "caixa" -> Icones.Frasco
    "queda" -> Icones.Degraus
    "crianca" -> Icones.Crianca
    "escudo" -> Icones.Escudo
    "vacina" -> Icones.Seringa
    else -> Icones.Informacao
}

/** Chosen so neighbours in the two-column grid never share a colour. */
fun tomDoModulo(chave: String): Tom = when (chave) {
    "pessoa", "cirurgia", "vacina" -> TomAzul
    "falar", "caixa" -> TomTurquesa
    "direitos", "queda" -> TomLilas
    "maos", "escudo" -> TomVerde
    "medicacao", "crianca" -> TomCoral
    "erro" -> TomAmbar
    else -> TomAzul
}

private fun icone(vararg tracos: String, cheio: String? = null, rodar: Float = 0f): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        group(rotate = rodar, pivotX = 12f, pivotY = 12f) {
            for (traco in tracos) {
                addPath(
                    pathData = addPathNodes(traco),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
            if (cheio != null) addPath(pathData = addPathNodes(cheio), fill = SolidColor(Color.Black))
        }
    }.build()
