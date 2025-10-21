package com.example.proyecto.presentation.reserve

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyecto.R
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveScreen(
    state: ReserveState,
    onEvent: (ReserveEvent) -> Unit,
    onBackClick: () -> Unit,
    onReserveSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onEvent(ReserveEvent.ErrorDismissed)
        }
    }

    LaunchedEffect(state.isReserveSuccessful) {
        if (state.isReserveSuccessful) {
            onReserveSuccess()
        }
    }

    // Success Dialog
    if (state.isReserveSuccessful) {
        AlertDialog(
            onDismissRequest = { onEvent(ReserveEvent.SuccessDismissed) },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorResource(R.color.success)
                )
            },
            title = { Text(stringResource(R.string.reserve_success_title)) },
            text = { Text(stringResource(R.string.reserve_success_message)) },
            confirmButton = {
                Button(onClick = { onEvent(ReserveEvent.SuccessDismissed) }) {
                    Text(stringResource(R.string.reserve_success_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.reserve_title,
                            state.space?.name ?: ""
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.nav_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.spacing_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Date Picker
            DatePickerField(
                label = stringResource(R.string.reserve_date_label),
                selectedDate = state.date,
                onDateSelected = { onEvent(ReserveEvent.DateChanged(it)) },
                enabled = !state.isLoading
            )

            // Time Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                TimePickerField(
                    label = stringResource(R.string.reserve_time_start_label),
                    selectedTime = state.startTime,
                    onTimeSelected = { onEvent(ReserveEvent.StartTimeChanged(it)) },
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                )

                TimePickerField(
                    label = stringResource(R.string.reserve_time_end_label),
                    selectedTime = state.endTime,
                    onTimeSelected = { onEvent(ReserveEvent.EndTimeChanged(it)) },
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }

            // Description Field
            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(ReserveEvent.DescriptionChanged(it)) },
                label = { Text(stringResource(R.string.reserve_description_label)) },
                placeholder = { Text(stringResource(R.string.reserve_description_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                enabled = !state.isLoading
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Confirm Button
            Button(
                onClick = { onEvent(ReserveEvent.ConfirmReservation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.button_height)),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.uvg_green)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.reserve_confirm_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.toString(),
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
            }
        }
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDays() * 86400000L
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val date = instant.toLocalDateTime(TimeZone.UTC).date
                            onDateSelected(date)
                        }
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Schedule, "Seleccionar hora")
            }
        }
    )

    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.hour ?: 8,
            initialMinute = selectedTime?.minute ?: 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val time = LocalTime(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(time)
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}