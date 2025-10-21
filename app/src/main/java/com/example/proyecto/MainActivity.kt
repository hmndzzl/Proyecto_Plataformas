package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.proyecto.data.local.AppDatabase
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.presentation.navigation.AppNavigation
import com.example.proyecto.presentation.navigation.Screen
import com.example.proyecto.ui.theme.UVGEspaciosTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = AppDatabase.getInstance(applicationContext)
        authRepository = AuthRepository(database)

        enableEdgeToEdge()

        setContent {
            UVGEspaciosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Determine start destination based on auth state
                    val startDestination = if (authRepository.isLoggedIn()) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    AppNavigation(
                        database = database,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}