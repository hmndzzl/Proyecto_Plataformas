package com.example.proyecto.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.domain.usecase.*
import com.example.proyecto.domain.usecase.preferences.*
import com.example.proyecto.presentation.admin.AdminViewModel
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

// Dashboard ViewModel Factory - ACTUALIZADO
class DashboardViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSpacesUseCase: GetSpacesUseCase,
    private val getReservationsForMonthUseCase: GetReservationsForMonthUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                getCurrentUserUseCase,
                getSpacesUseCase,
                getReservationsForMonthUseCase
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

// Profile ViewModel Factory - ACTUALIZADO
class ProfileViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                getCurrentUserUseCase,
                getUserReservationsUseCase,
                cancelReservationUseCase,
                logoutUseCase,
                getThemeUseCase,
                saveThemeUseCase,
                getLanguageUseCase,
                saveLanguageUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Admin ViewModel Factory
class AdminViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPendingReservationsUseCase: GetPendingReservationsUseCase,
    private val approveReservationUseCase: ApproveReservationUseCase,
    private val rejectReservationUseCase: RejectReservationUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(
                getCurrentUserUseCase,
                getPendingReservationsUseCase,
                approveReservationUseCase,
                rejectReservationUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}