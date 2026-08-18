package com.sv.elveloz.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.usecase.CalculateCostUseCase
import com.sv.elveloz.domain.usecase.CompleteRentalUseCase
import com.sv.elveloz.domain.usecase.GetActiveRentalUseCase
import com.sv.elveloz.domain.usecase.GetCarsUseCase
import com.sv.elveloz.domain.usecase.RentCarUseCase
import com.sv.elveloz.domain.usecase.UpdateCarStatusUseCase
import com.sv.elveloz.domain.usecase.CancelRentalUseCase
import com.sv.elveloz.domain.usecase.AprobarReservaUseCase
import com.sv.elveloz.domain.usecase.RechazarReservaUseCase
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
    private val cancelRentalUseCase: CancelRentalUseCase,
    private val aprobarReservaUseCase: AprobarReservaUseCase,
    private val rechazarReservaUseCase: RechazarReservaUseCase,
    private val getPendingRentalsUseCase: () -> Flow<List<RentalEntity>>,
    private val agregarVehiculoUseCase: com.sv.elveloz.domain.usecase.CasoUsoAgregarVehiculo
) : ViewModel() {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun onAgregarVehiculo(marca: String, modelo: String, precio: Double, imagenUrl: String, ubicacion: String) {
        viewModelScope.launch {
            val nuevoVehiculo = CarEntity(
                brand = marca,
                model = modelo,
                pricePerDay = precio,
                status = CarStatus.DISPONIBLE,
                imageResName = "car_placeholder",
                imageUrl = imagenUrl,
                location = ubicacion,
                rating = 5.0
            )
            agregarVehiculoUseCase(nuevoVehiculo)
        }
    }

    fun onMoverAMantenimiento() {
        val detail = _rentalDetail.value ?: return
        viewModelScope.launch {
            updateCarStatusUseCase(detail.car.id, CarStatus.MANTENIMIENTO)
            _rentalDetail.value = null
        }
    }

    var rolActual: RolUsuario = RolUsuario.CLIENTE
        set(value) {
            field = value
            if (value == RolUsuario.CLIENTE) {
                _filterStatus.value = "DISPONIBLE"
            } else {
                _filterStatus.value = "TODOS"
            }
        }

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

    // FILTRO ESTRICTO DE NOTIFICACIONES POR SESIÓN DE FIREBASE
    val pendingRentals: StateFlow<List<RentalEntity>> = getPendingRentalsUseCase()
        .map { listaGlobal ->
            if (rolActual == RolUsuario.RECEPCIONISTA) {
                listaGlobal
            } else {
                listaGlobal.filter { it.usuarioId == currentUserId }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rentalDetail: StateFlow<RentalDetailUiState?> = _rentalDetail

    fun onFilterChange(newFilter: String) {
        _filterStatus.value = newFilter
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCarClicked(car: CarEntity) {
        if (car.status == CarStatus.DISPONIBLE) {
            _selectedCar.value = car
        } else {
            viewModelScope.launch {
                val rental = getActiveRentalUseCase(car.id)
                _rentalDetail.value = RentalDetailUiState(car, rental)
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
                usuarioId = currentUserId, // ASIGNA EL ID DE FIREBASE
                customerName = customerName,
                pickupDateMs = pickupDateMs,
                returnDateMs = returnDateMs,
                totalCost = totalCost,
                estado = "SOLICITADA",
                notificada = false // LA RESERVA NACE COMO NO LEIDA
            )
            rentCarUseCase(rental, car.id)
            _selectedCar.value = null
        }
    }

    fun onApproveRental(rental: RentalEntity) {
        viewModelScope.launch {
            val updatedRental = rental.copy(estado = "APROBADA", isActive = true)
            aprobarReservaUseCase(updatedRental)
            updateCarStatusUseCase(rental.carId, CarStatus.EN_PROCESO)
        }
    }

    fun onRejectRental(rental: RentalEntity, carId: Int) {
        viewModelScope.launch {
            val updatedRental = rental.copy(estado = "RECHAZADA", isActive = false)
            rechazarReservaUseCase(updatedRental, carId)
            updateCarStatusUseCase(carId, CarStatus.DISPONIBLE)
        }
    }

    fun onMarkAsInUse() {
        val detail = _rentalDetail.value ?: return
        viewModelScope.launch {
            updateCarStatusUseCase(detail.car.id, CarStatus.EN_USO)
            _rentalDetail.value = null
        }
    }

    fun onCompleteRental() {
        val detail = _rentalDetail.value ?: return
        val rental = detail.rental
        viewModelScope.launch {
            if (rental != null) {
                completeRentalUseCase(rental, detail.car.id)
            } else {
                updateCarStatusUseCase(detail.car.id, CarStatus.DISPONIBLE)
            }
            _rentalDetail.value = null
        }
    }

    fun onCancelRental() {
        val detail = _rentalDetail.value ?: return
        val rental = detail.rental
        viewModelScope.launch {
            if (rental != null) {
                cancelRentalUseCase(rental, detail.car.id)
            } else {
                updateCarStatusUseCase(detail.car.id, CarStatus.DISPONIBLE)
            }
            _rentalDetail.value = null
        }
    }
}