package com.example.proyecto.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyecto.R
import com.example.proyecto.domain.model.Reservation
import com.example.proyecto.domain.model.ReservationStatus
import com.example.proyecto.domain.model.SpaceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onEvent(ProfileEvent.ErrorDismissed)
        }
    }

    LaunchedEffect(state.isLogoutSuccessful) {
        if (state.isLogoutSuccessful) {
            onLogoutSuccess()
        }
    }

    // Logout Confirmation Dialog
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileEvent.LogoutCancelled) },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = colorResource(R.color.error)
                )
            },
            title = { Text(stringResource(R.string.profile_logout_button)) },
            text = { Text(stringResource(R.string.profile_logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = { onEvent(ProfileEvent.LogoutConfirmed) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.error)
                    )
                ) {
                    Text(stringResource(R.string.profile_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileEvent.LogoutCancelled) }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.nav_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                // User Header
                item {
                    UserHeader(
                        name = state.user?.name ?: "",
                        email = state.user?.email ?: "",
                        reservationCount = state.reservations.size
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.profile_settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_medium))
                    )
                }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_medium)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (state.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = colorResource(R.color.uvg_green)
                                )
                                Text(
                                    text = stringResource(R.string.profile_theme_label),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Switch(
                                checked = state.isDarkTheme,
                                onCheckedChange = { onEvent(ProfileEvent.ThemeChanged(it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorResource(R.color.uvg_green),
                                    checkedTrackColor = colorResource(R.color.uvg_green).copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_medium))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    tint = colorResource(R.color.uvg_green)
                                )
                                Text(
                                    text = stringResource(R.string.profile_language_label),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                            ) {
                                // Spanish Button
                                FilterChip(
                                    selected = state.currentLanguage == "es",
                                    onClick = { onEvent(ProfileEvent.LanguageChanged("es")) },
                                    label = { Text(stringResource(R.string.profile_language_spanish)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorResource(R.color.uvg_green),
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                // English Button
                                FilterChip(
                                    selected = state.currentLanguage == "en",
                                    onClick = { onEvent(ProfileEvent.LanguageChanged("en")) },
                                    label = { Text(stringResource(R.string.profile_language_english)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorResource(R.color.uvg_green),
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }

                // Reservations Section
                item {
                    Text(
                        text = stringResource(R.string.profile_history_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_medium))
                    )
                }

                if (state.reservations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.spacing_xl)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_no_reservations),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(state.reservations) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onCancelClick = {
                                if (reservation.status == ReservationStatus.PENDING ||
                                    reservation.status == ReservationStatus.APPROVED) {
                                    onEvent(ProfileEvent.CancelReservation(reservation.id))
                                }
                            }
                        )
                    }
                }

                // Logout Button
                item {
                    Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    OutlinedButton(
                        onClick = { onEvent(ProfileEvent.LogoutClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.button_height)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(R.color.error)
                        )
                    ) {
                        Icon(Icons.Default.Logout, null)
                        Spacer(Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(stringResource(R.string.profile_logout_button))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        content()
    }
}

@Composable
fun UserHeader(
    name: String,
    email: String,
    reservationCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.uvg_green)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_xl)),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large)),
                        tint = colorResource(R.color.uvg_green)
                    )
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.surface
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Surface(
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$reservationCount reservas",
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.spacing_medium),
                        vertical = dimensionResource(R.dimen.spacing_small)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.reservation_item_padding))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(dimensionResource(R.dimen.reservation_status_indicator_size)),
                        shape = CircleShape,
                        color = when (reservation.spaceType) {
                            SpaceType.CANCHA -> colorResource(R.color.uvg_blue)
                            SpaceType.GARDEN -> colorResource(R.color.uvg_green)
                        }
                    ) {}

                    Text(
                        text = reservation.spaceName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    color = when (reservation.status) {
                        ReservationStatus.PENDING -> colorResource(R.color.status_pending_bg)
                        ReservationStatus.APPROVED -> colorResource(R.color.status_available_bg)
                        ReservationStatus.REJECTED -> colorResource(R.color.status_reserved_bg)
                        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                        ReservationStatus.COMPLETED -> colorResource(R.color.info_bg)
                    }
                ) {
                    Text(
                        text = when (reservation.status) {
                            ReservationStatus.PENDING -> stringResource(R.string.reservation_status_pending)
                            ReservationStatus.APPROVED -> stringResource(R.string.reservation_status_approved)
                            ReservationStatus.REJECTED -> stringResource(R.string.reservation_status_rejected)
                            ReservationStatus.CANCELLED -> stringResource(R.string.reservation_status_cancelled)
                            ReservationStatus.COMPLETED -> stringResource(R.string.reservation_status_completed)
                        },
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.spacing_small),
                            vertical = 4.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (reservation.status) {
                            ReservationStatus.PENDING -> colorResource(R.color.status_pending)
                            ReservationStatus.APPROVED -> colorResource(R.color.status_available)
                            ReservationStatus.REJECTED -> colorResource(R.color.status_reserved)
                            ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                            ReservationStatus.COMPLETED -> colorResource(R.color.info)
                        }
                    )
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xs))
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = reservation.date.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xs))
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                R.string.time_slot_format,
                                reservation.startTime.toString(),
                                reservation.endTime.toString()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (reservation.status == ReservationStatus.PENDING ||
                    reservation.status == ReservationStatus.APPROVED) {
                    TextButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(R.color.error)
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            if (reservation.description.isNotBlank()) {
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xs)))
                Text(
                    text = reservation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}