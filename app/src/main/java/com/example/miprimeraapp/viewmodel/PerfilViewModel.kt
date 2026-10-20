package com.example.miprimeraapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.miprimeraapp.model.Perfil
import com.example.miprimeraapp.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel : ViewModel() {
    private val perfilEjemplo = Perfil(
        nombre = "Camila Rojas",
        profesion = "Desarrolladora móvil",
        ciudad = "Santiago, Chile",
        descripcion = "Me gusta crear apps simples y bonitas. Actualmente aprendo " +
            "Jetpack Compose y arquitectura MVVM.",
        intereses = listOf("Kotlin", "Compose", "Diseño UI", "Material 3", "Git")
    )

    private val _uiState = MutableStateFlow(PerfilUiState(perfil = perfilEjemplo))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()
}
