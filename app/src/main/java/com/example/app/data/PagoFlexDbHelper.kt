package com.example.app.data

import com.example.app.validacion.Validaciones
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.app.model.AplazamientoCuota
import com.example.app.model.CatalogoServicios
import com.example.app.model.PlanCuotas
import com.example.app.model.Rol
import com.example.app.model.Comprobante
import com.example.app.model.Compromiso
import com.example.app.model.Servicio
import com.example.app.model.Usuario
import com.example.app.model.Reporte

class PagoFlexDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "PagoFlex.db"
        private const val VERSION_BD = 27
        private const val TABLA_ROLES = "roles"
        private const val TABLA_USUARIOS = "usuarios"
        private const val TABLA_SERVICIOS = "servicios"
        private const val TABLA_COMPROMISOS = "compromisos"
        private const val TABLA_PAGOS = "pagos"
        private const val TABLA_REPORTES = "reportes"

        private const val ID_ROL = "id_rol"
        private const val NOMBRE_ROL = "nombre_rol"

        private const val ID_USUARIO = "id_usuario"
        private const val RUT = "rut"
        private const val NOMBRE = "nombre"
        private const val CORREO = "correo"
        private const val CONTRASENA = "contrasena"
        private const val ID_SERVICIO = "id_servicio"
        private const val NOMBRE_EMPRESA = "nombre_empresa"
        private const val RUT_PAGADOR = "rut_pagador"
        private const val ALIAS = "alias"
        private const val FOLIO = "folio"
        private const val CONCEPTO = "concepto"
        private const val MONTO = "monto"
        private const val VENCIMIENTO = "vencimiento"
        private const val ESTADO = "estado"
        private const val RECARGO = "recargo"
        private const val NUMERO_CUOTA = "numero_cuota"
        private const val ID_COMPROMISO = "id_compromiso"
        private const val NUMERO_COMPROBANTE = "numero_comprobante"
        private const val MONTO_PAGADO = "monto_pagado"
        private const val FECHA_HORA = "fecha_hora"
        private const val CANAL = "canal"
        private const val MEDIO_PAGO = "medio_pago"
        private const val MOTIVO_REPORTE = "motivo"
        private const val ESTADO_REPORTE = "estado_reporte"
        private const val FECHA_REPORTE = "fecha_reporte"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES ("+
                    "$ID_ROL INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$NOMBRE_ROL TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS(" +
                    "$ID_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "$RUT TEXT NOT NULL UNIQUE,"+
                    "$NOMBRE TEXT NOT NULL,"+
                    "$CORREO TEXT NOT NULL UNIQUE,"+
                    "$CONTRASENA TEXT NOT NULL,"+
                    "telefono TEXT NOT NULL DEFAULT '',"+
                    "direccion TEXT NOT NULL DEFAULT '',"+
                    "ciudad TEXT NOT NULL DEFAULT '',"+
                    "avatar BLOB,"+
                    "$ID_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ID_ROL))"

        private const val SQL_CREAR_SERVICIOS =
            "CREATE TABLE $TABLA_SERVICIOS (" +
                    "$ID_SERVICIO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ID_USUARIO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($ID_USUARIO), " +
                    "$NOMBRE_EMPRESA TEXT NOT NULL, " +
                    "$RUT_PAGADOR TEXT NOT NULL, " +
                    "$ALIAS TEXT NOT NULL)"

        private const val SQL_CREAR_COMPROMISOS =
            "CREATE TABLE $TABLA_COMPROMISOS (" +
                    "$ID_COMPROMISO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$FOLIO TEXT NOT NULL UNIQUE, " +
                    "$ID_USUARIO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($ID_USUARIO), " +
                    "$NOMBRE_EMPRESA TEXT NOT NULL, $CONCEPTO TEXT NOT NULL, " +
                    "$MONTO INTEGER NOT NULL CHECK($MONTO > 0), " +
                    "$VENCIMIENTO TEXT NOT NULL, $ESTADO TEXT NOT NULL, " +
                    "$RECARGO INTEGER NOT NULL DEFAULT 0, $NUMERO_CUOTA TEXT NOT NULL DEFAULT '', plan_id TEXT NOT NULL DEFAULT '')"

        private const val SQL_CREAR_PAGOS =
            "CREATE TABLE $TABLA_PAGOS (" +
                    "id_pago INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$NUMERO_COMPROBANTE TEXT NOT NULL UNIQUE, " +
                    "$FOLIO TEXT NOT NULL UNIQUE REFERENCES $TABLA_COMPROMISOS($FOLIO), " +
                    "$NOMBRE_EMPRESA TEXT NOT NULL, $CONCEPTO TEXT NOT NULL, " +
                    "$MONTO_PAGADO INTEGER NOT NULL, $FECHA_HORA TEXT NOT NULL, $CANAL TEXT NOT NULL, $MEDIO_PAGO TEXT NOT NULL)"

        private const val SQL_CREAR_REPORTES =
            "CREATE TABLE $TABLA_REPORTES (" +
                    "id_reporte INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$FOLIO TEXT NOT NULL UNIQUE REFERENCES $TABLA_COMPROMISOS($FOLIO), " +
                    "$MOTIVO_REPORTE TEXT NOT NULL, " +
                    "$ESTADO_REPORTE TEXT NOT NULL DEFAULT 'Pendiente', " +
                    "$FECHA_REPORTE TEXT NOT NULL, respuesta TEXT)"

        private const val SQL_PROTEGER_PAGOS_UPDATE =
            "CREATE TRIGGER IF NOT EXISTS pagos_sin_modificacion " +
                    "BEFORE UPDATE ON $TABLA_PAGOS BEGIN SELECT RAISE(ABORT, 'El comprobante es inmutable'); END"

        private const val SQL_PROTEGER_PAGOS_DELETE =
            "CREATE TRIGGER IF NOT EXISTS pagos_sin_eliminacion " +
                    "BEFORE DELETE ON $TABLA_PAGOS BEGIN SELECT RAISE(ABORT, 'El comprobante es inmutable'); END"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$NOMBRE_ROL AS $NOMBRE_ROL " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$ID_ROL = r.$ID_ROL"

    }
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase){
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_SERVICIOS)
        db.execSQL(SQL_CREAR_COMPROMISOS)
        db.execSQL(SQL_CREAR_PAGOS)
        db.execSQL(SQL_CREAR_REPORTES)
        crearTablaAplazamientos(db)
        db.execSQL(SQL_PROTEGER_PAGOS_UPDATE)
        db.execSQL(SQL_PROTEGER_PAGOS_DELETE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS $TABLA_SERVICIOS")
            db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
            db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
            onCreate(db)
            return
        } else if (oldVersion < 5) {
            db.execSQL(
                "CREATE TABLE servicios_nueva (" +
                    "$ID_SERVICIO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ID_USUARIO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($ID_USUARIO), " +
                    "$NOMBRE_EMPRESA TEXT NOT NULL, " +
                    "$RUT_PAGADOR TEXT NOT NULL, " +
                    "$ALIAS TEXT NOT NULL)"
            )
            db.execSQL(
                "INSERT INTO servicios_nueva " +
                    "($ID_SERVICIO, $ID_USUARIO, $NOMBRE_EMPRESA, $RUT_PAGADOR, $ALIAS) " +
                    "SELECT $ID_SERVICIO, $ID_USUARIO, $NOMBRE_EMPRESA, rut_empresa, $ALIAS " +
                    "FROM $TABLA_SERVICIOS"
            )
            db.execSQL("DROP TABLE $TABLA_SERVICIOS")
            db.execSQL("ALTER TABLE servicios_nueva RENAME TO $TABLA_SERVICIOS")
        }
        if (oldVersion < 9) {
            db.execSQL("UPDATE $TABLA_COMPROMISOS SET $NOMBRE_EMPRESA = 'Duoc Uc' WHERE $FOLIO = 'PF-UNI-001' OR $NOMBRE_EMPRESA = 'Universidad'")
            db.execSQL("UPDATE $TABLA_SERVICIOS SET $NOMBRE_EMPRESA = 'Enel' WHERE $NOMBRE_EMPRESA = 'Luz'")
        }
        if (oldVersion < 11) {
            db.execSQL(SQL_CREAR_REPORTES)
        }
        if (oldVersion in 11..26) {
            db.execSQL("ALTER TABLE $TABLA_REPORTES ADD COLUMN respuesta TEXT")
        }
        if (oldVersion < 15 && newVersion >= 15) {
            db.execSQL("ALTER TABLE $TABLA_USUARIOS ADD COLUMN avatar BLOB")
        }
        if (oldVersion < 14) {
            db.execSQL("ALTER TABLE $TABLA_USUARIOS ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLA_USUARIOS ADD COLUMN direccion TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLA_USUARIOS ADD COLUMN ciudad TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 13) {
            db.execSQL("DELETE FROM $TABLA_USUARIOS WHERE $CORREO = 'miranda@empresa.com'")
            db.execSQL("DELETE FROM $TABLA_ROLES WHERE $NOMBRE_ROL = 'EJECUTIVO'")
        }
        if (oldVersion < 18) {
            db.execSQL(
                "INSERT OR IGNORE INTO $TABLA_ROLES ($ID_ROL, $NOMBRE_ROL) VALUES (3, 'EJECUTIVO')"
            )
            db.execSQL(
                "INSERT OR IGNORE INTO $TABLA_USUARIOS ($RUT, $NOMBRE, $CORREO, $CONTRASENA, $ID_ROL) " +
                "VALUES ('12.345.678-9', 'Carla Ejecutivo', 'carla@duocuc.cl', 'eje123', 3)"
            )
        }
        if (oldVersion < 26) {
            crearTablaAplazamientos(db)
        }
        if (oldVersion < 25) {
            db.execSQL(
                "UPDATE $TABLA_USUARIOS SET $CORREO = ? WHERE LOWER($CORREO) = ? AND $ID_ROL = 3 " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER($CORREO) = ?)",
                arrayOf("carla@duocuc.cl", "carla@empresa.com", "carla@duocuc.cl")
            )
        }
        if (oldVersion < 24) {
            db.execSQL("ALTER TABLE $TABLA_COMPROMISOS ADD COLUMN plan_id TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 23) {
            db.execSQL(
                "DELETE FROM $TABLA_COMPROMISOS WHERE $FOLIO = ? AND $CONCEPTO = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ?) " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ?)",
                arrayOf("PF-DEMO-PEND-1", "Materiales de estudio", "PF-DEMO-PEND-1", "PF-DEMO-PEND-1")
            )
            db.execSQL(
                "UPDATE $TABLA_COMPROMISOS SET $MONTO = ? WHERE $FOLIO = ? AND $CONCEPTO = ? " +
                    "AND $MONTO = ? AND $ESTADO IN ('Pendiente', 'Vencido') " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ?) " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ?)",
                arrayOf<Any>(182000, "PF-DEMO-PEND-3", "Matrícula", 45000, "PF-DEMO-PEND-3", "PF-DEMO-PEND-3")
            )
        }
        if (oldVersion < 21) {
            db.execSQL(
                "DELETE FROM $TABLA_COMPROMISOS WHERE $FOLIO = ? AND $MONTO = ? " +
                    "AND $CONCEPTO = ? AND NOT EXISTS (SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ?) " +
                    "AND NOT EXISTS (SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ?)",
                arrayOf<Any>("PF-EMP-001", 890000, "Mensualidad empresa", "PF-EMP-001", "PF-EMP-001")
            )
        }
        if (oldVersion < 16) {
            val ejemplos = listOf(
                Triple("PF-UNI-001", "2026-10-25", "+7 days"),
                Triple("PF-LUZ-001", "2026-10-20", "-7 days"),
                Triple("PF-COND-001", "2026-10-28", "+14 days"),
                Triple("PF-AGUA-001", "2026-10-29", "-3 days")
            )
            for ((folio, fechaOriginal, cambio) in ejemplos) {
                db.execSQL(
                    "UPDATE $TABLA_COMPROMISOS SET $VENCIMIENTO = date('now', 'localtime', ?), " +
                        "$ESTADO = 'Pendiente' WHERE $FOLIO = ? AND $VENCIMIENTO = ? " +
                        "AND $ESTADO IN ('Pendiente', 'Vencido') AND $ID_USUARIO IN " +
                        "(SELECT $ID_USUARIO FROM $TABLA_USUARIOS WHERE $RUT = ?)",
                    arrayOf(cambio, folio, fechaOriginal, "20.435.214-5")
                )
            }
        }
        if (oldVersion < 17) {
            val fechas = listOf(
                "PF-UNI-001" to "2026-11-19",
                "PF-LUZ-001" to "2026-11-25",
                "PF-COND-001" to "2026-12-10",
                "PF-AGUA-001" to "2026-12-20"
            )
            for ((folio, fecha) in fechas) {
                db.execSQL(
                    "UPDATE $TABLA_COMPROMISOS SET $VENCIMIENTO = ?, $ESTADO = 'Pendiente' " +
                        "WHERE $FOLIO = ? AND $ESTADO IN ('Pendiente', 'Vencido') " +
                        "AND $ID_USUARIO IN (SELECT $ID_USUARIO FROM $TABLA_USUARIOS WHERE $RUT = ?)",
                    arrayOf(fecha, folio, "20.435.214-5")
                )
            }
        }
    }

    private fun crearTablaAplazamientos(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS aplazamientos_cuota (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, operacion TEXT NOT NULL, " +
                "folio TEXT NOT NULL REFERENCES compromisos(folio), usuario_id INTEGER NOT NULL REFERENCES usuarios(id_usuario), " +
                "fecha_anterior TEXT NOT NULL, fecha_nueva TEXT NOT NULL, " +
                "monto_anterior INTEGER NOT NULL, monto_nuevo INTEGER NOT NULL, " +
                "solicitada INTEGER NOT NULL, fecha_accion TEXT NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS aplazamiento_unico ON aplazamientos_cuota(folio) WHERE solicitada = 1")
    }

    fun empresaEjecutivo(id: Long): String? = readableDatabase.rawQuery(
        "SELECT e.empresa FROM ejecutivo_empresa e JOIN usuarios u ON u.id_usuario = e.id_usuario JOIN roles r ON r.id_rol = u.id_rol WHERE e.id_usuario = ? AND r.nombre_rol = 'EJECUTIVO'",
        arrayOf(id.toString())
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    fun listarCompromisosEjecutivo(id: Long): List<Compromiso> {
        val empresa = empresaEjecutivo(id) ?: return emptyList()
        return listarCompromisos(empresa = empresa)
    }

    fun montosPagadosEjecutivo(id: Long): Map<String, Int> {
        val empresa = empresaEjecutivo(id) ?: return emptyMap()
        val montos = mutableMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT p.folio, p.monto_pagado FROM pagos p JOIN compromisos c ON c.folio = p.folio WHERE c.nombre_empresa = ? COLLATE NOCASE",
            arrayOf(empresa)
        ).use { cursor -> while (cursor.moveToNext()) montos[cursor.getString(0)] = cursor.getInt(1) }
        return montos
    }

    fun registrarCompromisoEjecutivo(id: Long, rut: String, concepto: String, monto: Int, fecha: String, cuota: String): Boolean {
        val empresa = empresaEjecutivo(id) ?: return false
        if (!Validaciones.rutValido(rut) || concepto.isBlank() || monto <= 0) return false
        val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { isLenient = false }
        val fechaValida = try {
            fecha.matches(Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}""")) &&
                formatoFecha.parse(fecha) != null && fecha >= formatoFecha.format(java.util.Date())
        } catch (_: Exception) { false }
        if (!fechaValida) return false
        val cantidadCuotas = if (cuota.isBlank()) 1 else cuota.toIntOrNull() ?: return false
        if (cantidadCuotas !in 1..120 || monto < cantidadCuotas) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val persona = db.rawQuery(
                "SELECT id_usuario FROM usuarios WHERE REPLACE(REPLACE(UPPER(rut), '.', ''), '-', '') = ? AND id_rol = 1",
                arrayOf(Validaciones.normalizarRut(rut))
            ).use { if (it.moveToFirst()) it.getLong(0) else return false }
            val cuotas = PlanCuotas.calcular(monto, cantidadCuotas, fecha)
            val planId = if (cuota.isBlank()) "" else java.util.UUID.randomUUID().toString()

            for (cuotaCalculada in cuotas) {
                val numero = if (cuota.isBlank()) "" else "${cuotaCalculada.numero}/$cantidadCuotas"
                val existe = db.rawQuery(
                    "SELECT 1 FROM compromisos WHERE id_usuario = ? AND nombre_empresa = ? COLLATE NOCASE AND concepto = ? COLLATE NOCASE AND numero_cuota = ?",
                    arrayOf(persona.toString(), empresa, concepto, numero)
                ).use { it.moveToFirst() }
                if (existe) return false

                val folio = "PF-" + java.util.UUID.randomUUID().toString().take(12).uppercase()
                val valores = ContentValues().apply {
                    put("folio", folio)
                    put("id_usuario", persona)
                    put("nombre_empresa", empresa)
                    put("concepto", concepto)
                    put("monto", cuotaCalculada.monto)
                    put("vencimiento", cuotaCalculada.vencimiento)
                    put("estado", "Pendiente")
                    put("numero_cuota", numero)
                    put("plan_id", planId)
                }
                if (db.insert("compromisos", null, valores) == -1L) return false
                db.execSQL(
                    "INSERT INTO acciones_ejecutivo (folio, ejecutivo_id, accion, fecha) VALUES (?, ?, 'Registro', datetime('now', 'localtime'))",
                    arrayOf<Any>(folio, id)
                )
            }
            db.setTransactionSuccessful()
            return true
        } finally { db.endTransaction() }
    }

    fun modificarCuotaEjecutivo(id: Long, folio: String, monto: Int, fecha: String): Boolean {
        val empresa = empresaEjecutivo(id) ?: return false
        if (monto <= 0 || !fecha.matches(Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}"""))) return false
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { isLenient = false }
        val fechaValida = try { formato.parse(fecha) != null && fecha >= formato.format(java.util.Date()) } catch (_: Exception) { false }
        if (!fechaValida) return false

        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery(
                "SELECT plan_id, numero_cuota FROM compromisos WHERE folio = ? AND nombre_empresa = ? COLLATE NOCASE AND estado IN ('Pendiente', 'Vencido') AND numero_cuota <> ''",
                arrayOf(folio, empresa)
            ).use { if (it.moveToFirst()) it.getString(0) to it.getString(1) else return false }
            val numero = datos.second.substringBefore('/').toIntOrNull() ?: return false
            if (datos.first.isNotBlank()) {
                val fueraDeOrden = db.rawQuery(
                    "SELECT 1 FROM compromisos WHERE plan_id = ? AND folio <> ? AND estado <> 'Anulado' AND ((CAST(substr(numero_cuota, 1, instr(numero_cuota, '/') - 1) AS INTEGER) < ? AND vencimiento >= ?) OR (CAST(substr(numero_cuota, 1, instr(numero_cuota, '/') - 1) AS INTEGER) > ? AND vencimiento <= ?)) LIMIT 1",
                    arrayOf(datos.first, folio, numero.toString(), fecha, numero.toString(), fecha)
                ).use { it.moveToFirst() }
                if (fueraDeOrden) return false
            }
            val valores = ContentValues().apply { put("monto", monto); put("vencimiento", fecha); put("estado", "Pendiente") }
            if (db.update("compromisos", valores, "folio = ? AND nombre_empresa = ? COLLATE NOCASE AND estado IN ('Pendiente', 'Vencido')", arrayOf(folio, empresa)) != 1) return false
            db.execSQL(
                "INSERT INTO acciones_ejecutivo (folio, ejecutivo_id, accion, fecha) VALUES (?, ?, 'Modificación de cuota', datetime('now', 'localtime'))",
                arrayOf<Any>(folio, id)
            )
            db.setTransactionSuccessful()
            return true
        } finally { db.endTransaction() }
    }

    fun anularCompromisoEjecutivo(id: Long, folio: String, motivo: String): Boolean {
        val empresa = empresaEjecutivo(id) ?: return false
        if (motivo.isBlank()) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val valores = ContentValues().apply { put("estado", "Anulado") }
            val actualizados = db.update("compromisos", valores, "folio = ? AND nombre_empresa = ? COLLATE NOCASE AND estado IN ('Pendiente', 'Vencido', 'En revisión')", arrayOf(folio, empresa))
            if (actualizados != 1) return false
            db.execSQL("INSERT INTO acciones_ejecutivo (folio, ejecutivo_id, accion, motivo, fecha) VALUES (?, ?, 'Anulación', ?, datetime('now', 'localtime'))", arrayOf<Any>(folio, id, motivo))
            db.setTransactionSuccessful()
            return true
        } finally { db.endTransaction() }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion >= 8 && newVersion >= 7) return
        super.onDowngrade(db, oldVersion, newVersion)
    }

    fun listarCompromisos(idUsuario: Long? = null, empresa: String? = null): List<Compromiso> {
        val lista = mutableListOf<Compromiso>()
        val foliosVencidos = mutableListOf<String>()
        val condiciones = mutableListOf<String>()
        val valores = mutableListOf<String>()
        if (idUsuario != null) { condiciones.add("c.$ID_USUARIO = ?"); valores.add(idUsuario.toString()) }
        if (empresa != null) { condiciones.add("c.$NOMBRE_EMPRESA = ? COLLATE NOCASE"); valores.add(empresa) }
        val filtro = if (condiciones.isEmpty()) "" else "WHERE " + condiciones.joinToString(" AND ")
        val argumentos = if (valores.isEmpty()) null else valores.toTypedArray()
        readableDatabase.rawQuery(
            "SELECT c.*, u.$RUT, u.$NOMBRE, u.$CORREO, u.telefono, u.$ID_ROL, r.$NOMBRE_ROL AS $NOMBRE_ROL " +
                    "FROM $TABLA_COMPROMISOS c " +
                    "INNER JOIN $TABLA_USUARIOS u ON c.$ID_USUARIO = u.$ID_USUARIO " +
                    "INNER JOIN $TABLA_ROLES r ON u.$ID_ROL = r.$ID_ROL " +
                    "$filtro ORDER BY c.$VENCIMIENTO, c.$ID_COMPROMISO",
            argumentos
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val vencimiento = cursor.getString(cursor.getColumnIndexOrThrow(VENCIMIENTO))
                var estado = cursor.getString(cursor.getColumnIndexOrThrow(ESTADO))
                if (estado == "Pendiente" && vencimiento < java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())) {
                    estado = "Vencido"
                    foliosVencidos.add(cursor.getString(cursor.getColumnIndexOrThrow(FOLIO)))
                }
                lista.add(
                    Compromiso(
                        folio = cursor.getString(cursor.getColumnIndexOrThrow(FOLIO)),
                        empresa = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_EMPRESA)),
                        usuario = Usuario(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_USUARIO)),
                            rut = cursor.getString(cursor.getColumnIndexOrThrow(RUT)),
                            nombre = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE)),
                            correo = cursor.getString(cursor.getColumnIndexOrThrow(CORREO)),
                            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                            rolId = cursor.getLong(cursor.getColumnIndexOrThrow(ID_ROL)),
                            rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_ROL))
                        ),
                        concepto = cursor.getString(cursor.getColumnIndexOrThrow(CONCEPTO)),
                        monto = cursor.getInt(cursor.getColumnIndexOrThrow(MONTO)),
                        vencimiento = vencimiento,
                        estado = estado,
                        recargo = cursor.getInt(cursor.getColumnIndexOrThrow(RECARGO)),
                        numeroCuota = cursor.getString(cursor.getColumnIndexOrThrow(NUMERO_CUOTA)),
                        planId = cursor.getString(cursor.getColumnIndexOrThrow("plan_id"))
                    )
                )
            }
        }
        foliosVencidos.forEach { vencido ->
            writableDatabase.execSQL("UPDATE $TABLA_COMPROMISOS SET $ESTADO = 'Vencido' WHERE $FOLIO = ?", arrayOf(vencido))
        }
        return lista
    }

    fun insertarCompromiso(compromiso: Compromiso, idUsuario: Long): Long = writableDatabase.insert(
        TABLA_COMPROMISOS, null, ContentValues().apply {
            put(FOLIO, compromiso.folio)
            put(ID_USUARIO, idUsuario)
            put(NOMBRE_EMPRESA, compromiso.empresa)
            put(CONCEPTO, compromiso.concepto)
            put(MONTO, compromiso.monto)
            put(VENCIMIENTO, compromiso.vencimiento)
            put(ESTADO, compromiso.estado)
            put(RECARGO, compromiso.recargo)
            put(NUMERO_CUOTA, compromiso.numeroCuota)
            put("plan_id", compromiso.planId)
        }
    )

    fun eliminarCompromiso(folio: String, idUsuario: Long): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val tienePagoOReporte = db.rawQuery(
                "SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ? " +
                        "UNION ALL SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ? LIMIT 1",
                arrayOf(folio, folio)
            ).use { it.moveToFirst() }
            if (tienePagoOReporte) return false

            val eliminados = db.delete(
                TABLA_COMPROMISOS,
                "$FOLIO = ? AND $ID_USUARIO = ? AND $ESTADO IN ('Pendiente', 'Vencido')",
                arrayOf(folio, idUsuario.toString())
            )
            if (eliminados != 1) return false

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun marcarCompromisoEnRevision(folio: String): Boolean {
        val valores = ContentValues().apply { put(ESTADO, "En revisión") }
        return writableDatabase.update(
            TABLA_COMPROMISOS,
            valores,
            "$FOLIO = ? AND $ESTADO IN ('Pendiente', 'Vencido')",
            arrayOf(folio)
        ) == 1
    }

    fun registrarReporte(folio: String, motivo: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val actualizado = db.update(
                TABLA_COMPROMISOS,
                ContentValues().apply { put(ESTADO, "En revisión") },
                "$FOLIO = ? AND $ESTADO IN ('Pendiente', 'Vencido')",
                arrayOf(folio)
            )
            if (actualizado != 1) return false

            val fecha = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.US
            ).format(java.util.Date())
            val insertado = db.insert(
                TABLA_REPORTES,
                null,
                ContentValues().apply {
                    put(FOLIO, folio)
                    put(MOTIVO_REPORTE, motivo)
                    put(ESTADO_REPORTE, "Pendiente")
                    put(FECHA_REPORTE, fecha)
                }
            )
            if (insertado == -1L) return false

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun listarReportes(): List<Reporte> {
        val lista = mutableListOf<Reporte>()
        readableDatabase.rawQuery(
            "SELECT $FOLIO, $MOTIVO_REPORTE, $ESTADO_REPORTE, respuesta FROM $TABLA_REPORTES ORDER BY $FECHA_REPORTE DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Reporte(
                        folio = cursor.getString(cursor.getColumnIndexOrThrow(FOLIO)),
                        motivo = cursor.getString(cursor.getColumnIndexOrThrow(MOTIVO_REPORTE)),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow(ESTADO_REPORTE)),
                        respuesta = cursor.getString(cursor.getColumnIndexOrThrow("respuesta"))
                    )
                )
            }
        }
        return lista
    }

    fun listarReportesEjecutivo(id: Long): List<Reporte> {
        val empresa = empresaEjecutivo(id) ?: return emptyList()
        val lista = mutableListOf<Reporte>()
        readableDatabase.rawQuery(
            "SELECT r.folio, r.motivo, r.estado_reporte, r.respuesta FROM reportes r JOIN compromisos c ON c.folio = r.folio WHERE c.nombre_empresa = ? COLLATE NOCASE ORDER BY r.fecha_reporte DESC",
            arrayOf(empresa)
        ).use { cursor ->
            while (cursor.moveToNext()) lista.add(Reporte(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
        }
        return lista
    }

    fun actualizarEstadoReporte(folio: String, estado: String): Boolean {
        if (estado != "En proceso") return false
        return writableDatabase.update(
            TABLA_REPORTES,
            ContentValues().apply { put(ESTADO_REPORTE, estado) },
            "$FOLIO = ? AND $ESTADO_REPORTE = 'Pendiente'",
            arrayOf(folio)
        ) == 1
    }

    fun resolverReporte(folio: String, estadoCompromiso: String, respuesta: String): Boolean {
        if (estadoCompromiso !in listOf("Pendiente", "Anulado") || respuesta.isBlank()) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val compromisoActualizado = db.update(
                TABLA_COMPROMISOS,
                ContentValues().apply { put(ESTADO, estadoCompromiso) },
                "$FOLIO = ? AND $ESTADO = 'En revisión'",
                arrayOf(folio)
            )
            if (compromisoActualizado != 1) return false

            val reporteActualizado = db.update(
                TABLA_REPORTES,
                ContentValues().apply {
                    put(ESTADO_REPORTE, "Resuelto")
                    put("respuesta", respuesta.trim())
                },
                "$FOLIO = ? AND $ESTADO_REPORTE = 'En proceso'",
                arrayOf(folio)
            )
            if (reporteActualizado != 1) return false

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun confirmarPago(folio: String, medioPago: String): Comprobante? {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery(
                "SELECT * FROM $TABLA_COMPROMISOS WHERE $FOLIO = ?", arrayOf(folio)
            ).use { c ->
                if (!c.moveToFirst()) return null
                val fechaVencimiento = c.getString(c.getColumnIndexOrThrow(VENCIMIENTO))
                val estadoGuardado = c.getString(c.getColumnIndexOrThrow(ESTADO))
                val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val estado = if (estadoGuardado == "Pendiente" && fechaVencimiento < hoy) "Vencido" else estadoGuardado
                if (estado != "Pendiente" && estado != "Vencido") return null
                Triple(
                    c.getString(c.getColumnIndexOrThrow(NOMBRE_EMPRESA)),
                    c.getString(c.getColumnIndexOrThrow(CONCEPTO)),
                    c.getInt(c.getColumnIndexOrThrow(MONTO)) + if (estado == "Vencido") c.getInt(c.getColumnIndexOrThrow(RECARGO)) else 0
                )
            }
            val bloqueo = db.rawQuery(
                "SELECT 1 FROM $TABLA_COMPROMISOS actual JOIN $TABLA_COMPROMISOS anterior " +
                        "ON actual.$ID_USUARIO = anterior.$ID_USUARIO AND actual.$NOMBRE_EMPRESA = anterior.$NOMBRE_EMPRESA " +
                        "AND (anterior.$VENCIMIENTO < actual.$VENCIMIENTO OR (anterior.$VENCIMIENTO = actual.$VENCIMIENTO AND anterior.$ID_COMPROMISO < actual.$ID_COMPROMISO)) " +
                        "WHERE actual.$FOLIO = ? AND anterior.$ESTADO IN ('Pendiente','Vencido') LIMIT 1", arrayOf(folio)
            ).use { it.moveToFirst() }
            if (bloqueo) return null
            val secuencia = db.rawQuery("SELECT COUNT(*) + 1 FROM $TABLA_PAGOS", null).use { c -> c.moveToFirst(); c.getInt(0) }
            val numero = "CPR-${secuencia.toString().padStart(6, '0')}"
            val fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
            val recibo = Comprobante(
                numero = numero,
                folioCompromiso = folio,
                empresa = datos.first,
                concepto = datos.second,
                montoPagado = datos.third,
                fechaHora = fecha,
                canal = "App",
                medioPago = medioPago
            )
            val insertado = db.insert(TABLA_PAGOS, null, ContentValues().apply {
                put(NUMERO_COMPROBANTE, recibo.numero)
                put(FOLIO, recibo.folioCompromiso)
                put(NOMBRE_EMPRESA, recibo.empresa)
                put(CONCEPTO, recibo.concepto)
                put(MONTO_PAGADO, recibo.montoPagado)
                put(FECHA_HORA, recibo.fechaHora)
                put(CANAL, "App")
                put(MEDIO_PAGO, recibo.medioPago)
            })
            if (insertado == -1L) return null
            db.update(TABLA_COMPROMISOS, ContentValues().apply { put(ESTADO, "Pagado") }, "$FOLIO = ?", arrayOf(folio))
            db.setTransactionSuccessful()
            return recibo
        } finally {
            db.endTransaction()
        }
    }

    fun buscarComprobante(folio: String): Comprobante? = readableDatabase.rawQuery(
        "SELECT * FROM $TABLA_PAGOS WHERE $FOLIO = ?", arrayOf(folio)
    ).use { c ->
        if (!c.moveToFirst()) null else Comprobante(
            numero = c.getString(c.getColumnIndexOrThrow(NUMERO_COMPROBANTE)),
            folioCompromiso = c.getString(c.getColumnIndexOrThrow(FOLIO)),
            empresa = c.getString(c.getColumnIndexOrThrow(NOMBRE_EMPRESA)),
            concepto = c.getString(c.getColumnIndexOrThrow(CONCEPTO)),
            montoPagado = c.getInt(c.getColumnIndexOrThrow(MONTO_PAGADO)),
            fechaHora = c.getString(c.getColumnIndexOrThrow(FECHA_HORA)),
            canal = c.getString(c.getColumnIndexOrThrow(CANAL)),
            medioPago = c.getString(c.getColumnIndexOrThrow(MEDIO_PAGO))
        )
    }

    fun listarComprobantes(idUsuario: Long): List<Comprobante> {
        val lista = mutableListOf<Comprobante>()
        readableDatabase.rawQuery(
            "SELECT p.* FROM $TABLA_PAGOS p " +
                    "INNER JOIN $TABLA_COMPROMISOS c ON p.$FOLIO = c.$FOLIO " +
                    "WHERE c.$ID_USUARIO = ? ORDER BY p.$FECHA_HORA DESC",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Comprobante(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow(NUMERO_COMPROBANTE)),
                        folioCompromiso = cursor.getString(cursor.getColumnIndexOrThrow(FOLIO)),
                        empresa = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_EMPRESA)),
                        concepto = cursor.getString(cursor.getColumnIndexOrThrow(CONCEPTO)),
                        montoPagado = cursor.getInt(cursor.getColumnIndexOrThrow(MONTO_PAGADO)),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(FECHA_HORA)),
                        canal = cursor.getString(cursor.getColumnIndexOrThrow(CANAL)),
                        medioPago = cursor.getString(cursor.getColumnIndexOrThrow(MEDIO_PAGO))
                    )
                )
            }
        }
        return lista
    }

    fun listarRoles(): List<Rol>{
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ID_ROL, $NOMBRE_ROL FROM $TABLA_ROLES ORDER BY $ID_ROL",
            null
        ).use {  cursor ->
            while (cursor.moveToNext()){
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_ROL)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_ROL))
                    )
                )
            }
        }
        return roles
    }

    fun insertarUsuario(usuario: Usuario, contrasena: String): Long {
        if (!Validaciones.rutValido(usuario.rut) || !Validaciones.correoValido(usuario.correo) ||
            usuario.nombre.isBlank() || contrasena.isBlank() || contrasena.length < 6) return -1L
        val db = writableDatabase
        db.beginTransaction()
        try {
            if (existeOtraCuenta(db, usuario.copy(id = 0))) return -1L
            val valores = valoresDe(usuario).apply { put(CONTRASENA, contrasena) }
            val id = db.insert(TABLA_USUARIOS, null, valores)
            if (id != -1L) db.setTransactionSuccessful()
            return id
        } finally { db.endTransaction() }
    }

    private fun existeOtraCuenta(db: SQLiteDatabase, usuario: Usuario): Boolean = db.rawQuery(
        "SELECT 1 FROM $TABLA_USUARIOS WHERE $ID_USUARIO != ? AND (" +
            "REPLACE(REPLACE(REPLACE(UPPER($RUT), '.', ''), '-', ''), ' ', '') = ? " +
            "OR LOWER(TRIM($CORREO)) = ?) LIMIT 1",
        arrayOf(usuario.id.toString(), Validaciones.normalizarRut(usuario.rut), usuario.correo.trim().lowercase())
    ).use { it.moveToFirst() }

    fun esEmpresaGestionada(nombre: String): Boolean {
        if (CatalogoServicios.esDuoc(nombre)) return true
        val buscada = CatalogoServicios.normalizarNombre(nombre)
        return readableDatabase.rawQuery("SELECT empresa FROM ejecutivo_empresa", null).use { cursor ->
            var encontrada = false
            while (cursor.moveToNext()) {
                if (CatalogoServicios.normalizarNombre(cursor.getString(0)) == buscada) {
                    encontrada = true
                    break
                }
            }
            encontrada
        }
    }

    fun insertarServicio(servicio: Servicio): Long {
        if (servicio.nombreEmpresa.isBlank()) return -1L
        val disponible = CatalogoServicios.buscar(servicio.nombreEmpresa)
        val nombreEmpresa = if (CatalogoServicios.esDuoc(servicio.nombreEmpresa)) "Duoc Uc" else disponible?.empresa ?: return -1L
        val gestionada = esEmpresaGestionada(nombreEmpresa)
        if (!Validaciones.rutValido(servicio.rutPagador) || servicio.alias.isBlank()) return -1L
        val db = writableDatabase
        db.beginTransaction()
        try {
            val rutCuenta = db.rawQuery(
                "SELECT $RUT FROM $TABLA_USUARIOS WHERE $ID_USUARIO = ? AND $ID_ROL = 1",
                arrayOf(servicio.usuarioId.toString())
            ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else return -1L }

            val servicioRepetido = db.rawQuery(
                "SELECT 1 FROM $TABLA_SERVICIOS WHERE $ID_USUARIO = ? AND $NOMBRE_EMPRESA = ? COLLATE NOCASE AND REPLACE(REPLACE(UPPER($RUT_PAGADOR), '.', ''), '-', '') = ? LIMIT 1",
                arrayOf(servicio.usuarioId.toString(), nombreEmpresa, Validaciones.normalizarRut(servicio.rutPagador))
            ).use { it.moveToFirst() }
            if (servicioRepetido) return -1L

            val valoresServicio = ContentValues().apply {
                put(ID_USUARIO, servicio.usuarioId)
                put(NOMBRE_EMPRESA, nombreEmpresa)
                put(RUT_PAGADOR, Validaciones.formatearRut(servicio.rutPagador))
                put(ALIAS, servicio.alias.trim())
            }
            val servicioId = db.insert(TABLA_SERVICIOS, null, valoresServicio)
            if (servicioId == -1L) return -1L

            val mismoRut = Validaciones.normalizarRut(rutCuenta) == Validaciones.normalizarRut(servicio.rutPagador)
            val cobroExistente = mismoRut && db.rawQuery(
                "SELECT 1 FROM $TABLA_COMPROMISOS WHERE $ID_USUARIO = ? AND $NOMBRE_EMPRESA = ? COLLATE NOCASE AND $ESTADO IN ('Pendiente', 'Vencido', 'En revisión') LIMIT 1",
                arrayOf(servicio.usuarioId.toString(), nombreEmpresa)
            ).use { it.moveToFirst() }

            if (!cobroExistente && !gestionada) {
                val montoPrueba = disponible?.monto ?: return -1L
                val fecha = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 30) }
                val vencimiento = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(fecha.time)
                val valoresCobro = ContentValues().apply {
                    put(FOLIO, "SERV-" + java.util.UUID.randomUUID().toString().take(12).uppercase())
                    put(ID_USUARIO, servicio.usuarioId)
                    put(NOMBRE_EMPRESA, nombreEmpresa)
                    put(CONCEPTO, servicio.alias.trim())
                    put(MONTO, montoPrueba)
                    put(VENCIMIENTO, vencimiento)
                    put(ESTADO, "Pendiente")
                }
                if (db.insert(TABLA_COMPROMISOS, null, valoresCobro) == -1L) return -1L
            }
            db.setTransactionSuccessful()
            return servicioId
        } finally {
            db.endTransaction()
        }
    }

    fun crearCuotasServicio(usuarioId: Long, folio: String, cantidad: Int): Boolean {
        if (cantidad !in 2..12) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val compromiso = listarCompromisos(usuarioId).find { it.folio == folio } ?: return false
            if (CatalogoServicios.buscar(compromiso.empresa) == null || esEmpresaGestionada(compromiso.empresa) ||
                compromiso.estado !in listOf("Pendiente", "Vencido") ||
                compromiso.numeroCuota.isNotBlank() || compromiso.planId.isNotBlank()) return false

            val tieneHistorial = db.rawQuery(
                "SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ? UNION ALL SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ? LIMIT 1",
                arrayOf(folio, folio)
            ).use { it.moveToFirst() }
            if (tieneHistorial) return false
            val total = compromiso.monto.toLong() + if (compromiso.estado == "Vencido") compromiso.recargo else 0
            if (total < cantidad || total > Int.MAX_VALUE) return false
            val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val primerVencimiento = maxOf(compromiso.vencimiento, hoy)
            val cuotas = PlanCuotas.calcular(total.toInt(), cantidad, primerVencimiento)
            val planId = java.util.UUID.randomUUID().toString()

            cuotas.forEachIndexed { indice, cuota ->
                val valores = ContentValues().apply {
                    put(MONTO, cuota.monto)
                    put(VENCIMIENTO, cuota.vencimiento)
                    put(ESTADO, "Pendiente")
                    put(RECARGO, 0)
                    put(NUMERO_CUOTA, "${cuota.numero}/$cantidad")
                    put("plan_id", planId)
                }
                if (indice == 0) {
                    if (db.update(TABLA_COMPROMISOS, valores, "$FOLIO = ? AND $ID_USUARIO = ? AND $ESTADO IN ('Pendiente', 'Vencido')", arrayOf(folio, usuarioId.toString())) != 1) return false
                } else {
                    valores.put(FOLIO, "SERV-" + java.util.UUID.randomUUID().toString().take(12).uppercase())
                    valores.put(ID_USUARIO, usuarioId)
                    valores.put(NOMBRE_EMPRESA, compromiso.empresa)
                    valores.put(CONCEPTO, compromiso.concepto)
                    if (db.insert(TABLA_COMPROMISOS, null, valores) == -1L) return false
                }
            }
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun aplazarCuotaServicio(usuarioId: Long, folio: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val seleccionada = listarCompromisos(usuarioId).find { it.folio == folio } ?: return false
            if (CatalogoServicios.buscar(seleccionada.empresa) == null || esEmpresaGestionada(seleccionada.empresa) ||
                seleccionada.estado != "Pendiente" || seleccionada.planId.isBlank()) return false
            val numero = seleccionada.numeroCuota.substringBefore('/').toIntOrNull() ?: return false
            val yaAplazada = db.rawQuery(
                "SELECT 1 FROM aplazamientos_cuota WHERE folio = ? AND solicitada = 1",
                arrayOf(folio)
            ).use { it.moveToFirst() }
            if (yaAplazada) return false

            val posteriores = listarCompromisos(usuarioId)
                .filter { it.planId == seleccionada.planId && (it.numeroCuota.substringBefore('/').toIntOrNull() ?: 0) >= numero && it.estado != "Anulado" }
                .sortedBy { it.numeroCuota.substringBefore('/').toIntOrNull() }
            if (posteriores.isEmpty() || posteriores.any { it.estado != "Pendiente" }) return false
            val tieneHistorial = posteriores.any { compromiso ->
                db.rawQuery(
                    "SELECT 1 FROM $TABLA_PAGOS WHERE $FOLIO = ? UNION ALL SELECT 1 FROM $TABLA_REPORTES WHERE $FOLIO = ? LIMIT 1",
                    arrayOf(compromiso.folio, compromiso.folio)
                ).use { it.moveToFirst() }
            }
            if (tieneHistorial) return false
            val nuevoMonto = AplazamientoCuota.montoConRecargo(seleccionada.monto)
            if (nuevoMonto > Int.MAX_VALUE) return false
            val operacion = java.util.UUID.randomUUID().toString()
            for (compromiso in posteriores) {
                val nuevaFecha = try {
                    AplazamientoCuota.mesSiguiente(compromiso.vencimiento)
                } catch (_: Exception) { return false }
                val esSeleccionada = compromiso.folio == folio
                val monto = if (esSeleccionada) nuevoMonto.toInt() else compromiso.monto
                val valores = ContentValues().apply { put(VENCIMIENTO, nuevaFecha); put(MONTO, monto) }
                if (db.update(TABLA_COMPROMISOS, valores, "$FOLIO = ? AND $ID_USUARIO = ? AND $ESTADO = 'Pendiente'", arrayOf(compromiso.folio, usuarioId.toString())) != 1) return false
                db.execSQL(
                    "INSERT INTO aplazamientos_cuota (operacion, folio, usuario_id, fecha_anterior, fecha_nueva, monto_anterior, monto_nuevo, solicitada, fecha_accion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))",
                    arrayOf<Any>(operacion, compromiso.folio, usuarioId, compromiso.vencimiento, nuevaFecha, compromiso.monto, monto, if (esSeleccionada) 1 else 0)
                )
            }
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun eliminarServicio(idServicio: Long, idUsuario: Long): Boolean {
        val filasEliminadas = writableDatabase.delete(
            TABLA_SERVICIOS,
            "$ID_SERVICIO = ? AND $ID_USUARIO = ?",
            arrayOf(idServicio.toString(), idUsuario.toString())
        )
        return filasEliminadas > 0
    }

    fun listarServicios(idUsuario: Long): List<Servicio> {
        val servicios = mutableListOf<Servicio>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_SERVICIOS WHERE $ID_USUARIO = ? ORDER BY $ID_SERVICIO DESC",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                servicios.add(
                    Servicio(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_SERVICIO)),
                        usuarioId = cursor.getLong(cursor.getColumnIndexOrThrow(ID_USUARIO)),
                        nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_EMPRESA)),
                        rutPagador = cursor.getString(cursor.getColumnIndexOrThrow(RUT_PAGADOR)),
                        alias = cursor.getString(cursor.getColumnIndexOrThrow(ALIAS))
                    )
                )
            }
        }
        return servicios
    }

    fun iniciarSesion(identificador: String, contrasena: String): Usuario? {
        val entrada = identificador.trim()
        val rutNormalizado = entrada.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE (LOWER(u.$CORREO) = ? OR " +
                "REPLACE(REPLACE(REPLACE(UPPER(u.$RUT), '.', ''), '-', ''), ' ', '') = ?) " +
                "AND u.$CONTRASENA = ?",
            arrayOf(entrada.lowercase(), rutNormalizado, contrasena)
        ).firstOrNull()
    }

    fun cambiarContrasena(idUsuario: Long, actual: String, nueva: String): Boolean {
        if (nueva.isBlank() || nueva.length < 6) return false
        val valores = ContentValues().apply { put(CONTRASENA, nueva) }
        return writableDatabase.update(
            TABLA_USUARIOS, valores,
            "$ID_USUARIO = ? AND $CONTRASENA = ?",
            arrayOf(idUsuario.toString(), actual)
        ) == 1
    }

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$NOMBRE", null)

    fun buscarUsuarios(texto: String): List<Usuario>{
        val patron = "%$texto%"

        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS " +
                    "WHERE u.$NOMBRE LIKE ? OR u.$CORREO LIKE ? OR u.$RUT LIKE ? " +
                    "ORDER BY u.$NOMBRE",
            arrayOf(patron, patron, patron)
        )
    }
    fun buscarPorId(id: Long): Usuario? {
        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$ID_USUARIO = ?",
            arrayOf(id.toString())
        ).firstOrNull()
    }

    fun actualizarUsuario(usuario: Usuario): Int {
        if (usuario.nombre.isBlank() || !Validaciones.correoValido(usuario.correo)) return 0
        val db = writableDatabase
        db.beginTransaction()
        try {
            val anterior = buscarPorId(usuario.id) ?: return 0
            val conservaRut = Validaciones.normalizarRut(anterior.rut) == Validaciones.normalizarRut(usuario.rut)
            if ((!conservaRut && !Validaciones.rutValido(usuario.rut)) || existeOtraCuenta(db, usuario)) return 0
            val filas = db.update(TABLA_USUARIOS, valoresDe(usuario), "$ID_USUARIO = ?", arrayOf(usuario.id.toString()))
            if (filas == 1) db.setTransactionSuccessful()
            return filas
        } finally { db.endTransaction() }
    }

    fun actualizarAvatar(id: Long, avatar: ByteArray): Boolean =
        writableDatabase.update(TABLA_USUARIOS, ContentValues().apply { put("avatar", avatar) },
            "$ID_USUARIO = ?", arrayOf(id.toString())) == 1

    fun eliminarUsuario(id: Long): Int {
        return writableDatabase.delete(
            TABLA_USUARIOS,
            "$ID_USUARIO = ?",
            arrayOf(id.toString())
        )
    }

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
            put(RUT, Validaciones.formatearRut(usuario.rut))
            put(NOMBRE, usuario.nombre)
            put(CORREO, usuario.correo.trim().lowercase())
            put(ID_ROL, usuario.rolId)
            put("telefono", usuario.telefono)
            put("direccion", usuario.direccion)
            put("ciudad", usuario.ciudad)
            put("avatar", usuario.avatar)
    }

    private fun consultarUsuarios(sql: String, argumentos: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()

        readableDatabase.rawQuery(sql, argumentos).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }
    private fun leerUsuario(cursor: Cursor): Usuario {
        return Usuario(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_USUARIO)),
            rut = cursor.getString(cursor.getColumnIndexOrThrow(RUT)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE)),
            correo = cursor.getString(cursor.getColumnIndexOrThrow(CORREO)),
            rolId = cursor.getLong(cursor.getColumnIndexOrThrow(ID_ROL)),
            rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(NOMBRE_ROL)),
            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
            direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
            ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ciudad")),
            avatar = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"))
        )
    }
}
