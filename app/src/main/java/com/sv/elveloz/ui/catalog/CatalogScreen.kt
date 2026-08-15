// Ruta: com.sv.elveloz/ui/catalog/CatalogScreen.kt
package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val rentalDetail by viewModel.rentalDetail.collectAsState()
    val pendingRentals by viewModel.pendingRentals.collectAsState()
    val rol = viewModel.rolActual

    var showNotifications by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (rol == RolUsuario.RECEPCIONISTA) "Recepción" else "Cliente",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "El Veloz",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                if (rol == RolUsuario.RECEPCIONISTA) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { showNotifications = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                        if (pendingRentals.isNotEmpty()) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Text(pendingRentals.size.toString())
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            }

            // 2. Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text("Buscar por marca o modelo...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                )
            )

            // 3. Panel de Estadísticas
            if (rol == RolUsuario.RECEPCIONISTA) {
                QuickStatsPanel(
                    cars = uiState.allCars,
                    onFilterSelected = { viewModel.onFilterChange(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Filtros
            if (rol == RolUsuario.RECEPCIONISTA) {
                Text(
                    text = "Filtro de Flota",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FilterChipsRow(
                    selectedFilter = uiState.filterStatus ?: "",
                    onFilterSelected = { viewModel.onFilterChange(newFilter = it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. Inventario
            Text(
                text = if (rol == RolUsuario.RECEPCIONISTA) "Estado del Inventario" else "Vehículos Disponibles",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (uiState.cars.isEmpty()) {
                EmptySearchState(
                    searchQuery = uiState.searchQuery,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.cars, key = { it.id }) { car ->
                        ReceptionistVehicleCard(
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
                coroutineScope.launch {
                    val mensaje = if (rol == RolUsuario.RECEPCIONISTA) "Reserva registrada" else "Solicitud enviada"
                    snackbarHostState.showSnackbar(mensaje)
                }
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
            onMarkAsInUse = {
                viewModel.onMarkAsInUse()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo marcado como retirado") }
            },
            onCompleteRental = {
                viewModel.onCompleteRental()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo devuelto con éxito") }
            },
            onCancelRental = {
                viewModel.onCancelRental()
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva cancelada correctamente") }
            },
            onApproveRental = { rental ->
                viewModel.onApproveRental(rental)
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") }
            },
            onRejectRental = { rental, carId ->
                viewModel.onRejectRental(rental, carId)
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") }
            }
        )
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Solicitudes Pendientes") },
            text = {
                if (pendingRentals.isEmpty()) {
                    Text("No hay solicitudes nuevas.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(pendingRentals) { rental ->
                            val car = uiState.allCars.find { it.id == rental.carId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "${car?.brand ?: "Vehículo"} ${car?.model ?: ""}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "Cliente: ${rental.customerName}")
                                    Text(text = "Monto: $${rental.totalCost}")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { viewModel.onRejectRental(rental, rental.carId) }) {
                                            Text("Rechazar", color = Color.Red)
                                        }
                                        Button(onClick = { viewModel.onApproveRental(rental) }) {
                                            Text("Aprobar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun ReceptionistVehicleCard(
    car: CarEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedPrice = format.format(car.pricePerDay)
    val isAvailable = car.status == CarStatus.DISPONIBLE
    val alphaValue = if (isAvailable) 1f else 0.5f

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(30.dp), tint = Color.Gray.copy(alpha = alphaValue))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${car.brand} ${car.model}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = alphaValue))
                Text(text = "$formattedPrice / día", fontSize = 13.sp, color = Color.DarkGray.copy(alpha = alphaValue))
            }
            Surface(shape = RoundedCornerShape(8.dp), color = getStatusColor(car.status).copy(alpha = 0.15f)) {
                Text(
                    text = when (car.status) {
                        CarStatus.DISPONIBLE -> "Disponible"
                        CarStatus.PEND_APROBACION -> "Pend. Aprobación"
                        CarStatus.EN_PROCESO -> "En Proceso"
                        CarStatus.EN_USO -> "En Uso"
                    },
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = getStatusColor(car.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun getStatusColor(status: CarStatus) = when (status) {
    CarStatus.DISPONIBLE -> Color(0xFF4CAF50)
    CarStatus.PEND_APROBACION -> Color(0xFF9C27B0)
    CarStatus.EN_PROCESO -> Color(0xFFFF9800)
    CarStatus.EN_USO -> Color(0xFF2196F3)
}

@Composable
private fun QuickStatsPanel(cars: List<CarEntity>, onFilterSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatItem(label = "Disp.", value = cars.count { it.status == CarStatus.DISPONIBLE }, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f).clickable { onFilterSelected("DISPONIBLE") })
        StatItem(label = "Pend.", value = cars.count { it.status == CarStatus.PEND_APROBACION }, color = Color(0xFF9C27B0), modifier = Modifier.weight(1f).clickable { onFilterSelected("PEND_APROBACION") })
        StatItem(label = "Proc.", value = cars.count { it.status == CarStatus.EN_PROCESO }, color = Color(0xFFFF9800), modifier = Modifier.weight(1f).clickable { onFilterSelected("EN_PROCESO") })
        StatItem(label = "Uso", value = cars.count { it.status == CarStatus.EN_USO }, color = Color(0xFF2196F3), modifier = Modifier.weight(1f).clickable { onFilterSelected("EN_USO") })
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, color.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("TODOS", "DISPONIBLE", "PEND_APROBACION", "EN_PROCESO", "EN_USO")
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { 
                    Text(when(filter) {
                        "PEND_APROBACION" -> "Pendientes"
                        "EN_PROCESO" -> "En Proceso"
                        "EN_USO" -> "En Uso"
                        else -> filter.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }) 
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF374151), selectedLabelColor = Color.White)
            )
        }
    }
}

@Composable
fun EmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color.LightGray)
        Text(text = "No se encontraron vehículos", style = MaterialTheme.typography.titleLarge)
        if (searchQuery.isNotEmpty()) Text(text = "No hay coincidencias para \"$searchQuery\"", color = Color.Gray)
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Seleccionar"
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }.format(Date(millis))
}

@Composable
private fun RentalDetailDialog(detail: RentalDetailUiState, onDismiss: () -> Unit, onMarkAsInUse: () -> Unit, onCompleteRental: () -> Unit, onCancelRental: () -> Unit, onApproveRental: (RentalEntity) -> Unit, onRejectRental: (RentalEntity, Int) -> Unit) {
    val car = detail.car
    val rental = detail.rental
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${car.brand} ${car.model}") },
        text = {
            Column {
                RentalStepper(status = car.status)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                if (rental != null) {
                    Text("Cliente: ${rental.customerName}")
                    Text("Recogida: ${formatDate(rental.pickupDateMs)}")
                    Text("Entrega: ${formatDate(rental.returnDateMs)}")
                    Text("Costo: $${rental.totalCost}", fontWeight = FontWeight.Bold)
                    Text("Estado: ${rental.estado}")
                }
            }
        },
        confirmButton = {
            when (car.status) {
                CarStatus.PEND_APROBACION -> {
                    Button(onClick = { if (rental != null) onApproveRental(rental) }) { Text("Aprobar Reserva") }
                }
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
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    )
}

@Composable
private fun RentalStepper(status: CarStatus) {
    val steps = listOf("Solicitado", "Aprobado", "En Uso", "Devuelto")
    val currentIndex = when (status) { 
        CarStatus.PEND_APROBACION -> 0
        CarStatus.EN_PROCESO -> 1
        CarStatus.EN_USO -> 2
        else -> 3 
    }
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
private fun RentalDialog(car: CarEntity, onDismiss: () -> Unit, onConfirm: (String, Long, Long) -> Unit, calculateCost: (Long, Long) -> Result<Double>) {
    var customerName by remember { mutableStateOf("") }
    var pickupDateMs by remember { mutableStateOf<Long?>(null) }
    var returnDateMs by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPickupPicker by remember { mutableStateOf(false) }
    var showReturnPicker by remember { mutableStateOf(false) }

    val calculatedCost = remember(pickupDateMs, returnDateMs) {
        if (pickupDateMs != null && returnDateMs != null) {
            calculateCost(pickupDateMs!!, returnDateMs!!)
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar ${car.brand} ${car.model}") },
        text = {
            Column {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { showPickupPicker = true }) { Text("Recogida: ${formatDate(pickupDateMs)}") }
                TextButton(onClick = { showReturnPicker = true }) { Text("Entrega: ${formatDate(returnDateMs)}") }
                
                calculatedCost?.onSuccess { cost ->
                    val format = NumberFormat.getCurrencyInstance(Locale.US)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Resumen de Alquiler", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "Costo Total: ${format.format(cost)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                errorMessage?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (customerName.isBlank() || pickupDateMs == null || returnDateMs == null) { errorMessage = "Completa todo"; return@Button }
                val result = calculateCost(pickupDateMs!!, returnDateMs!!)
                if (result.isFailure) { errorMessage = result.exceptionOrNull()?.message; return@Button }
                onConfirm(customerName, pickupDateMs!!, returnDateMs!!)
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showPickupPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showPickupPicker = false }, confirmButton = { TextButton(onClick = { pickupDateMs = state.selectedDateMillis; showPickupPicker = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
    if (showReturnPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showReturnPicker = false }, confirmButton = { TextButton(onClick = { returnDateMs = state.selectedDateMillis; showReturnPicker = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
}
