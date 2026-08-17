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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
            } catch (e: Exception) {
                null
            }
        }?.asImageBitmap()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
        }
    }

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
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (rol == RolUsuario.RECEPCIONISTA) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color.Black, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
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
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por marca o modelo...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }
            } else {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
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
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp).size(16.dp).background(Color(0xFF4B5563), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = notificacionesCliente.size.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp).clip(CircleShape).clickable { showProfileDialog = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.sv.elveloz.R.drawable.perfil),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp).clip(CircleShape).clickable { showProfileDialog = true },
                                contentScale = ContentScale.Crop
                            )
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
            } else {
                uiState.cars
            }

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
                                    CustomerCarCard(
                                        car = car,
                                        onCardClick = { carDetailsToShow = car },
                                        onReserveClick = { viewModel.onCarClicked(car) }
                                    )
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

    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("Editar Perfil", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(contentAlignment = Alignment.Center) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(90.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.sv.elveloz.R.drawable.perfil),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(90.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(28.dp)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, contentDescription = "Cambiar", modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$profileFirstName $profileLastName", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = profileFirstName, onValueChange = { profileFirstName = it },
                        placeholder = { Text("First Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profileLastName, onValueChange = { profileLastName = it },
                        placeholder = { Text("Last Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profileEmail, onValueChange = { profileEmail = it },
                        placeholder = { Text("Email", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profilePhone, onValueChange = { profilePhone = it },
                        placeholder = { Text("Phone", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            sharedPrefs.edit().apply {
                                putString("firstName", profileFirstName)
                                putString("lastName", profileLastName)
                                putString("email", profileEmail)
                                putString("phone", profilePhone)
                                apply()
                            }
                            showProfileDialog = false
                            coroutineScope.launch { snackbarHostState.showSnackbar("Datos guardados correctamente") }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937))
                    ) {
                        Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onLogout) {
                            Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showProfileDialog = false; showReservationsDialog = true }) {
                            Text("Reservaciones", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showReservationsDialog) {
        AlertDialog(
            onDismissRequest = { showReservationsDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Mis Reservaciones", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(mockReservations) { res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = res.carName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Fechas: ${res.date}", fontSize = 13.sp, color = Color.DarkGray)
                                Text(text = "Total: ${res.price}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val statusColor = when(res.status) {
                                        "Pendiente" -> Color(0xFFF57C00)
                                        "Aprobada", "Activa" -> Color(0xFF4CAF50)
                                        "Cancelada", "Rechazada" -> Color(0xFFD32F2F)
                                        else -> Color.Gray
                                    }
                                    Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, statusColor)) {
                                        Text(text = res.status.uppercase(Locale.getDefault()), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }

                                    if (res.status == "Pendiente") {
                                        TextButton(
                                            onClick = {
                                                mockReservations = mockReservations.map {
                                                    if (it.id == res.id) it.copy(status = "Cancelada") else it
                                                }
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Reservación cancelada") }
                                            }
                                        ) {
                                            Text("Cancelar", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReservationsDialog = false }) { Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        )
    }

    carDetailsToShow?.let { car ->
        CarDetailsDialog(
            car = car,
            onDismiss = { carDetailsToShow = null },
            onReserve = {
                carDetailsToShow = null
                viewModel.onCarClicked(car)
            }
        )
    }

    if (showCustomerNotifications) {
        AlertDialog(
            onDismissRequest = { showCustomerNotifications = false },
            containerColor = Color.White, shape = RoundedCornerShape(20.dp),
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
                } else {
                    Text("No tienes notificaciones nuevas por el momento.", color = Color.Gray)
                }
            },
            confirmButton = { TextButton(onClick = { showCustomerNotifications = false }) { Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold) } }
        )
    }

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

@Composable
fun TopSearchBar(searchQuery: String, onSearchChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = searchQuery, onValueChange = onSearchChange, modifier = Modifier.weight(1f),
            placeholder = { Text("Busca tu auto ideal...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Black, unfocusedIndicatorColor = Color(0xFFE5E7EB), cursorColor = Color.Black),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            onClick = { },
            modifier = Modifier.size(54.dp).background(Color.White, shape = CircleShape).border(1.dp, Color(0xFFE5E7EB), CircleShape)
        ) { Icon(Icons.Filled.List, contentDescription = null, tint = Color.Black) }
    }
}

@Composable
fun BrandFilterRow(selectedBrand: String, onBrandSelected: (String) -> Unit) {
    val brands = listOf("Todos", "Toyota", "Nissan", "Honda", "Hyundai", "Kia", "Mazda", "Chevrolet", "Ford")
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(brands) { brand ->
            val isSelected = selectedBrand == brand
            val iconResId = when (brand.lowercase(Locale.getDefault())) {
                "todos" -> com.sv.elveloz.R.drawable.logo_all
                "toyota" -> com.sv.elveloz.R.drawable.logo_toyota
                "nissan" -> com.sv.elveloz.R.drawable.logo_nissan
                "honda" -> com.sv.elveloz.R.drawable.logo_honda
                "hyundai" -> com.sv.elveloz.R.drawable.logo_hyundai
                "kia" -> com.sv.elveloz.R.drawable.logo_kia
                "mazda" -> com.sv.elveloz.R.drawable.logo_mazda
                "chevrolet" -> com.sv.elveloz.R.drawable.logo_chevrolet
                "ford" -> com.sv.elveloz.R.drawable.logo_ford
                else -> 0
            }
            Surface(
                shape = RoundedCornerShape(50), color = if (isSelected) Color(0xFF1F2937) else Color.White,
                border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                modifier = Modifier.clickable { onBrandSelected(brand) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (iconResId != 0) {
                        Image(painter = painterResource(id = iconResId), contentDescription = null, modifier = Modifier.size(24.dp))
                    } else {
                        Box(modifier = Modifier.size(24.dp).background(if (isSelected) Color.White else Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = if (isSelected) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (brand == "Todos") "ALL" else brand, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun CustomerCarCard(car: CarEntity, onCardClick: () -> Unit, onReserveClick: () -> Unit) {
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

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId), contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(12.dp), contentScale = ContentScale.Fit,
                        alpha = if (isAvailable) 1f else 0.4f
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(56.dp),
                        tint = if (isAvailable) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "${car.brand} ${car.model}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "$formattedPrice/Día", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAvailable) Color(0xFF1F2937) else Color(0xFFE5E7EB),
                    modifier = if (isAvailable) Modifier.clickable { onReserveClick() } else Modifier
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isAvailable) "Reservar" else "No disponible",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAvailable) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
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
private fun CarGridCard(car: CarEntity, rol: RolUsuario, onClick: () -> Unit) {
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                if (imageResId != 0) {
                    androidx.compose.foundation.Image(painter = androidx.compose.ui.res.painterResource(id = imageResId), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Icon(imageVector = Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(56.dp), tint = if (isAvailable) Color.DarkGray else Color.Gray.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "${car.brand} ${car.model}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (car.status) {
                    CarStatus.DISPONIBLE -> "Disponible"
                    CarStatus.PEND_APROBACION -> "Pend. Aprobación"
                    CarStatus.EN_PROCESO -> "En Proceso"
                    CarStatus.EN_USO -> "En Uso"
                    CarStatus.MANTENIMIENTO -> "Mantenimiento"
                },
                fontSize = 11.sp, fontWeight = FontWeight.Medium, color = getStatusColor(car.status)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = formattedPrice, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(text = "/ Día", fontSize = 10.sp, color = Color.Gray)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = if (isAvailable) Color(0xFF1F2937) else Color(0xFFE5E7EB)) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(text = if (isAvailable) "Reservar" else "Gestionar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAvailable) Color.White else Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRequiredCard(rental: RentalEntity, car: CarEntity?, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(modifier = Modifier.width(280.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), border = BorderStroke(1.dp, Color(0xFFFFB74D)), shape = RoundedCornerShape(12.dp)) {
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
    CarStatus.MANTENIMIENTO -> Color(0xFFF44336)
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
                selected = selectedFilter == filter, onClick = { onFilterSelected(filter) },
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
private fun RentalStepper(status: CarStatus) {
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
private fun RentalDialog(car: CarEntity, onDismiss: () -> Unit, onConfirm: (String, Long, Long) -> Unit, calculateCost: (Long, Long) -> Result<Double>) {
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