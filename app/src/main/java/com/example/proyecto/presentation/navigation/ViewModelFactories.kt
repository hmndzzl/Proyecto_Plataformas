package com.example.proyecto.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.domain.usecase.*
import com.example.proyecto.presentation.login.LoginViewModel
import com.example.proyecto.presentation.dashboard.DashboardViewModel
import com.example.proyecto.presentation.availability.AvailabilityViewModel
import com.example.proyecto.presentation.reserve.ReserveViewModel
import com.example.proyecto.presentation.profile.ProfileViewModel

// Login ViewModel Factory
class LoginViewModelFactory(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginUseCase,
                validateEmailUseCase,
                validatePasswordUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Dashboard ViewModel Factory
class DashboardViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSpacesUseCase: GetSpacesUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                getCurrentUserUseCase,
                getSpacesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Availability ViewModel Factory
class AvailabilityViewModelFactory(
    private val getSpaceByIdUseCase: GetSpaceByIdUseCase,
    private val getTimeSlotsUseCase: GetTimeSlotsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AvailabilityViewModel::class.java)) {
            return AvailabilityViewModel(
                getSpaceByIdUseCase,
                getTimeSlotsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Reserve ViewModel Factory
class ReserveViewModelFactory(
    private val getSpaceByIdUseCase: GetSpaceByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createReservationUseCase: CreateReservationUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReserveViewModel::class.java)) {
            return ReserveViewModel(
                getSpaceByIdUseCase,
                getCurrentUserUseCase,
                createReservationUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Profile ViewModel Factory
class ProfileViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                getCurrentUserUseCase,
                getUserReservationsUseCase,
                cancelReservationUseCase,
                logoutUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}