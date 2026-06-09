package com.example

import android.app.Application
import com.example.data.CalculatorDatabase
import com.example.data.CalculationRepository

class CalculatorApplication : Application() {
    val database by lazy { CalculatorDatabase.getDatabase(this) }
    val repository by lazy { CalculationRepository(database.calculationDao()) }
}
