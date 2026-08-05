package com.sv.elveloz.ui.catalog

import com.sv.elveloz.data.local.entity.CarEntity

data class CatalogUiState(
    val cars: List<CarEntity> = emptyList(),
    val filterStatus: String = "TODOS",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val selectedCar: CarEntity? = null
)