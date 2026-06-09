package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Fetch global calculation repository from Application instance
        val app = application as CalculatorApplication
        val repository = app.repository
        
        // Setup view model utilizing custom factory
        val factory = CalculatorViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                CalculatorScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
