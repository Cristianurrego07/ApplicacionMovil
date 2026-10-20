package cl.duocuc.miregistro.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.miregistro.model.CobranzaEmpresa
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.ReporteProblema
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "pagoflex.db"
        private const val VERSION_BD = 4

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_CIUDAD = "ciudad"
        private const val USU_ROL = "id_rol"
        private const val USU_AVATAR = "avatar"

        private const val TABLA_COMPROMISOS = "compromisos"
        private const val COM_ID = "id_compromiso"
        private const val COM_USUARIO = "usuario_id"
        private const val COM_TITULO = "titulo"
        private const val COM_SUBTITULO = "subtitulo"
        private const val COM_MONTO = "monto"
        private const val COM_VENCIMIENTO = "fecha_vencimiento"
        private const val COM_ESTADO = "estado"
        private const val COM_EMPRESA = "empresa"

        private const val TABLA_REPORTES = "reportes"
        private const val REP_ID = "id_reporte"
        private const val REP_CODIGO = "codigo"
        private const val REP_TITULO = "titulo"
        private const val REP_CLIENTE = "cliente"
        private const val REP_ESTADO = "estado"
        private const val REP_FECHA = "fecha"
        private const val REP_DESCRIPCION = "descripcion"
        private const val REP_TIPO = "tipo"

        private const val TABLA_COBRANZAS = "cobranzas"
        private const val COB_ID = "id_cobranza"
        private const val COB_CODIGO = "codigo"
        private const val COB_EMPRESA = "empresa"
        private const val COB_MONTO = "monto"
        private const val COB_VENCIMIENTO = "vencimiento"
        private const val COB_DESCRIPCION = "descripcion"
        private const val COB_ESTADO = "estado"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USU_NOMBRE TEXT NOT NULL, " +
                "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                "$USU_CONTRASENA TEXT NOT NULL, " +
                "$USU_CIUDAD TEXT NOT NULL, " +
                "$USU_AVATAR BLOB, " +
                "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_AGREGAR_AVATAR =
            "ALTER TABLE $TABLA_USUARIOS ADD COLUMN $USU_AVATAR BLOB"

        private const val SQL_CREAR_COMPROMISOS =
            "CREATE TABLE $TABLA_COMPROMISOS (" +
                "$COM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COM_USUARIO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID), " +
                "$COM_TITULO TEXT NOT NULL, " +
                "$COM_SUBTITULO TEXT NOT NULL, " +
                "$COM_MONTO REAL NOT NULL, " +
                "$COM_VENCIMIENTO TEXT NOT NULL, " +
                "$COM_ESTADO TEXT NOT NULL, " +
                "$COM_EMPRESA TEXT NOT NULL)"

        private const val SQL_CREAR_REPORTES =
            "CREATE TABLE $TABLA_REPORTES (" +
                "$REP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$REP_CODIGO TEXT NOT NULL, " +
                "$REP_TITULO TEXT NOT NULL, " +
                "$REP_CLIENTE TEXT NOT NULL, " +
                "$REP_ESTADO TEXT NOT NULL, " +
                "$REP_FECHA TEXT NOT NULL, " +
                "$REP_DESCRIPCION TEXT NOT NULL, " +
                "$REP_TIPO TEXT NOT NULL)"

        private const val SQL_CREAR_COBRANZAS =
            "CREATE TABLE $TABLA_COBRANZAS (" +
                "$COB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COB_CODIGO TEXT NOT NULL, " +
                "$COB_EMPRESA TEXT NOT NULL, " +
                "$COB_MONTO REAL NOT NULL, " +
                "$COB_VENCIMIENTO TEXT NOT NULL, " +
                "$COB_DESCRIPCION TEXT NOT NULL, " +
                "$COB_ESTADO TEXT NOT NULL)"

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                "(1, 'Usuario'), (2, 'Agente PagoFlex'), (3, 'Ejecutivo Empresa')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_CIUDAD, $USU_ROL) VALUES " +
                "('Carla Agente', 'carla@pagoflex.cl', '123456', 'Santiago', 2), " +
                "('Daniela Ejecutivo', 'daniela@empresa.cl', '123456', 'Santiago', 3)"

        private const val SQL_PRECARGAR_REPORTES =
            "INSERT INTO $TABLA_REPORTES ($REP_CODIGO, $REP_TITULO, $REP_CLIENTE, $REP_ESTADO, $REP_FECHA, $REP_DESCRIPCION, $REP_TIPO) VALUES " +
                "('#RP-001', 'Error en pago con tarjeta', 'Empresa Los Ríos', 'Abierto', '12 may, 2025 - 10:24', 'El cliente reporta que al intentar realizar un pago con tarjeta aparece un error de conexión.', 'Reporte'), " +
                "('#RP-002', 'No recibe notificaciones', 'Constructora Sol', 'En proceso', '11 may, 2025', 'El cliente no recibe alertas de recordatorio.', 'Reporte'), " +
                "('RF-14', 'Mejorar notificaciones de pago', 'Sistema General', 'En proceso', '12 may, 2025', 'Implementar notificaciones push en tiempo real para aumentar la tasa de pago.', 'Recomendación'), " +
                "('RF-15', 'Optimizar flujo de cobranza', 'Sistema General', 'Pendiente', '12 may, 2025', 'Revisar tiempos de recordatorio de pago.', 'Recomendación'), " +
                "('RF-16', 'Actualizar documentación', 'Sistema General', 'Pendiente', '12 may, 2025', 'Revisar y actualizar manuales de uso.', 'Recomendación')"

        private const val SQL_PRECARGAR_COBRANZAS =
            "INSERT INTO $TABLA_COBRANZAS ($COB_CODIGO, $COB_EMPRESA, $COB_MONTO, $COB_VENCIMIENTO, $COB_DESCRIPCION, $COB_ESTADO) VALUES " +
                "('N° C-00123', 'Luz del Sur', 120000.0, '15 may. 2025', 'Servicio de electricidad - Mayo 2025', 'En curso'), " +
                "('N° C-00124', 'Aguas Andinas', 85000.0, '18 may. 2025', 'Servicio sanitario - Mayo 2025', 'En curso'), " +
                "('N° C-00125', 'Movistar', 65000.0, '20 may. 2025', 'Telefonía e internet', 'En curso'), " +
                "('N° C-00126', 'Entel', 42000.0, '10 may. 2025', 'Plan móvil corporativo', 'Pagada')"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                "FROM $TABLA_USUARIOS u " +
                "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_COMPROMISOS)
        db.execSQL(SQL_CREAR_REPORTES)
        db.execSQL(SQL_CREAR_COBRANZAS)

        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_REPORTES)
        db.execSQL(SQL_PRECARGAR_COBRANZAS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            try {
                db.execSQL(SQL_AGREGAR_AVATAR)
            } catch (e: Exception) {}
        }
    }

    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    fun insertarUsuario(usuario: Usuario): Long {
        val db = writableDatabase
        val id = db.insert(TABLA_USUARIOS, null, ContentValues().apply {
            put(USU_NOMBRE, usuario.nombre)
            put(USU_CORREO, usuario.correo)
            put(USU_CONTRASENA, usuario.contrasena)
            put(USU_CIUDAD, usuario.ciudad)
            put(USU_ROL, usuario.idRol)
        })
        if (id > 0 && usuario.idRol == 1L) {
            db.execSQL(
                "INSERT INTO $TABLA_COMPROMISOS ($COM_USUARIO, $COM_TITULO, $COM_SUBTITULO, $COM_MONTO, $COM_VENCIMIENTO, $COM_ESTADO, $COM_EMPRESA) VALUES " +
                    "(?, 'Universidad', 'Instituto Profesional', 35000.0, '15 de octubre de 2026', 'Pendiente', 'Universidad'), " +
                    "(?, 'Luz', 'Servicio eléctrico', 28000.0, '20 oct. 2026', 'Pendiente', 'Luz'), " +
                    "(?, 'Condominio', 'Gastos comunes', 15000.0, '28 oct. 2026', 'Pendiente', 'Condominio'), " +
                    "(?, 'Agua', 'Servicio sanitario', 35000.0, '29 oct. 2026', 'Pagada', 'Agua'), " +
                    "(?, 'Internet', 'Fibra óptica', 23000.0, '30 oct. 2026', 'Pendiente', 'Internet')",
                arrayOf(id, id, id, id, id)
            )
        }
        return id
    }

    fun autenticar(correo: String, contrasena: String): Usuario? {
        val sql = "$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ? AND u.$USU_CONTRASENA = ?"
        return consultarUsuarios(sql, arrayOf(correo, contrasena)).firstOrNull()
    }

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(
            TABLA_USUARIOS,
            ContentValues().apply {
                put(USU_NOMBRE, usuario.nombre)
                put(USU_CORREO, usuario.correo)
                put(USU_CIUDAD, usuario.ciudad)
                put(USU_ROL, usuario.idRol)
            },
            "$USU_ID = ?",
            arrayOf(usuario.id.toString())
        )

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val valores = ContentValues().apply { put(USU_AVATAR, avatar) }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    // ----------------- COMPROMISOS -----------------
    fun listarCompromisos(idUsuario: Long): List<Compromiso> {
        val lista = mutableListOf<Compromiso>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_COMPROMISOS WHERE $COM_USUARIO = ?",
            arrayOf(idUsuario.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Compromiso(
                        id = c.getLong(c.getColumnIndexOrThrow(COM_ID)),
                        usuarioId = c.getLong(c.getColumnIndexOrThrow(COM_USUARIO)),
                        titulo = c.getString(c.getColumnIndexOrThrow(COM_TITULO)),
                        subtitulo = c.getString(c.getColumnIndexOrThrow(COM_SUBTITULO)),
                        monto = c.getDouble(c.getColumnIndexOrThrow(COM_MONTO)),
                        fechaVencimiento = c.getString(c.getColumnIndexOrThrow(COM_VENCIMIENTO)),
                        estado = c.getString(c.getColumnIndexOrThrow(COM_ESTADO)),
                        empresa = c.getString(c.getColumnIndexOrThrow(COM_EMPRESA))
                    )
                )
            }
        }
        return lista
    }

    fun pagarCompromiso(id: Long) {
        writableDatabase.execSQL("UPDATE $TABLA_COMPROMISOS SET $COM_ESTADO = 'Pagada' WHERE $COM_ID = ?", arrayOf(id.toString()))
    }

    // ----------------- REPORTES / RECOMENDACIONES (Agente) -----------------
    fun listarReportes(): List<ReporteProblema> {
        val lista = mutableListOf<ReporteProblema>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_REPORTES", null).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    ReporteProblema(
                        id = c.getLong(c.getColumnIndexOrThrow(REP_ID)),
                        codigo = c.getString(c.getColumnIndexOrThrow(REP_CODIGO)),
                        titulo = c.getString(c.getColumnIndexOrThrow(REP_TITULO)),
                        cliente = c.getString(c.getColumnIndexOrThrow(REP_CLIENTE)),
                        estado = c.getString(c.getColumnIndexOrThrow(REP_ESTADO)),
                        fecha = c.getString(c.getColumnIndexOrThrow(REP_FECHA)),
                        descripcion = c.getString(c.getColumnIndexOrThrow(REP_DESCRIPCION)),
                        tipo = c.getString(c.getColumnIndexOrThrow(REP_TIPO))
                    )
                )
            }
        }
        return lista
    }

    fun actualizarEstadoReporte(id: Long, nuevoEstado: String) {
        writableDatabase.execSQL("UPDATE $TABLA_REPORTES SET $REP_ESTADO = ? WHERE $REP_ID = ?", arrayOf(nuevoEstado, id.toString()))
    }

    // ----------------- COBRANZAS (Ejecutivo) -----------------
    fun listarCobranzas(): List<CobranzaEmpresa> {
        val lista = mutableListOf<CobranzaEmpresa>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_COBRANZAS", null).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    CobranzaEmpresa(
                        id = c.getLong(c.getColumnIndexOrThrow(COB_ID)),
                        codigo = c.getString(c.getColumnIndexOrThrow(COB_CODIGO)),
                        empresa = c.getString(c.getColumnIndexOrThrow(COB_EMPRESA)),
                        monto = c.getDouble(c.getColumnIndexOrThrow(COB_MONTO)),
                        vencimiento = c.getString(c.getColumnIndexOrThrow(COB_VENCIMIENTO)),
                        descripcion = c.getString(c.getColumnIndexOrThrow(COB_DESCRIPCION)),
                        estado = c.getString(c.getColumnIndexOrThrow(COB_ESTADO))
                    )
                )
            }
        }
        return lista
    }

    fun insertarCobranza(cobranza: CobranzaEmpresa) {
        writableDatabase.insert(TABLA_COBRANZAS, null, ContentValues().apply {
            put(COB_CODIGO, cobranza.codigo)
            put(COB_EMPRESA, cobranza.empresa)
            put(COB_MONTO, cobranza.monto)
            put(COB_VENCIMIENTO, cobranza.vencimiento)
            put(COB_DESCRIPCION, cobranza.descripcion)
            put(COB_ESTADO, cobranza.estado)
        })
    }

    fun anularCobranza(id: Long) {
        writableDatabase.execSQL("UPDATE $TABLA_COBRANZAS SET $COB_ESTADO = 'Anulada' WHERE $COB_ID = ?", arrayOf(id.toString()))
    }

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Usuario(
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
                        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
                        contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRASENA)),
                        ciudad = cursor.getString(cursor.getColumnIndexOrThrow(USU_CIUDAD)),
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
                        idRol = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
                        nombreRol = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),
                        avatar = cursor.getColumnIndexOrThrow(USU_AVATAR).let { columna ->
                            if (cursor.isNull(columna)) null else cursor.getBlob(columna)
                        }
                    )
                )
            }
        }
        return lista
    }
}
