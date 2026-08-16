package com.sv.elveloz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sv.elveloz.ui.AppViewModelFactory
import com.sv.elveloz.ui.NavegacionApp
import com.sv.elveloz.ui.theme.ElVelozTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            // Ya hay sesión iniciada en el teléfono. Buscamos su rol en Firestore.
            FirebaseFirestore.getInstance().collection("usuarios").document(usuarioActual.uid).get()
                .addOnSuccessListener { document ->
                    val rol = document.getString("rol") ?: "CLIENTE"
                    // NOTA: Ajusta "catalog/$rol" si tu ruta se llama distinto en tu NavegacionApp
                    iniciarUI("catalog/$rol")
                }
                .addOnFailureListener {
                    // Fallback de seguridad
                    iniciarUI("login")
                }
        } else {
            // No hay sesión, lo mandamos a loguearse
            iniciarUI("login")
        }
    }

    private fun iniciarUI(rutaInicial: String) {
        setContent {
            ElVelozTheme {
                val factory = AppViewModelFactory(applicationContext)
                // Le pasamos la ruta inicial a tu sistema de navegación
                NavegacionApp(factory = factory, startDestination = rutaInicial)
            }
        }
    }
}