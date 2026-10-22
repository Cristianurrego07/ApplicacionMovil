package com.example.app

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.app.data.PagoFlexDbHelper
import com.example.app.model.Usuario
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
                db.insertarUsuario(Usuario(rut = "11.111.111-1", nombre = "Prueba Login",
                    correo = "prueba@example.com", rolId = 1, rolNombre = "CLIENTE"), "claveInicial123")
                val prueba = requireNotNull(db.iniciarSesion(" PRUEBA@EXAMPLE.COM ", "claveInicial123"))
                assertEquals("CLIENTE", prueba.rolNombre)
                val cliente = requireNotNull(db.iniciarSesion("20.435.214-5", "cli123"))
                assertEquals("CLIENTE", cliente.rolNombre)
                assertEquals(cliente.id, db.iniciarSesion("20.435.214-5", "cli123")?.id)
                assertEquals(cliente.id, db.iniciarSesion("204352145", "cli123")?.id)
                assertEquals("AGENTE", db.iniciarSesion("10625244k", "age123")?.rolNombre)
                assertNull(db.iniciarSesion("204352145", "incorrecta"))
                assertNull(db.iniciarSesion("204352145", "cli123 "))

                // Cada cambio afecta solo a su cuenta y persiste tras cerrar la base.
                for ((rut, anterior) in listOf("204352145" to "cli123", "10625244k" to "age123")) {
                    val usuario = requireNotNull(db.iniciarSesion(rut, anterior))
                    assertFalse(db.cambiarContrasena(usuario.id, "incorrecta", "NuevaClave123"))
                    assertFalse(db.cambiarContrasena(usuario.id, anterior, "12345"))
                    assertNotNull(db.iniciarSesion(rut, anterior))
                    assertTrue(db.cambiarContrasena(usuario.id, anterior, "NuevaClave123"))
                    assertNull(db.iniciarSesion(rut, anterior))
                    val actualizado = requireNotNull(db.iniciarSesion(rut, "NuevaClave123"))
                    assertEquals(usuario.rolNombre, actualizado.rolNombre)
                    assertEquals(usuario.nombre, actualizado.nombre)
                }
                assertNotNull(db.iniciarSesion("prueba@example.com", "claveInicial123"))
            }
            PagoFlexDbHelper(contexto).use { db ->
                assertNotNull(db.iniciarSesion("204352145", "NuevaClave123"))
                assertNotNull(db.iniciarSesion("10625244k", "NuevaClave123"))
            }
        } finally {
            base.deleteDatabase(prefijo + "PagoFlex.db")
        }
    }
}
