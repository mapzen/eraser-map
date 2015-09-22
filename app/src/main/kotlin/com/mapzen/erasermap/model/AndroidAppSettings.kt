package com.mapzen.erasermap.model

import android.location.Location
import android.preference.PreferenceManager
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.valhalla.Router
import java.io.File

public class AndroidAppSettings(val application: EraserMapApplication) : AppSettings {
    companion object {
        public val KEY_DISTANCE_UNITS: String = "list_distance_units"
        public val KEY_MOCK_LOCATION_ENABLED: String = "checkbox_mock_location"
        public val KEY_MOCK_LOCATION_VALUE: String = "edittext_mock_location"
        public val KEY_MOCK_ROUTE_ENABLED: String = "checkbox_mock_route"
        public val KEY_MOCK_ROUTE_VALUE: String = "edittext_mock_route"
    }

    private val distanceUnitsDefaultValue = application.getString(R.string.distance_units_default)

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    override var distanceUnits: Router.DistanceUnits
        get() {
            val value = prefs.getString(KEY_DISTANCE_UNITS, distanceUnitsDefaultValue)
            when (value) {
                Router.DistanceUnits.MILES.toString() -> return Router.DistanceUnits.MILES
                Router.DistanceUnits.KILOMETERS.toString() -> return Router.DistanceUnits.KILOMETERS
            }

            return Router.DistanceUnits.MILES
        }
        set(value) {
            prefs.edit().putString(KEY_DISTANCE_UNITS, value.toString()).commit()
        }

    override var isMockLocationEnabled: Boolean
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var mockLocation: Location
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var isMockRouteEnabled: Boolean
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var mockRoute: File
        get() = throw UnsupportedOperationException()
        set(value) {
        }
}
