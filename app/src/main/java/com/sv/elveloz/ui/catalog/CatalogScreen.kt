package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
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
    var showAddCarDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9FAFB),
        floatingActionButton = {
            if (rol == RolUsuario.RECEPCIONISTA) {
                FloatingActionButton(
                    onClick = { showAddCarDialog = true },
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
                }
            }
        }
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
                        text = if (rol == RolUsuario.RECEPCIONISTA) "Panel de Gestión" else "Hola, Cliente",
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

                Spacer(modifier = Modifier.weight(1f))
                
                if (rol == RolUsuario.RECEPCIONISTA) {
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
                }
                
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                }
            }

            // 2. Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text("Buscar marca o modelo...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // 3. Quick Stats (Solo Recepcionista)
            if (rol == RolUsuario.RECEPCIONISTA) {
                QuickStatsPanel(
                    cars = uiState.allCars,
                    onFilterSelected = { viewModel.onFilterChange(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Filtros
            if (rol == RolUsuario.RECEPCIONISTA) {
                FilterChipsRow(
                    selectedFilter = uiState.filterStatus ?: "TODOS",
                    onFilterSelected = { viewModel.onFilterChange(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. Grid de Vehículos
            Text(
                text = if (rol == RolUsuario.RECEPCIONISTA) "Flota Completa" else "Vehículos Disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (uiState.cars.isEmpty()) {
                EmptySearchState(searchQuery = uiState.searchQuery, modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.cars, key = { it.id }) { car ->
                        VehicleGridCard(
                            car = car,
                            onClick = { viewModel.onCarClicked(car) }
                        )
                    }
                }
            }
        }
    }

    // Modals y Dialogs
    if (showAddCarDialog) {
        AddCarDialog(
            onDismiss = { showAddCarDialog = false },
            onConfirm = { marca, modelo, precio, url, ubicacion ->
                viewModel.onAgregarVehiculo(marca, modelo, precio, url, ubicacion)
                showAddCarDialog = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo agregado exitosamente") }
            }
        )
    }

    uiState.selectedCar?.let { car ->
        RentalDialog(
            car = car,
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { customerName, pickupMs, returnMs ->
                viewModel.onConfirmRental(customerName, pickupMs, returnMs)
                coroutineScope.launch {
                    val msg = if (rol == RolUsuario.RECEPCIONISTA) "Renta registrada" else "Solicitud enviada"
                    snackbarHostState.showSnackbar(msg)
                }
            },
            calculateCost = { p, r -> viewModel.calculateCost(p, r, car.pricePerDay) }
        )
    }

    rentalDetail?.let { detail ->
        RentalDetailDialog(
            detail = detail,
            onDismiss = { viewModel.onDismissDetail() },
            onMarkAsInUse = {
                viewModel.onMarkAsInUse()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo en uso") }
            },
            onCompleteRental = {
                viewModel.onCompleteRental()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo devuelto") }
            },
            onCancelRental = {
                viewModel.onCancelRental()
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva cancelada") }
            },
            onApproveRental = { rental ->
                viewModel.onApproveRental(rental)
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") }
            },
            onRejectRental = { rental, id ->
                viewModel.onRejectRental(rental, id)
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") }
            },
            onMoverAMantenimiento = {
                viewModel.onMoverAMantenimiento()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo en mantenimiento") }
            },
            rol = rol
        )
    }

    if (showNotifications) {
        PendingRentalsDialog(
            pendingRentals = pendingRentals,
            allCars = uiState.allCars,
            onDismiss = { showNotifications = false },
            onApprove = { viewModel.onApproveRental(it) },
            onReject = { r, id -> viewModel.onRejectRental(r, id) }
        )
    }
}

@Composable
fun VehicleGridCard(car: CarEntity, onClick: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF3F4F6))) {
                SubcomposeAsyncImage(
                    model = car.imageUrl ?: car.imageResName, // Prioridad a URL
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = { CircularProgressIndicator(modifier = Modifier.scale(0.5f)) },
                    error = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray) }
                )
                
                /* Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(18.dp),
                        tint = Color.Gray
                    )
                } */
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "${car.brand} ${car.model}", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                
               /* Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = car.rating.toString(), fontSize = 12.sp, color = Color.Gray)
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                } */

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(text = car.location, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${format.format(car.pricePerDay)}/Day",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color.Black, // Color explícito para evitar tintes del sistema
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF008A30),
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable { onClick() }
                    ) {
                        Text(
                            text = "Reservar",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsPanel(cars: List<CarEntity>, onFilterSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatItem(label = "Disp.", value = cars.count { it.status == CarStatus.DISPONIBLE }, color = Color(0xFF10B981), modifier = Modifier.weight(1f).clickable { onFilterSelected("DISPONIBLE") })
        StatItem(label = "Pend.", value = cars.count { it.status == CarStatus.PEND_APROBACION }, color = Color(0xFF8B5CF6), modifier = Modifier.weight(1f).clickable { onFilterSelected("PEND_APROBACION") })
        StatItem(label = "Mant.", value = cars.count { it.status == CarStatus.MANTENIMIENTO }, color = Color(0xFFEF4444), modifier = Modifier.weight(1f).clickable { onFilterSelected("MANTENIMIENTO") })
        StatItem(label = "Uso", value = cars.count { it.status == CarStatus.EN_USO }, color = Color(0xFF3B82F6), modifier = Modifier.weight(1f).clickable { onFilterSelected("EN_USO") })
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, color.copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
            Text(text = value.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("TODOS", "DISPONIBLE", "PEND_APROBACION", "EN_PROCESO", "MANTENIMIENTO")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Black, selectedLabelColor = Color.White)
            )
        }
    }
}

