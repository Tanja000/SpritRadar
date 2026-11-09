package com.lilapause.spritradar.model

import kotlin.math.roundToInt

data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val label: String = ""
)

enum class FuelType(
    val displayName: String,
    val shortName: String,
    val apiCode: String
) {
    E10("Super E10", "E10", "e10"),
    E5("Super E5 (Benzin)", "Benzin E5", "e5"),
    DIESEL("Diesel", "Diesel", "diesel");

    companion object {
        fun fromApiCode(code: String): FuelType =
            entries.firstOrNull { it.apiCode.equals(code, ignoreCase = true) } ?: E10
    }
}

enum class DetourOption(val km: Int, val label: String) {
    KM_1(1, "1 km"),
    KM_5(5, "5 km"),
    KM_10(10, "10 km"),
    KM_15(15, "15 km"),
    KM_30(30, "30 km");

    companion object {
        fun fromKm(km: Int): DetourOption =
            entries.firstOrNull { it.km == km } ?: KM_5
    }
}

enum class SortMode(val label: String, val shortLabel: String) {
    NET_SAVINGS("Effektiv günstigste (inkl. Umweg)", "Netto-Preis"),
    RAW_PRICE("Günstigster Literpreis", "Literpreis"),
    MIN_DETOUR("Kürzester Umweg", "Umweg")
}

data class VehicleProfile(
    val consumptionPer100Km: Double = 6.8,
    val tankAmountLiters: Double = 45.0,
    val preferredFuel: FuelType = FuelType.E10
)

data class FavoriteRoute(
    val id: String,
    val title: String,
    val startName: String,
    val startPoint: GeoPoint,
    val destName: String,
    val destPoint: GeoPoint
)

data class GasStation(
    val id: String,
    val name: String,
    val brand: String,
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val place: String,
    val lat: Double,
    val lng: Double,
    val isOpen: Boolean,
    val priceE5: Double?,
    val priceE10: Double?,
    val priceDiesel: Double?,
    val distanceFromStartKm: Double,
    val airDistanceToRouteKm: Double,
    val detourKm: Double,
    val detourMinutes: Int,
    val routeProgressFraction: Double,
    val nearestRoutePoint: GeoPoint
) {
    fun priceFor(fuelType: FuelType): Double? = when (fuelType) {
        FuelType.E10 -> priceE10
        FuelType.E5 -> priceE5
        FuelType.DIESEL -> priceDiesel
    }

    val displayBrandOrName: String
        get() = if (brand.isNotBlank()) brand else name

    val fullAddress: String
        get() = buildString {
            if (street.isNotBlank()) {
                append(street)
                if (houseNumber.isNotBlank()) append(" $houseNumber")
            }
            if (postCode.isNotBlank() || place.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append("$postCode $place".trim())
            }
        }
}

enum class VerdictLevel {
    BEST_NET_CHOICE,
    WORTH_DETOUR,
    ON_ROUTE_BASELINE,
    DETOUR_TOO_EXPENSIVE
}

data class StationEvaluation(
    val station: GasStation,
    val fuelType: FuelType,
    val literPrice: Double,
    val tankAmountLiters: Double,
    val consumptionPer100Km: Double,
    val grossFuelCostEuro: Double,
    val detourExtraLiters: Double,
    val detourFuelCostEuro: Double,
    val effectiveTotalCostEuro: Double,
    val netSavingsVsBaselineEuro: Double,
    val netDiffVsBestEuro: Double,
    val literCentsDiffVsBestNet: Int,
    val rankByNetCost: Int,
    val rankByLiterPrice: Int,
    val verdictLevel: VerdictLevel,
    val recommendationHeadline: String,
    val recommendationMessage: String,
    val comparisonStationName: String? = null
) {
    val formattedLiterPriceMain: String
        get() {
            val truncated = ((literPrice * 100).toInt()) / 100.0
            return String.format(java.util.Locale.GERMANY, "%.2f", truncated)
        }

    val formattedPriceSuperscript: String
        get() {
            val thirdDecimal = ((literPrice * 1000).roundToInt() % 10)
            return thirdDecimal.toString()
        }
}

enum class TrafficLightColor {
    GREEN,
    YELLOW,
    RED
}

data class HourlyPricePoint(
    val hour: Int,
    val avgPrice: Double,
    val isBestWindow: Boolean,
    val isCurrentHour: Boolean
)

data class HistoricalPriceAnalysis(
    val light: TrafficLightColor,
    val badgeText: String,
    val headline: String,
    val recommendationText: String,
    val currentAvgPrice: Double,
    val historicalDailyAvgPrice: Double,
    val historicalDailyMinPrice: Double,
    val historicalDailyMaxPrice: Double,
    val bestWindowLabel: String,
    val expectedEveningPrice: Double,
    val potentialWaitSavingsEuro: Double,
    val hourlyCurve: List<HourlyPricePoint>
)

data class RouteSearchResult(
    val startPoint: GeoPoint,
    val destPoint: GeoPoint,
    val routePolyline: List<GeoPoint>,
    val totalRouteDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val evaluations: List<StationEvaluation>,
    val historicalAnalysis: HistoricalPriceAnalysis,
    val isLiveApiData: Boolean,
    val dataSourceNote: String,
    val lastUpdatedTimeFormatted: String = ""
)
