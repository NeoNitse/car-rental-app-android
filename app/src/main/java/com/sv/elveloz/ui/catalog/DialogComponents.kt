package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)).padding(12.dp)
                ) {
                    Text(text = "⚙️ Auto", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    Text(text = "⛽ Gasolina", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    Text(text = "👤 5", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
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
                    Text("Estado: ${rental.estado}", color = Color.Black)
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
    var customerName by remember { mutableStateOf("") }
    var pickupDateMs by remember { mutableStateOf<Long?>(null) }
    var returnDateMs by remember { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    val calculatedCost = remember(pickupDateMs, returnDateMs) { if (pickupDateMs != null && returnDateMs != null) calculateCost(pickupDateMs!!, returnDateMs!!) else null }
    val isFormValid = customerName.isNotBlank() && pickupDateMs != null && returnDateMs != null

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White, shape = RoundedCornerShape(24.dp),
        title = { Text("Reservar ${car.brand} ${car.model}", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName, onValueChange = { customerName = it }, label = { Text("Nombre del cliente", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color.LightGray, cursorColor = Color.Black)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showPicker = true }, shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pickupDateMs == null) Color.LightGray else Color.Black), color = Color(0xFFFAFAFA)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        if (pickupDateMs != null && returnDateMs != null) {
                            Text("${formatDate(pickupDateMs)}   →   ${formatDate(returnDateMs)}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text("Seleccionar recogida y entrega", color = Color.DarkGray, fontSize = 14.sp)
                        }
                    }
                }
                calculatedCost?.onSuccess { cost ->
                    val format = NumberFormat.getCurrencyInstance(Locale.US)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), border = BorderStroke(1.dp, Color(0xFFE5E5E5))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen de Alquiler", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Costo Total", fontWeight = FontWeight.Medium, color = Color.Black)
                                Text(format.format(cost), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
                calculatedCost?.onFailure { exception -> Text(exception.message ?: "Rango de fechas inválido", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isFormValid) onConfirm(customerName, pickupDateMs!!, returnDateMs!!) }, enabled = isFormValid,
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black, disabledContainerColor = Color(0xFFE0E0E0))
            ) { Text("Confirmar", color = if (isFormValid) Color.White else Color.Gray) }
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