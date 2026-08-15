package com.sv.elveloz.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sv.elveloz.data.local.dao.UsuarioDao
import com.sv.elveloz.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelLogin(private val usuarioDao: UsuarioDao) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoLogin>(EstadoLogin.Idle)
    val estado = _estado.asStateFlow()

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            _estado.value = EstadoLogin.Cargando
            val usuario = usuarioDao.login(correo, contrasena)
            if (usuario != null) {
                _estado.value = EstadoLogin.Exito(usuario)
            } else {
                _estado.value = EstadoLogin.Error("Credenciales incorrectas")
            }
        }
    }

    fun resetEstado() {
        _estado.value = EstadoLogin.Idle
    }
}

sealed class EstadoLogin {
    object Idle : EstadoLogin()
    object Cargando : EstadoLogin()
    data class Exito(val usuario: UsuarioEntity) : EstadoLogin()
    data class Error(val mensaje: String) : EstadoLogin()
}