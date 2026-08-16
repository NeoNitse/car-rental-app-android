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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario
import com.sv.elveloz.ui.shared.ElVelozDateTimePickerDialog
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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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
                        Text(text = if (rol == RolUsuario.RECEPCIONISTA) "Recepción" else "Cliente", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "El Veloz", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión") }
                }
            }

            // 2. Buscador
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
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
            }

            // 3. ALERTAS EN VIVO (Acción Requerida)
            if (rol == RolUsuario.RECEPCIONISTA && pendingRentals.isNotEmpty()) {
                item {
                    Text(
                        text = "Acción Requerida (${pendingRentals.size})",
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F)
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingRentals, key = { it.id }) { rental ->
                            val car = uiState.allCars.find { it.id == rental.carId }
                            ActionRequiredCard(
                                rental = rental,
                                car = car,
                                onApprove = {
                                    viewModel.onApproveRental(rental)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") }
                                },
                                onReject = {
                                    viewModel.onRejectRental(rental, rental.carId)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") }
                                }
                            )
                        }
                    }
                }
            }

            // 4. Panel de Estadísticas
            if (rol == RolUsuario.RECEPCIONISTA) {
                item {
                    QuickStatsPanel(cars = uiState.allCars, onFilterSelected = { viewModel.onFilterChange(it) })
                }
            }

            // 5. Filtros
            if (rol == RolUsuario.RECEPCIONISTA) {
                item {
                    Text("Filtro de Flota", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                    FilterChipsRow(selectedFilter = uiState.filterStatus ?: "", onFilterSelected = { viewModel.onFilterChange(newFilter = it) })
                }
            }

            // 6. Inventario Título
            item {
                Text(
                    text = if (rol == RolUsuario.RECEPCIONISTA) "Catálogo de Vehículos" else "Vehículos Disponibles",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black
                )
            }

            // 7. Lista de Carros en Cuadrícula (2 Columnas estilo la imagen de referencia)
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
            } else if (uiState.cars.isEmpty()) {
                item {
                    EmptySearchState(searchQuery = uiState.searchQuery, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else {
                items(uiState.cars.chunked(2), key = { rowCars -> rowCars.first().id }) { rowCars ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCars.forEach { car ->
                            Box(modifier = Modifier.weight(1f)) {
                                CarGridCard(
                                    car = car,
                                    rol = rol,
                                    onClick = { viewModel.onCarClicked(car) }
                                )
                            }
                        }
                        if (rowCars.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
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
                    val mensaje = if (rol == RolUsuario.RECEPCIONISTA) "Reserva de mostrador registrada" else "Solicitud enviada al mostrador"
                    snackbarHostState.showSnackbar(mensaje)
                }
            },
            calculateCost = { pickupMs, returnMs -> viewModel.calculateCost(pickupMs, returnMs, car.pricePerDay) }
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
                viewModel.onDismissDetail()
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") }
            },
            onRejectRental = { rental, carId ->
                viewModel.onRejectRental(rental, carId)
                viewModel.onDismissDetail()
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") }
            }
        )
    }
}


@Composable
private fun CarGridCard(
    car: CarEntity,
    rol: RolUsuario,
    onClick: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedPrice = format.format(car.pricePerDay)
    val isAvailable = car.status == CarStatus.DISPONIBLE

    val imageResId = when (car.imageResName.trim()) {
        "toyota_corolla" -> com.sv.elveloz.R.drawable.toyota_corolla
        "nissan_sentra" -> com.sv.elveloz.R.drawable.nissan_sentra
        "honda_cr" -> com.sv.elveloz.R.drawable.honda_cr
        "hyundai_tucson" -> com.sv.elveloz.R.drawable.hyundai_tucson
        else -> 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Contenedor Visual de la Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    // Si encontró la imagen mapeada, la muestra a pantalla completa
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = imageResId),
                        contentDescription = "${car.brand} ${car.model}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    // Si no, muestra el ícono por defecto
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = if (isAvailable) Color.DarkGray else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ya regresamos el texto a la normalidad
            Text(
                text = "${car.brand} ${car.model}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (car.status) {
                    CarStatus.DISPONIBLE -> "Disponible"
                    CarStatus.PEND_APROBACION -> "Pend. Aprobación"
                    CarStatus.EN_PROCESO -> "En Proceso"
                    CarStatus.EN_USO -> "En Uso"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = getStatusColor(car.status)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formattedPrice,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = "/ Día",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAvailable) Color(0xFF1F2937) else Color(0xFFE5E7EB)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAvailable) "Reservar" else "Gestionar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRequiredCard(rental: RentalEntity, car: CarEntity?, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.width(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFF57C00))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Solicitud", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Vehículo: ${car?.brand ?: ""} ${car?.model ?: ""}", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "Cliente: ${rental.customerName}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Costo: $${rental.totalCost}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onReject) { Text("Rechazar", color = Color.Red) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("Aprobar") }
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
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color.LightGray)
        Text(text = "No se encontraron vehículos", style = MaterialTheme.typography.titleLarge)
        if (searchQuery.isNotEmpty()) Text(text = "No hay coincidencias para \"$searchQuery\"", color = Color.Gray)
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Seleccionar"
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date(millis))
}

@Composable
private fun RentalDetailDialog(detail: RentalDetailUiState, onDismiss: () -> Unit, onMarkAsInUse: () -> Unit, onCompleteRental: () -> Unit, onCancelRental: () -> Unit, onApproveRental: (RentalEntity) -> Unit, onRejectRental: (RentalEntity, Int) -> Unit) {
    val car = detail.car
    val rental = detail.rental
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
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
                    Text("¡Aviso! Datos de reserva no encontrados (Historial borrado).", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Puedes forzar la liberación del vehículo usando los botones de abajo.", color = Color.DarkGray, fontSize = 13.sp)
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
                TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.Gray) }
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
    var showPicker by remember { mutableStateOf(false) }

    val calculatedCost = remember(pickupDateMs, returnDateMs) {
        if (pickupDateMs != null && returnDateMs != null) calculateCost(pickupDateMs!!, returnDateMs!!) else null
    }

    val isFormValid = customerName.isNotBlank() && pickupDateMs != null && returnDateMs != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
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
                calculatedCost?.onFailure { exception ->
                    Text(exception.message ?: "Rango de fechas inválido", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
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
            onConfirm = { startDate, endDate, startTime, endTime -> pickupDateMs = startDate; returnDateMs = endDate; showPicker = false }
        )
    }
}