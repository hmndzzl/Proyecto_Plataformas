package com.example.proyecto.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto.R
import com.example.proyecto.domain.model.SpaceType
import kotlinx.datetime.Month


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onEvent: (DashboardEvent) -> Unit,
    onSpaceClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            onEvent(DashboardEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.dashboard_greeting,
                            state.user?.name ?: ""
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, "Perfil")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.spaces.isEmpty()) {
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
                    .padding(dimensionResource(R.dimen.spacing_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
            ) {
                // Space Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                ) {
                    state.spaces.forEach { space ->
                        SpaceCard(
                            name = when (space.type) {
                                SpaceType.CANCHA -> stringResource(R.string.dashboard_cancha)
                                SpaceType.GARDEN -> stringResource(R.string.dashboard_garden)
                            },
                            type = space.type,
                            onClick = { onSpaceClick(space.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Calendar Section
                CalendarSection(
                    currentMonth = state.currentMonth,
                    calendarDays = state.calendarDays,
                    onPreviousMonth = { onEvent(DashboardEvent.PreviousMonth) },
                    onNextMonth = { onEvent(DashboardEvent.NextMonth) }
                )
            }
        }
    }
}

@Composable
fun SpaceCard(
    name: String,
    type: SpaceType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                SpaceType.CANCHA -> colorResource(R.color.uvg_blue)
                SpaceType.GARDEN -> colorResource(R.color.uvg_green)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (type) {
                    SpaceType.CANCHA -> Icons.Default.SportsSoccer
                    SpaceType.GARDEN -> Icons.Default.Park
                },
                contentDescription = name,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large)),
                tint = Color.White
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun CalendarSection(
    currentMonth: kotlinx.datetime.LocalDate,
    calendarDays: List<com.example.proyecto.domain.model.CalendarDay>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.surface_variant_light)
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = stringResource(R.string.dashboard_calendar_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
            )

            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, "Mes anterior")
                }

                Text(
                    text = "${getMonthName(currentMonth.month)} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, "Siguiente mes")
                }
            }

            // Days of week headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    stringResource(R.string.calendar_monday),
                    stringResource(R.string.calendar_tuesday),
                    stringResource(R.string.calendar_wednesday),
                    stringResource(R.string.calendar_thursday),
                    stringResource(R.string.calendar_friday),
                    stringResource(R.string.calendar_saturday),
                    stringResource(R.string.calendar_sunday)
                ).forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(250.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarDays) { day ->
                    CalendarDayItem(day)
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(day: com.example.proyecto.domain.model.CalendarDay) {
    val backgroundColor = when {
        !day.isAvailable -> Color.Transparent
        day.isToday -> colorResource(R.color.calendar_today)
        day.hasReservations -> colorResource(R.color.status_reserved_bg)
        else -> colorResource(R.color.status_available_bg)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(dimensionResource(R.dimen.calendar_day_corner_radius)))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (day.isAvailable)
                MaterialTheme.colorScheme.onSurface
            else
                colorResource(R.color.calendar_inactive)
        )
    }
}

@Composable
private fun getMonthName(month: Month): String {
    return when (month) {
        Month.JANUARY -> "Enero"
        Month.FEBRUARY -> "Febrero"
        Month.MARCH -> "Marzo"
        Month.APRIL -> "Abril"
        Month.MAY -> "Mayo"
        Month.JUNE -> "Junio"
        Month.JULY -> "Julio"
        Month.AUGUST -> "Agosto"
        Month.SEPTEMBER -> "Septiembre"
        Month.OCTOBER -> "Octubre"
        Month.NOVEMBER -> "Noviembre"
        Month.DECEMBER -> "Diciembre"
    }
}