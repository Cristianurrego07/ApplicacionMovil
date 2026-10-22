package com.example.app

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.app.data.PagoFlexDbHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginDatabaseTest {
    @Test fun autenticaPorCorreoORutSinAlterarLaContrasena() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val prefijo = "prueba_login_${System.nanoTime()}_"
        val contexto = object : ContextWrapper(base) {
            override fun getDatabasePath(name: String) = base.getDatabasePath(prefijo + name)
            override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
                base.openOrCreateDatabase(prefijo + name, mode, factory)
            override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase =
                base.openOrCreateDatabase(prefijo + name, mode, factory, errorHandler)
        }
        try {
            PagoFlexDbHelper(contexto).use { db ->
                val cliente = requireNotNull(db.iniciarSesion(" SOFIA@DUOCUC.CL ", "cli123"))
                assertEquals("CLIENTE", cliente.rolNombre)
                assertEquals(cliente.id, db.iniciarSesion("20.435.214-5", "cli123")?.id)
                assertEquals(cliente.id, db.iniciarSesion("204352145", "cli123")?.id)
                assertEquals("AGENTE", db.iniciarSesion("10625244k", "age123")?.rolNombre)
                assertNull(db.iniciarSesion("204352145", "incorrecta"))
                assertNull(db.iniciarSesion("204352145", "cli123 "))
            }
        } finally {
            base.deleteDatabase(prefijo + "PagoFlex.db")
        }
    }
}
