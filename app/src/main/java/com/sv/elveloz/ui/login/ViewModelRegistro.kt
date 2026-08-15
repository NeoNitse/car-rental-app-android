package com.sv.elveloz.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sv.elveloz.data.local.dao.UsuarioDao
import com.sv.elveloz.data.local.entity.UsuarioEntity
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelRegistro(private val usuarioDao: UsuarioDao) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoRegistro>(EstadoRegistro.Idle)
    val estado = _estado.asStateFlow()

    fun registrar(nombre: String, correo: String, contrasena: String) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _estado.value = EstadoRegistro.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _estado.value = EstadoRegistro.Cargando
            try {
                val nuevoUsuario = UsuarioEntity(
                    nombre = nombre,
                    correo = correo,
                    contrasena = contrasena,
                    rol = RolUsuario.CLIENTE
                )
                usuarioDao.insertar(nuevoUsuario)
                _estado.value = EstadoRegistro.Exito
            } catch (e: Exception) {
                _estado.value = EstadoRegistro.Error("Error al registrar: ${e.message}")
            }
        }
    }

    fun resetEstado() {
        _estado.value = EstadoRegistro.Idle
    }
}

sealed class EstadoRegistro {
    object Idle : EstadoRegistro()
    object Cargando : EstadoRegistro()
    object Exito : EstadoRegistro()
    data class Error(val mensaje: String) : EstadoRegistro()
}