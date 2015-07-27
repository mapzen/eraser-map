package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.dummy.TestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class MapzenLocationTest {
    private var mapzenLocation: MapzenLocation? = null

    @Before
    fun setUp() {
        mapzenLocation = MapzenLocation(RuntimeEnvironment.application as EraserMapApplication)
    }

    @Test
    fun shouldNotBeNull() {
        assertThat(mapzenLocation).isNotNull()
    }

    @Test
    fun connect_shouldConnectLocationClient() {
        mapzenLocation?.connect()
        assertThat(mapzenLocation?.locationClient?.isConnected()).isTrue()
    }

    @Test
    fun disconnect_shouldDisconnectLocationClient() {
        mapzenLocation?.connect()
        mapzenLocation?.disconnect()
        assertThat(mapzenLocation?.locationClient?.isConnected()).isFalse()
    }

    @Test
    fun initLocationUpdates_shouldConnectIfDisconnected() {
        mapzenLocation?.connect()
        mapzenLocation?.disconnect()
        mapzenLocation?.initLocationUpdates {  }
        assertThat(mapzenLocation?.locationClient?.isConnected()).isTrue()
    }

    @Test
    fun initLocationUpdates_shouldRequestLocationUpdates() {
        val expected = TestHelper.getTestLocation()
        var actual: Location? = null
        mapzenLocation?.initLocationUpdates { location: Location -> actual = location }
        LocationServices.FusedLocationApi.setMockMode(true)
        LocationServices.FusedLocationApi.setMockLocation(expected)
        assertThat(actual).isEqualTo(expected)
    }
}
