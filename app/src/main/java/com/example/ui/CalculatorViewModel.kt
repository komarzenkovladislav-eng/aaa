package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationEntity
import com.example.data.CalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(private val repository: CalculationRepository) : ViewModel() {

    private val evaluator = ExpressionEvaluator()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    val history: StateFlow<List<CalculationEntity>> = repository.allCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onDigit(digit: String) {
        val current = _expression.value
        if (current == "0") {
            _expression.value = digit
        } else {
            _expression.value = current + digit
        }
        updatePreview()
    }

    fun onDecimal() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "0."
            updatePreview()
            return
        }

        val lastNumberSegment = current.split("[+×÷\\-]".toRegex()).lastOrNull() ?: ""
        if (!lastNumberSegment.contains(".")) {
            _expression.value = current + "."
        }
    }

    fun onOperator(op: String) {
        val current = _expression.value
        if (current.isEmpty()) {
            if (op == "-") {
                _expression.value = "-"
            }
            return
        }

        val lastChar = current.last().toString()
        if (lastChar == "+" || lastChar == "-" || lastChar == "×" || lastChar == "÷") {
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value = current + op
        }
        updatePreview()
    }

    fun onPercentage() {
        val current = _expression.value
        if (current.isNotEmpty() && !current.endsWith("+") && !current.endsWith("-") && !current.endsWith("×") && !current.endsWith("÷")) {
            _expression.value = current + "%"
            updatePreview()
        }
    }

    fun onToggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "-"
            return
        }

        val operatorRegex = "[+×÷\\-]".toRegex()
        val matches = operatorRegex.findAll(current).toList()
        if (matches.isEmpty()) {
            if (current.startsWith("-")) {
                _expression.value = current.substring(1)
            } else {
                _expression.value = "-$current"
            }
        } else {
            val lastMatch = matches.last()
            val lastOpIndex = lastMatch.range.first
            val lastOp = lastMatch.value

            val prefix = current.substring(0, lastOpIndex)
            val suffix = current.substring(lastOpIndex + 1)

            if (suffix.isNotEmpty()) {
                if (lastOp == "-") {
                    val beforeOp = if (lastOpIndex > 0) current[lastOpIndex - 1].toString() else ""
                    val isUnaryMinus = lastOpIndex == 0 || beforeOp.matches(operatorRegex)
                    if (isUnaryMinus) {
                        _expression.value = prefix + suffix
                    } else {
                        _expression.value = "$prefix+-$suffix"
                    }
                } else if (lastOp == "+") {
                    _expression.value = "$prefix-$suffix"
                } else {
                    _expression.value = "$prefix$lastOp-$suffix"
                }
            }
        }
        updatePreview()
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = current.dropLast(1)
            updatePreview()
        }
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = ""
    }

    fun onCalculate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        val result = evaluator.evaluate(expr)
        if (result.isNotEmpty() && result != "Ошибка" && result != "Деление на 0") {
            viewModelScope.launch {
                repository.insert(expr, result)
            }
            _expression.value = result
            _previewResult.value = ""
        } else if (result == "Ошибка" || result == "Деление на 0") {
            _previewResult.value = result
        }
    }

    fun onHistoryItemClicked(item: CalculationEntity) {
        _expression.value = item.expression
        _previewResult.value = item.result
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun updatePreview() {
        val expr = _expression.value
        if (expr.isEmpty()) {
            _previewResult.value = ""
            return
        }
        val result = evaluator.evaluate(expr)
        if (result.isNotEmpty() && result != "Ошибка" && result != expr) {
            _previewResult.value = result
        } else {
            _previewResult.value = ""
        }
    }
}

class CalculatorViewModelFactory(private val repository: CalculationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
