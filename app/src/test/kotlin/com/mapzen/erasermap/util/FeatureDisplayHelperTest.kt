package com.mapzen.erasermap.util

import android.content.Context
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.ConfidenceHandler
import com.mapzen.erasermap.model.IntentQueryParser
import com.mapzen.erasermap.model.LocationConverter
import com.mapzen.erasermap.model.PermissionManager
import com.mapzen.erasermap.model.TestAppSettings
import com.mapzen.erasermap.model.TestLostSettingsChecker
import com.mapzen.erasermap.model.TestMapzenLocation
import com.mapzen.erasermap.model.TestRouteManager
import com.mapzen.erasermap.presenter.MainPresenterImpl
import com.mapzen.erasermap.presenter.TestLostClientManager
import com.mapzen.erasermap.presenter.ViewStateManager
import com.mapzen.pelias.SimpleFeature
import com.mapzen.tangram.LngLat
import com.squareup.otto.Bus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class FeatureDisplayHelperTest {

  lateinit var helper: FeatureDisplayHelper
  lateinit var presenter: MainPresenterImpl
  lateinit var confidenceHandler: ConfidenceHandler
  lateinit var context: Context
  val droppedPin = "Dropped Pin"
  val galvanize = "Galvanize"

  @Before fun setup(){
    val mapzenLocation: TestMapzenLocation = TestMapzenLocation()
    val routeManager: TestRouteManager = TestRouteManager()
    val settings: TestAppSettings = TestAppSettings()
    val bus: Bus = Bus()
    val vsm: ViewStateManager = ViewStateManager()
    val iqp: IntentQueryParser = Mockito.mock(IntentQueryParser::class.java)
    val converter: LocationConverter = LocationConverter()
    val clientManager: TestLostClientManager = TestLostClientManager()
    val locationSettingsChecker = TestLostSettingsChecker()
    val permissionManager = PermissionManager()
    presenter = MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm, iqp,
        converter, clientManager, locationSettingsChecker, permissionManager)
    confidenceHandler = ConfidenceHandler(presenter)
    context = Mockito.spy(Context::class.java)
    `when`(context.getString(R.string.dropped_pin)).thenReturn(droppedPin)
    helper = FeatureDisplayHelper(context, confidenceHandler)
  }

  @Test fun getDisplayName_missingConfidence_shouldReturnDroppedPin() {
    confidenceHandler.longPressed = true
    presenter.reverseGeoLngLat = LngLat(0.0, 0.0)
    val feature = SimpleFeature.create("1", "1", galvanize, "",
        "", "", "", "", "", "", "", 0.0, "", "", 0.0, 0.0)
    val displayName = helper.getDisplayName(feature)
    assertThat(displayName).isEqualTo(droppedPin)
  }

  @Test fun getDisplayName_notLongPressed_shouldReturnFeatureName() {
    val feature = SimpleFeature.create("1", "1", galvanize, "",
        "", "", "", "", "", "", "", 1.0, "", "", 0.0, 0.0)
    val displayName = helper.getDisplayName(feature)
    assertThat(displayName).isEqualTo(galvanize)
  }

  @Test fun getDisplayName_lowConfidenceReverseGeo_shouldReturnDroppedPin() {
    confidenceHandler.longPressed = true
    presenter.reverseGeoLngLat = LngLat(0.0, 0.0)
    val feature = SimpleFeature.create("1", "1", galvanize, "",
        "", "", "", "", "", "", "", 0.7, "", "", 0.0, 0.0)
    val displayName = helper.getDisplayName(feature)
    assertThat(displayName).isEqualTo(droppedPin)
  }

  @Test fun getDisplayName_highConfidenceReverseGeo_shouldReturnFeatureName() {
    confidenceHandler.longPressed = true
    presenter.reverseGeoLngLat = LngLat(0.0, 0.0)
    val feature = SimpleFeature.create("1", "1", galvanize, "",
        "", "", "", "", "", "", "", 0.9, "", "", 0.0, 0.0)
    val displayName = helper.getDisplayName(feature)
    assertThat(displayName).isEqualTo(galvanize)
  }

  @Test fun getDisplayName_blankFeatureName_shouldReturnDroppedPin() {
    val feature = SimpleFeature.create("1", "1", "", "",
        "", "", "", "", "", "", "", 0.7, "", "", 0.0, 0.0)
    val displayName = helper.getDisplayName(feature)
    assertThat(displayName).isEqualTo(droppedPin)
  }
}
