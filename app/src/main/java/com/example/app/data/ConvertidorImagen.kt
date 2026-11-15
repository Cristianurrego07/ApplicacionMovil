package com.example.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

object ConvertidorImagen {

    // Tamaño máximo del lado más largo de la imagen.
    private const val LADO_MAXIMO = 400

    // Calidad de compresión JPEG, de 0 a 100.
    private const val CALIDAD_JPEG = 80

    // Lee una imagen de la galería y la convierte en Bitmap.
    fun desdeUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val origen = ImageDecoder.createSource(
                    context.contentResolver,
                    uri
                )

                ImageDecoder.decodeBitmap(origen) { decoder, info, _ ->
                    val ladoMayor = maxOf(
                        info.size.width,
                        info.size.height
                    )

                    if (ladoMayor > LADO_MAXIMO) {
                        val escala = LADO_MAXIMO.toFloat() / ladoMayor

                        val ancho = (info.size.width * escala)
                            .toInt()
                            .coerceAtLeast(1)

                        val alto = (info.size.height * escala)
                            .toInt()
                            .coerceAtLeast(1)

                        decoder.setTargetSize(ancho, alto)
                    }

                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                val opciones = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                context.contentResolver.openInputStream(uri).use { entrada ->
                    BitmapFactory.decodeStream(entrada, null, opciones)
                }

                opciones.inSampleSize = 1

                while (
                    maxOf(opciones.outWidth, opciones.outHeight) /
                    opciones.inSampleSize > LADO_MAXIMO * 2
                ) {
                    opciones.inSampleSize *= 2
                }

                opciones.inJustDecodeBounds = false

                context.contentResolver.openInputStream(uri).use { entrada ->
                    BitmapFactory.decodeStream(entrada, null, opciones)
                }
            }
        } catch (_: Exception) {
            // No se pudo leer la imagen.
            null
        }
    }

    // Reduce y comprime un Bitmap como JPEG.
    fun aBytes(bitmap: Bitmap): ByteArray {
        val imagenReducida = achicar(bitmap)
        val salida = ByteArrayOutputStream()

        imagenReducida.compress(
            Bitmap.CompressFormat.JPEG,
            CALIDAD_JPEG,
            salida
        )

        return salida.toByteArray()
    }

    // Convierte los bytes guardados en una imagen para mostrarla.
    fun aBitmap(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size
        )
    }

    // Reduce la imagen conservando sus proporciones.
    private fun achicar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)

        if (ladoMayor <= LADO_MAXIMO) {
            return bitmap
        }

        val escala = LADO_MAXIMO.toFloat() / ladoMayor

        val ancho = (bitmap.width * escala)
            .toInt()
            .coerceAtLeast(1)

        val alto = (bitmap.height * escala)
            .toInt()
            .coerceAtLeast(1)

        return Bitmap.createScaledBitmap(
            bitmap,
            ancho,
            alto,
            true
        )
    }
}