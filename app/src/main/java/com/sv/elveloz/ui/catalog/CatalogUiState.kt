package com.sv.elveloz.ui.catalog

import com.sv.elveloz.data.local.entity.CarEntity

data class CatalogUiState(
    val cars: List<CarEntity> = emptyList(),
    val allCars: List<CarEntity> = emptyList(),
    val filterStatus: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedCar: CarEntity? = null,
)