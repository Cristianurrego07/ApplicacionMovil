package com.example.app

import android.graphics.Bitmap
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.app.data.ConvertidorImagen
import com.example.app.data.PagoFlexDbHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarTest {
    @Test
    fun comprimeYRecuperaFotoConProporcionOriginal() {
        val original = Bitmap.createBitmap(1200, 600, Bitmap.Config.ARGB_8888)
        val bytes = ConvertidorImagen.aBytes(original)
        val recuperada = requireNotNull(ConvertidorImagen.aBitmap(bytes))
        assertEquals(400, recuperada.width)
        assertEquals(200, recuperada.height)
        assertNull(ConvertidorImagen.aBitmap(byteArrayOf(1, 2, 3)))
        original.recycle()
        recuperada.recycle()
    }

    @Test
    fun migracionConservaUsuarioYPermiteGuardarAvatar() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        PagoFlexDbHelper(contexto).use { helper ->
            SQLiteDatabase.create(null).use { db ->
                db.execSQL("CREATE TABLE usuarios (id_usuario INTEGER PRIMARY KEY, nombre TEXT)")
                db.execSQL("INSERT INTO usuarios VALUES (1, 'Sofía Pérez')")
                helper.onUpgrade(db, 14, 15)
                db.rawQuery("SELECT nombre, avatar FROM usuarios WHERE id_usuario = 1", null).use {
                    check(it.moveToFirst())
                    assertEquals("Sofía Pérez", it.getString(0))
                    assertTrue(it.isNull(1))
                }
                val imagen = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
                val bytes = ConvertidorImagen.aBytes(imagen)
                db.execSQL("UPDATE usuarios SET avatar = ? WHERE id_usuario = 1", arrayOf(bytes))
                db.rawQuery("SELECT avatar FROM usuarios WHERE id_usuario = 1", null).use {
                    check(it.moveToFirst())
                    assertArrayEquals(bytes, it.getBlob(0))
                }
                imagen.recycle()
            }
        }
    }
}
