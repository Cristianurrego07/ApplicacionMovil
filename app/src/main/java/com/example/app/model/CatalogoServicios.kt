package com.example.app.model

import java.text.Normalizer
import java.util.Locale

data class ServicioDisponible(val empresa: String, val monto: Int)

object CatalogoServicios {
    fun normalizarNombre(nombre: String): String {
        return Normalizer.normalize(nombre.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("\\s+"), " ")
    }

    fun esDuoc(nombre: String): Boolean {
        return normalizarNombre(nombre).replace(" ", "") in listOf("duoc", "duocuc")
    }

    fun buscar(nombre: String): ServicioDisponible? {
        val nombreIngresado = nombre.trim()
        if (nombreIngresado.isBlank() || esDuoc(nombreIngresado)) return null

        return when (normalizarNombre(nombreIngresado)) {
            "enel", "luz" -> ServicioDisponible("Enel", 28000)
            "agua", "aguas andinas" -> ServicioDisponible("Aguas Andinas", 35000)
            "internet" -> ServicioDisponible("Internet", 23000)
            "condominio" -> ServicioDisponible("Condominio", 45000)
            else -> ServicioDisponible(nombreIngresado, 30000)
        }
    }
}
