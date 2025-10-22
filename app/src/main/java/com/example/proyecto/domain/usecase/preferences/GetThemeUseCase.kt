package com.example.proyecto.domain.usecase.preferences

import com.example.proyecto.data.local.datastore.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow

// Theme Use Cases
class GetThemeUseCase(private val preferencesDataStore: UserPreferencesDataStore) {
    operator fun invoke(): Flow<Boolean> {
        return preferencesDataStore.isDarkTheme
    }
}

class SaveThemeUseCase(private val preferencesDataStore: UserPreferencesDataStore) {
    suspend operator fun invoke(isDark: Boolean) {
        preferencesDataStore.setDarkTheme(isDark)
    }
}

// Language Use Cases
class GetLanguageUseCase(private val preferencesDataStore: UserPreferencesDataStore) {
    operator fun invoke(): Flow<String> {
        return preferencesDataStore.language
    }
}

class SaveLanguageUseCase(private val preferencesDataStore: UserPreferencesDataStore) {
    suspend operator fun invoke(languageCode: String) {
        preferencesDataStore.setLanguage(languageCode)
    }
}