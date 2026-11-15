package com.example.app.validacion

object Validaciones {

    // Quita puntos, guion y espacios para trabajar con el RUT.
    fun normalizarRut(valor: String): String {
        return valor
            .trim()
            .uppercase()
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")
    }

    // Comprueba el cuerpo y el dígito verificador del RUT.
    fun rutValido(valor: String): Boolean {
        val rut = normalizarRut(valor)

        // El RUT debe tener entre 1 y 8 dígitos más su verificador.
        if (!rut.matches(Regex("[0-9]{1,8}[0-9K]"))) {
            return false
        }

        val cuerpo = rut.dropLast(1)

        if (cuerpo.first() == '0') {
            return false
        }

        val suma = cuerpo
            .reversed()
            .mapIndexed { indice, digito ->
                digito.digitToInt() * (2 + indice % 6)
            }
            .sum()

        val verificador = when (val resto = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }

        return rut.last() == verificador
    }

    // Agrega puntos y guion para mostrar y guardar el RUT.
    fun formatearRut(valor: String): String {
        val rut = normalizarRut(valor)

        if (rut.length < 2) {
            return rut
        }

        val cuerpo = rut
            .dropLast(1)
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "$cuerpo-${rut.last()}"
    }

    // Comprueba la estructura del correo electrónico.
    fun correoValido(valor: String): Boolean {
        val correo = valor.trim()

        if (correo.length > 254 || correo.count { it == '@' } != 1) {
            return false
        }

        val partes = correo.split('@')
        val local = partes[0]

        if (
            local.isEmpty() ||
            local.length > 64 ||
            local.startsWith('.') ||
            local.endsWith('.') ||
            ".." in local
        ) {
            return false
        }

        val caracteresValidos = Regex("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+")

        if (!local.matches(caracteresValidos)) {
            return false
        }

        val dominio = partes[1].split('.')
        val formatoParteDominio = Regex(
            "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
        )

        return dominio.size >= 2 &&
                dominio.all { parte ->
                    parte.length in 1..63 &&
                            parte.matches(formatoParteDominio)
                } &&
                dominio.last().length >= 2
    }
}