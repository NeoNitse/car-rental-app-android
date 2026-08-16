package com.sv.elveloz.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun PantallaLogin(
    viewModel: ViewModelLogin,
    onLoginExitoso: (UserSession) -> Unit,
    onRegistrarseClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(estado) {
        if (estado is EstadoLogin.Exito) {
            onLoginExitoso((estado as EstadoLogin.Exito).usuario)
            viewModel.resetEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "El Veloz - Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val imagen = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val descripcion = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                    Icon(imageVector = imagen, contentDescription = descripcion)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (estado is EstadoLogin.Cargando) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(correo, contrasena) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRegistrarseClick) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }

        if (estado is EstadoLogin.Error) {
            Text(
                text = (estado as EstadoLogin.Error).mensaje,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}