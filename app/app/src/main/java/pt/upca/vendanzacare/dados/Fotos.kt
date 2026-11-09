package pt.upca.vendanzacare.dados

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Packaging photos, kept **inside the app** (`files/fotos/`), never in the gallery and never uploaded.
 *
 * Both routes end in the same place:
 * - **Camera** — [novoDestino] hands the phone's own camera app a private file to write into (through a
 *   FileProvider, so no CAMERA permission and no camera library); [importar] then normalises it.
 * - **Gallery** — [importar] copies the picked image in. A copy instead of a persisted URI grant: the grant
 *   is lost when the original is deleted or the gallery app is replaced, and the box photo would vanish.
 *
 * Normalising = rotate by EXIF (camera JPEGs are stored sideways and BitmapFactory ignores the tag), shrink
 * to [LADO] px on the long side, JPEG 85. A 12 MP shot becomes ~150 KB.
 *
 * Nothing here reads the photo's content. Recognising the medicine from the box is planned for the IA UC
 * (architecture.md §12.10); this class is where that would plug in, on the device.
 */
object Fotos {

    private const val PASTA = "fotos"
    private const val LADO = 1280
    private const val QUALIDADE = 85

    private fun autoridade(contexto: Context) = "${contexto.packageName}.fotos"

    private fun pasta(contexto: Context) = File(contexto.filesDir, PASTA).apply { mkdirs() }

    /** An empty private file for the camera app to write into, as a content:// URI it is allowed to open. */
    fun novoDestino(contexto: Context): Uri {
        val ficheiro = File(pasta(contexto), "captura-${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(contexto, autoridade(contexto), ficheiro)
    }

    /**
     * Reads [origem] (the camera's file or a gallery pick), writes a straightened, shrunk copy into the app's
     * folder and returns its URI as a string — the value stored in [pt.upca.vendanzacare.dominio.Medicamento.foto].
     * A capture file is deleted once copied. Null when the image cannot be read. Call off the main thread.
     */
    fun importar(contexto: Context, origem: Uri): String? = runCatching {
        val resolver = contexto.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(origem)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0) return null

        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= LADO) amostra *= 2
        val bruto = resolver.openInputStream(origem)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
        } ?: return null

        val graus = resolver.openInputStream(origem)?.use { rotacao(ExifInterface(it)) } ?: 0
        val escala = minOf(1f, LADO.toFloat() / maxOf(bruto.width, bruto.height))
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(graus.toFloat())
        }
        val pronto = Bitmap.createBitmap(bruto, 0, 0, bruto.width, bruto.height, matriz, true)

        val destino = File(pasta(contexto), "foto-${UUID.randomUUID()}.jpg")
        destino.outputStream().use { pronto.compress(Bitmap.CompressFormat.JPEG, QUALIDADE, it) }
        if (pronto !== bruto) pronto.recycle()
        bruto.recycle()

        apagarSeNossa(contexto, origem.toString())
        FileProvider.getUriForFile(contexto, autoridade(contexto), destino).toString()
    }.getOrNull()

    /** Deletes a photo this class created. Anything else (an old gallery URI) is left alone. */
    fun apagarSeNossa(contexto: Context, foto: String?) {
        ficheiroDe(contexto, foto)?.delete()
    }

    /**
     * Removes photos no medicine points to any more (a replaced photo, a deleted medicine, a capture that was
     * never saved). Only files older than a day: a form still open — or restored after the system killed the
     * app — may hold a photo that is not saved yet.
     */
    fun limpar(contexto: Context, emUso: Collection<String?>) {
        val usados = emUso.mapNotNull { ficheiroDe(contexto, it)?.name }.toSet()
        val limite = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        pasta(contexto).listFiles()?.forEach { f ->
            if (f.name !in usados && f.lastModified() < limite) f.delete()
        }
    }

    /** A small bitmap of [foto] for the reminder notification; null when there is none. */
    fun miniatura(contexto: Context, foto: String?, lado: Int = 256): Bitmap? {
        if (foto.isNullOrBlank()) return null
        return runCatching {
            val uri = Uri.parse(foto)
            val resolver = contexto.contentResolver
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            var amostra = 1
            while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= lado) amostra *= 2
            resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
            }
        }.getOrNull()
    }

    private fun ficheiroDe(contexto: Context, foto: String?): File? {
        if (foto.isNullOrBlank()) return null
        val uri = Uri.parse(foto)
        if (uri.authority != autoridade(contexto)) return null
        val nome = uri.lastPathSegment ?: return null
        return File(pasta(contexto), nome).takeIf { it.exists() }
    }

    private fun rotacao(exif: ExifInterface): Int = when (
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
