package com.sv.elveloz.domain.usecase

class CalculateCostUseCase {
    operator fun invoke(pickupDateMs: Long, returnDateMs: Long, pricePerDay: Double): Result<Double> {
        if (returnDateMs < pickupDateMs) {
            return Result.failure(IllegalArgumentException("La fecha de entrega no puede ser anterior a la fecha de recogida"))
        }
        val diffMs = returnDateMs - pickupDateMs
        val days = (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        val totalCost = days * pricePerDay
        return Result.success(totalCost)
    }
}