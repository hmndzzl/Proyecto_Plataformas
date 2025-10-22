package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.proyecto.data.local.datastore.UserPreferencesDataStore
import com.example.proyecto.data.local.AppDatabase
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.presentation.navigation.AppNavigation
import com.example.proyecto.presentation.navigation.Screen
import com.example.proyecto.ui.theme.UVGEspaciosTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var preferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = AppDatabase.getInstance(applicationContext)
        authRepository = AuthRepository(database)
        preferencesDataStore = UserPreferencesDataStore(applicationContext)

        enableEdgeToEdge()

        setContent {
            // Observe theme and language preferences
            val isDarkTheme by preferencesDataStore.isDarkTheme.collectAsState(initial = false)
            val language by preferencesDataStore.language.collectAsState(initial = "es")

            // Update app locale
            LaunchedEffect(language) {
                updateLocale(language)
            }

            UVGEspaciosTheme(darkTheme = isDarkTheme) {
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
                        preferencesDataStore = preferencesDataStore,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}