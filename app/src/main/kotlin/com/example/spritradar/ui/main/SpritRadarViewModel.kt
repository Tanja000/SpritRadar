package com.example.spritradar.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spritradar.data.SpritRadarRepository
import com.example.spritradar.data.TankerkoenigConfig
import com.example.spritradar.data.UserPreferencesRepository
import com.example.spritradar.model.DetourOption
import com.example.spritradar.model.FavoriteRoute
import com.example.spritradar.model.FuelType
import com.example.spritradar.model.GasStation
import com.example.spritradar.model.GeoPoint
import com.example.spritradar.model.HistoricalPriceAnalysis
import com.example.spritradar.model.RouteSearchResult
import com.example.spritradar.model.SortMode
import com.example.spritradar.model.StationEvaluation
import com.example.spritradar.model.VehicleProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class AppTab(val title: String) {
    ROUTE_PLANNER("Suche"),
    RESULTS_MAP("Karte")
}

data class SpritRadarUiState(
    val selectedTab: AppTab = AppTab.ROUTE_PLANNER,
    val startInput: String = "München (Mein Standort)",
    val destInput: String = "Augsburg",
    val explicitStartPoint: GeoPoint? = GeoPoint(48.1351, 11.5820, "München"),
    val explicitDestPoint: GeoPoint? = GeoPoint(48.3705, 10.8978, "Augsburg"),
    val startSuggestions: List<GeoPoint> = emptyList(),
    val destSuggestions: List<GeoPoint> = emptyList(),
    val detourOption: DetourOption = DetourOption.KM_10,
    val vehicleProfile: VehicleProfile = VehicleProfile(),
    val sortMode: SortMode = SortMode.NET_SAVINGS,
    val showMapSection: Boolean = true,
    val showCalculatorSliders: Boolean = true,
    val favoriteRoutes: List<FavoriteRoute> = UserPreferencesRepository.defaultFavoriteRoutes(),
    val apiKey: String = TankerkoenigConfig.DEFAULT_API_KEY,
    val simulatedHour: Int? = null,
    val isLoading: Boolean = false,
    val searchCompletedMessage: String? = null,
    val routeResult: RouteSearchResult? = null,
    val selectedStationId: String? = null,
    val gpsStatusMessage: String? = null
) {
    val sortedEvaluations: List<StationEvaluation>
        get() {
            val list = routeResult?.evaluations.orEmpty()
            return when (sortMode) {
                SortMode.NET_SAVINGS -> list.sortedBy { it.effectiveTotalCostEuro }
                SortMode.RAW_PRICE -> list.sortedWith(compareBy<StationEvaluation> { it.literPrice }.thenBy { it.station.detourKm })
                SortMode.MIN_DETOUR -> list.sortedWith(compareBy<StationEvaluation> { it.station.detourKm }.thenBy { it.effectiveTotalCostEuro })
            }
        }
}

