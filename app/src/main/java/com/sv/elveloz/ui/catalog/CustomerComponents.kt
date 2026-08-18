package com.sv.elveloz.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
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
import com.sv.elveloz.domain.model.CarStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TopSearchBar(searchQuery: String, onSearchChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = searchQuery, onValueChange = onSearchChange, modifier = Modifier.weight(1f),
            placeholder = { Text("Busca por marca o modelo...", color = Color.Gray, fontSize = 13.sp) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$formattedPrice/Día",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAvailable) Color(0xFF1F2937) else Color(0xFFE5E7EB),
                    modifier = (if (isAvailable) Modifier.clickable { onReserveClick() } else Modifier)
                        .wrapContentWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAvailable) "Reservar" else "No disponible",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) Color.White else Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}