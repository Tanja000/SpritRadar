package com.example.spritradar.data

import android.content.Context
import com.example.spritradar.model.DetourOption
import com.example.spritradar.model.FavoriteRoute
import com.example.spritradar.model.FuelType
import com.example.spritradar.model.GeoPoint
import com.example.spritradar.model.VehicleProfile
import org.json.JSONArray
import org.json.JSONObject

object TankerkoenigConfig {
    /**
     * Hinterlegter persönlicher Tankerkönig API-Key für Echtzeit-Preisabfragen (MTS-K).
     */
    const val DEFAULT_API_KEY: String = "59f342bf-77b8-4c4c-87b9-74cb864827a0"

    const val BASE_LIST_URL = "https://creativecommons.tankerkoenig.de/json/list.php"
}

class UserPreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("spritradar_prefs", Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val saved = prefs.getString(KEY_API_KEY, null)?.trim().orEmpty()
        return if (saved.isNotEmpty()) saved else TankerkoenigConfig.DEFAULT_API_KEY.trim()
    }

    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getVehicleProfile(): VehicleProfile {
        val consumption = prefs.getFloat(KEY_CONSUMPTION, 6.8f).toDouble()
        val tankLiters = prefs.getFloat(KEY_TANK_LITERS, 45.0f).toDouble()
        val fuelCode = prefs.getString(KEY_FUEL_TYPE, FuelType.E10.apiCode) ?: FuelType.E10.apiCode
        return VehicleProfile(
            consumptionPer100Km = consumption.coerceIn(2.5, 25.0),
            tankAmountLiters = tankLiters.coerceIn(5.0, 120.0),
            preferredFuel = FuelType.fromApiCode(fuelCode)
        )
    }

    fun saveVehicleProfile(profile: VehicleProfile) {
        prefs.edit()
            .putFloat(KEY_CONSUMPTION, profile.consumptionPer100Km.toFloat())
            .putFloat(KEY_TANK_LITERS, profile.tankAmountLiters.toFloat())
            .putString(KEY_FUEL_TYPE, profile.preferredFuel.apiCode)
            .apply()
    }

    fun getDetourOption(): DetourOption {
        val km = prefs.getInt(KEY_DETOUR_KM, DetourOption.KM_5.km)
        return DetourOption.fromKm(km)
    }

    fun saveDetourOption(option: DetourOption) {
        prefs.edit().putInt(KEY_DETOUR_KM, option.km).apply()
    }

    fun getFavoriteRoutes(): List<FavoriteRoute> {
        val raw = prefs.getString(KEY_FAVORITE_ROUTES, null) ?: return defaultFavoriteRoutes()
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<FavoriteRoute>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    FavoriteRoute(
                        id = obj.optString("id", "fav_$i"),
                        title = obj.getString("title"),
                        startName = obj.getString("startName"),
                        startPoint = GeoPoint(
                            lat = obj.getDouble("startLat"),
                            lng = obj.getDouble("startLng"),
                            label = obj.getString("startName")
                        ),
                        destName = obj.getString("destName"),
                        destPoint = GeoPoint(
                            lat = obj.getDouble("destLat"),
                            lng = obj.getDouble("destLng"),
                            label = obj.getString("destName")
                        )
                    )
                )
            }
            if (result.isEmpty()) defaultFavoriteRoutes() else result
        } catch (_: Exception) {
            defaultFavoriteRoutes()
        }
    }

    fun saveFavoriteRoutes(routes: List<FavoriteRoute>) {
        val array = JSONArray()
        routes.forEach { route ->
            val obj = JSONObject().apply {
                put("id", route.id)
                put("title", route.title)
                put("startName", route.startName)
                put("startLat", route.startPoint.lat)
                put("startLng", route.startPoint.lng)
                put("destName", route.destName)
                put("destLat", route.destPoint.lat)
                put("destLng", route.destPoint.lng)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITE_ROUTES, array.toString()).apply()
    }

    companion object {
        private const val KEY_API_KEY = "tankerkoenig_api_key"
        private const val KEY_CONSUMPTION = "vehicle_consumption_l_100km"
        private const val KEY_TANK_LITERS = "vehicle_tank_liters"
        private const val KEY_FUEL_TYPE = "preferred_fuel_type"
        private const val KEY_DETOUR_KM = "selected_detour_km"
        private const val KEY_FAVORITE_ROUTES = "favorite_routes_json"

        fun defaultFavoriteRoutes(): List<FavoriteRoute> = listOf(
            FavoriteRoute(
                id = "muc_aux",
                title = "Pendelstrecke A8",
                startName = "München",
                startPoint = GeoPoint(48.1351, 11.5820, "München"),
                destName = "Augsburg",
                destPoint = GeoPoint(48.3705, 10.8978, "Augsburg")
            ),
            FavoriteRoute(
                id = "ber_pdm",
                title = "Hauptstadt-Route",
                startName = "Berlin",
                startPoint = GeoPoint(52.5200, 13.4050, "Berlin"),
                destName = "Potsdam",
                destPoint = GeoPoint(52.3906, 13.0645, "Potsdam")
            ),
            FavoriteRoute(
                id = "cgn_dus",
                title = "Rhein-Pendler",
                startName = "Köln",
                startPoint = GeoPoint(50.9375, 6.9603, "Köln"),
                destName = "Düsseldorf",
                destPoint = GeoPoint(51.2277, 6.7735, "Düsseldorf")
            ),
            FavoriteRoute(
                id = "fra_mzn",
                title = "Rhein-Main Express",
                startName = "Frankfurt am Main",
                startPoint = GeoPoint(50.1109, 8.6821, "Frankfurt am Main"),
                destName = "Mainz",
                destPoint = GeoPoint(49.9929, 8.2473, "Mainz")
            ),
            FavoriteRoute(
                id = "ham_hl",
                title = "Ostsee-Zubringer A1",
                startName = "Hamburg",
                startPoint = GeoPoint(53.5511, 9.9937, "Hamburg"),
                destName = "Lübeck",
                destPoint = GeoPoint(53.8655, 10.6866, "Lübeck")
            )
        )
    }
}