class SpritRadarViewModel(
    private val repository: SpritRadarRepository = SpritRadarRepository(),
    private val prefsRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpritRadarUiState())
    val uiState: StateFlow<SpritRadarUiState> = _uiState.asStateFlow()

    private var currentRawStations: List<GasStation> = emptyList()
    private var searchJob: Job? = null

    init {
        val savedProfile = prefsRepository?.getVehicleProfile() ?: VehicleProfile()
        val savedDetour = prefsRepository?.getDetourOption() ?: DetourOption.KM_10
        val savedFavorites = prefsRepository?.getFavoriteRoutes() ?: UserPreferencesRepository.defaultFavoriteRoutes()
        val savedApiKey = prefsRepository?.getApiKey().orEmpty().ifEmpty { TankerkoenigConfig.DEFAULT_API_KEY }

        _uiState.update {
            it.copy(
                vehicleProfile = savedProfile,
                detourOption = savedDetour,
                favoriteRoutes = savedFavorites,
                apiKey = savedApiKey
            )
        }
        performRouteSearch()
        startFiveMinuteAutoRefresh()
    }

    private fun startFiveMinuteAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L) // Tankerkönig MTS-K 5-minute update interval
                refreshLivePrices(isAutoRefresh = true)
            }
        }
    }

    fun refreshLivePrices(isAutoRefresh: Boolean = false) {
        performRouteSearch(
            switchToResultsTab = false,
            isRefresh = true,
            isAutoRefresh = isAutoRefresh
        )
    }

    private var startSuggestJob: Job? = null
    private var destSuggestJob: Job? = null

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun dismissSearchCompletedMessage() {
        _uiState.update { it.copy(searchCompletedMessage = null) }
    }

    fun updateStartInput(value: String) {
        _uiState.update {
            it.copy(
                startInput = value,
                explicitStartPoint = null,
                gpsStatusMessage = null
            )
        }
        startSuggestJob?.cancel()
        if (value.trim().length >= 2) {
            startSuggestJob = viewModelScope.launch {
                val suggestions = repository.getPlaceSuggestions(value)
                _uiState.update { it.copy(startSuggestions = suggestions) }
            }
        } else {
            _uiState.update { it.copy(startSuggestions = emptyList()) }
        }
    }

    fun selectStartSuggestion(suggestion: GeoPoint) {
        startSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                startInput = suggestion.label,
                explicitStartPoint = suggestion,
                startSuggestions = emptyList()
            )
        }
    }

    fun updateDestInput(value: String) {
        _uiState.update {
            it.copy(
                destInput = value,
                explicitDestPoint = null
            )
        }
        destSuggestJob?.cancel()
        if (value.trim().length >= 2) {
            destSuggestJob = viewModelScope.launch {
                val suggestions = repository.getPlaceSuggestions(value)
                _uiState.update { it.copy(destSuggestions = suggestions) }
            }
        } else {
            _uiState.update { it.copy(destSuggestions = emptyList()) }
        }
    }

    fun selectDestSuggestion(suggestion: GeoPoint) {
        destSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                destInput = suggestion.label,
                explicitDestPoint = suggestion,
                destSuggestions = emptyList()
            )
        }
    }

    fun swapStartAndDestination() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                startInput = state.destInput,
                destInput = state.startInput,
                explicitStartPoint = state.explicitDestPoint,
                explicitDestPoint = state.explicitStartPoint
            )
        }
        performRouteSearch()
    }

    fun setGpsStartLocation(lat: Double, lng: Double, label: String = "Mein Standort (GPS)") {
        val pt = GeoPoint(lat, lng, label)
        _uiState.update {
            it.copy(
                startInput = label,
                explicitStartPoint = pt,
                gpsStatusMessage = "GPS-Standort aktiv"
            )
        }
        performRouteSearch()
    }

    fun selectFavoriteRoute(favorite: FavoriteRoute) {
        _uiState.update {
            it.copy(
                startInput = favorite.startName,
                destInput = favorite.destName,
                explicitStartPoint = favorite.startPoint,
                explicitDestPoint = favorite.destPoint
            )
        }
        performRouteSearch()
    }

    fun saveCurrentRouteAsFavorite() {
        val state = _uiState.value
        val result = state.routeResult ?: return
        val startClean = state.startInput.replace("(Mein Standort)", "").replace("(GPS)", "").trim().ifEmpty { "Start" }
        val destClean = state.destInput.trim().ifEmpty { "Ziel" }

        val alreadyExists = state.favoriteRoutes.any {
            it.startName.equals(startClean, ignoreCase = true) &&
                it.destName.equals(destClean, ignoreCase = true)
        }
        if (alreadyExists) return

        val newFav = FavoriteRoute(
            id = UUID.randomUUID().toString(),
            title = "$startClean → $destClean",
            startName = startClean,
            startPoint = result.startPoint.copy(label = startClean),
            destName = destClean,
            destPoint = result.destPoint.copy(label = destClean)
        )
        val updated = listOf(newFav) + state.favoriteRoutes
        prefsRepository?.saveFavoriteRoutes(updated)
        _uiState.update { it.copy(favoriteRoutes = updated) }
    }

    fun deleteFavoriteRoute(routeId: String) {
        val updated = _uiState.value.favoriteRoutes.filterNot { it.id == routeId }
        prefsRepository?.saveFavoriteRoutes(updated)
        _uiState.update { it.copy(favoriteRoutes = updated) }
    }

    fun selectDetourOption(option: DetourOption) {
        prefsRepository?.saveDetourOption(option)
        _uiState.update { it.copy(detourOption = option) }
        performRouteSearch()
    }

    fun selectFuelType(fuelType: FuelType) {
        val updatedProfile = _uiState.value.vehicleProfile.copy(preferredFuel = fuelType)
        prefsRepository?.saveVehicleProfile(updatedProfile)
        _uiState.update { it.copy(vehicleProfile = updatedProfile) }
        recalculateInMemory()
    }

    fun updateConsumption(litersPer100Km: Double) {
        val rounded = (Math.round(litersPer100Km * 10.0) / 10.0).coerceIn(3.0, 20.0)
        val updatedProfile = _uiState.value.vehicleProfile.copy(consumptionPer100Km = rounded)
        prefsRepository?.saveVehicleProfile(updatedProfile)
        _uiState.update { it.copy(vehicleProfile = updatedProfile) }
        recalculateInMemory()
    }

    fun updateTankAmount(liters: Double) {
        val rounded = Math.round(liters).toDouble().coerceIn(10.0, 90.0)
        val updatedProfile = _uiState.value.vehicleProfile.copy(tankAmountLiters = rounded)
        prefsRepository?.saveVehicleProfile(updatedProfile)
        _uiState.update { it.copy(vehicleProfile = updatedProfile) }
        recalculateInMemory()
    }

    fun selectSortMode(mode: SortMode) {
        _uiState.update { it.copy(sortMode = mode) }
    }

    fun toggleMapSection() {
        _uiState.update { it.copy(showMapSection = !it.showMapSection) }
    }

    fun toggleCalculatorSliders() {
        _uiState.update { it.copy(showCalculatorSliders = !it.showCalculatorSliders) }
    }

    fun selectStation(stationId: String) {
        _uiState.update { it.copy(selectedStationId = stationId) }
    }

    fun updateSimulatedHour(hour: Int?) {
        _uiState.update { it.copy(simulatedHour = hour) }
        recalculateInMemory()
    }

    fun updateApiKey(newKey: String) {
        val clean = newKey.trim()
        prefsRepository?.setApiKey(clean)
        _uiState.update { it.copy(apiKey = clean) }
        performRouteSearch()
    }

    fun performRouteSearch(
        switchToResultsTab: Boolean = false,
        isRefresh: Boolean = false,
        isAutoRefresh: Boolean = false
    ) {
        searchJob?.cancel()
        val snapshot = _uiState.value
        _uiState.update {
            it.copy(
                isLoading = true,
                startSuggestions = emptyList(),
                destSuggestions = emptyList(),
                searchCompletedMessage = if (isRefresh) "Aktualisiere Tankerkönig Live-Preise..." else null
            )
        }

        searchJob = viewModelScope.launch {
            val result = repository.searchRouteAndStations(
                startQuery = snapshot.startInput,
                destQuery = snapshot.destInput,
                explicitStartPoint = snapshot.explicitStartPoint,
                explicitDestPoint = snapshot.explicitDestPoint,
                detourOption = snapshot.detourOption,
                vehicleProfile = snapshot.vehicleProfile,
                apiKey = snapshot.apiKey,
                simulatedHourOverride = snapshot.simulatedHour
            )
            currentRawStations = result.evaluations.map { it.station }
            val bestId = result.evaluations.minByOrNull { it.rankByNetCost }?.station?.id
            val msg = when {
                isAutoRefresh -> "Automatischer 5-Min.-Abgleich: ${result.evaluations.size} Live-Preise aktualisiert (${result.lastUpdatedTimeFormatted})"
                isRefresh -> "Live-Preise aktualisiert: ${result.evaluations.size} Tankstellen (Stand: ${result.lastUpdatedTimeFormatted})"
                switchToResultsTab -> "Suche abgeschlossen: ${result.evaluations.size} Tankstellen gefunden (Stand: ${result.lastUpdatedTimeFormatted})!"
                else -> null
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    routeResult = result,
                    selectedStationId = bestId,
                    selectedTab = if (switchToResultsTab) AppTab.RESULTS_MAP else it.selectedTab,
                    searchCompletedMessage = msg
                )
            }
        }
    }

    private fun recalculateInMemory() {
        val currentResult = _uiState.value.routeResult ?: return
        if (currentRawStations.isEmpty()) return

        val profile = _uiState.value.vehicleProfile
        val newEvals = repository.evaluateStations(
            stations = currentRawStations,
            fuelType = profile.preferredFuel,
            consumptionPer100Km = profile.consumptionPer100Km,
            tankAmountLiters = profile.tankAmountLiters
        )
        val hour = _uiState.value.simulatedHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val newHistorical: HistoricalPriceAnalysis = repository.buildHistoricalPriceAnalysis(
            evaluations = newEvals,
            fuelType = profile.preferredFuel,
            tankAmountLiters = profile.tankAmountLiters,
            currentHour = hour
        )
        val bestId = newEvals.minByOrNull { it.rankByNetCost }?.station?.id ?: _uiState.value.selectedStationId
        _uiState.update {
            it.copy(
                routeResult = currentResult.copy(
                    evaluations = newEvals,
                    historicalAnalysis = newHistorical
                ),
                selectedStationId = bestId
            )
        }
    }
}
