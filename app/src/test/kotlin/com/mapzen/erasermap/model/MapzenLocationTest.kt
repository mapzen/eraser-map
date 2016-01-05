package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LocationServices.FusedLocationApi
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
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
    val mapzenLocation = MapzenLocationImpl(locationClient, settings, bus,
            application as EraserMapApplication)

    @Test fun shouldNotBeNull() {
        assertThat(mapzenLocation).isNotNull()
    }

    @Test fun initLocationUpdates_shouldConnectIfDisconnected() {
        mapzenLocation.startLocationUpdates()
        assertThat(mapzenLocation.locationClient.isConnected).isTrue()
    }

    @Test fun initLocationUpdates_shouldRequestLocationUpdates() {
        val expected = TestHelper.getTestLocation()
        val subscriber = LocationChangeSubscriber()
        bus.register(subscriber)
        mapzenLocation.startLocationUpdates()
        FusedLocationApi.setMockMode(true)
        FusedLocationApi.setMockLocation(expected)
        assertThat(subscriber.event?.location).isEqualTo(expected)
    }

    @Test fun getLastLocation_shouldReturnMockLocationIfMockModeEnabled() {
        val location = TestHelper.getTestLocation()
        location.latitude = 1.0
        location.longitude = 2.0
        mapzenLocation.settings.isMockLocationEnabled = true
        mapzenLocation.settings.mockLocation = location
        mapzenLocation.locationClient.disconnect()
        assertThat(mapzenLocation.getLastLocation()!!.latitude).isEqualTo(1.0)
        assertThat(mapzenLocation.getLastLocation()!!.longitude).isEqualTo(2.0)
    }

    @Test fun getLastLocation_shouldNotReturnMockLocationIfMockModeNotEnabled() {
        val location = TestHelper.getTestLocation()
        location.latitude = 1.0
        location.longitude = 2.0
        mapzenLocation.settings.isMockLocationEnabled = false
        mapzenLocation.settings.mockLocation = location
        mapzenLocation.locationClient.disconnect()
        assertThat(mapzenLocation.getLastLocation()).isNull()
    }

    @Test fun onLocationUpdate_shouldPostOneEvent() {
        val subscriber = LocationChangeSubscriber()
        bus.register(subscriber)
        mapzenLocation.onLocationUpdate(TestHelper.getTestLocation())
        assertThat(subscriber.event?.location).isNotNull()
    }

    @Test fun onLocationUpdate_shouldNotPostEventIfDistanceIsLessThanMinimumDisplacement() {
        val location1 = TestHelper.getTestLocation(0.0, 0.0)
        val location2 = TestHelper.getTestLocation(0.0, 0.0)
        val subscriber = LocationChangeSubscriber()
        bus.register(subscriber)
        mapzenLocation.onLocationUpdate(location1)
        mapzenLocation.onLocationUpdate(location2)
        assertThat(subscriber.event?.location).isSameAs(location1)
    }

    class LocationChangeSubscriber {
        public var event: LocationChangeEvent? = null

        @Subscribe fun onLocationChangeEvent(event: LocationChangeEvent) {
            this.event = event
        }
    }
}
