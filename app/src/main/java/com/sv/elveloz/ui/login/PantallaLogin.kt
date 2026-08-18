package com.sv.elveloz.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

// Colores base del diseño
val AzulNegro = Color(0xFF0A1128)
val GrisFondo = Color(0xFFF3F4F6)
val GrisBorde = Color(0xFFD1D5DB)
val GrisTexto = Color(0xFF374151)

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
    
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(estado) {
        if (estado is EstadoLogin.Exito) {
            onLoginExitoso((estado as EstadoLogin.Exito).usuario)
            viewModel.resetEstado()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ajuste dinámico de espacio superior según el alto de pantalla
            val topSpacer = if (screenHeight < 600.dp) 40.dp else 80.dp
            Spacer(modifier = Modifier.height(topSpacer))

            // Recreación del Logo con Imagen Real
            Image(
                painter = painterResource(id = com.sv.elveloz.R.drawable.logo_app),
                contentDescription = "Logo Horizon Rides",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Horizon Rides", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AzulNegro)

            val middleSpacer = if (screenHeight < 600.dp) 30.dp else 50.dp
            Spacer(modifier = Modifier.height(middleSpacer))

            // Campo Correo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Correo Electrónico", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("ejemplo@correo.com", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisFondo,
                        unfocusedContainerColor = GrisFondo,
                        focusedBorderColor = AzulNegro,
                        unfocusedBorderColor = GrisBorde,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Campo Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contraseña", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text("••••••••", color = Color(0xFF9CA3AF), letterSpacing = 2.sp) },
                    visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val imagen = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                            Icon(imageVector = imagen, contentDescription = null, tint = Color(0xFF9CA3AF))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisFondo,
                        unfocusedContainerColor = GrisFondo,
                        focusedBorderColor = AzulNegro,
                        unfocusedBorderColor = GrisBorde,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón
            if (estado is EstadoLogin.Cargando) {
                CircularProgressIndicator(color = AzulNegro)
            } else {
                Button(
                    onClick = { viewModel.login(correo, contrasena) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulNegro)
                ) {
                    Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            if (estado is EstadoLogin.Error) {
                Text(
                    text = (estado as EstadoLogin.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Espacio flexible que empuja el link hacia abajo si hay espacio
            Spacer(modifier = Modifier.height(40.dp))

            // Enlace de Registro
            val registerText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF6B7280), fontSize = 15.sp)) { append("¿No tienes cuenta? ") }
                withStyle(style = SpanStyle(color = AzulNegro, fontWeight = FontWeight.Bold, fontSize = 15.sp)) { append("Regístrate") }
            }
            ClickableText(
                text = registerText,
                onClick = { offset -> onRegistrarseClick() },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}