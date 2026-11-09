package com.example.spritradar.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.spritradar.data.SpritRadarRepository
import com.example.spritradar.data.UserPreferencesRepository
import com.example.spritradar.model.DetourOption
import com.example.spritradar.model.FavoriteRoute
import com.example.spritradar.model.FuelType
import com.example.spritradar.model.GeoPoint
import com.example.spritradar.model.RouteSearchResult
import com.example.spritradar.model.SortMode
import com.example.spritradar.model.StationEvaluation
import com.example.spritradar.ui.components.InteractiveRouteMap
import com.example.spritradar.ui.components.PriceAmpelCard
import com.example.spritradar.ui.components.SpritRadarLogoIcon
import com.example.spritradar.ui.components.StationCard
import com.example.spritradar.ui.theme.SpritRadarTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SpritRadarViewModel = viewModel {
        SpritRadarViewModel(
            repository = SpritRadarRepository(),
            prefsRepository = UserPreferencesRepository(context.applicationContext)
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            resolveDeviceLocation(context)?.let { (lat, lng) ->
                viewModel.setGpsStartLocation(lat, lng, "Mein Standort (GPS)")
            } ?: viewModel.setGpsStartLocation(48.1351, 11.5820, "Mein Standort (München)")
        } else {
            viewModel.setGpsStartLocation(48.1351, 11.5820, "Mein Standort (Standard)")
        }
    }

    SpritRadarContent(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onStartInputChange = viewModel::updateStartInput,
        onSelectStartSuggestion = viewModel::selectStartSuggestion,
        onDestInputChange = viewModel::updateDestInput,
        onSelectDestSuggestion = viewModel::selectDestSuggestion,
        onSwapLocations = viewModel::swapStartAndDestination,
        onUseGpsLocation = {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                resolveDeviceLocation(context)?.let { (lat, lng) ->
                    viewModel.setGpsStartLocation(lat, lng, "Mein Standort (GPS)")
                } ?: viewModel.setGpsStartLocation(48.1351, 11.5820, "Mein Standort (GPS)")
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onSearchRoute = { viewModel.performRouteSearch(switchToResultsTab = true) },
        onRefreshPrices = { viewModel.refreshLivePrices(isAutoRefresh = false) },
        onDismissSearchMessage = viewModel::dismissSearchCompletedMessage,
        onSelectFavorite = viewModel::selectFavoriteRoute,
        onSaveFavorite = viewModel::saveCurrentRouteAsFavorite,
        onDeleteFavorite = viewModel::deleteFavoriteRoute,
        onSelectDetour = viewModel::selectDetourOption,
        onSelectFuel = viewModel::selectFuelType,
        onConsumptionChange = viewModel::updateConsumption,
        onTankAmountChange = viewModel::updateTankAmount,
        onSelectSortMode = viewModel::selectSortMode,
        onToggleCalculator = viewModel::toggleCalculatorSliders,
        onSelectStation = viewModel::selectStation,
        onSelectSimulatedHour = viewModel::updateSimulatedHour,
        onNavigateStation = { evaluation ->
            launchGoogleMapsNavigation(
                context = context,
                evaluation = evaluation
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpritRadarContent(
    uiState: SpritRadarUiState,
    onSelectTab: (AppTab) -> Unit,
    onStartInputChange: (String) -> Unit,
    onSelectStartSuggestion: (GeoPoint) -> Unit,
    onDestInputChange: (String) -> Unit,
    onSelectDestSuggestion: (GeoPoint) -> Unit,
    onSwapLocations: () -> Unit,
    onUseGpsLocation: () -> Unit,
    onSearchRoute: () -> Unit,
    onRefreshPrices: () -> Unit,
    onDismissSearchMessage: () -> Unit,
    onSelectFavorite: (FavoriteRoute) -> Unit,
    onSaveFavorite: () -> Unit,
    onDeleteFavorite: (String) -> Unit,
    onSelectDetour: (DetourOption) -> Unit,
    onSelectFuel: (FuelType) -> Unit,
    onConsumptionChange: (Double) -> Unit,
    onTankAmountChange: (Double) -> Unit,
    onSelectSortMode: (SortMode) -> Unit,
    onToggleCalculator: () -> Unit,
    onSelectStation: (String) -> Unit,
    onSelectSimulatedHour: (Int?) -> Unit,
    onNavigateStation: (StationEvaluation) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLegalInfoDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val evaluations = uiState.sortedEvaluations
    val result = uiState.routeResult

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SpritRadar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        // Requirement #4: Info button in the top bar next to the icon for Data Source & Legal Info
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { showLegalInfoDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "ⓘ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Info",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Requirement #6: Identical App Start Icon in the top right
                        SpritRadarLogoIcon(
                            size = 40.dp,
                            modifier = Modifier.clickable { showLegalInfoDialog = true }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                AppTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Notification / Banner when search finished
            AnimatedVisibility(visible = uiState.searchCompletedMessage != null) {
                Surface(
                    color = Color(0xFF16A34A).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissSearchMessage() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ ${uiState.searchCompletedMessage}",
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CenteredDeleteCross(
                            tint = Color(0xFF16A34A),
                            bgColor = Color(0xFF16A34A).copy(alpha = 0.15f),
                            onClick = onDismissSearchMessage
                        )
                    }
                }
            }

            when (uiState.selectedTab) {
                AppTab.ROUTE_PLANNER -> {
                    // TAB 1 ("Suche"): Only route/parameter inputs and search button
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            RoutePlannerInputCard(
                                uiState = uiState,
                                onStartInputChange = onStartInputChange,
                                onSelectStartSuggestion = onSelectStartSuggestion,
                                onDestInputChange = onDestInputChange,
                                onSelectDestSuggestion = onSelectDestSuggestion,
                                onSwapLocations = onSwapLocations,
                                onUseGpsLocation = onUseGpsLocation,
                                onSearchRoute = {
                                    focusManager.clearFocus()
                                    onSearchRoute()
                                },
                                onSelectFavorite = onSelectFavorite,
                                onSaveFavorite = onSaveFavorite,
                                onDeleteFavorite = onDeleteFavorite,
                                onSelectDetour = onSelectDetour,
                                onSelectFuel = onSelectFuel,
                                onConsumptionChange = onConsumptionChange,
                                onTankAmountChange = onTankAmountChange,
                                onToggleCalculator = onToggleCalculator
                            )
                        }
                    }
                }

                AppTab.RESULTS_MAP -> {
                    // TAB 2 ("Karte"): Active parameters (read-only), expanding map + station details below map, ampel, station list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ActiveParametersHeader(
                                uiState = uiState,
                                result = result,
                                onRefreshPrices = onRefreshPrices
                            )
                        }

                        if (result != null) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "🗺️ Routen- & Korridorkarte (${SpritRadarRepository.formatKm(result.totalRouteDistanceKm)} • ~${result.estimatedDurationMinutes} Min.)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    InteractiveRouteMap(
                                        startPoint = result.startPoint,
                                        destPoint = result.destPoint,
                                        routePolyline = result.routePolyline,
                                        detourOption = uiState.detourOption,
                                        evaluations = evaluations,
                                        selectedStationId = uiState.selectedStationId,
                                        onSelectStation = onSelectStation,
                                        onNavigateStation = onNavigateStation
                                    )
                                }
                            }

                            item {
                                PriceAmpelCard(
                                    analysis = result.historicalAnalysis,
                                    simulatedHour = uiState.simulatedHour,
                                    onSelectSimulatedHour = onSelectSimulatedHour
                                )
                            }
                        }

                        // Sorting Mode Selector
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Gefundene Tankstellen (${evaluations.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SortMode.entries.forEach { mode ->
                                        FilterChip(
                                            selected = uiState.sortMode == mode,
                                            onClick = { onSelectSortMode(mode) },
                                            label = {
                                                Text(
                                                    text = mode.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (uiState.sortMode == mode) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Station Cards List
                        items(
                            items = evaluations,
                            key = { it.station.id }
                        ) { evaluation ->
                            StationCard(
                                evaluation = evaluation,
                                isSelected = evaluation.station.id == uiState.selectedStationId,
                                onSelect = { onSelectStation(evaluation.station.id) },
                                onNavigateDirect = { onNavigateStation(evaluation) }
                            )
                        }

                        // Data Source & 5-Min Refresh Info in Tab "Karte"
                        item {
                            DataSourceFooter(
                                lastUpdated = result?.lastUpdatedTimeFormatted.orEmpty(),
                                isLoading = uiState.isLoading,
                                onRefreshClick = onRefreshPrices,
                                onOpenLegalDialog = { showLegalInfoDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLegalInfoDialog) {
        DataAndLegalInfoDialog(
            lastUpdated = result?.lastUpdatedTimeFormatted.orEmpty(),
            onDismiss = { showLegalInfoDialog = false }
        )
    }
}

@Composable
private fun ActiveParametersHeader(
    uiState: SpritRadarUiState,
    result: RouteSearchResult?,
    onRefreshPrices: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ Gewählte Parameter:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) { onRefreshPrices() }
                ) {
                    Text(
                        text = if (uiState.isLoading) "⏳ Lädt..." else "🔄 Aktualisieren",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ParamBadge("Route", "${uiState.startInput.take(14)} → ${uiState.destInput.take(14)}")
                ParamBadge("Kraftstoff", uiState.vehicleProfile.preferredFuel.displayName)
                ParamBadge("Max. Umweg", uiState.detourOption.label)
                ParamBadge("Verbrauch", "${String.format(Locale.GERMANY, "%.1f", uiState.vehicleProfile.consumptionPer100Km)} L/100km")
                ParamBadge("Tankmenge", "${uiState.vehicleProfile.tankAmountLiters.roundToInt()} L")
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = buildString {
                    append("🟢 Echtzeit-Daten von Tankerkönig (Update alle 5 Min.)")
                    if (result != null && result.lastUpdatedTimeFormatted.isNotBlank()) {
                        append(" • Stand: ${result.lastUpdatedTimeFormatted}")
                    }
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ParamBadge(title: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$title:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RoutePlannerInputCard(
    uiState: SpritRadarUiState,
    onStartInputChange: (String) -> Unit,
    onSelectStartSuggestion: (GeoPoint) -> Unit,
    onDestInputChange: (String) -> Unit,
    onSelectDestSuggestion: (GeoPoint) -> Unit,
    onSwapLocations: () -> Unit,
    onUseGpsLocation: () -> Unit,
    onSearchRoute: () -> Unit,
    onSelectFavorite: (FavoriteRoute) -> Unit,
    onSaveFavorite: () -> Unit,
    onDeleteFavorite: (String) -> Unit,
    onSelectDetour: (DetourOption) -> Unit,
    onSelectFuel: (FuelType) -> Unit,
    onConsumptionChange: (Double) -> Unit,
    onTankAmountChange: (Double) -> Unit,
    onToggleCalculator: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Requirement #1 & #2: Saved Routes Bar WITHOUT the hint text, and with a perfectly centered Canvas "X" button
            if (uiState.favoriteRoutes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.favoriteRoutes.forEach { fav ->
                        val isCurrent = uiState.startInput.contains(fav.startName, ignoreCase = true) &&
                            uiState.destInput.contains(fav.destName, ignoreCase = true)
                        val contentColor = if (isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                            ) {
                                Text(
                                    text = "${fav.startName} → ${fav.destName}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor,
                                    modifier = Modifier.clickable { onSelectFavorite(fav) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CenteredDeleteCross(
                                    tint = contentColor,
                                    bgColor = contentColor.copy(alpha = 0.16f),
                                    onClick = { onDeleteFavorite(fav.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Start & Destination Inputs with GPS & Swap
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        OutlinedTextField(
                            value = uiState.startInput,
                            onValueChange = onStartInputChange,
                            label = { Text("Startort (Default: Mein Standort)") },
                            singleLine = true,
                            trailingIcon = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable { onUseGpsLocation() }
                                ) {
                                    Text(
                                        text = "📍 GPS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.startSuggestions.isNotEmpty()) {
                            SuggestionsDropdownStrip(
                                suggestions = uiState.startSuggestions,
                                onSelect = onSelectStartSuggestion
                            )
                        }
                    }

                    Column {
                        OutlinedTextField(
                            value = uiState.destInput,
                            onValueChange = onDestInputChange,
                            label = { Text("Zielort") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearchRoute() }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.destSuggestions.isNotEmpty()) {
                            SuggestionsDropdownStrip(
                                suggestions = uiState.destSuggestions,
                                onSelect = onSelectDestSuggestion
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onSwapLocations() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⇅",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    TextButton(
                        onClick = onSaveFavorite,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("★ Merken", fontSize = 11.sp)
                    }
                }
            }

            // Max Detour Selection (1, 5, 10, 15, 30 km)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Maximaler Umweg:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetourOption.entries.forEach { option ->
                        val selected = uiState.detourOption == option
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectDetour(option) },
                            label = {
                                Text(
                                    text = option.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Fuel Type Selection (Benzin E5, Super E10, Diesel)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Kraftstoff:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FuelType.entries.forEach { fuel ->
                        val selected = uiState.vehicleProfile.preferredFuel == fuel
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFuel(fuel) },
                            label = {
                                Text(
                                    text = fuel.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Integrated Vehicle Consumption & Tank Volume Calculator
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCalculator() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧮 Smart-Umwegrechner (${String.format(Locale.GERMANY, "%.1f", uiState.vehicleProfile.consumptionPer100Km)} L/100km • ${uiState.vehicleProfile.tankAmountLiters.roundToInt()} L Tankmenge)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.showCalculatorSliders) "▲" else "Anpassen ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = uiState.showCalculatorSliders) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Verbrauch deines Autos:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${String.format(Locale.GERMANY, "%.1f", uiState.vehicleProfile.consumptionPer100Km)} L / 100 km",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = uiState.vehicleProfile.consumptionPer100Km.toFloat(),
                                onValueChange = { onConsumptionChange(it.toDouble()) },
                                valueRange = 3.5f..16.0f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Geplante Tankmenge:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${uiState.vehicleProfile.tankAmountLiters.roundToInt()} Liter",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = uiState.vehicleProfile.tankAmountLiters.toFloat(),
                                onValueChange = { onTankAmountChange(it.toDouble()) },
                                valueRange = 10f..85f
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Prominent Search Button with loading indicator
            Button(
                onClick = onSearchRoute,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Suche läuft...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🔍 Günstigste Tankstellen suchen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Mathematically centered "X" button inside a circle using Canvas drawing
 * so font metrics/padding never shift the cross off-center.
 */
@Composable
private fun CenteredDeleteCross(
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            val stroke = 1.8.dp.toPx()
            drawLine(
                color = tint,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SuggestionsDropdownStrip(
    suggestions: List<GeoPoint>,
    onSelect: (GeoPoint) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            suggestions.forEach { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(place) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📍 ", fontSize = 12.sp)
                    Text(
                        text = place.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DataSourceFooter(
    lastUpdated: String,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    onOpenLegalDialog: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Datenquelle: Tankerkönig / MTS-K (CC BY 4.0)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ⓘ Lizenzen & Rechtliches",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenLegalDialog() }
                )
            }
            Text(
                text = buildString {
                    append("Die Echtzeit-Spritpreise werden alle 5 Minuten aktualisiert.")
                    if (lastUpdated.isNotBlank()) {
                        append(" (Stand: $lastUpdated)")
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRefreshClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isLoading) "⏳ Aktualisiere Daten..." else "🔄 Preise jetzt aktualisieren",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DataAndLegalInfoDialog(
    lastUpdated: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpritRadarLogoIcon(size = 32.dp)
                Text(
                    text = "Datenquellen & Rechtliches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Tankerkönig / MTS-K Attribution (CC BY 4.0)
                LegalSectionBox(
                    title = "1. Kraftstoffpreise & Tankstellendaten",
                    body = buildString {
                        append("Datenquelle: Tankerkönig (https://creativecommons.tankerkoenig.de) / Markttransparenzstelle für Kraftstoffe (MTS-K).\n")
                        append("Lizenz: Creative Commons Namensnennung 4.0 International (CC BY 4.0).\n")
                        append("Aktualisierung: Die Echtzeit-Preisdaten werden alle 5 Minuten aktualisiert.")
                        if (lastUpdated.isNotBlank()) {
                            append("\nLetzter Abruf: $lastUpdated")
                        }
                    },
                    linkLabel = "CC BY 4.0 Lizenztext öffnen ↗",
                    onLinkClick = {
                        openExternalUrl(context, "https://creativecommons.org/licenses/by/4.0/deed.de")
                    }
                )

                // 2. OpenStreetMap / OSRM Attribution (ODbL)
                LegalSectionBox(
                    title = "2. Geodaten, Ortssuche & Routenberechnung",
                    body = "Ortsvorschläge (Nominatim) und Straßenrouten-Geometrien (Project OSRM) basieren auf Geodaten von © OpenStreetMap-Mitwirkenden unter der Open Database License (ODbL 1.0).",
                    linkLabel = "OpenStreetMap Copyright & Lizenz ↗",
                    onLinkClick = {
                        openExternalUrl(context, "https://www.openstreetmap.org/copyright")
                    }
                )

                // 3. Disclaimer on Price Accuracy & Road Safety
                LegalSectionBox(
                    title = "3. Haftungsausschluss & Preisangaben",
                    body = "Alle Preis-, Umweg-, Verbrauchs- und Ersparnisberechnungen sowie die historische Preis-Ampel dienen ausschließlich der unverbindlichen Orientierung und erfolgen ohne Gewähr auf Richtigkeit, Vollständigkeit und Aktualität. Kraftstoffpreise können sich jederzeit kurzfristig ändern. Rechtlich verbindlich ist ausschließlich der vor Ort an der Zapfsäule ausgewiesene Preis.\n\nSicherheitshinweis (§ 23 Abs. 1a StVO): Bitte bediene die App niemals während der Fahrt als Fahrzeugführer."
                )

                // 4. Privacy / GDPR Notice
                LegalSectionBox(
                    title = "4. Datenschutz & Standortverarbeitung (DSGVO)",
                    body = "• Standort (GPS): Dein Gerätestandort wird nur bei aktivem Antippen des ‚GPS‘-Buttons abgefragt und ausschließlich zur Routen- und Umkreisberechnung verwendet.\n• Schnittstellen: Zur Abfrage von Orten, Routen und Tankstellen werden die eingegebenen Koordinaten/Suchbegriffe direkt an Tankerkönig (creativecommons.tankerkoenig.de), OpenStreetMap Nominatim und Project OSRM übermittelt.\n• Lokale Speicherung: Fahrzeugverbrauch, Tankmenge und gespeicherte Routen werden ausschließlich lokal auf deinem Smartphone gespeichert.\n• Externe Navigation: Erst beim Klick auf ‚Mit Google Maps navigieren‘ wird Google Maps extern aufgerufen."
                )

                // 5. Trademarks
                LegalSectionBox(
                    title = "5. Markenrechtlicher Hinweis",
                    body = "Alle genannten Marken- und Tankstellenbezeichnungen (wie Aral, Shell, TotalEnergies, Esso, JET, AVIA, HEM, bft, STAR, Google Maps) sind Eigentum der jeweiligen Rechteinhaber und werden ausschließlich zur sachlichen Information über die jeweilige Tankstelle genannt."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

@Composable
private fun LegalSectionBox(
    title: String,
    body: String,
    linkLabel: String? = null,
    onLinkClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (linkLabel != null && onLinkClick != null) {
                Text(
                    text = linkLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { onLinkClick() }
                )
            }
        }
    }
}

private fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore if no browser available
    }
}

@SuppressLint("MissingPermission")
private fun resolveDeviceLocation(context: Context): Pair<Double, Double>? {
    return try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = locationManager.getProviders(true)
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider)
            if (loc != null) {
                return loc.latitude to loc.longitude
            }
        }
        null
    } catch (_: Exception) {
        null
    }
}

private fun launchGoogleMapsNavigation(
    context: Context,
    evaluation: StationEvaluation
) {
    val st = evaluation.station
    val uri = Uri.parse(
        String.format(
            Locale.US,
            "google.navigation:q=%.5f,%.5f",
            st.lat,
            st.lng
        )
    )

    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(mapsIntent)
    } catch (_: Exception) {
        val fallbackUri = Uri.parse(
            String.format(
                Locale.US,
                "https://www.google.com/maps/dir/?api=1&destination=%.5f,%.5f&travelmode=driving",
                st.lat,
                st.lng
            )
        )
        val browserIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(browserIntent)
        } catch (_: Exception) {
            // Ignore if headless
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    val repo = SpritRadarRepository()
    val start = GeoPoint(48.1351, 11.5820, "München")
    val dest = GeoPoint(48.3705, 10.8978, "Augsburg")
    val poly = listOf(start, GeoPoint(48.25, 11.20), dest)
    val dummyStations = listOf(
        com.example.spritradar.model.GasStation(
            id = "1",
            name = "HEM Gewerbepark Süd",
            brand = "HEM",
            street = "Industriering 8",
            houseNumber = "",
            postCode = "86150",
            place = "Korridor km 24",
            lat = 48.23,
            lng = 11.25,
            isOpen = true,
            priceE5 = 1.709,
            priceE10 = 1.649,
            priceDiesel = 1.569,
            distanceFromStartKm = 24.0,
            airDistanceToRouteKm = 1.3,
            detourKm = 2.6,
            detourMinutes = 3,
            routeProgressFraction = 0.4,
            nearestRoutePoint = GeoPoint(48.22, 11.24)
        )
    )
    val evals = repo.evaluateStations(dummyStations, FuelType.E10, 6.8, 45.0)
    val hist = repo.buildHistoricalPriceAnalysis(evals, FuelType.E10, 45.0, 15)
    val previewState = SpritRadarUiState(
        routeResult = RouteSearchResult(
            startPoint = start,
            destPoint = dest,
            routePolyline = poly,
            totalRouteDistanceKm = 64.2,
            estimatedDurationMinutes = 46,
            evaluations = evals,
            historicalAnalysis = hist,
            isLiveApiData = true,
            dataSourceNote = "Datenquelle: Tankerkönig / MTS-K (CC BY 4.0)",
            lastUpdatedTimeFormatted = "18:15:00 Uhr"
        ),
        selectedStationId = "1"
    )

    SpritRadarTheme {
        SpritRadarContent(
            uiState = previewState,
            onSelectTab = {},
            onStartInputChange = {},
            onSelectStartSuggestion = {},
            onDestInputChange = {},
            onSelectDestSuggestion = {},
            onSwapLocations = {},
            onUseGpsLocation = {},
            onSearchRoute = {},
            onRefreshPrices = {},
            onDismissSearchMessage = {},
            onSelectFavorite = {},
            onSaveFavorite = {},
            onDeleteFavorite = {},
            onSelectDetour = {},
            onSelectFuel = {},
            onConsumptionChange = {},
            onTankAmountChange = {},
            onSelectSortMode = {},
            onToggleCalculator = {},
            onSelectStation = {},
            onSelectSimulatedHour = {},
            onNavigateStation = {}
        )
    }
}
