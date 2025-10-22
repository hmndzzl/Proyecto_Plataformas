package com.example.proyecto.presentation.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto.data.local.datastore.UserPreferencesDataStore
import com.example.proyecto.data.local.AppDatabase
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.data.repository.SpaceRepository
import com.example.proyecto.domain.usecase.*
import com.example.proyecto.domain.usecase.preferences.*
import com.example.proyecto.presentation.admin.AdminScreen
import com.example.proyecto.presentation.admin.AdminViewModel
import com.example.proyecto.presentation.availability.AvailabilityScreen
import com.example.proyecto.presentation.availability.AvailabilityViewModel
import com.example.proyecto.presentation.dashboard.DashboardScreen
import com.example.proyecto.presentation.dashboard.DashboardViewModel
import com.example.proyecto.presentation.day_reservations.DayReservationsScreen
import com.example.proyecto.presentation.login.LoginScreen
import com.example.proyecto.presentation.login.LoginViewModel
import com.example.proyecto.presentation.profile.ProfileScreen
import com.example.proyecto.presentation.profile.ProfileViewModel
import com.example.proyecto.presentation.reserve.ReserveScreen
import com.example.proyecto.presentation.reserve.ReserveViewModel
import kotlinx.datetime.LocalDate

// Navigation Routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Availability : Screen("availability/{spaceId}") {
        fun createRoute(spaceId: String) = "availability/$spaceId"
    }
    object Reserve : Screen("reserve/{spaceId}") {
        fun createRoute(spaceId: String) = "reserve/$spaceId"
    }
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object DayReservations : Screen("day_reservations/{date}") {
        fun createRoute(date: LocalDate) = "day_reservations/${date}"
    }
}

