package com.example.app.validacion

/** Reglas compartidas por registro, edición y búsqueda de cuentas. */
object Validaciones {
    fun normalizarRut(valor: String): String = valor.trim().uppercase()
        .replace(".", "").replace("-", "").replace(" ", "")

    fun rutValido(valor: String): Boolean {
        val rut = normalizarRut(valor)
        if (!rut.matches(Regex("[0-9]{1,8}[0-9K]"))) return false
        val cuerpo = rut.dropLast(1)
        if (cuerpo.first() == '0') return false
        val suma = cuerpo.reversed().mapIndexed { indice, digito ->
            digito.digitToInt() * (2 + indice % 6)
        }.sum()
        val verificador = when (val resto = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return rut.last() == verificador
    }

    fun formatearRut(valor: String): String {
        val rut = normalizarRut(valor)
        if (rut.length < 2) return rut
        val cuerpo = rut.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${rut.last()}"
    }

    fun correoValido(valor: String): Boolean {
        val correo = valor.trim()
        if (correo.length > 254 || correo.count { it == '@' } != 1) return false
        val partes = correo.split('@')
        val local = partes[0]
        if (local.isEmpty() || local.length > 64 || local.startsWith('.') || local.endsWith('.') || ".." in local) return false
        if (!local.matches(Regex("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"))) return false
        val dominio = partes[1].split('.')
        return dominio.size >= 2 && dominio.all {
            it.length in 1..63 && it.matches(Regex("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"))
        } && dominio.last().length >= 2
    }
}
