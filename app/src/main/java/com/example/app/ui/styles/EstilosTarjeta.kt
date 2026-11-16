package com.example.app.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.Dimens

object EstilosTarjeta {

    // Forma de las esquinas
    val forma = RoundedCornerShape(Dimens.radioTarjeta)

    // Color de fondo
    @Composable
    fun colores() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    // Sombra de la tarjeta
    @Composable
    fun elevacion(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp
    )
}