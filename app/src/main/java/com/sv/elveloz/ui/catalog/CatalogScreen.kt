package com.sv.elveloz.ui.catalog

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.launch
import java.util.Locale

data class MockReservation(
    val id: Int,
    val carName: String,
    val date: String,
    val price: String,
    val status: String
)

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

    var selectedBrand by remember { mutableStateOf("Todos") }
    var showCustomerNotifications by remember { mutableStateOf(false) }
    var carDetailsToShow by remember { mutableStateOf<CarEntity?>(null) }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showReservationsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("ElVelozProfile", Context.MODE_PRIVATE) }

    var profileFirstName by remember { mutableStateOf(sharedPrefs.getString("firstName", "Juan") ?: "Juan") }
    var profileLastName by remember { mutableStateOf(sharedPrefs.getString("lastName", "Pérez") ?: "Pérez") }
    var profileEmail by remember { mutableStateOf(sharedPrefs.getString("email", "juan.perez@elveloz.com") ?: "juan.perez@elveloz.com") }
    var profilePhone by remember { mutableStateOf(sharedPrefs.getString("phone", "+503 7000-0000") ?: "+503 7000-0000") }

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val profileBitmap = remember(profileImageUri) {
        profileImageUri?.let { uri ->
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) { null }
        }?.asImageBitmap()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) profileImageUri = uri }

    var mockReservations by remember {
        mutableStateOf(
            listOf(
                MockReservation(1, "Toyota Corolla 2023", "20/08/2026 - 22/08/2026", "$90.00", "Pendiente"),
                MockReservation(2, "Nissan Sentra 2022", "15/08/2026 - 17/08/2026", "$80.00", "Aprobada"),
                MockReservation(3, "Kia Picanto 2022", "01/08/2026 - 03/08/2026", "$60.00", "Finalizada")
            )
        )
    }

    val notificacionesCliente = uiState.allCars.filter { it.status == CarStatus.EN_PROCESO }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (rol == RolUsuario.RECEPCIONISTA) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(40.dp).background(Color.Black, shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Recepción", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "El Veloz", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery, onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar por marca o modelo...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Color.Black)
                    )
                }
            } else {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Box(modifier = Modifier.size(44.dp).background(Color.Black, shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "El Veloz", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier.size(44.dp).border(1.dp, Color(0xFFE5E7EB), CircleShape).clickable { showCustomerNotifications = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                            if (notificacionesCliente.isNotEmpty()) {
                                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp).size(16.dp).background(Color(0xFF4B5563), CircleShape), contentAlignment = Alignment.Center) {
                                    Text(text = notificacionesCliente.size.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (profileBitmap != null) {
                            Image(bitmap = profileBitmap, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape).clickable { showProfileDialog = true }, contentScale = ContentScale.Crop)
                        } else {
                            Image(painter = painterResource(id = com.sv.elveloz.R.drawable.perfil), contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape).clickable { showProfileDialog = true }, contentScale = ContentScale.Crop)
                        }
                    }
                }
                item { TopSearchBar(searchQuery = uiState.searchQuery, onSearchChange = { viewModel.onSearchQueryChange(it) }) }
                item { BrandFilterRow(selectedBrand = selectedBrand, onBrandSelected = { selectedBrand = it }) }
                item { Text(text = "Recomendado para ti", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp)) }
            }

            if (rol == RolUsuario.RECEPCIONISTA && pendingRentals.isNotEmpty()) {
                item { Text(text = "Acción Requerida (${pendingRentals.size})", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F)) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pendingRentals, key = { it.id }) { rental ->
                            val car = uiState.allCars.find { it.id == rental.carId }
                            ActionRequiredCard(
                                rental = rental, car = car,
                                onApprove = { viewModel.onApproveRental(rental); coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") } },
                                onReject = { viewModel.onRejectRental(rental, rental.carId); coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") } }
                            )
                        }
                    }
                }
            }

            if (rol == RolUsuario.RECEPCIONISTA) {
                item { QuickStatsPanel(cars = uiState.allCars, onFilterSelected = { viewModel.onFilterChange(it) }) }
                item {
                    Text("Filtro de Flota", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                    FilterChipsRow(selectedFilter = uiState.filterStatus ?: "", onFilterSelected = { viewModel.onFilterChange(newFilter = it) })
                }
                item { Text(text = "Catálogo de Vehículos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
            }

            val displayCars = if (rol == RolUsuario.CLIENTE && selectedBrand != "Todos") {
                uiState.cars.filter { it.brand.equals(selectedBrand, ignoreCase = true) }
            } else { uiState.cars }

            if (uiState.isLoading) {
                item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Black) } }
            } else if (displayCars.isEmpty()) {
                item { EmptySearchState(searchQuery = uiState.searchQuery, modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else {
                items(displayCars.chunked(2), key = { rowCars -> rowCars.first().id }) { rowCars ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowCars.forEach { car ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (rol == RolUsuario.CLIENTE) {
                                    CustomerCarCard(car = car, onCardClick = { carDetailsToShow = car }, onReserveClick = { viewModel.onCarClicked(car) })
                                } else {
                                    CarGridCard(car = car, rol = rol, onClick = { viewModel.onCarClicked(car) })
                                }
                            }
                        }
                        if (rowCars.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // ==============================================================================
    // DIÁLOGOS EN LÍNEA (Perfil, Notificaciones y Reservaciones)
    // ==============================================================================

    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Editar Perfil", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (profileBitmap != null) {
                            Image(bitmap = profileBitmap, contentDescription = null, modifier = Modifier.size(90.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Image(painter = painterResource(id = com.sv.elveloz.R.drawable.perfil), contentDescription = null, modifier = Modifier.size(90.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        }
                        Surface(
                            shape = CircleShape, color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = 4.dp).size(28.dp).clickable { imagePickerLauncher.launch("image/*") }
                        ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.DarkGray) } }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$profileFirstName $profileLastName", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = profileFirstName, onValueChange = { profileFirstName = it }, placeholder = { Text("First Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profileLastName, onValueChange = { profileLastName = it }, placeholder = { Text("Last Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profileEmail, onValueChange = { profileEmail = it }, placeholder = { Text("Email", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profilePhone, onValueChange = { profilePhone = it }, placeholder = { Text("Phone", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            sharedPrefs.edit().apply {
                                putString("firstName", profileFirstName); putString("lastName", profileLastName)
                                putString("email", profileEmail); putString("phone", profilePhone); apply()
                            }
                            showProfileDialog = false
                            coroutineScope.launch { snackbarHostState.showSnackbar("Datos guardados correctamente") }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937))
                    ) { Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onLogout) { Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold) }
                        TextButton(onClick = { showProfileDialog = false; showReservationsDialog = true }) { Text("Reservaciones", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    if (showReservationsDialog) {
        AlertDialog(
            onDismissRequest = { showReservationsDialog = false }, containerColor = Color.White, shape = RoundedCornerShape(24.dp),
            title = { Text("Mis Reservaciones", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                    items(mockReservations) { res ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), border = BorderStroke(1.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = res.carName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Fechas: ${res.date}", fontSize = 13.sp, color = Color.DarkGray)
                                Text(text = "Total: ${res.price}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    val statusColor = when(res.status) { "Pendiente" -> Color(0xFFF57C00); "Aprobada", "Activa" -> Color(0xFF4CAF50); "Cancelada", "Rechazada" -> Color(0xFFD32F2F); else -> Color.Gray }
                                    Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, statusColor)) {
                                        Text(text = res.status.uppercase(Locale.getDefault()), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    if (res.status == "Pendiente") {
                                        TextButton(
                                            onClick = {
                                                mockReservations = mockReservations.map { if (it.id == res.id) it.copy(status = "Cancelada") else it }
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Reservación cancelada") }
                                            }
                                        ) { Text("Cancelar", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showReservationsDialog = false }) { Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold) } }
        )
    }

    if (showCustomerNotifications) {
        AlertDialog(
            onDismissRequest = { showCustomerNotifications = false }, containerColor = Color.White, shape = RoundedCornerShape(20.dp),
            title = { Text("Notificaciones", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                if (notificacionesCliente.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("¡Buenas noticias! Tienes reservas aprobadas:", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                        notificacionesCliente.forEach { car ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)), modifier = Modifier.fillMaxWidth()) {
                                Text("Tu ${car.brand} ${car.model} está listo para ser retirado en sucursal.", modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Color.Black)
                            }
                        }
                    }
                } else { Text("No tienes notificaciones nuevas por el momento.", color = Color.Gray) }
            },
            confirmButton = { TextButton(onClick = { showCustomerNotifications = false }) { Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold) } }
        )
    }



    carDetailsToShow?.let { car -> CarDetailsDialog(car = car, onDismiss = { carDetailsToShow = null }, onReserve = { carDetailsToShow = null; viewModel.onCarClicked(car) }) }

    uiState.selectedCar?.let { car ->
        RentalDialog(
            car = car, onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { customerName, pickupMs, returnMs ->
                viewModel.onConfirmRental(customerName, pickupMs, returnMs)
                coroutineScope.launch { snackbarHostState.showSnackbar(if (rol == RolUsuario.RECEPCIONISTA) "Reserva de mostrador registrada" else "Solicitud enviada al mostrador") }
            },
            calculateCost = { pickupMs, returnMs -> viewModel.calculateCost(pickupMs, returnMs, car.pricePerDay) }
        )
    }

    rentalDetail?.let { detail ->
        RentalDetailDialog(
            detail = detail, onDismiss = { viewModel.onDismissDetail() },
            onMarkAsInUse = { viewModel.onMarkAsInUse(); coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo marcado como retirado") } },
            onCompleteRental = { viewModel.onCompleteRental(); coroutineScope.launch { snackbarHostState.showSnackbar("Vehículo devuelto con éxito") } },
            onCancelRental = { viewModel.onCancelRental(); coroutineScope.launch { snackbarHostState.showSnackbar("Reserva cancelada correctamente") } },
            onApproveRental = { rental -> viewModel.onApproveRental(rental); viewModel.onDismissDetail(); coroutineScope.launch { snackbarHostState.showSnackbar("Reserva aprobada") } },
            onRejectRental = { rental, carId -> viewModel.onRejectRental(rental, carId); viewModel.onDismissDetail(); coroutineScope.launch { snackbarHostState.showSnackbar("Reserva rechazada") } }
        )
    }
}