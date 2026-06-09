package com.example.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ExpressionEvaluator {

    private var pos = -1
    private var ch = -1
    private var str = ""

    fun evaluate(expression: String): String {
        if (expression.isBlank()) return ""
        try {
            var cleanedExpr = expression.trim()
            while (cleanedExpr.endsWith("+") || cleanedExpr.endsWith("-") || 
                   cleanedExpr.endsWith("×") || cleanedExpr.endsWith("÷")) {
                cleanedExpr = cleanedExpr.substring(0, cleanedExpr.length - 1).trim()
            }
            if (cleanedExpr.isEmpty()) return ""

            val sanitized = cleanedExpr
                .replace("×", "*")
                .replace("÷", "/")
            
            val result = parse(sanitized)
            if (result.isNaN()) return "Ошибка"
            if (result.isInfinite()) return "Деление на 0"

            return formatResult(result)
        } catch (e: ArithmeticException) {
            return "Деление на 0"
        } catch (e: Exception) {
            return "Ошибка"
        }
    }

    private fun formatResult(value: Double): String {
        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#.##########", symbols)
        val formatted = df.format(value)
        return if (formatted == "-0") "0" else formatted
    }

    @Synchronized
    private fun parse(expression: String): Double {
        str = expression
        pos = -1
        ch = -1
        nextChar()
        val rx = parseExpression()
        if (pos < str.length) throw IllegalArgumentException("Unexpected characters found")
        return rx
    }

    private fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor()
            else if (eat('/'.code)) {
                val divisor = parseFactor()
                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                x /= divisor
            } else return x
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = pos
        if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
            while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
            val numStr = str.substring(startPos, pos)
            x = if (numStr == ".") 0.0 else numStr.toDouble()
        } else {
            throw IllegalArgumentException("Unexpected character")
        }

        if (eat('%'.code)) {
            x /= 100.0
        }

        return x
    }
}
