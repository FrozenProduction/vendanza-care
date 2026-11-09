package pt.upca.vendanzacare.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Longest side the app ever needs: the 160 dp preview in the form at xxxhdpi is ~640 px. */
private const val LADO_MAXIMO = 640

/**
 * The packaging photo, loaded from the private `content://` URI the medication entry stores — a copy inside the
 * app's own folder, made and shrunk by `dados/Fotos.kt`. Nothing is uploaded; if the file is gone the image
 * simply does not appear.
 *
 * Decoded down-sampled: a 12 MP camera photo decoded whole is ~48 MB of heap per thumbnail, enough to kill
 * the app on the 2 GB phones it targets.
 */
@Composable
fun FotoDaEmbalagem(
    uri: String?,
    modifier: Modifier = Modifier.size(64.dp),
    forma: Shape = RoundedCornerShape(14.dp),
) {
    if (uri.isNullOrBlank()) return
    val contexto = LocalContext.current

    val imagem by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { runCatching { ler(contexto, uri) }.getOrNull()?.asImageBitmap() }
    }

    imagem?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(forma),
        )
    }
}

private fun ler(contexto: Context, uri: String): Bitmap? {
    val resolver = contexto.contentResolver
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri.toUri())?.use { BitmapFactory.decodeStream(it, null, limites) }
    var amostra = 1
    while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= LADO_MAXIMO) amostra *= 2
    val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
    return resolver.openInputStream(uri.toUri())?.use { BitmapFactory.decodeStream(it, null, opcoes) }
}
