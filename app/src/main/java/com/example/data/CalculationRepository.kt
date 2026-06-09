package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val allCalculations: Flow<List<CalculationEntity>> = calculationDao.getAllCalculations()

    suspend fun insert(expression: String, result: String) {
        calculationDao.insertCalculation(
            CalculationEntity(expression = expression, result = result)
        )
    }

    suspend fun deleteById(id: Long) {
        calculationDao.deleteCalculationById(id)
    }

    suspend fun clearAll() {
        calculationDao.clearAllCalculations()
    }
}
