package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.lost.api.LocationServices.FusedLocationApi
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.dummy.TestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class MapzenLocationTest {
    var mapzenLocation = MapzenLocationImpl(application as EraserMapApplication)

    @Test
    fun shouldNotBeNull() {
        assertThat(mapzenLocation).isNotNull()
    }

    @Test
    fun connect_shouldConnectLocationClient() {
        mapzenLocation.connect()
        assertThat(mapzenLocation.locationClient?.isConnected()).isTrue()
    }

    @Test
    fun disconnect_shouldDisconnectLocationClient() {
        mapzenLocation.connect()
        mapzenLocation.disconnect()
        assertThat(mapzenLocation.locationClient!!.isConnected()).isFalse()
    }

    @Test
    fun initLocationUpdates_shouldConnectIfDisconnected() {
        mapzenLocation.connect()
        mapzenLocation.disconnect()
        mapzenLocation.initLocationUpdates {  }
        assertThat(mapzenLocation.locationClient?.isConnected()).isTrue()
    }

    @Test
    fun initLocationUpdates_shouldRequestLocationUpdates() {
        val expected = TestHelper.getTestLocation()
        var actual: Location? = null
        mapzenLocation.initLocationUpdates { location: Location -> actual = location }
        FusedLocationApi.setMockMode(true)
        FusedLocationApi.setMockLocation(expected)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun connect_shouldSetMockLocationIfMockModeEnabled() {
        val editor = mapzenLocation.prefs!!.edit()
        editor.putBoolean(AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED, true)
        editor.putString(AndroidAppSettings.KEY_MOCK_LOCATION_VALUE, "1.0, 2.0")
        editor.commit()
        mapzenLocation.connect()
        assertThat(FusedLocationApi.getLastLocation().getLatitude()).isEqualTo(1.0)
        assertThat(FusedLocationApi.getLastLocation().getLongitude()).isEqualTo(2.0)
    }

    @Test
    fun connect_shouldNotSetMockLocationIfMockModeNotEnabled() {
        val editor = mapzenLocation.prefs!!.edit()
        editor.putBoolean(AndroidAppSettings.KEY_MOCK_LOCATION_ENABLED, false)
        editor.putString(AndroidAppSettings.KEY_MOCK_LOCATION_VALUE, "1.0, 2.0")
        editor.commit()
        mapzenLocation.connect()
        assertThat(FusedLocationApi.getLastLocation()).isNull()
    }
}
