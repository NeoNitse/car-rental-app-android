package com.sv.elveloz.ui.catalog

import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.domain.model.CarStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val rentalDetail by viewModel.rentalDetail.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            QuickStatsPanel(cars = uiState.allCars)

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por marca o modelo...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            FilterChipsRow(
                selectedFilter = uiState.filterStatus,
                onFilterSelected = { viewModel.onFilterChange(it) }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.cars, key = { it.id }) { car ->
                        CarCard(
                            car = car,
                            onClick = { viewModel.onCarClicked(car) }
                        )
                    }
                }
            }
        }
    }

    uiState.selectedCar?.let { car ->
        RentalDialog(
            car = car,
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { customerName, pickupMs, returnMs ->
                viewModel.onConfirmRental(customerName, pickupMs, returnMs)
            },
            calculateCost = { pickupMs, returnMs ->
                viewModel.calculateCost(pickupMs, returnMs, car.pricePerDay)
            }
        )
    }

    rentalDetail?.let { detail ->
        RentalDetailDialog(
            detail = detail,
            onDismiss = { viewModel.onDismissDetail() },
            onMarkAsInUse = { viewModel.onMarkAsInUse() },
            onCompleteRental = { viewModel.onCompleteRental() },
            onCancelRental = { viewModel.onCancelRental() }
        )
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Seleccionar"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

@Composable
private fun RentalDetailDialog(
    detail: RentalDetailUiState,
    onDismiss: () -> Unit,
    onMarkAsInUse: () -> Unit,
    onCompleteRental: () -> Unit,
    onCancelRental: () -> Unit
) {
    val car = detail.car
    val rental = detail.rental

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${car.brand} ${car.model}") },
        text = {
            Column {

                RentalStepper(status = car.status)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (rental != null) {
                    Text("Cliente: ${rental.customerName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Recogida: ${formatDate(rental.pickupDateMs)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Entrega: ${formatDate(rental.returnDateMs)}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Costo total: $${rental.totalCost}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text("No se encontró información de la reserva.")
                }
            }
        },
        confirmButton = {
            when (car.status) {
                CarStatus.EN_PROCESO -> {
                    Button(onClick = onMarkAsInUse) {
                        Text("Marcar como Retirado")
                    }
                }
                CarStatus.EN_USO -> {
                    Button(onClick = onCompleteRental) {
                        Text("Marcar como Devuelto")
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            Row {
                if (car.status == CarStatus.EN_PROCESO) {
                    TextButton(onClick = onCancelRental) {
                        Text("Cancelar Reserva", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}

@Composable
private fun RentalStepper(status: CarStatus) {
    val steps = listOf("Reservado", "En Uso", "Devuelto")
    val currentIndex = when (status) {
        CarStatus.EN_PROCESO -> 0
        CarStatus.EN_USO -> 1
        CarStatus.DISPONIBLE -> 2
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentIndex
            val color = if (isActive) Color(0xFF4CAF50) else Color(0xFFBDBDBD)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = if (isActive) 1f else 0.3f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Box(modifier = Modifier.size(10.dp).background(Color.White, shape = CircleShape))
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (index < currentIndex) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun QuickStatsPanel(cars: List<CarEntity>) {
    val disponibles = cars.count { it.status == CarStatus.DISPONIBLE }
    val enProceso = cars.count { it.status == CarStatus.EN_PROCESO }
    val enUso = cars.count { it.status == CarStatus.EN_USO }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(label = "Disponibles", value = disponibles, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
        StatItem(label = "En Proceso", value = enProceso, color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
        StatItem(label = "En Uso", value = enUso, color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium, color = color)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("TODOS", "DISPONIBLE", "EN_PROCESO", "EN_USO")
    val labels = mapOf(
        "TODOS" to "Todos (8)",
        "DISPONIBLE" to "Disponibles",
        "EN_PROCESO" to "En Proceso",
        "EN_USO" to "En Uso"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(labels[filter] ?: filter) }
            )
        }
    }
}

@Composable
private fun CarCard(car: CarEntity, onClick: () -> Unit) {
    val (accentColor, containerColor) = when (car.status) {
        CarStatus.DISPONIBLE -> Color(0xFF4CAF50) to Color(0xFFE8F5E9)
        CarStatus.EN_PROCESO -> Color(0xFFFF9800) to Color(0xFFFFF3E0)
        CarStatus.EN_USO -> Color(0xFF2196F3) to Color(0xFFE3F2FD)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(containerColor, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(text = "${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$${car.pricePerDay} / día",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            StatusBadge(status = car.status)
        }
    }
}

@Composable
private fun StatusBadge(status: CarStatus) {
    val (label, color) = when (status) {
        CarStatus.DISPONIBLE -> "Disponible" to Color(0xFF4CAF50)
        CarStatus.EN_PROCESO -> "En Proceso" to Color(0xFFFF9800)
        CarStatus.EN_USO -> "En Uso" to Color(0xFF2196F3)
    }
    Text(
        text = label,
        color = Color.White,
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalDialog(
    car: CarEntity,
    onDismiss: () -> Unit,
    onConfirm: (customerName: String, pickupMs: Long, returnMs: Long) -> Unit,
    calculateCost: (pickupMs: Long, returnMs: Long) -> Result<Double>
) {
    var customerName by remember { mutableStateOf("") }
    var pickupDateMs by remember { mutableStateOf<Long?>(null) }
    var returnDateMs by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPickupPicker by remember { mutableStateOf(false) }
    var showReturnPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar ${car.brand} ${car.model}") },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nombre del cliente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text("Fecha de recogida", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = formatDate(pickupDateMs),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                        showPickupPicker = true
                    },
                    enabled = false
                )

                Text("Fecha de entrega", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = formatDate(returnDateMs),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
                        showReturnPicker = true
                    },
                    enabled = false
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val pickup = pickupDateMs
                val returnD = returnDateMs
                if (customerName.isBlank()) {
                    errorMessage = "Ingresá el nombre del cliente"
                    return@Button
                }
                if (pickup == null || returnD == null) {
                    errorMessage = "Seleccioná ambas fechas"
                    return@Button
                }
                val result = calculateCost(pickup, returnD)
                if (result.isFailure) {
                    errorMessage = result.exceptionOrNull()?.message ?: "Fechas inválidas"
                    return@Button
                }
                onConfirm(customerName, pickup, returnD)
            }) {
                Text("Confirmar Reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showPickupPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPickupPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickupDateMs = state.selectedDateMillis
                    showPickupPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPickupPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showReturnPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showReturnPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    returnDateMs = state.selectedDateMillis
                    showReturnPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReturnPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}