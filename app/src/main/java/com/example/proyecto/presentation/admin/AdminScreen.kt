package com.example.proyecto.presentation.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyecto.R
import com.example.proyecto.domain.model.Reservation
import com.example.proyecto.domain.model.SpaceType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    state: AdminState,
    onEvent: (AdminEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onEvent(AdminEvent.ErrorDismissed)
        }
    }

    // Approve Confirmation Dialog
    if (state.showApproveDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(AdminEvent.DialogDismissed) },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorResource(R.color.status_available)
                )
            },
            title = { Text(stringResource(R.string.admin_approve_title)) },
            text = {
                Text(stringResource(R.string.admin_approve_message))
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(AdminEvent.ApproveConfirmed) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.status_available)
                    )
                ) {
                    Text(stringResource(R.string.admin_approve))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AdminEvent.DialogDismissed) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Reject Confirmation Dialog
    if (state.showRejectDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(AdminEvent.DialogDismissed) },
            icon = {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = colorResource(R.color.error)
                )
            },
            title = { Text(stringResource(R.string.admin_reject_title)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    Text(stringResource(R.string.admin_reject_message))
                    OutlinedTextField(
                        value = state.rejectionReason,
                        onValueChange = { onEvent(AdminEvent.RejectionReasonChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.admin_rejection_reason_hint)) },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(AdminEvent.RejectConfirmed) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.error)
                    ),
                    enabled = state.rejectionReason.isNotBlank()
                ) {
                    Text(stringResource(R.string.admin_reject))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AdminEvent.DialogDismissed) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.pendingReservations.isEmpty()) {
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
                // Admin Header
                item {
                    AdminHeader(
                        name = state.user?.name ?: "",
                        role = state.user?.role?.name ?: "",
                        pendingCount = state.pendingReservations.size
                    )
                }

                // Pending Reservations Section
                item {
                    Text(
                        text = stringResource(R.string.admin_pending_reservations),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_medium))
                    )
                }

                if (state.pendingReservations.isEmpty()) {
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
                                    text = stringResource(R.string.admin_no_pending),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(state.pendingReservations) { reservation ->
                        AdminReservationCard(
                            reservation = reservation,
                            isSelected = state.selectedReservation?.id == reservation.id,
                            onReservationClick = {
                                onEvent(AdminEvent.ReservationClicked(reservation))
                            },
                            onApproveClick = {
                                onEvent(AdminEvent.ReservationClicked(reservation))
                                onEvent(AdminEvent.ApproveClicked)
                            },
                            onRejectClick = {
                                onEvent(AdminEvent.ReservationClicked(reservation))
                                onEvent(AdminEvent.RejectClicked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHeader(
    name: String,
    role: String,
    pendingCount: Int
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
                        Icons.Default.AdminPanelSettings,
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
                text = role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Surface(
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Text(
                    text = stringResource(R.string.admin_pending_count, pendingCount),
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
fun AdminReservationCard(
    reservation: Reservation,
    isSelected: Boolean,
    onReservationClick: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReservationClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                colorResource(R.color.uvg_green).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, colorResource(R.color.uvg_green))
        } else null
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
                    color = colorResource(R.color.status_pending_bg)
                ) {
                    Text(
                        text = stringResource(R.string.reservation_status_pending),
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.spacing_small),
                            vertical = 4.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.status_pending)
                    )
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

            // User info
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xs)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = reservation.userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

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
                            text = "${reservation.startTime} - ${reservation.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.error)
                    )
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(dimensionResource(R.dimen.spacing_xs)))
                    Text(stringResource(R.string.admin_reject))
                }

                Button(
                    onClick = onApproveClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.status_available)
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(dimensionResource(R.dimen.spacing_xs)))
                    Text(stringResource(R.string.admin_approve))
                }
            }
        }
    }
}