package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CarGridCard(car: CarEntity, rol: RolUsuario, onClick: () -> Unit) {
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
                    Image(painter = painterResource(id = imageResId), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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

// TARJETA DE NOTIFICACIÓN (Sin botones para evitar redundancia)
@Composable
fun ActionRequiredCard(
    rental: RentalEntity,
    car: CarEntity?,
    onApprove: () -> Unit, // Estos parámetros ya no se usan en la UI, pero los dejamos para no romper el CatalogScreen
    onReject: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedCost = format.format(rental.totalCost)

    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título Principal
            Text(
                text = car?.let { "${it.brand} ${it.model}" } ?: "Vehículo Desconocido",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Datos del Cliente
            Text(
                text = rental.customerName,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Fila de Precio y Mensaje de Acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Precio resaltado
                Text(
                    text = formattedCost,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Mensaje sutil indicando la acción
                Text(
                    text = "Ir al catálogo para gestionar",
                    color = Color(0xFF4CAF50), // Un verde que invita a la acción
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun getStatusColor(status: CarStatus) = when (status) {
    CarStatus.DISPONIBLE -> Color(0xFF4CAF50)
    CarStatus.PEND_APROBACION -> Color(0xFF9C27B0)
    CarStatus.EN_PROCESO -> Color(0xFFFF9800)
    CarStatus.EN_USO -> Color(0xFF2196F3)
    CarStatus.MANTENIMIENTO -> Color(0xFFF44336)
}

@Composable
fun QuickStatsPanel(cars: List<CarEntity>, onFilterSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatItem(label = "Disp.", value = cars.count { it.status == CarStatus.DISPONIBLE }, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f).clickable { onFilterSelected("DISPONIBLE") })
        StatItem(label = "Pend.", value = cars.count { it.status == CarStatus.PEND_APROBACION }, color = Color(0xFF9C27B0), modifier = Modifier.weight(1f).clickable { onFilterSelected("PEND_APROBACION") })
        StatItem(label = "Proc.", value = cars.count { it.status == CarStatus.EN_PROCESO }, color = Color(0xFFFF9800), modifier = Modifier.weight(1f).clickable { onFilterSelected("EN_PROCESO") })
        StatItem(label = "Uso", value = cars.count { it.status == CarStatus.EN_USO }, color = Color(0xFF2196F3), modifier = Modifier.weight(1f).clickable { onFilterSelected("EN_USO") })
    }
}

@Composable
fun StatItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, color.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
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