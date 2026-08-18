package com.sv.elveloz.ui.login

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaRegistro(
    viewModel: ViewModelRegistro,
    onRegistroExitoso: () -> Unit,
    onBack: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    // Variables separadas para Nombre y Apellido
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

    // EL PUENTE DE SINCRONIZACIÓN
    LaunchedEffect(estado) {
        if (estado is EstadoRegistro.Exito) {
            // 1. Obtenemos el ID del usuario que se acaba de registrar en Firebase
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: "default"

            // 2. Guardamos sus datos directamente en la caja fuerte de su perfil
            val sharedPrefs = context.getSharedPreferences("ElVelozProfile_$uid", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                putString("firstName", nombre)
                putString("lastName", apellido)
                putString("email", correo)
                apply()
            }

            // 3. Continuamos a la app
            onRegistroExitoso()
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
            val topSpacer = if (screenHeight < 600.dp) 30.dp else 70.dp
            Spacer(modifier = Modifier.height(topSpacer))

            // Recreación del Logo con Imagen Real
            Image(
                painter = painterResource(id = com.sv.elveloz.R.drawable.logo_app),
                contentDescription = "Horizon rides logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Horizon Rides", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AzulNegro)

            val middleSpacer = if (screenHeight < 600.dp) 20.dp else 40.dp
            Spacer(modifier = Modifier.height(middleSpacer))

            // Campos de Nombre y Apellido uno a la par del otro
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Nombre", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = { Text("Tu nombre", color = Color(0xFF9CA3AF)) },
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Apellido", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        placeholder = { Text("Tu apellido", color = Color(0xFF9CA3AF)) },
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
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

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
            if (estado is EstadoRegistro.Cargando) {
                CircularProgressIndicator(color = AzulNegro)
            } else {
                Button(
                    // Unimos Nombre y Apellido para que Firebase lo registre completo en la base de datos
                    onClick = { viewModel.registrar("$nombre $apellido", correo, contrasena) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulNegro)
                ) {
                    Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            if (estado is EstadoRegistro.Error) {
                Text(
                    text = (estado as EstadoRegistro.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Enlace de Retorno a Login
            val loginText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF6B7280), fontSize = 15.sp)) { append("¿Ya tienes cuenta? ") }
                withStyle(style = SpanStyle(color = AzulNegro, fontWeight = FontWeight.Bold, fontSize = 15.sp)) { append("Inicia Sesión") }
            }
            ClickableText(
                text = loginText,
                onClick = { offset -> onBack() },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}