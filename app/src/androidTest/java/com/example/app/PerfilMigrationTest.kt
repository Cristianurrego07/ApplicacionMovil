package com.example.app

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.app.data.PagoFlexDbHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerfilMigrationTest {
    @Test
    fun migracionConservaDatosYAgregaContacto() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        PagoFlexDbHelper(contexto).use { helper ->
            SQLiteDatabase.create(null).use { db ->
                db.execSQL("CREATE TABLE usuarios (id_usuario INTEGER PRIMARY KEY, nombre TEXT, rut TEXT, correo TEXT)")
                db.execSQL("INSERT INTO usuarios VALUES (1, 'Sofía Pérez', '20.435.214-5', 'sofia@example.com')")
                helper.onUpgrade(db, 13, 14)
                db.rawQuery("SELECT * FROM usuarios WHERE id_usuario = 1", null).use { cursor ->
                    check(cursor.moveToFirst())
                    assertEquals("Sofía Pérez", cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
                    for (campo in listOf("telefono", "direccion", "ciudad")) {
                        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow(campo)))
                    }
                }
                db.execSQL("UPDATE usuarios SET telefono = '+56 9 1234 5678', direccion = 'Calle 123', ciudad = 'Santiago' WHERE id_usuario = 1")
                db.rawQuery("SELECT ciudad FROM usuarios WHERE id_usuario = 1", null).use {
                    check(it.moveToFirst())
                    assertEquals("Santiago", it.getString(0))
                }
            }
        }
    }
}
