package com.sv.elveloz.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sv.elveloz.data.repository.CarRepositoryFirebaseImpl
import com.sv.elveloz.domain.usecase.CalculateCostUseCase
import com.sv.elveloz.domain.usecase.CancelRentalUseCase
import com.sv.elveloz.domain.usecase.CompleteRentalUseCase
import com.sv.elveloz.domain.usecase.GetActiveRentalUseCase
import com.sv.elveloz.domain.usecase.GetCarsUseCase
import com.sv.elveloz.domain.usecase.RentCarUseCase
import com.sv.elveloz.domain.usecase.UpdateCarStatusUseCase
import com.sv.elveloz.ui.catalog.CatalogViewModel

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val repository = CarRepositoryFirebaseImpl()

    @Suppress("UNCHECKED_CAST")
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
                    cancelRentalUseCase = CancelRentalUseCase(repository)
                ) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}