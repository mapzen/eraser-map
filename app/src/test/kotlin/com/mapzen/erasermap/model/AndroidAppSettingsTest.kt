package com.mapzen.erasermap.model

import android.preference.PreferenceManager
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.valhalla.Router
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
public class AndroidAppSettingsTest {
    private val settings: AppSettings = AndroidAppSettings(application as EraserMapApplication)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    @Test fun shouldNotBeNull() {
        assertThat(settings).isNotNull();
    }

    @Test fun distanceUnits_shouldReturnDefaultValue() {
        assertThat(settings.distanceUnits).isEqualTo(Router.DistanceUnits.MILES)
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
}
