package com.example.proyecto.presentation.availability

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
import com.example.proyecto.domain.model.SlotStatus
import com.example.proyecto.domain.model.TimeSlot
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    state: AvailabilityState,
    onEvent: (AvailabilityEvent) -> Unit,
    onBackClick: () -> Unit,
    onReserveClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onEvent(AvailabilityEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.space?.name ?: stringResource(R.string.availability_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.nav_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = dimensionResource(R.dimen.elevation_app_bar)
            ) {
                Button(
                    onClick = onReserveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.spacing_medium))
                        .height(dimensionResource(R.dimen.button_height)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.uvg_green)
                    )
                ) {
                    Icon(Icons.Default.AddCircle, null)
                    Spacer(Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    Text(stringResource(R.string.availability_reserve_button))
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.timeSlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Date Navigator
                DateNavigator(
                    currentDate = state.currentDate,
                    onPreviousDay = { onEvent(AvailabilityEvent.PreviousDay) },
                    onNextDay = { onEvent(AvailabilityEvent.NextDay) }
                )

                Divider()

                // Time Slots List
                if (state.timeSlots.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.status_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        items(state.timeSlots) { slot ->
                            TimeSlotItem(slot)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateNavigator(
    currentDate: kotlinx.datetime.LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Default.ChevronLeft, stringResource(R.string.availability_previous_day))
            }

            Text(
                text = formatDate(currentDate),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.ChevronRight, stringResource(R.string.availability_next_day))
            }
        }
    }
}

@Composable
fun TimeSlotItem(slot: TimeSlot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.reservation_item_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.reservation_item_padding)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Indicator
                Surface(
                    modifier = Modifier.size(dimensionResource(R.dimen.reservation_status_indicator_size)),
                    shape = CircleShape,
                    color = when (slot.status) {
                        SlotStatus.AVAILABLE -> colorResource(R.color.status_available)
                        SlotStatus.RESERVED -> colorResource(R.color.status_reserved)
                        SlotStatus.PENDING_APPROVAL -> colorResource(R.color.status_pending)
                        SlotStatus.BLOCKED -> colorResource(R.color.calendar_inactive)
                    }
                ) {}

                Column {
                    Text(
                        text = stringResource(
                            R.string.time_slot_format,
                            slot.startTime.toString(),
                            slot.endTime.toString()
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (slot.status != SlotStatus.AVAILABLE && slot.reservedByName != null) {
                        Text(
                            text = stringResource(
                                R.string.availability_reserved_by,
                                slot.reservedByName
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                color = when (slot.status) {
                    SlotStatus.AVAILABLE -> colorResource(R.color.status_available_bg)
                    SlotStatus.RESERVED -> colorResource(R.color.status_reserved_bg)
                    SlotStatus.PENDING_APPROVAL -> colorResource(R.color.status_pending_bg)
                    SlotStatus.BLOCKED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Text(
                    text = when (slot.status) {
                        SlotStatus.AVAILABLE -> stringResource(R.string.availability_available)
                        SlotStatus.RESERVED -> stringResource(R.string.availability_reserved)
                        SlotStatus.PENDING_APPROVAL -> "Pendiente"
                        SlotStatus.BLOCKED -> "Bloqueado"
                    },
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.spacing_small),
                        vertical = 4.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (slot.status) {
                        SlotStatus.AVAILABLE -> colorResource(R.color.status_available)
                        SlotStatus.RESERVED -> colorResource(R.color.status_reserved)
                        SlotStatus.PENDING_APPROVAL -> colorResource(R.color.status_pending)
                        SlotStatus.BLOCKED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun formatDate(date: kotlinx.datetime.LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern(stringResource(R.string.availability_date_format), Locale.getDefault())
    return date.toJavaLocalDate().format(formatter)
}