@Composable
fun EmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Text("No hay resultados para \"$searchQuery\"", color = Color.Gray)
        }
    }
}

@Composable
private fun RentalDetailDialog(
    detail: RentalDetailUiState, 
    onDismiss: () -> Unit, 
    onMarkAsInUse: () -> Unit, 
    onCompleteRental: () -> Unit, 
    onCancelRental: () -> Unit, 
    onApproveRental: (RentalEntity) -> Unit, 
    onRejectRental: (RentalEntity, Int) -> Unit,
    onMoverAMantenimiento: () -> Unit,
    rol: RolUsuario
) {
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
                }
                
                if (rol == RolUsuario.RECEPCIONISTA) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (car.status == CarStatus.DISPONIBLE) {
                            Button(onClick = onMoverAMantenimiento, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                Text("A Mantenimiento")
                            }
                        } else if (car.status == CarStatus.MANTENIMIENTO) {
                            Button(onClick = onMarkAsInUse) {
                                Text("Disponible")
                            }
                        }
                        
                        if (car.status == CarStatus.PEND_APROBACION && rental != null) {
                            TextButton(onClick = { onRejectRental(rental, car.id) }) {
                                Text("Rechazar Solicitud", color = Color.Red)
                            }
                        } else if (car.status == CarStatus.EN_PROCESO) {
                            TextButton(onClick = onCancelRental) {
                                Text("Cancelar Reserva", color = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (rol == RolUsuario.RECEPCIONISTA) {
                when (car.status) {
                    CarStatus.PEND_APROBACION -> Button(onClick = { if (rental != null) onApproveRental(rental) }) { Text("Aprobar") }
                    CarStatus.EN_PROCESO -> Button(onClick = onMarkAsInUse) { Text("Entregar") }
                    CarStatus.EN_USO -> Button(onClick = onCompleteRental) { Text("Recibir") }
                    else -> {}
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
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
            val color = if (isActive) Color(0xFF10B981) else Color.LightGray
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(24.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                    if (isActive) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Text(text = label, fontSize = 8.sp, color = color)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalDialog(car: CarEntity, onDismiss: () -> Unit, onConfirm: (String, Long, Long) -> Unit, calculateCost: (Long, Long) -> Result<Double>) {
    var name by remember { mutableStateOf("") }
    var pMs by remember { mutableStateOf<Long?>(null) }
    var rMs by remember { mutableStateOf<Long?>(null) }
    var showP by remember { mutableStateOf(false) }
    var showR by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar ${car.brand}") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tu Nombre") }, modifier = Modifier.fillMaxWidth())
                Row {
                    TextButton(onClick = { showP = true }) { Text("Desde: ${formatDate(pMs)}") }
                    TextButton(onClick = { showR = true }) { Text("Hasta: ${formatDate(rMs)}") }
                }
                if (pMs != null && rMs != null) {
                    calculateCost(pMs!!, rMs!!).onSuccess { 
                        Text("Costo Estimado: $${it}", fontWeight = FontWeight.Bold, color = Color(
                            0xFF017249
                        )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && pMs != null && rMs != null) onConfirm(name, pMs!!, rMs!!) }) { Text("Enviar Solicitud") }
        }
    )
    if (showP) {
        val state = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showP = false }, confirmButton = { TextButton(onClick = { pMs = state.selectedDateMillis; showP = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
    if (showR) {
        val state = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showR = false }, confirmButton = { TextButton(onClick = { rMs = state.selectedDateMillis; showR = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
}

@Composable
private fun AddCarDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String, String) -> Unit) {
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("San Salvador, SV") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Automóvil") },
        text = {
            Column {
                OutlinedTextField(value = marca, onValueChange = { marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio por día") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL de la fotografía") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (marca.isNotBlank() && modelo.isNotBlank()) onConfirm(marca, modelo, precio.toDoubleOrNull() ?: 0.0, url, ubicacion) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PendingRentalsDialog(pendingRentals: List<RentalEntity>, allCars: List<CarEntity>, onDismiss: () -> Unit, onApprove: (RentalEntity) -> Unit, onReject: (RentalEntity, Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitudes Pendientes") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(pendingRentals) { rental ->
                    val car = allCars.find { it.id == rental.carId }
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${car?.brand} ${car?.model}", fontWeight = FontWeight.Bold)
                            Text("Cliente: ${rental.customerName}")
                            Row {
                                TextButton(onClick = { onReject(rental, rental.carId) }) { Text("Rechazar", color = Color.Red) }
                                TextButton(onClick = { onApprove(rental) }) { Text("Aprobar") }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

private fun formatDate(m: Long?): String = if (m == null) "Seleccionar" else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date(m))
