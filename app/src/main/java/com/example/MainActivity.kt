package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.LiquidationDatabase
import com.example.data.LiquidationRepository
import com.example.ui.LiquidationViewModel
import com.example.ui.LiquidationViewModelFactory
import com.example.ui.MainView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support custom status bar edge-to-edge drawing
        enableEdgeToEdge()

        // Initialize SQLite Room database, registering lifecycleScope for seeding
        val database = LiquidationDatabase.getDatabase(this, lifecycleScope)
        val repository = LiquidationRepository(database.liquidationDao())
        
        // Simple and robust constructor injection for the MVVM architecture
        val viewModelFactory = LiquidationViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[LiquidationViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainView(viewModel = viewModel)
                }
            }
        }
    }
}
