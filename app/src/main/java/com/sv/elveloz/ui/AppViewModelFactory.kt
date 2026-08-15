package com.sv.elveloz.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sv.elveloz.data.local.AppDatabase
import com.sv.elveloz.data.repository.CarRepositoryImpl
import com.sv.elveloz.domain.usecase.CalculateCostUseCase
import com.sv.elveloz.domain.usecase.CancelRentalUseCase
import com.sv.elveloz.domain.usecase.CompleteRentalUseCase
import com.sv.elveloz.domain.usecase.GetActiveRentalUseCase
import com.sv.elveloz.domain.usecase.GetCarsUseCase
import com.sv.elveloz.domain.usecase.RentCarUseCase
import com.sv.elveloz.domain.usecase.UpdateCarStatusUseCase
import com.sv.elveloz.domain.usecase.AprobarReservaUseCase
import com.sv.elveloz.domain.usecase.RechazarReservaUseCase
import com.sv.elveloz.ui.catalog.CatalogViewModel
import com.sv.elveloz.ui.login.ViewModelLogin
import com.sv.elveloz.ui.login.ViewModelRegistro

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy { AppDatabase.getInstance(context) }
    private val repository by lazy {
        CarRepositoryImpl(database.carDao(), database.rentalDao())
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CatalogViewModel::class.java) -> {
                CatalogViewModel(
                    getCarsUseCase = GetCarsUseCase(repository),
                    rentCarUseCase = RentCarUseCase(repository),
                    calculateCostUseCase = CalculateCostUseCase(),
                    getActiveRentalUseCase = GetActiveRentalUseCase(repository),
                    updateCarStatusUseCase = UpdateCarStatusUseCase(repository),
                    completeRentalUseCase = CompleteRentalUseCase(repository),
                    cancelRentalUseCase = CancelRentalUseCase(repository),
                    aprobarReservaUseCase = AprobarReservaUseCase(repository),
                    rechazarReservaUseCase = RechazarReservaUseCase(repository),
                    getPendingRentalsUseCase = { repository.getPendingRentals() },
                    agregarVehiculoUseCase = com.sv.elveloz.domain.usecase.CasoUsoAgregarVehiculo(repository)
                ) as T
            }
            modelClass.isAssignableFrom(ViewModelLogin::class.java) -> {
                ViewModelLogin(database.usuarioDao()) as T
            }
            modelClass.isAssignableFrom(ViewModelRegistro::class.java) -> {
                ViewModelRegistro(database.usuarioDao()) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}