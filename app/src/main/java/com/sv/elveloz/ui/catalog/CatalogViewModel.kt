package com.sv.elveloz.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.usecase.CalculateCostUseCase
import com.sv.elveloz.domain.usecase.CompleteRentalUseCase
import com.sv.elveloz.domain.usecase.GetActiveRentalUseCase
import com.sv.elveloz.domain.usecase.GetCarsUseCase
import com.sv.elveloz.domain.usecase.RentCarUseCase
import com.sv.elveloz.domain.usecase.UpdateCarStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.sv.elveloz.domain.usecase.CancelRentalUseCase

data class RentalDetailUiState(
    val car: CarEntity,
    val rental: RentalEntity?
)

class CatalogViewModel(
    private val getCarsUseCase: GetCarsUseCase,
    private val rentCarUseCase: RentCarUseCase,
    private val calculateCostUseCase: CalculateCostUseCase,
    private val getActiveRentalUseCase: GetActiveRentalUseCase,
    private val updateCarStatusUseCase: UpdateCarStatusUseCase,
    private val completeRentalUseCase: CompleteRentalUseCase,
    private val cancelRentalUseCase: CancelRentalUseCase
) : ViewModel() {

    private val _filterStatus = MutableStateFlow("TODOS")
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCar = MutableStateFlow<CarEntity?>(null)
    private val _rentalDetail = MutableStateFlow<RentalDetailUiState?>(null)

    val uiState: StateFlow<CatalogUiState> = combine(
        getCarsUseCase(),
        _filterStatus,
        _searchQuery,
        _selectedCar
    ) { cars, filter, query, selectedCar ->
        val filteredByStatus = if (filter == "TODOS") {
            cars
        } else {
            cars.filter { it.status.name == filter }
        }
        val filteredBySearch = if (query.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.brand.contains(query, ignoreCase = true) ||
                        it.model.contains(query, ignoreCase = true)
            }
        }
        CatalogUiState(
            cars = filteredBySearch,
            allCars = cars,
            filterStatus = filter,
            searchQuery = query,
            selectedCar = selectedCar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState(isLoading = true)
    )

    val rentalDetail: StateFlow<RentalDetailUiState?> = _rentalDetail

    fun onFilterChange(newFilter: String) {
        _filterStatus.value = newFilter
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCarClicked(car: CarEntity) {
        when (car.status) {
            CarStatus.DISPONIBLE -> {
                _selectedCar.value = car
            }
            CarStatus.EN_PROCESO, CarStatus.EN_USO -> {
                viewModelScope.launch {
                    val rental = getActiveRentalUseCase(carId = car.id.toInt())
                    _rentalDetail.value = RentalDetailUiState(car, rental)
                }
            }
            CarStatus.MANTENIMIENTO -> {

            }
        }
    }

    fun onDismissDialog() {
        _selectedCar.value = null
    }

    fun onDismissDetail() {
        _rentalDetail.value = null
    }

    fun calculateCost(pickupDateMs: Long, returnDateMs: Long, pricePerDay: Double): Result<Double> {
        return calculateCostUseCase(pickupDateMs, returnDateMs, pricePerDay)
    }

    fun onConfirmRental(customerName: String, pickupDateMs: Long, returnDateMs: Long) {
        val car = _selectedCar.value ?: return
        val costResult = calculateCostUseCase(pickupDateMs, returnDateMs, car.pricePerDay)
        val totalCost = costResult.getOrNull() ?: return

        viewModelScope.launch {
            val rental = RentalEntity(
                carId = car.id,
                customerName = customerName,
                pickupDateMs = pickupDateMs,
                returnDateMs = returnDateMs,
                totalCost = totalCost
            )
            rentCarUseCase(rental, car.id.toInt())
            _selectedCar.value = null
        }
    }

    fun onMarkAsInUse() {
        val detail = _rentalDetail.value ?: return
        viewModelScope.launch {
            updateCarStatusUseCase(detail.car.id.toInt(), CarStatus.EN_USO)
            _rentalDetail.value = null
        }
    }

    fun onCompleteRental() {
        val detail = _rentalDetail.value ?: return
        val rental = detail.rental ?: return
        viewModelScope.launch {
            completeRentalUseCase(rental, detail.car.id.toInt())
            _rentalDetail.value = null
        }
    }

    fun onCancelRental() {
        val detail = _rentalDetail.value
        viewModelScope.launch {
            if (detail != null) {

                // 1. Cancelamos la renta enviando el objeto completo y el ID del auto
                detail.rental?.let { rental ->
                    cancelRentalUseCase(rental = rental, carId = detail.car.id.toInt())
                }

                // 2. Liberamos el auto forzando su estado a DISPONIBLE
                updateCarStatusUseCase(detail.car.id.toInt(), CarStatus.DISPONIBLE)
            }

            // 3. Limpiamos la pantalla
            _rentalDetail.value = null
            _selectedCar.value = null
        }
    }
    fun setCarMaintenanceStatus(carId: String, isGoingToWorkshop: Boolean) {
        viewModelScope.launch {
            val newStatus = if (isGoingToWorkshop) CarStatus.MANTENIMIENTO else CarStatus.DISPONIBLE
            updateCarStatusUseCase(carId.toInt(), newStatus)
        }
    }
}