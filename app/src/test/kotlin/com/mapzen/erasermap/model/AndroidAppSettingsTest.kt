package com.mapzen.erasermap.model

import android.preference.PreferenceManager
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.valhalla.Router
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config
import java.io.File

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
public class AndroidAppSettingsTest {
    private val settings: AppSettings = AndroidAppSettings(application as EraserMapApplication)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    @Test fun shouldNotBeNull() {
        assertThat(settings).isNotNull();
    }

    @Test fun distanceUnits_shouldReturnDefaultValue() {
        assertThat(settings.distanceUnits).isEqualTo(AppSettings.DEFAULT_UNITS)
    }

    @Test fun distanceUnits_shouldReturnPreferenceValueMiles() {
        val key = AndroidAppSettings.KEY_DISTANCE_UNITS
        prefs.edit().putString(key, Router.DistanceUnits.MILES.toString()).commit()
        assertThat(settings.distanceUnits).isEqualTo(Router.DistanceUnits.MILES)
    }

    @Test fun distanceUnits_shouldReturnPreferenceValueKilometers() {
        val key = AndroidAppSettings.KEY_DISTANCE_UNITS
        prefs.edit().putString(key, Router.DistanceUnits.KILOMETERS.toString()).commit()
        assertThat(settings.distanceUnits).isEqualTo(Router.DistanceUnits.KILOMETERS)
    }

    @Test fun distanceUnits_shouldSetPreferenceValueMiles() {
        val key = AndroidAppSettings.KEY_DISTANCE_UNITS
        settings.distanceUnits = Router.DistanceUnits.MILES
        assertThat(prefs.getString(key, null)).isEqualTo(Router.DistanceUnits.MILES.toString())
    }

    @Test fun distanceUnits_shouldSetPreferenceValueKilometers() {
        val key = AndroidAppSettings.KEY_DISTANCE_UNITS
        settings.distanceUnits = Router.DistanceUnits.KILOMETERS
        assertThat(prefs.getString(key, null)).isEqualTo(Router.DistanceUnits.KILOMETERS.toString())
    }

    @Test fun isMockLocationEnabled_shouldReturnDefaultValue() {
        assertThat(settings.isMockLocationEnabled).isFalse()
    }

    @Test fun isMockLocationEnabled_shouldReturnPreferenceValueTrue() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED
        prefs.edit().putBoolean(key, true).commit()
        assertThat(settings.isMockLocationEnabled).isTrue()
    }

    @Test fun isMockLocationEnabled_shouldReturnPreferenceValueFalse() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED
        prefs.edit().putBoolean(key, false).commit()
        assertThat(settings.isMockLocationEnabled).isFalse()
    }

    @Test fun isMockLocationEnabled_shouldSetPreferenceValueTrue() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED
        settings.isMockLocationEnabled = true
        assertThat(prefs.getBoolean(key, false)).isTrue()
    }

    @Test fun isMockLocationEnabled_shouldSetPreferenceValueFalse() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED
        settings.isMockLocationEnabled = false
        assertThat(prefs.getBoolean(key, true)).isFalse()
    }

    @Test fun mockLocation_shouldReturnDefaultValue() {
        assertThat(settings.mockLocation.getLatitude()).isEqualTo(40.7443)
        assertThat(settings.mockLocation.getLongitude()).isEqualTo(-73.9903)
    }

    @Test fun mockLocation_shouldReturnPreferenceValue() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_VALUE
        prefs.edit().putString(key, "1.0, 2.0").commit()
        assertThat(settings.mockLocation.getLatitude()).isEqualTo(1.0)
        assertThat(settings.mockLocation.getLongitude()).isEqualTo(2.0)
    }

    @Test fun mockLocation_shouldSetPreferenceValue() {
        val key = AndroidAppSettings.KEY_MOCK_LOCATION_VALUE
        val location = TestHelper.getTestLocation()
        location.setLatitude(1.0)
        location.setLongitude(2.0)
        settings.mockLocation = location
        assertThat(prefs.getString(key, null)).isEqualTo("1.0, 2.0")
    }

    @Test fun isMockRouteEnabled_shouldReturnDefaultValue() {
        assertThat(settings.isMockRouteEnabled).isFalse()
    }

    @Test fun isMockRouteEnabled_shouldReturnPreferenceValueTrue() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_ENABLED
        prefs.edit().putBoolean(key, true).commit()
        assertThat(settings.isMockRouteEnabled).isTrue()
    }

    @Test fun isMockRouteEnabled_shouldReturnPreferenceValueFalse() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_ENABLED
        prefs.edit().putBoolean(key, false).commit()
        assertThat(settings.isMockRouteEnabled).isFalse()
    }

    @Test fun isMockRouteEnabled_shouldSetPreferenceValueTrue() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_ENABLED
        settings.isMockRouteEnabled = true
        assertThat(prefs.getBoolean(key, false)).isTrue()
    }

    @Test fun isMockRouteEnabled_shouldSetPreferenceValueFalse() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_ENABLED
        settings.isMockRouteEnabled = false
        assertThat(prefs.getBoolean(key, true)).isFalse()
    }

    @Test fun mockRoute_shouldReturnDefaultValue() {
        assertThat(settings.mockRoute).hasName("lost.gpx")
    }

    @Test fun mockRoute_shouldReturnPreferenceValue() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_VALUE
        prefs.edit().putString(key, "acme.gpx").commit()
        assertThat(settings.mockRoute).hasName("acme.gpx")
    }

    @Test fun mockRoute_shouldSetPreferenceValue() {
        val key = AndroidAppSettings.KEY_MOCK_ROUTE_VALUE
        settings.mockRoute = File("ymca.gpx")
        assertThat(prefs.getString(key, null)).isEqualTo("ymca.gpx")
    }
}
