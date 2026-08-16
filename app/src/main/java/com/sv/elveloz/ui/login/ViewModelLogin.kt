package com.sv.elveloz.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelLogin : ViewModel() { // Sin dependencias locales

    private val _estado = MutableStateFlow<EstadoLogin>(EstadoLogin.Idle)
    val estado = _estado.asStateFlow()

    fun login(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _estado.value = EstadoLogin.Error("Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _estado.value = EstadoLogin.Cargando
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            // 1. Iniciar sesión en Firebase Auth
            auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    if (firebaseUser != null) {
                        // 2. Consultar el rol del usuario en Firestore
                        db.collection("usuarios").document(firebaseUser.uid).get()
                            .addOnSuccessListener { document ->
                                val rolStr = document.getString("rol") ?: "CLIENTE"
                                val rol = try { RolUsuario.valueOf(rolStr) } catch (e: Exception) { RolUsuario.CLIENTE }
                                val nombre = document.getString("nombre") ?: ""

                                val userSession = UserSession(firebaseUser.uid, nombre, correo, rol)
                                _estado.value = EstadoLogin.Exito(userSession)
                            }
                            .addOnFailureListener {
                                // Fallback por seguridad
                                _estado.value = EstadoLogin.Exito(UserSession(firebaseUser.uid, "", correo, RolUsuario.CLIENTE))
                            }
                    }
                }
                .addOnFailureListener { e ->
                    _estado.value = EstadoLogin.Error("Credenciales incorrectas: ${e.localizedMessage}")
                }
        }
    }

    fun resetEstado() {
        _estado.value = EstadoLogin.Idle
    }
}

// Modelo ligero para reemplazar a la entidad de Room
data class UserSession(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario
)

sealed class EstadoLogin {
    object Idle : EstadoLogin()
    object Cargando : EstadoLogin()
    data class Exito(val usuario: UserSession) : EstadoLogin()
    data class Error(val mensaje: String) : EstadoLogin()
}