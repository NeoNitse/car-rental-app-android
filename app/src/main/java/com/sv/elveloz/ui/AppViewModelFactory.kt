package com.sv.elveloz.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sv.elveloz.data.local.AppDatabase
import com.sv.elveloz.data.repository.CarRepositoryFirebaseImpl
import com.sv.elveloz.domain.usecase.CalculateCostUseCase
import com.sv.elveloz.domain.usecase.CancelRentalUseCase
import com.sv.elveloz.domain.usecase.CompleteRentalUseCase
import com.sv.elveloz.domain.usecase.GetActiveRentalUseCase
import com.sv.elveloz.domain.usecase.GetCarsUseCase
import com.sv.elveloz.domain.usecase.RentCarUseCase
import com.sv.elveloz.domain.usecase.UpdateCarStatusUseCase
import com.sv.elveloz.domain.usecase.AprobarReservaUseCase
import com.sv.elveloz.domain.usecase.RechazarReservaUseCase
import com.sv.elveloz.domain.usecase.CasoUsoAgregarVehiculo
import com.sv.elveloz.ui.catalog.CatalogViewModel
import com.sv.elveloz.ui.login.ViewModelLogin
import com.sv.elveloz.ui.login.ViewModelRegistro

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy { AppDatabase.getInstance(context) }

    // Aquí hacemos el cambio para conectarnos a Firebase
    private val repository by lazy {
        CarRepositoryFirebaseImpl()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
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
                        agregarVehiculoUseCase = CasoUsoAgregarVehiculo(repository)
                    ) as T
                }
                modelClass.isAssignableFrom(ViewModelLogin::class.java) -> {
                    ViewModelLogin() as T
                }
                modelClass.isAssignableFrom(ViewModelRegistro::class.java) -> {
                    ViewModelRegistro() as T
                }
                else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        } catch (e: Exception) {
            // Esta es la trampa. Si algo explota, lo capturamos y lo obligamos a imprimirse.
            android.util.Log.e("ERROR_VELOZ", "¡AQUÍ ESTÁ LA AUTOPSIA!", e)
            throw e
        }
    }
}