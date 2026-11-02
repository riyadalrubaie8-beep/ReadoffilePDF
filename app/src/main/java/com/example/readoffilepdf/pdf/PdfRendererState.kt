package com.example.readoffilepdf.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PdfRendererState private constructor(
    private val parcelFileDescriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
    val fileName: String,
) {
    val pageCount: Int get() = renderer.pageCount
    private val mutex = Mutex()
    private val cache = LruCache<String, Bitmap>(30)

    suspend fun renderPage(pageIndex: Int, targetWidthPx: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (pageIndex !in 0 until pageCount) return@withContext null

        val cacheKey = "$pageIndex-$targetWidthPx"
        cache[cacheKey]?.let { return@withContext it }

        mutex.withLock {
            // Check cache again inside lock to prevent redundant work
            cache[cacheKey]?.let { return@withContext it }

            var page: PdfRenderer.Page? = null
            try {
                page = renderer.openPage(pageIndex)
                val pageWidth = page.width
                val pageHeight = page.height
                if (pageWidth <= 0 || pageHeight <= 0) return@withContext null

                val aspectRatio = pageWidth.toFloat() / pageHeight.toFloat()
                val width = targetWidthPx.coerceAtLeast(300)
                val height = (width / aspectRatio).toInt().coerceAtLeast(300)

                val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE) // Fill background white for transparent PDFs

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                cache.put(cacheKey, bitmap)
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                page?.close()
            }
        }
    }

    fun close() {
        try {
            renderer.close()
        } catch (_: Exception) {
        }
        try {
            parcelFileDescriptor.close()
        } catch (_: Exception) {
        }
        cache.evictAll()
    }

    companion object {
        fun create(context: Context, uri: Uri): PdfRendererState? {
            return try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
                val renderer = PdfRenderer(pfd)
                val fileName = getFileName(context, uri) ?: "document.pdf"
                PdfRendererState(pfd, renderer, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun getFileName(context: Context, uri: Uri): String? {
            var name: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = it.getString(index)
                        }
                    }
                }
            }
            if (name == null) {
                name = uri.path?.let { path ->
                    val cut = path.lastIndexOf('/')
                    if (cut != -1) path.substring(cut + 1) else path
                }
            }
            return name
        }
    }
}
