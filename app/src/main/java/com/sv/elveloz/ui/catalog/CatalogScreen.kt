package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.domain.model.CarStatus
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val rentalDetail by viewModel.rentalDetail.collectAsState()

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

            // 1. Header (Panel de control)
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
                    Text(text = "Recepción", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "El Veloz", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }

            // 2. Buscador Estilo Moderno
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
            QuickStatsPanel(cars = uiState.allCars)
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Filtros (Operativos)
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

            // 5. Inventario en Lista
            Text(
                text = "Estado del Inventario",
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
                            onClick = { viewModel.onCarClicked(car) },
                            onMaintenanceClick = { isGoingToWorkshop ->
                                viewModel.setCarMaintenanceStatus(car.id, isGoingToWorkshop)
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGOS ---
    uiState.selectedCar?.let { car ->
        RentalDialog(
            car = car,
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { customerName, pickupMs, returnMs ->
                viewModel.onConfirmRental(customerName, pickupMs, returnMs)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Reserva registrada para $customerName")
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
                viewModel.onDismissDetail()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo marcado como retirado") }
            },
            onCompleteRental = {
                viewModel.onCompleteRental()
                viewModel.onDismissDetail()
                coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo devuelto con éxito") }
            },
            onCancelRental = {
                viewModel.onCancelRental()
                viewModel.onDismissDetail()
                coroutineScope.launch { snackbarHostState.showSnackbar("Reserva cancelada correctamente") }
            }
        )
    }
}

@Composable
private fun ReceptionistVehicleCard(
    car: CarEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMaintenanceClick: (Boolean) -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedPrice = format.format(car.pricePerDay)

    val isAvailableOrMaintenance = car.status == CarStatus.DISPONIBLE || car.status == CarStatus.MANTENIMIENTO
    val alphaValue = if (isAvailableOrMaintenance) 1f else 0.5f

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Gray.copy(alpha = alphaValue)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${car.brand} ${car.model}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = alphaValue),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$formattedPrice / día",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray.copy(alpha = alphaValue)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(car.status).copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                val statusText = when (car.status) {
                    CarStatus.DISPONIBLE -> "Disponible"
                    CarStatus.EN_PROCESO -> "En Proceso"
                    CarStatus.EN_USO -> "En Uso"
                    CarStatus.MANTENIMIENTO -> "En Taller"
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(car.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            if (car.status == CarStatus.DISPONIBLE || car.status == CarStatus.MANTENIMIENTO) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (car.status == CarStatus.DISPONIBLE) {
                            DropdownMenuItem(
                                text = { Text("Enviar a Taller", fontWeight = FontWeight.Medium, color = Color.Black) },
                                leadingIcon = { Icon(Icons.Filled.Build, tint = Color(0xFFE53935), contentDescription = null) },
                                onClick = {
                                    expanded = false
                                    onMaintenanceClick(true)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Marcar Reparado", fontWeight = FontWeight.Medium, color = Color.Black) },
                                leadingIcon = { Icon(Icons.Filled.CheckCircle, tint = Color(0xFF4CAF50), contentDescription = null) },
                                onClick = {
                                    expanded = false
                                    onMaintenanceClick(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getStatusColor(status: CarStatus): Color {
    return when (status) {
        CarStatus.DISPONIBLE -> Color(0xFF4CAF50)
        CarStatus.EN_PROCESO -> Color(0xFFFF9800)
        CarStatus.EN_USO -> Color(0xFF2196F3)
        CarStatus.MANTENIMIENTO -> Color(0xFFE53935)
    }
}

@Composable
private fun QuickStatsPanel(cars: List<CarEntity>) {
    val disponibles = cars.count { it.status == CarStatus.DISPONIBLE }
    val enProceso = cars.count { it.status == CarStatus.EN_PROCESO }
    val enUso = cars.count { it.status == CarStatus.EN_USO }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(label = "Disp.", value = disponibles, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
        StatItem(label = "Proc.", value = enProceso, color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
        StatItem(label = "En Uso", value = enUso, color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("TODOS", "DISPONIBLE", "EN_PROCESO", "EN_USO", "MANTENIMIENTO")
    val labels = mapOf(
        "TODOS" to "Todos (8)", "DISPONIBLE" to "Disponibles",
        "EN_PROCESO" to "En Proceso", "EN_USO" to "En Uso", "MANTENIMIENTO" to "En Taller"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(labels[filter] ?: filter, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(50),
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = Color.DarkGray,
                    selectedContainerColor = Color(0xFF374151),
                    selectedLabelColor = Color.White
                ),
                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = selectedFilter == filter,
                    borderColor = Color.LightGray, selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun EmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No se encontraron vehículos", style = MaterialTheme.typography.titleLarge, color = Color.Black)
        if (searchQuery.isNotEmpty()) {
            Text(text = "No hay coincidencias para \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
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

    // Estados para Entrega (EN_PROCESO)
    var checkLicense by remember { mutableStateOf(false) }
    var checkDeposit by remember { mutableStateOf(false) }
    var checkKeys by remember { mutableStateOf(false) }
    val allDeliveryChecked = checkLicense && checkDeposit && checkKeys

    // Estados para Devolución (EN_USO)
    var checkFuel by remember { mutableStateOf(false) }
    var checkDamage by remember { mutableStateOf(false) }
    var checkKeysReturn by remember { mutableStateOf(false) }
    val allReturnChecked = checkFuel && checkDamage && checkKeysReturn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${car.brand} ${car.model}") },
        text = {
            Column {
                RentalStepper(status = car.status)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (rental != null) {
                    val format = NumberFormat.getCurrencyInstance(Locale.US)
                    val formattedTotal = format.format(rental.totalCost)

                    Text("Cliente: ${rental.customerName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Recogida: ${formatDate(rental.pickupDateMs)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Entrega: ${formatDate(rental.returnDateMs)}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Costo total: $formattedTotal",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )

                    // 1. CHECKLIST DE ENTREGA (Usando el nuevo diseño moderno)
                    if (car.status == CarStatus.EN_PROCESO) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Requisitos de Entrega:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(4.dp))

                                ModernChecklistRow("Licencia verificada", checkLicense) { checkLicense = it }
                                ModernChecklistRow("Depósito retenido", checkDeposit) { checkDeposit = it }
                                ModernChecklistRow("Llaves entregadas", checkKeys) { checkKeys = it }
                            }
                        }
                    }

                    // 2. CHECKLIST DE DEVOLUCIÓN (NUEVO)
                    if (car.status == CarStatus.EN_USO) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Auditoría de Devolución:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(4.dp))

                                ModernChecklistRow("Combustible verificado", checkFuel) { checkFuel = it }
                                ModernChecklistRow("Inspección sin daños", checkDamage) { checkDamage = it }
                                ModernChecklistRow("Llaves recibidas", checkKeysReturn) { checkKeysReturn = it }
                            }
                        }
                    }

                } else {
                    Text("No se encontró información de la reserva.")
                }
            }
        },
        confirmButton = {
            when (car.status) {
                CarStatus.EN_PROCESO -> {
                    Button(
                        onClick = onMarkAsInUse,
                        enabled = allDeliveryChecked
                    ) { Text("Aprobar Entrega") }
                }
                CarStatus.EN_USO -> {
                    Button(
                        onClick = onCompleteRental,
                        enabled = allReturnChecked,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) { Text("Finalizar y Liberar") }
                }
                else -> {}
            }
        },
        dismissButton = {
            Row {
                if (car.status == CarStatus.EN_PROCESO) {
                    TextButton(onClick = onCancelRental) { Text("Cancelar Reserva", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
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
        CarStatus.MANTENIMIENTO -> 2
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                    if (isActive) Box(modifier = Modifier.size(10.dp).background(Color.White, shape = CircleShape))
                }
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(top = 4.dp))
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
                    value = customerName, onValueChange = { customerName = it },
                    label = { Text("Nombre del cliente") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text("Fecha de recogida", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = formatDate(pickupDateMs), onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { showPickupPicker = true },
                    enabled = false
                )

                Text("Fecha de entrega", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = formatDate(returnDateMs), onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { showReturnPicker = true },
                    enabled = false
                )

                errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                errorMessage = null
                val pickup = pickupDateMs
                val returnD = returnDateMs

                if (customerName.isBlank()) {
                    errorMessage = "Ingresa el nombre del cliente"
                    return@Button
                }
                if (pickup == null || returnD == null) {
                    errorMessage = "Selecciona ambas fechas"
                    return@Button
                }

                val result = calculateCost(pickup, returnD)
                if (result.isFailure) {
                    errorMessage = result.exceptionOrNull()?.message ?: "Fechas inválidas"
                    return@Button
                }

                onConfirm(customerName, pickup, returnD)
                onDismiss()
            }) {
                Text("Confirmar Reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
            dismissButton = { TextButton(onClick = { showPickupPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
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
            dismissButton = { TextButton(onClick = { showReturnPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }
}

@Composable
fun ModernChecklistRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Animaciones suaves para el fondo y el ícono al hacer clic
    val backgroundColor by animateColorAsState(
        targetValue = if (isChecked) Color(0xFFE3F2FD) else Color.Transparent, label = "bg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isChecked) Color(0xFF2196F3) else Color(0xFFCBD5E1), label = "icon"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isChecked) Color(0xFF1E293B) else Color(0xFF64748B)
        )
    }
}