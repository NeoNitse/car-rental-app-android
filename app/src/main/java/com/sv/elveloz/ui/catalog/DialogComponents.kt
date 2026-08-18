package com.sv.elveloz.ui.catalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.ui.shared.ElVelozDateTimePickerDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long?): String {
    if (millis == null) return "Seleccionar"
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date(millis))
}

@Composable
fun CarDetailsDialog(car: CarEntity, onDismiss: () -> Unit, onReserve: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedPrice = format.format(car.pricePerDay)
    val isAvailable = car.status == CarStatus.DISPONIBLE

    val imageResId = when (car.imageResName.trim()) {
        "toyota_corolla" -> com.sv.elveloz.R.drawable.toyota_corolla
        "nissan_sentra" -> com.sv.elveloz.R.drawable.nissan_sentra
        "honda_cr" -> com.sv.elveloz.R.drawable.honda_cr
        "hyundai_tucson" -> com.sv.elveloz.R.drawable.hyundai_tucson
        "kia_picanto" -> com.sv.elveloz.R.drawable.kia_picanto
        "mazda_cx" -> com.sv.elveloz.R.drawable.mazda_cx
        "chevrolet_aveo" -> com.sv.elveloz.R.drawable.chevrolet_aveo
        "ford_ranger" -> com.sv.elveloz.R.drawable.ford_ranger
        else -> 0
    }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White, shape = RoundedCornerShape(24.dp), title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId), contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(16.dp), contentScale = ContentScale.Fit,
                            alpha = if (isAvailable) 1f else 0.4f
                        )
                    }
                    if (!isAvailable) {
                        Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(6.dp)) {
                            Text("Actualmente en uso", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "${car.brand} ${car.model}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CarSpecItem(icon = "⚙️", label = "Transmisión", value = "Automática", modifier = Modifier.weight(1f))
                        CarSpecItem(icon = "⛽", label = "Combustible", value = "Gasolina", modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CarSpecItem(icon = "👤", label = "Capacidad", value = "5 Pasajeros", modifier = Modifier.weight(1f))
                        CarSpecItem(icon = "❄️", label = "Climatización", value = "Aire Acond.", modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val kilometrajeRealista = "${(car.id * 14) + 22},500 km"
                        CarSpecItem(icon = "🛣️", label = "Kilometraje", value = kilometrajeRealista, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "$formattedPrice / Día", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isAvailable) Color(0xFF4CAF50) else Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isAvailable) onReserve() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isAvailable) Color.Black else Color.LightGray),
                enabled = isAvailable
            ) {
                Text(if (isAvailable) "Ir a Reservar" else "Vehículo no disponible", color = if (isAvailable) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar", color = Color.Gray) }
        }
    )
}

