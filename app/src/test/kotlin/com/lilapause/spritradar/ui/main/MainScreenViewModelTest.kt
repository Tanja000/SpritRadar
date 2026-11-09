package com.lilapause.spritradar.ui.main

import com.lilapause.spritradar.data.SpritRadarRepository
import com.lilapause.spritradar.model.FuelType
import com.lilapause.spritradar.model.GasStation
import com.lilapause.spritradar.model.GeoPoint
import com.lilapause.spritradar.model.TrafficLightColor
import com.lilapause.spritradar.model.VerdictLevel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    private val repository = SpritRadarRepository()

    @Test
    fun evaluateStations_detectsWhenDetourCostsMoreThanTwoCentPriceAdvantage() {
        val stationB = GasStation(
            id = "station_b",
            name = "Tankstelle B (Nah)",
            brand = "HEM",
            street = "Hauptstr. 1",
            houseNumber = "",
            postCode = "80000",
            place = "München",
            lat = 48.15,
            lng = 11.55,
            isOpen = true,
            priceE5 = 1.729,
            priceE10 = 1.669,
            priceDiesel = 1.589,
            distanceFromStartKm = 10.0,
            airDistanceToRouteKm = 0.5,
            detourKm = 1.0,
            detourMinutes = 2,
            routeProgressFraction = 0.3,
            nearestRoutePoint = GeoPoint(48.15, 11.55)
        )

        // 2 cents cheaper per liter (1.649 vs 1.669 -> saves 0.80 € on 40L),
        // but 12 km extra detour at 7.5 L/100km costs ~1.48 € extra fuel!
        val stationA = GasStation(
            id = "station_a",
            name = "Tankstelle A (Weit)",
            brand = "bft",
            street = "Landstr. 99",
            houseNumber = "",
            postCode = "80000",
            place = "Umland",
            lat = 48.20,
            lng = 11.45,
            isOpen = true,
            priceE5 = 1.709,
            priceE10 = 1.649,
            priceDiesel = 1.569,
            distanceFromStartKm = 15.0,
            airDistanceToRouteKm = 6.5,
            detourKm = 13.0,
            detourMinutes = 16,
            routeProgressFraction = 0.5,
            nearestRoutePoint = GeoPoint(48.18, 11.50)
        )

        val evals = repository.evaluateStations(
            stations = listOf(stationA, stationB),
            fuelType = FuelType.E10,
            consumptionPer100Km = 7.5,
            tankAmountLiters = 40.0
        )

        val evalA = evals.first { it.station.id == "station_a" }
        val evalB = evals.first { it.station.id == "station_b" }

        assertEquals(1, evalB.rankByNetCost)
        assertEquals(VerdictLevel.DETOUR_TOO_EXPENSIVE, evalA.verdictLevel)
        assertTrue(evalA.recommendationMessage.contains("Tankstelle B"))
    }

    @Test
    fun historicalPriceAnalysis_returnsRedInMorningPeakAndGreenInEveningWindow() {
        val morning = repository.buildHistoricalPriceAnalysis(
            evaluations = emptyList(),
            fuelType = FuelType.E10,
            tankAmountLiters = 45.0,
            currentHour = 7
        )
        val evening = repository.buildHistoricalPriceAnalysis(
            evaluations = emptyList(),
            fuelType = FuelType.E10,
            tankAmountLiters = 45.0,
            currentHour = 19
        )

        assertEquals(TrafficLightColor.RED, morning.light)
        assertEquals(TrafficLightColor.GREEN, evening.light)
    }
}
