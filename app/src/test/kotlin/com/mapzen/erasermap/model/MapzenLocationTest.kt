package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LocationServices.FusedLocationApi
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.dummy.TestHelper
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class MapzenLocationTest {
    val locationClient = LostApiClient.Builder(application).build()
    val settings = TestAppSettings()
    val bus = Bus()
    val mapzenLocation = MapzenLocationImpl(locationClient, settings, bus)

    @Test fun shouldNotBeNull() {
        assertThat(mapzenLocation).isNotNull()
    }

    @Test fun connect_shouldConnectLocationClient() {
        mapzenLocation.connect()
        assertThat(mapzenLocation.locationClient.isConnected).isTrue()
    }

    @Test fun disconnect_shouldDisconnectLocationClient() {
        mapzenLocation.connect()
        mapzenLocation.disconnect()
        assertThat(mapzenLocation.locationClient.isConnected).isFalse()
    }

    @Test fun initLocationUpdates_shouldConnectIfDisconnected() {
        mapzenLocation.connect()
        mapzenLocation.disconnect()
        mapzenLocation.initLocationUpdates()
        assertThat(mapzenLocation.locationClient.isConnected).isTrue()
    }

    @Test fun initLocationUpdates_shouldRequestLocationUpdates() {
        val expected = TestHelper.getTestLocation()
        val subscriber = LocationChangeSubscriber()
        bus.register(subscriber)
        mapzenLocation.initLocationUpdates()
        FusedLocationApi.setMockMode(true)
        FusedLocationApi.setMockLocation(expected)
        assertThat(subscriber.event?.location).isEqualTo(expected)
    }

    @Test fun connect_shouldSetMockLocationIfMockModeEnabled() {
        val location = TestHelper.getTestLocation()
        location.latitude = 1.0
        location.longitude = 2.0
        mapzenLocation.settings.isMockLocationEnabled = true
        mapzenLocation.settings.mockLocation = location
        mapzenLocation.connect()
        assertThat(FusedLocationApi.lastLocation.latitude).isEqualTo(1.0)
        assertThat(FusedLocationApi.lastLocation.longitude).isEqualTo(2.0)
    }

    @Test fun connect_shouldNotSetMockLocationIfMockModeNotEnabled() {
        val location = TestHelper.getTestLocation()
        location.latitude = 1.0
        location.longitude = 2.0
        mapzenLocation.settings.isMockLocationEnabled = false
        mapzenLocation.settings.mockLocation = location
        mapzenLocation.connect()
        assertThat(FusedLocationApi.lastLocation).isNull()
    }

    class LocationChangeSubscriber {
        public var event: LocationChangeEvent? = null

        @Subscribe fun onLocationChangeEvent(event: LocationChangeEvent) {
            this.event = event
        }
    }
}