@Composable
fun RentalDetailDialog(detail: RentalDetailUiState, onDismiss: () -> Unit, onMarkAsInUse: () -> Unit, onCompleteRental: () -> Unit, onCancelRental: () -> Unit, onApproveRental: (RentalEntity) -> Unit, onRejectRental: (RentalEntity, Int) -> Unit) {
    val car = detail.car
    val rental = detail.rental
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = { Text("${car.brand} ${car.model}", color = Color.Black, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                RentalStepper(status = car.status)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                if (rental != null) {
                    Text("Cliente: ${rental.customerName}", color = Color.Black)
                    Text("Recogida: ${formatDate(rental.pickupDateMs)}", color = Color.DarkGray)
                    Text("Entrega: ${formatDate(rental.returnDateMs)}", color = Color.DarkGray)
                    Text("Costo: $${rental.totalCost}", fontWeight = FontWeight.ExtraBold, color = Color.Black)

                    // ESTADO MOVIDO ARRIBA
                    val estadoVisual = when (car.status) {
                        CarStatus.PEND_APROBACION -> "SOLICITADA"
                        CarStatus.EN_PROCESO -> "APROBADA"
                        CarStatus.EN_USO -> "RETIRADO / EN USO"
                        else -> rental.estado
                    }
                    Text("Estado: $estadoVisual", color = Color.Black)

                    // FEEDBACK MOVIDO ABAJO Y TEXTO CAMBIADO
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Documentos Verificados", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    Text("¡Aviso! Datos de reserva no encontrados.", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Puedes forzar la liberación del vehículo usando los botones de abajo.", color = Color.DarkGray, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            when (car.status) {
                CarStatus.PEND_APROBACION -> { Button(onClick = { if (rental != null) onApproveRental(rental) }) { Text("Aprobar Reserva") } }
                CarStatus.EN_PROCESO -> Button(onClick = onMarkAsInUse) { Text("Marcar como Retirado") }
                CarStatus.EN_USO -> Button(onClick = onCompleteRental) { Text("Marcar como Devuelto") }
                else -> {}
            }
        },
        dismissButton = {
            Row {
                if (car.status == CarStatus.PEND_APROBACION) {
                    TextButton(onClick = { if (rental != null) onRejectRental(rental, car.id) }) { Text("Rechazar", color = Color.Red) }
                } else if (car.status == CarStatus.EN_PROCESO) {
                    TextButton(onClick = onCancelRental) { Text("Cancelar", color = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.Gray) }
            }
        }
    )
}

@Composable
fun RentalStepper(status: CarStatus) {
    val steps = listOf("Solicitado", "Aprobado", "En Uso", "Devuelto")
    val currentIndex = when (status) { CarStatus.PEND_APROBACION -> 0; CarStatus.EN_PROCESO -> 1; CarStatus.EN_USO -> 2; else -> 3 }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentIndex
            val color = if (isActive) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(28.dp).background(color.copy(alpha = if (isActive) 1f else 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                    if (isActive) Box(modifier = Modifier.size(10.dp).background(Color.White, CircleShape))
                }
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
            }
            if (index < steps.size - 1) Spacer(modifier = Modifier.height(3.dp).weight(1f).padding(horizontal = 4.dp).background(if (index < currentIndex) Color(0xFF4CAF50) else Color(0xFFBDBDBD)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalDialog(car: CarEntity, onDismiss: () -> Unit, onConfirm: (String, Long, Long) -> Unit, calculateCost: (Long, Long) -> Result<Double>) {

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val registeredEmail = auth.currentUser?.email ?: ""
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf(registeredEmail) }
    var contactPhone by remember { mutableStateOf("") }

    var pickupDateMs by remember { mutableStateOf<Long?>(null) }
    var returnDateMs by remember { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    // Estados para la IA de validación de documentos
    var isVerifying by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf("Idle") } // Idle, Exito, Error

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            isVerifying = true
            verificationStatus = "Verificando..."

            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.uppercase(Locale.getDefault())

                        // 1. Verificación primaria (Licencia o palabras clave del país)
                        var isValid = text.contains("LICENCIA") || text.contains("SALVADOR")

                        // 2. Verificación profunda (Matemática del DUI salvadoreño)
                        if (!isValid) {
                            val regex = Regex("\\b\\d{8}-\\d\\b")
                            val match = regex.find(text)
                            if (match != null) {
                                val dui = match.value.replace("-", "")
                                var sum = 0
                                for (i in 0..7) {
                                    sum += dui[i].digitToInt() * (9 - i)
                                }
                                val remainder = sum % 10
                                val v = if (10 - remainder == 10) 0 else 10 - remainder
                                if (v == dui[8].digitToInt()) {
                                    isValid = true
                                }
                            }
                        }

                        verificationStatus = if (isValid) "Exito" else "Error"
                        isVerifying = false
                    }
                    .addOnFailureListener {
                        verificationStatus = "Error"
                        isVerifying = false
                    }
            } catch (e: Exception) {
                verificationStatus = "Error"
                isVerifying = false
            }
        }
    }

    val isEmailMatching = contactEmail.trim().equals(registeredEmail, ignoreCase = true)
    val isPhoneValid = contactPhone.isEmpty() || contactPhone.matches(Regex("^\\d{4}-\\d{4}$"))
    val calculatedCost = remember(pickupDateMs, returnDateMs) { if (pickupDateMs != null && returnDateMs != null) calculateCost(pickupDateMs!!, returnDateMs!!) else null }

    // El formulario solo es válido si la IA aprobó el documento
    val isFormValid = customerName.isNotBlank() && contactEmail.isNotBlank() && isEmailMatching && contactPhone.isNotBlank() && isPhoneValid && pickupDateMs != null && returnDateMs != null && verificationStatus == "Exito"

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White, shape = RoundedCornerShape(24.dp),
        title = { Text("Reserva: ${car.brand} ${car.model}", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Datos del Conductor", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                OutlinedTextField(
                    value = customerName, onValueChange = { customerName = it }, placeholder = { Text("Nombre completo", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB), focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                )

                OutlinedTextField(
                    value = contactEmail, onValueChange = { contactEmail = it }, placeholder = { Text("Correo electrónico", color = Color.Gray) },
                    isError = !isEmailMatching,
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB), focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black, errorIndicatorColor = Color.Red, errorCursorColor = Color.Red)
                )

                if (!isEmailMatching) {
                    Text("Debes usar tu correo registrado ($registeredEmail)", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedTextField(
                    value = contactPhone, onValueChange = { contactPhone = it }, placeholder = { Text("Teléfono (0000-0000)", color = Color.Gray) },
                    isError = !isPhoneValid,
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF9FAFB), unfocusedContainerColor = Color(0xFFF9FAFB), focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black, errorIndicatorColor = Color.Red, errorCursorColor = Color.Red)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Documento de Identidad (DUI / Licencia)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (verificationStatus == "Error") Color.Red else Color(0xFFE5E7EB)),
                    color = Color(0xFFF9FAFB)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analizando documento con IA...", color = Color.DarkGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        } else if (verificationStatus == "Exito") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verificación completa", color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else if (verificationStatus == "Error") {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verificación fallida. Sube imagen válida.", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.DarkGray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subir documento desde galería", color = Color.DarkGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Detalles del Viaje", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showPicker = true }, shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pickupDateMs == null) Color.LightGray else Color.Black), color = Color.White
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        if (pickupDateMs != null && returnDateMs != null) {
                            Text("📅  ${formatDate(pickupDateMs)}   →   ${formatDate(returnDateMs)}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text("📅  Seleccionar fechas", color = Color.DarkGray, fontSize = 13.sp)
                        }
                    }
                }

                calculatedCost?.onSuccess { cost ->
                    val format = NumberFormat.getCurrencyInstance(Locale.US)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), border = BorderStroke(1.dp, Color(0xFFE5E5E5))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen de Tarifa", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Costo Total", fontWeight = FontWeight.Medium, color = Color.Black)
                                Text(format.format(cost), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
                calculatedCost?.onFailure { exception -> Text(exception.message ?: "Rango de fechas inválido", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        val infoCompleta = "$customerName | Correo: $contactEmail | Tel: $contactPhone"
                        onConfirm(infoCompleta, pickupDateMs!!, returnDateMs!!)
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black, disabledContainerColor = Color(0xFFE0E0E0))
            ) { Text("Confirmar Reserva", color = if (isFormValid) Color.White else Color.Gray) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } }
    )

    if (showPicker) {
        ElVelozDateTimePickerDialog(
            onDismiss = { showPicker = false },
            onConfirm = { startDate, endDate, _, _ -> pickupDateMs = startDate; returnDateMs = endDate; showPicker = false }
        )
    }
}

@Composable
fun CarSpecItem(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFE5E7EB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}