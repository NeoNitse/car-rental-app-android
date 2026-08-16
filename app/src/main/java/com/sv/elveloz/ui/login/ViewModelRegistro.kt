package com.sv.elveloz.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelRegistro : ViewModel() {

    private val _estado = MutableStateFlow<EstadoRegistro>(EstadoRegistro.Idle)
    val estado = _estado.asStateFlow()

    fun registrar(nombre: String, correo: String, contrasena: String) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _estado.value = EstadoRegistro.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _estado.value = EstadoRegistro.Cargando
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            // 1. Crear credenciales en Firebase Authentication
            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    // 2. Guardar perfil y rol en Firestore
                    val userMap = hashMapOf(
                        "id" to userId,
                        "nombre" to nombre,
                        "correo" to correo,
                        "rol" to RolUsuario.CLIENTE.name
                    )

                    db.collection("usuarios").document(userId).set(userMap)
                        .addOnSuccessListener {
                            _estado.value = EstadoRegistro.Exito
                        }
                        .addOnFailureListener { e ->
                            _estado.value = EstadoRegistro.Error("Error al guardar perfil: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    _estado.value = EstadoRegistro.Error("Error al registrar: ${e.localizedMessage}")
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