@Composable
fun AppNavigation(
    database: AppDatabase,
    preferencesDataStore: UserPreferencesDataStore,
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()

    // Initialize repositories
    val authRepository = remember { AuthRepository(database) }
    val spaceRepository = remember { SpaceRepository(database) }

    // Initialize use cases
    val useCases = remember {
        UseCases(
            loginUseCase = LoginUseCase(authRepository),
            registerUseCase = RegisterUseCase(authRepository),
            logoutUseCase = LogoutUseCase(authRepository),
            getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
            observeAuthStateUseCase = ObserveAuthStateUseCase(authRepository),
            getSpacesUseCase = GetSpacesUseCase(spaceRepository),
            getSpaceByIdUseCase = GetSpaceByIdUseCase(spaceRepository),
            getTimeSlotsUseCase = GetTimeSlotsUseCase(spaceRepository),
            createReservationUseCase = CreateReservationUseCase(spaceRepository),
            getUserReservationsUseCase = GetUserReservationsUseCase(spaceRepository),
            cancelReservationUseCase = CancelReservationUseCase(spaceRepository),
            getPendingReservationsUseCase = GetPendingReservationsUseCase(spaceRepository),
            approveReservationUseCase = ApproveReservationUseCase(spaceRepository),
            rejectReservationUseCase = RejectReservationUseCase(spaceRepository),
            getReservationsForMonthUseCase = GetReservationsForMonthUseCase(spaceRepository),
            validateEmailUseCase = ValidateEmailUseCase(),
            validatePasswordUseCase = ValidatePasswordUseCase(),
            validateTimeSlotUseCase = ValidateTimeSlotUseCase(),
            getThemeUseCase = GetThemeUseCase(preferencesDataStore),
            saveThemeUseCase = SaveThemeUseCase(preferencesDataStore),
            getLanguageUseCase = GetLanguageUseCase(preferencesDataStore),
            saveLanguageUseCase = SaveLanguageUseCase(preferencesDataStore)
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(
                    useCases.loginUseCase,
                    useCases.validateEmailUseCase,
                    useCases.validatePasswordUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            LoginScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(
                    useCases.getCurrentUserUseCase,
                    useCases.getSpacesUseCase,
                    useCases.getReservationsForMonthUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            DashboardScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onSpaceClick = { spaceId ->
                    navController.navigate(Screen.Availability.createRoute(spaceId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onAdminClick = {
                    navController.navigate(Screen.Admin.route)
                },
                onDayClick = { date ->
                    navController.navigate(Screen.DayReservations.createRoute(date))
                }
            )
        }

        // Day Reservations Screen
        composable(
            route = Screen.DayReservations.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date") ?: ""
            val date = LocalDate.parse(dateString)

            // Get reservations from dashboard state
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Dashboard.route)
            }
            val dashboardViewModel: DashboardViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = DashboardViewModelFactory(
                    useCases.getCurrentUserUseCase,
                    useCases.getSpacesUseCase,
                    useCases.getReservationsForMonthUseCase
                )
            )

            val dashboardState by dashboardViewModel.state.collectAsState()
            val dayReservations = dashboardState.monthReservations.filter { it.date == date }

            DayReservationsScreen(
                date = date,
                reservations = dayReservations,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Availability Screen
        composable(
            route = Screen.Availability.route,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: ""

            val viewModel: AvailabilityViewModel = viewModel(
                factory = AvailabilityViewModelFactory(
                    useCases.getSpaceByIdUseCase,
                    useCases.getTimeSlotsUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            LaunchedEffect(spaceId) {
                viewModel.onEvent(
                    com.example.proyecto.presentation.availability.AvailabilityEvent.LoadAvailability(spaceId)
                )
            }

            AvailabilityScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onReserveClick = {
                    navController.navigate(Screen.Reserve.createRoute(spaceId))
                }
            )
        }

        // Reserve Screen
        composable(
            route = Screen.Reserve.route,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: ""

            val viewModel: ReserveViewModel = viewModel(
                factory = ReserveViewModelFactory(
                    useCases.getSpaceByIdUseCase,
                    useCases.getCurrentUserUseCase,
                    useCases.createReservationUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            LaunchedEffect(spaceId) {
                viewModel.onEvent(
                    com.example.proyecto.presentation.reserve.ReserveEvent.Initialize(spaceId)
                )
            }

            ReserveScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onReserveSuccess = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    useCases.getCurrentUserUseCase,
                    useCases.getUserReservationsUseCase,
                    useCases.cancelReservationUseCase,
                    useCases.logoutUseCase,
                    useCases.getThemeUseCase,
                    useCases.saveThemeUseCase,
                    useCases.getLanguageUseCase,
                    useCases.saveLanguageUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            ProfileScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Screen
        composable(Screen.Admin.route) {
            val viewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(
                    useCases.getCurrentUserUseCase,
                    useCases.getPendingReservationsUseCase,
                    useCases.approveReservationUseCase,
                    useCases.rejectReservationUseCase
                )
            )

            val state by viewModel.state.collectAsState()

            AdminScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// Data class to hold all use cases
data class UseCases(
    val loginUseCase: LoginUseCase,
    val registerUseCase: RegisterUseCase,
    val logoutUseCase: LogoutUseCase,
    val getCurrentUserUseCase: GetCurrentUserUseCase,
    val observeAuthStateUseCase: ObserveAuthStateUseCase,
    val getSpacesUseCase: GetSpacesUseCase,
    val getSpaceByIdUseCase: GetSpaceByIdUseCase,
    val getTimeSlotsUseCase: GetTimeSlotsUseCase,
    val createReservationUseCase: CreateReservationUseCase,
    val getUserReservationsUseCase: GetUserReservationsUseCase,
    val cancelReservationUseCase: CancelReservationUseCase,
    val getPendingReservationsUseCase: GetPendingReservationsUseCase,
    val approveReservationUseCase: ApproveReservationUseCase,
    val rejectReservationUseCase: RejectReservationUseCase,
    val getReservationsForMonthUseCase: GetReservationsForMonthUseCase,
    val validateEmailUseCase: ValidateEmailUseCase,
    val validatePasswordUseCase: ValidatePasswordUseCase,
    val validateTimeSlotUseCase: ValidateTimeSlotUseCase,
    val getThemeUseCase: GetThemeUseCase,
    val saveThemeUseCase: SaveThemeUseCase,
    val getLanguageUseCase: GetLanguageUseCase,
    val saveLanguageUseCase: SaveLanguageUseCase
)