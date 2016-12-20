package com.mapzen.erasermap.presenter

import com.mapzen.android.lost.api.Status
import com.mapzen.erasermap.R
import com.mapzen.erasermap.TestUtils.Companion.getFeature
import com.mapzen.erasermap.controller.TestMainController
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.dummy.TestHelper.getTestAndroidLocation
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.model.AndroidAppSettings
import com.mapzen.erasermap.model.ConfidenceHandler
import com.mapzen.erasermap.model.IntentQuery
import com.mapzen.erasermap.model.IntentQueryParser
import com.mapzen.erasermap.model.LocationClientManager
import com.mapzen.erasermap.model.LocationConverter
import com.mapzen.erasermap.model.LocationSettingsChecker
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.PermissionManager
import com.mapzen.erasermap.model.TestAppSettings
import com.mapzen.erasermap.model.TestLostSettingsChecker
import com.mapzen.erasermap.model.TestMapzenLocation
import com.mapzen.erasermap.model.TestRouteManager
import com.mapzen.erasermap.model.ValhallaRouteManagerTest.TestRouteCallback
import com.mapzen.erasermap.model.event.LocationChangeEvent
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.model.event.RoutePreviewEvent
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.DEFAULT
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTING
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH_RESULTS
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Geometry
import com.mapzen.pelias.gson.Properties
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.ArrayList
import java.util.HashMap

class MainPresenterTest {
    private val mainController: TestMainController = TestMainController()
    private val routeController: TestRouteController = TestRouteController()
    private val mapzenLocation: TestMapzenLocation = TestMapzenLocation()
    private val routeManager: TestRouteManager = TestRouteManager()
    private val settings: TestAppSettings = TestAppSettings()
    private val bus: Bus = Bus()
    private val vsm: ViewStateManager = ViewStateManager()
    private val iqp: IntentQueryParser = Mockito.mock(IntentQueryParser::class.java)
    private val converter: LocationConverter = LocationConverter()
    private val clientManager: TestLostClientManager = TestLostClientManager()
    private val locationSettingsChecker = TestLostSettingsChecker()
    private val permissionManager = PermissionManager()
    private val confidenceHandler = ConfidenceHandler()
    private val presenter = MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm, iqp,
        converter, clientManager, locationSettingsChecker, permissionManager, confidenceHandler)

    @Before fun setUp() {
        presenter.mainViewController = mainController
        presenter.routeViewController = routeController
    }

    @Test fun shouldNotBeNull() {
        assertThat(presenter).isNotNull()
    }

    @Test fun onSearchResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onSearchResultsAvailable_shouldDeactivateFindMeTracking() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        mainController.isFindMeTrackingEnabled = true
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isFindMeTrackingEnabled).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldResetCurrentSearchIndex() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.currentSearchIndex = 3
        presenter.onSearchResultsAvailable(result)
        assertThat(presenter.currentSearchIndex).isEqualTo(0)
    }

    @Test fun onSearchResultsAvailable_shouldToastErrorMessageIfResultsAreEmpty() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.toastifyResId).isEqualTo(R.string.no_results_found)
    }

    @Test fun onSearchResultsAvailable_shouldFocusSearchViewIfResultsAreEmpty() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.focusSearchView).isTrue()
    }

    @Test fun onReverseGeocodeResultsAvailable_shouldShowPointOnMap() {
        mainController.screenPosLngLat = LngLat(40.0, 70.0)
        presenter.poiTapPoint = floatArrayOf(40f, 70f)
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        feature.properties = Properties()
        features.add(feature)
        result.features = features
        presenter.onReverseGeocodeResultsAvailable(result)
        assertThat(mainController.reverseGeoPointOnMap?.longitude).isEqualTo(40.0)
        assertThat(mainController.reverseGeoPointOnMap?.latitude).isEqualTo(70.0)
    }

    @Test fun onPlaceSearchResultsAvailable_shouldShowPlaceSearchResults() {
        presenter.poiTapPoint = floatArrayOf(40f, 70f)
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        feature.properties = Properties()
        features.add(feature)
        result.features = features
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.placeSearchResults).isEqualTo(features)
    }

    @Test fun onPlaceSearchRequested_shouldOverridePlaceResult() {
        mainController.screenPosLngLat = LngLat(40.0, 70.0)
        presenter.poiTapPoint = floatArrayOf(40f, 70f)
        presenter.poiTapName = "Test Name"
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        feature.properties = Properties()
        features.add(feature)
        result.features = features
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(feature.geometry.coordinates[0]).isEqualTo(40.0)
        assertThat(feature.geometry.coordinates[1]).isEqualTo(70.0)
        assertThat(feature.properties.name).isEqualTo("Test Name")
    }

    @Test fun onMapPressed_shouldSetReverseLngLat() {
        mainController.screenPosLngLat = LngLat(40.0, 70.0)
        presenter.onMapPressed(0f, 0f)
        assertThat(presenter.reverseGeoLngLat?.longitude).isEqualTo(40.0)
        assertThat(presenter.reverseGeoLngLat?.latitude).isEqualTo(70.0)
    }

    @Test fun onMapPressed_shouldSetPoiTapPoint() {
        presenter.onMapPressed(100f, 50f)
        assertThat(presenter.poiTapPoint?.get(0)).isEqualTo(100f)
        assertThat(presenter.poiTapPoint?.get(1)).isEqualTo(50f)
    }

    @Test fun onMapDoubleTapped_shouldSetCorrectZoom() {
        mainController.screenPosLngLat = LngLat(40.0, 70.0)
        mainController.lngLat = LngLat(100.0, 30.0)
        mainController.zoom = 15f
        presenter.onMapDoubleTapped(0f, 0f)
        assertThat(mainController.zoom).isEqualTo(16f)
    }

    @Test fun onMapDoubleTapped_shouldSetCorrectPosition() {
        mainController.screenPosLngLat = LngLat(40.0, 70.0)
        mainController.lngLat = LngLat(100.0, 30.0)
        mainController.zoom = 15f
        presenter.onMapDoubleTapped(0f, 0f)
        assertThat(mainController.lngLat?.longitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.latitude).isEqualTo(50.0)
    }

    @Test fun onFeaturePicked_shouldSetPoiTapPoint() {
        presenter.onFeaturePicked(HashMap<String, String>(), LngLat(0.0,0.0), 100f, 50f)
        assertThat(presenter.poiTapPoint?.get(0)).isEqualTo(100.0f)
        assertThat(presenter.poiTapPoint?.get(1)).isEqualTo(50.0f)
    }

    @Test fun onFeaturePicked_shouldSetPoiTapName() {
        val props = HashMap<String, String>()
        val coordinates = LngLat(0.0,0.0)
        props.put(MainPresenterImpl.MAP_DATA_PROP_NAME, "Test Name")
        presenter.onFeaturePicked(props, coordinates, 100f, 50f)
        assertThat(presenter.poiTapName).isEqualTo("Test Name")
    }

    @Test fun onFeaturePicked_shouldHandleSearchResultTapped() {
        val props = HashMap<String, String>()
        val coordinates = LngLat(0.0,0.0)
        props.put(MainPresenterImpl.MAP_DATA_PROP_SEARCHINDEX, "0")
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        feature.properties = Properties()
        feature.geometry = Geometry()
        feature.geometry.coordinates = arrayListOf(0.0, 0.0)
        features.add(feature)
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.onFeaturePicked(props, coordinates, 100f, 50f)
        assertThat(mainController.searchResults?.get(0)).isEqualTo(feature)
    }

    @Test fun onFeaturePicked_shouldHandleReverseGeoRequested() {
        val props = HashMap<String, String>()
        val coordinates = LngLat(0.0,0.0)
        presenter.onFeaturePicked(props, coordinates, 100f, 50f)
        assertThat(mainController.reverseGeolocatePoint?.longitude).isEqualTo(100.0)
        assertThat(mainController.reverseGeolocatePoint?.latitude).isEqualTo(50.0)
    }

    @Test fun onFeaturePicked_shouldSetPoiCoordinates() {
        val props = HashMap<String, String>()
        val coordinates = LngLat(70.0, 40.0)
        presenter.onFeaturePicked(props, coordinates, 100f, 50f)
        assertThat(presenter.poiCoordinates?.longitude).isEqualTo(70.0)
        assertThat(presenter.poiCoordinates?.latitude).isEqualTo(40.0)
    }

    @Test fun onFeaturePicked_shouldNotSetPoiPointIfNoProperties() {
        presenter.poiTapPoint = floatArrayOf(70f, 40f)
        presenter.onFeaturePicked(null, null, 100f, 50f)
        assertThat(presenter.poiTapPoint?.get(0)).isEqualTo(70.0f)
        assertThat(presenter.poiTapPoint?.get(1)).isEqualTo(40.0f)
    }

    @Test fun onFeaturePicked_shouldReverseGeoWithCorrectPointsWhenNoProperties() {
        presenter.poiTapPoint = floatArrayOf(70f, 40f)
        presenter.onFeaturePicked(null, null, 100f, 50f)
        assertThat(mainController.reverseGeolocatePoint?.longitude).isEqualTo(70.0)
        assertThat(mainController.reverseGeolocatePoint?.latitude).isEqualTo(40.0)
    }

    @Test fun onRestoreOptionsMenu_shouldRestoreSettingsBtnAndViewAllForSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)

        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isSettingsVisible).isFalse()
        assertThat(mainController.isOptionsMenuIconList).isTrue()
        assertThat(mainController.isViewAllVisible).isTrue()
    }

    @Test fun onRestoreOptionsMenu_shouldShowListForSearchListVisible() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.resultListVisible = true
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isShowingSearchResultsList).isTrue()
    }

    @Test fun onRestoreOptionsMenu_shouldHideSettingsBtnRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRestoreOptionsMenu_shouldHideActionBar() {
        mainController.isActionBarHidden = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isActionBarHidden).isTrue()
    }

    @Test fun onRestoreOptionsMenu_shouldHideSettingsBtnRouteList() {
        presenter.onClickViewList()
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRestoreOptionsMenu_shouldHideSettingsBtnRouting() {
        presenter.onClickStartNavigation()
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRestoreOptionsMenu_shouldHideSettingsBtnRouteDirectionsList() {
        vsm.viewState = ROUTE_DIRECTION_LIST
        presenter.onRestoreOptionsMenu()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRestoreMapState_shouldRestorePreviousSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)

        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreMapState()
        assertThat(newController.searchResults).isEqualTo(features)
    }

    @Test fun onRestoreMapState_shouldRestorePreviousSearchResultsCurrentIndex() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.currentSearchIndex = 2

        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreMapState()
        assertThat(newController.currentSearchIndex).isEqualTo(2)
    }

    @Test fun onRestoreMapState_shouldAdjustAttributionRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreMapState()
        assertThat(mainController.isAttributionAboveOptions).isTrue()
    }

    @Test fun onRestoreMapState_shouldAdjustFindMeRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreMapState()
        assertThat(mainController.isFindMeAboveOptions).isTrue()
    }

    @Test fun onRestoreMapState_shouldRouteRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreMapState()
        assertThat(mainController.isRouting).isTrue()
    }

    @Test fun onRestoreMapState_shouldAdjustAttributionRoutePreviewList() {
        presenter.onClickViewList()
        presenter.onRestoreMapState()
        assertThat(mainController.isAttributionAboveOptions).isTrue()
    }

    @Test fun onRestoreMapState_shouldAdjustFindMeRoutePreviewList() {
        presenter.onClickViewList()
        presenter.onRestoreMapState()
        assertThat(mainController.isFindMeAboveOptions).isTrue()
    }

    @Test fun onRestoreMapState_shouldRouteRoutePreviewList() {
        presenter.onClickViewList()
        presenter.onRestoreMapState()
        assertThat(mainController.isRouting).isTrue()
    }

    @Test fun onRestoreMapState_shouldShowRouteBtnAndCenterMapRouting() {
        presenter.onClickStartNavigation()
        presenter.onRestoreMapState()
        assertThat(mainController.isRouteBtnVisibleAndMapCentered).isTrue()
    }

    @Test fun onRestoreViewState_shouldRestoreRoutePreviewDestination() {
        mainController.isRoutePreviewDestinationVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreViewState()
        assertThat(mainController.isRoutePreviewDestinationVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldShowRoutePreview() {
        mainController.isRoutePreviewVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreViewState()
        assertThat(mainController.isRoutePreviewVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldShowRoutePreviewDistanceTimeLayout() {
        mainController.isRoutePreviewDistanceTieVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onRestoreViewState()
        assertThat(mainController.isRoutePreviewDistanceTieVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldRestoreRoutePreviewList() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.onClickViewList()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.isDirectionListVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldShowRoutingMode() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.vsm.viewState = ROUTING
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.routingEnabled = true
        presenter.onRestoreViewState()
        assertThat(newController.isRoutingModeVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldNotTriggerRoutePreviewIfNotCorrectViewState() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onBackPressed()
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.isRoutePreviewVisible).isFalse()
    }

    @Test fun onCollapseSearchView_shouldHideSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.onCollapseSearchView()
        assertThat(mainController.searchResults).isNull()
    }

    @Test fun onQuerySubmit_shouldShowProgress() {
        presenter.onQuerySubmit("")
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onQuerySubmit_shouldToggleDebugModeForCorrectSearchTerm() {
        presenter.onQuerySubmit("")
        assertThat(mainController.debugSettingsEnabled).isFalse()

        presenter.onQuerySubmit(AndroidAppSettings.SHOW_DEBUG_SETTINGS_QUERY)
        assertThat(mainController.debugSettingsEnabled).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldHideProgress() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        mainController.showProgress()
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldShowActionViewAll() {
        mainController.isViewAllVisible = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isViewAllVisible).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldDoNothingIfCurrentQueryIsDebugToggle() {
        presenter.currentSearchTerm = AndroidAppSettings.SHOW_DEBUG_SETTINGS_QUERY
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(Feature())
        features.add(Feature())
        features.add(Feature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isNull()
        assertThat(mainController.focusSearchView).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldHideReverseGeoResult() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        mainController.reverseGeoCodeResults = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.reverseGeoCodeResults).isNull()
    }

    @Test fun onSearchResultsAvailable_shouldShowSearchResultsView() {
        mainController.searchResultsViewHidden = true
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResultsViewHidden).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldAddResultsToMap() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onSearchResultsAvailable_shouldHaveCorrectActiveIndex() {
        mainController.currentSearchIndex = 3
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.currentSearchIndex).isEqualTo(0)
    }

    @Test fun onSearchResultsAvailable_shouldLayoutAttributionAboveSearchResults() {
        mainController.isAttributionAboveSearchResults = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isAttributionAboveSearchResults).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldLayoutFindMeAboveSearchResults() {
        mainController.isFindMeAboveSearchResults = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isFindMeAboveSearchResults).isTrue()
    }

    @Test fun onQuerySubmit_shouldToggleShowDebugSettings() {
        mainController.debugSettingsEnabled = false
        presenter.onQuerySubmit("!!!!!!!!")
        assertThat(mainController.debugSettingsEnabled).isTrue()

        presenter.onQuerySubmit("!!!!!!!!")
        assertThat(mainController.debugSettingsEnabled).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldSetCurrentSearchIndexToZero() {
        mainController.currentSearchIndex = 3
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.currentSearchIndex).isEqualTo(0)
    }

    @Test fun onSearchResultsAvailable_shouldSetMapPosition() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0).toFeature()
        features.add(feature)
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.lngLat?.longitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.latitude).isEqualTo(40.0)
    }

    @Test fun onSearchResultsAvailable_shouldSetDefaultMapZoom() {
        mainController.zoom = MainPresenter.ROUTING_ZOOM
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.zoom).isEqualTo(MainPresenter.DEFAULT_ZOOM)
    }

    @Test fun onSearchResultsAvailable_shouldClearPreviousSearchResults() {
        mainController.searchResultsCleared = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResultsCleared).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldDrawSearchResultsAtCorrectPoints() {
        mainController.searchResultsPoints = null
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0).toFeature()
        features.add(feature)
        val anotherFeature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 30.0, 80.0).toFeature()
        features.add(anotherFeature)
        result.features = features
        presenter.onSearchResultsAvailable(result)

        assertThat(mainController.searchResultsPoints?.size).isEqualTo(2)
        assertThat(mainController.searchResultsPoints?.get(0)?.longitude).isEqualTo(70.0)
        assertThat(mainController.searchResultsPoints?.get(0)?.latitude).isEqualTo(40.0)
        assertThat(mainController.searchResultsPoints?.get(1)?.longitude).isEqualTo(80.0)
        assertThat(mainController.searchResultsPoints?.get(1)?.latitude).isEqualTo(30.0)
    }
    
    @Test fun onCollapseSearchView_shouldHideActionViewAll() {
        mainController.isViewAllVisible = true
        presenter.onCollapseSearchView()
        assertThat(mainController.isViewAllVisible).isFalse()
    }

    @Test fun onCollapseSearchView_shouldClearQueryText() {
        mainController.queryText = "query"
        presenter.onCollapseSearchView()
        assertThat(mainController.queryText).isEmpty()
    }

    @Test fun onCollapseSearchView_shouldShowActionSettings() {
        mainController.isSettingsVisible = false
        presenter.onCollapseSearchView()
        assertThat(mainController.isSettingsVisible).isTrue()
    }

    @Test fun onCollapseSearchView_shouldNotShowActionSettingsIfViewStateSearchPreview() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        mainController.isSettingsVisible = false
        presenter.onCollapseSearchView()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onExpandSearchView_shouldSetViewState() {
        vsm.viewState = DEFAULT
        presenter.onExpandSearchView()
        assertThat(vsm.viewState).isEqualTo(SEARCH)
    }

    @Test fun onExpandSearchView_shouldNotSetViewStateIfAlreadyViewingResults() {
        vsm.viewState = SEARCH_RESULTS
        presenter.onExpandSearchView()
        assertThat(vsm.viewState).isEqualTo(SEARCH_RESULTS)
    }

    @Test fun onExpandSearchView_shouldHideActionSettings() {
        mainController.isSettingsVisible = true
        presenter.onExpandSearchView()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldCollapseSearchView() {
        mainController.isSearchVisible = true
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isSearchVisible).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldHideSearchResults() {
        mainController.searchResults = ArrayList<Feature>()
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.searchResults).isNull()
    }

    @Test fun onRoutePreviewEvent_shouldShowRoutePreviewDestination() {
        mainController.isRoutePreviewDestinationVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isRoutePreviewDestinationVisible).isTrue()
    }

    @Test fun onRoutePreviewEvent_shouldDisableReverseGeocode() {
        presenter.onReverseGeoRequested(0f, 0f)
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(presenter.onReverseGeoRequested(0f, 0f)).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldDeactivateFindMeTracking() {
        mainController.isFindMeTrackingEnabled = true
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isFindMeTrackingEnabled).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldTriggerSettingsApi() {
        val resolutionSettingsChecker = ResolutionLocationSettingsChecker()
        val testPresenter = MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm, iqp,
            converter, clientManager, resolutionSettingsChecker, permissionManager,
            confidenceHandler)
        testPresenter.mainViewController = mainController

        testPresenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.settingsApiTriggered).isTrue()
    }

    @Test fun onBackPressed_shouldRestoreSearchResultsSearchIndex() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.currentSearchIndex = 2
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))

        presenter.onBackPressed()
        assertThat(mainController.currentSearchIndex).isEqualTo(2)
    }

    @Test fun onBackPressed_shouldRestoreSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.currentSearchIndex = 2
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))

        presenter.onBackPressed()
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onBackPressed_shouldHideRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onBackPressed()
        assertThat(mainController.isRoutePreviewVisible).isFalse()
    }

    @Test fun onBackPressed_shouldClearRouteLine() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        mainController.routeLine = Route(JSONObject())
        presenter.onBackPressed()
        assertThat(mainController.routeLine).isNull()
    }

    @Test fun onBackPressed_reverseGeo_shouldRestoreMapPosition() {
        presenter.onSearchResultsAvailable(Result())
        presenter.reverseGeo = true
        mainController.lngLat = LngLat(40.0, 70.0)
        mainController.zoom = 16f
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onBackPressed()
        assertThat(mainController.lngLat?.latitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.longitude).isEqualTo(40.0)
    }

    @Test fun onBackPressed_reverseGeo_shouldRestoreMapZoom() {
        presenter.onSearchResultsAvailable(Result())
        presenter.reverseGeo = true
        mainController.lngLat = LngLat(40.0, 70.0)
        mainController.zoom = 16f
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onBackPressed()
        assertThat(mainController.zoom).isEqualTo(16f)
    }

    @Test fun onClickViewList_shouldMakeDirectionsVisible() {
        presenter.onClickViewList()
        assertThat(mainController.isDirectionListVisible).isTrue()
    }

    @Test fun onClickViewList_shouldSetViewStateRoutePreviewList() {
        presenter.onClickViewList()
        assertThat(presenter.vsm.viewState).isEqualTo(
                ViewStateManager.ViewState.ROUTE_PREVIEW_LIST)
    }

    @Test fun onClickStartNavigation_shouldMakeRoutingModeVisible() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onClickStartNavigation()
        assertThat(mainController.isRoutingModeVisible).isTrue()
    }

    @Test fun onClickStartNavigation_shouldResetMute() {
        mainController.muted = true
        presenter.onClickStartNavigation()
        assertThat(mainController.muted).isFalse()
    }

    @Test fun onStartRoutingMode_shouldNotResetMute() {
        mainController.muted = true
        mainController.startRoutingMode(getTestFeature(), true)
        assertThat(mainController.muted).isTrue()

        mainController.muted = false
        mainController.startRoutingMode(getTestFeature(), false)
        assertThat(mainController.muted).isFalse()
    }

    @Test fun onLocationChanged_shouldNotifyRouteControllerIfRoutingIsEnabled() {
        presenter.routingEnabled = false
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestAndroidLocation()))
        assertThat(routeController.location).isNull()

        presenter.routingEnabled = true
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestAndroidLocation()))
        assertThat(routeController.location).isNotNull()
    }

    @Test fun onSearchResultSelected_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        features.add(getFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(2)
        assertThat(mainController.currentSearchItemPosition).isEqualTo(2)
    }

    @Test fun onSearchResultSelected_shouldSetMapPositionForCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0)
        features.add(feature.toFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(2)
        assertThat(mainController.lngLat?.longitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.latitude).isEqualTo(40.0)
    }

    @Test fun onSearchResultSelected_shouldSetMapZoomDefaultZoom() {
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(getFeature())
        features.add(getFeature())
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0)
        features.add(feature.toFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(0)
        assertThat(mainController.zoom).isEqualTo(16f)
    }

    @Test fun onSearchResultTapped_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0)
        features.add(feature.toFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultTapped(0)
        assertThat(mainController.currentSearchItemPosition).isEqualTo(0)
    }

    @Test fun onSearchResultTapped_shouldSetMapPositionOfCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0)
        features.add(feature.toFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultTapped(0)
        assertThat(mainController.lngLat?.longitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.latitude).isEqualTo(40.0)
    }

    @Test fun onSearchResultTapped_shouldSetMapZoomDefaultZoom() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = SimpleFeature.create("1", "1", "", "",
            "", "", "", "", "", "", "", 0.0, "", "", 40.0, 70.0)
        features.add(feature.toFeature())
        result.features = features
        mainController.currentSearchItemPosition = 2
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultTapped(0)
        assertThat(mainController.lngLat?.longitude).isEqualTo(70.0)
        assertThat(mainController.lngLat?.latitude).isEqualTo(40.0)
    }

    @Test fun onBackPressed_shouldDisconnectLocationUpdates() {
        presenter.vsm.viewState = ROUTING
        presenter.onBackPressed()
        assertThat(mapzenLocation.connected).isFalse()
    }

    @Test fun onResume_shouldNotDisconnectLocationUpdates() {
        mapzenLocation.connected = true
        presenter.onClickStartNavigation()
        presenter.onResume()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onClickStartNavigation_shouldReconnectLocationClientAndInitLocationUpdates() {
        mapzenLocation.connected = false
        vsm.viewState = DEFAULT
        presenter.onClickStartNavigation()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onResume_shouldNotReconnectClientAndInitUpdatesWhileRouting() {
        presenter.onClickStartNavigation()
        mapzenLocation.connected = false
        presenter.onResume()
        assertThat(mapzenLocation.connected).isFalse()
    }

    @Test fun onBackPressed_shouldUpdateViewState() {
        vsm.viewState = ROUTE_DIRECTION_LIST
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(ROUTING)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(ROUTE_PREVIEW)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(SEARCH_RESULTS)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(DEFAULT)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(DEFAULT)
    }

    @Test fun onBackPressed_shouldHideProgressInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.isProgressVisible = true
        presenter.onBackPressed()
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onBackPressed_shouldCancelRouteRequestInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.routeRequestCanceled = false
        presenter.onBackPressed()
        assertThat(mainController.routeRequestCanceled).isTrue()
    }

    @Test fun onBackPressed_shouldShowActionBarInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.isActionBarHidden = true
        presenter.onBackPressed()
        assertThat(mainController.isActionBarHidden).isFalse()
    }

    @Test fun onBackPressed_shouldUpdateReverseToFalseInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        routeManager.reverse = true
        presenter.onBackPressed()
        assertThat(routeManager.reverse).isFalse()
    }

    @Test fun onBackPressed_shouldHideRoutePreviewViewInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.isRoutePreviewViewVisible = true
        presenter.onBackPressed()
        assertThat(mainController.isRoutePreviewViewVisible).isFalse()
    }

    @Test fun onBackPressed_shouldHideMapRoutePinsInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.routePinsVisible = true
        presenter.onBackPressed()
        assertThat(mainController.routePinsVisible).isFalse()
    }

    @Test fun onBackPressed_shouldLayoutAttributionAboveSearchResultsInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.isAttributionAboveSearchResults = false
        presenter.onBackPressed()
        assertThat(mainController.isAttributionAboveSearchResults).isTrue()
    }

    @Test fun onBackPressed_shouldLayoutFindMeAboveSearchResultsInStateRoutePreview() {
        vsm.viewState = ROUTE_PREVIEW
        mainController.isFindMeAboveSearchResults = false
        presenter.onBackPressed()
        assertThat(mainController.isFindMeAboveSearchResults).isTrue()
    }

    @Test fun configureMapzenMap_shouldSetMapLocationFirstTimeInvoked() {
        presenter.configureMapzenMap()
        assertThat(mainController.lngLat).isNotNull()
    }

    @Test fun configureMapzenMap_shouldNotSetMapLocationSecondTimeInvoked() {
        presenter.configureMapzenMap()
        mainController.lngLat = null
        presenter.configureMapzenMap()
        val location = mainController.lngLat
        assertThat(location).isNull()
    }

    @Test fun onReroute_shouldShowProgress() {
        routeManager.reset()
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onReroute(getTestLocation())
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onReroute_shouldFetchRoute() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        routeManager.reset()
        presenter.onReroute(getTestLocation())
        assertThat(routeManager.origin).isNotNull()
        assertThat(routeManager.destination).isNotNull()
        assertThat(routeManager.route).isNotNull()
    }

    @Test fun onReroute_shouldSetHeadingIfHasBearingIsTrue() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        routeManager.reset()
        val location = TestHelper.getMockLocation(0.0, 0.0, 180f, true)
        presenter.onReroute(location)
        assertThat(routeManager.bearing).isEqualTo(180f)
    }

    @Test fun onReroute_shouldNotSetHeadingIfHasBearingIsFalse() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        routeManager.reset()
        val location = TestHelper.getMockLocation(0.0, 0.0, 180f, false)
        presenter.onReroute(location)
        assertThat(routeManager.bearing).isEqualTo(null)
    }

    @Test fun onRouteFailure_shouldHideProgress() {
        mainController.isProgressVisible = true
        presenter.failure(404)
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun success_shouldHideProgress() {
        mainController.isProgressVisible = true
        presenter.success(Route(JSONObject()))
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onRouteSuccess_shouldShowRoutingMode() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        mainController.isRoutingModeVisible = false
        presenter.success(Route(JSONObject()))
    }

    @Test fun onRerouteSuccess_shouldDrawRouteLine() {
        val route = Route(JSONObject())
        mainController.routeLine = null
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.success(route)
        assertThat(mainController.routeLine as Route).isEqualTo(route)
    }

    @Test fun onRerouteSuccess_shouldNotResetMute() {
        mainController.muted = true
        presenter.success(Route(JSONObject()))
        assertThat(mainController.muted).isTrue()

        mainController.muted = false
        presenter.success(Route(JSONObject()))
        assertThat(mainController.muted).isFalse()
    }

    @Test fun onMuteClick_shouldToggleMute() {
        val muted = mainController.muted
        presenter.onMuteClick()
        val mutedAfter = mainController.muted
        assertThat(mutedAfter).isNotEqualTo(muted)
    }

    @Test fun onCompassClick_shouldResetMapRotation() {
        mainController.rotation = 180f
        presenter.onCompassClick()
        assertThat(mainController.rotation).isEqualTo(0f)
    }

    @Test fun onReverseGeoRequested_shouldReverseGeolocate() {
        presenter.onReverseGeoRequested(0f, 0f)
        assertThat(mainController.reverseGeolocatePoint).isNotNull()
    }

    @Test fun onPlaceSearchRequested_shouldReverseGeolocate() {
        presenter.onPlaceSearchRequested("");
        assertThat(mainController.placeSearchPoint).isNotNull()
    }

    @Test fun onReverseGeoRequested_shouldNotReverseGeolocateWhileRouting() {
        presenter.vsm.viewState = ROUTE_PREVIEW
        presenter.onReverseGeoRequested(0f, 0f)
        assertThat(mainController.reverseGeolocatePoint).isNull()

        presenter.vsm.viewState = ROUTING
        presenter.onReverseGeoRequested(0f, 0f)
        assertThat(mainController.reverseGeolocatePoint).isNull()

        presenter.vsm.viewState = ROUTE_DIRECTION_LIST
        presenter.onReverseGeoRequested(0f, 0f)
        assertThat(mainController.reverseGeolocatePoint).isNull()
    }

    @Test fun onPlaceSearchRequested_shouldNotPlaceSearchWhileRouting() {
        presenter.vsm.viewState = ROUTE_PREVIEW
        presenter.onPlaceSearchRequested("");
        assertThat(mainController.placeSearchPoint).isNull()

        presenter.vsm.viewState = ROUTING
        presenter.onPlaceSearchRequested("");
        assertThat(mainController.placeSearchPoint).isNull()

        presenter.vsm.viewState = ROUTE_DIRECTION_LIST
        presenter.onPlaceSearchRequested("");
        assertThat(mainController.placeSearchPoint).isNull()
    }

    @Test fun onRouteCancelEvent_shouldPopBackStack() {
        mainController.popBackStack = false
        presenter.onRouteCancelEvent(RouteCancelEvent())
        assertThat(mainController.popBackStack).isTrue()

        vsm.viewState = ROUTING
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(ROUTE_PREVIEW)
    }

    @Test fun onIntentQueryReceived_shouldIgnoreEmptyQuery() {
        val query = ""
        presenter.onIntentQueryReceived(query)
        verify(iqp, never()).parse(query)
    }

    @Test fun onIntentQueryReceived_shouldInvokeIntentQueryParser() {
        val queryString = "q=test_query"
        presenter.onIntentQueryReceived(queryString)
        verify(iqp).parse(queryString)
    }

    @Test fun onIntentQueryReceived_shouldSetCurrentSearchTerm() {
        val input = "q=test_query"
        val expected = "test_query"
        `when`(iqp.parse(input)).thenReturn(IntentQuery(expected, LngLat()))
        presenter.onIntentQueryReceived(input)
        assertThat(presenter.currentSearchTerm).isEqualTo(expected)
    }

    @Test fun onIntentQueryReceived_shouldExecuteSearch() {
        val input = "q=test_query"
        val expected = "test_query"
        `when`(iqp.parse(input)).thenReturn(IntentQuery(expected, LngLat()))
        presenter.onIntentQueryReceived(input)
        assertThat(mainController.queryText).isEqualTo(expected)
    }

    @Test fun onIntentQueryReceived_shouldSetMapPosition() {
        val input = "q=test_query&sll=1.0,2.0"
        val expected = LngLat(2.0, 1.0)
        `when`(iqp.parse(input)).thenReturn(IntentQuery("test_query", expected))
        presenter.onIntentQueryReceived(input)
        assertThat(mainController.lngLat).isEqualTo(expected)
    }

    @Test fun onRouteRequest_shouldShowProgress() {
        mainController.isProgressVisible = false
        presenter.onRouteRequest(TestRouteCallback())
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onRouteRequest_shouldCancelRequest() {
        mainController.routeRequestCanceled = false
        presenter.onRouteRequest(TestRouteCallback())
        assertThat(mainController.routeRequestCanceled).isTrue()
    }

    @Test fun onRouteSuccess_shouldSetRouteManagerRoute() {
        routeManager.route = null
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(routeManager.route as Route).isEqualTo(route)
    }

    @Test fun onRouteSuccess_shouldSetRoutePreviewRoute() {
        mainController.routePreviewRoute = null
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.routePreviewRoute as Route).isEqualTo(route)
    }

    @Test fun onRouteSuccess_shouldHideActionBar() {
        mainController.isActionBarHidden = false
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.isActionBarHidden).isTrue()
    }

    @Test fun onRouteSuccess_shouldShowRoutePreview() {
        mainController.isRoutePreviewVisible = false
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.isRoutePreviewVisible).isTrue()
    }

    @Test fun onRouteSuccess_shouldShowRoutePreviewDistanceTimeLayout() {
        mainController.isRoutePreviewDistanceTieVisible = false
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.isRoutePreviewDistanceTieVisible).isTrue()
    }

    @Test fun onRouteSuccess_shouldShowRoutePreviewPins() {
        mainController.routePinLocations = null
        val routeJson = getFixture("valhalla_route")
        val route = Route(routeJson)
        presenter.onRouteSuccess(route)
        assertThat(mainController.routePinLocations?.get(0)?.latitude)?.isEqualTo(52.503028)
        assertThat(mainController.routePinLocations?.get(0)?.longitude)?.isEqualTo(13.42053)
    }

    @Test fun onRouteSuccess_shouldDrawRouteOnRoutePreview() {
        mainController.routeLine = null
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.routeLine as Route).isEqualTo(route)
    }

    @Test fun onRouteSuccess_shouldHideProgress() {
        mainController.isProgressVisible = true
        val route = Route(JSONObject())
        presenter.onRouteSuccess(route)
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onRouteRequest_shouldFetchRoute() {
        presenter.onRouteRequest(TestRouteCallback())
        assertThat(routeManager.route).isNotNull()
    }

    @Test fun onClickStartNavigation_shouldSetIsNewTrue() {
        mainController.isNew = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onClickStartNavigation()
        assertThat(mainController.isNew).isTrue()
    }

    @Test fun onRerouteSuccess_shouldSetIsNewFalse() {
        mainController.isNew = true
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.success(Route(JSONObject()))
        assertThat(mainController.isNew).isFalse()
    }

    @Test fun onFeaturePicked_shouldReverseGeolocate() {
        val props = HashMap<String, String>()
        val coordinates = LngLat(0.0,0.0)
        presenter.reverseGeo = true
        presenter.onFeaturePicked(props, coordinates, 40.0f, 70.0f)
        assertThat(mainController.reverseGeolocatePoint?.longitude).isEqualTo(40.0)
        assertThat(mainController.reverseGeolocatePoint?.latitude).isEqualTo(70.0)
    }

    @Test fun checkPermissionAndEnableLocation_shouldSetMyLocationEnabledIfPermissionsGranted() {
        permissionManager.granted = true
        presenter.checkPermissionAndEnableLocation()
        assertThat(mainController.isCurrentLocationEnabled).isTrue()
    }

    @Test fun checkPermissionAndEnableLocation_shouldNotEnableMyLocationIfPermissionsNotGranted() {
        permissionManager.granted = false
        presenter.checkPermissionAndEnableLocation()
        assertThat(mainController.isCurrentLocationEnabled).isFalse()
    }

    @Test fun onClickFindMe_shouldResetTilt() {
        mainController.tilt = 30f
        presenter.onClickFindMe()
        assertThat(mainController.tilt).isEqualTo(0f)
    }

    @Test fun onExitNavigation_shouldSetViewStateDefault() {
        vsm.viewState = ROUTING
        presenter.onExitNavigation()
        assertThat(vsm.viewState).isEqualTo(DEFAULT)
    }

    @Test fun onExitNavigation_shouldDisableRouting() {
        presenter.routingEnabled = true
        presenter.onExitNavigation()
        assertThat(presenter.routingEnabled).isFalse()
    }

    @Test fun onExitNavigation_shouldResetReverse() {
        routeManager.reverse = true
        presenter.onExitNavigation()
        assertThat(routeManager.reverse).isFalse()
    }

    @Test fun onExitNavigation_shouldEnableLocation() {
        permissionManager.granted = true
        mainController.isCurrentLocationEnabled = false
        presenter.onExitNavigation()
        assertThat(mainController.isCurrentLocationEnabled).isTrue()
    }

    @Test fun onExitNavigation_shouldStopVoiceNavigationController() {
        mainController.isVoiceNavigationStopped = false
        presenter.onExitNavigation()
        assertThat(mainController.isVoiceNavigationStopped).isTrue()
    }

    @Test fun onExitNavigation_shouldClearRoute() {
        mainController.routeLine = Route(JSONObject())
        presenter.onExitNavigation()
        assertThat(mainController.routeLine).isNull()
    }

    @Test fun onExitNavigation_shouldHideRouteIcon() {
        mainController.isRouteIconVisible = true
        presenter.onExitNavigation()
        assertThat(mainController.isRouteIconVisible).isFalse()
    }

    @Test fun onExitNavigation_shouldHideRouteModeView() {
        mainController.isRouteModeViewVisible = true
        presenter.onExitNavigation()
        assertThat(mainController.isRouteModeViewVisible).isFalse()
    }

    @Test fun onExitNavigation_shouldShowActionBar() {
        mainController.isActionBarHidden = true
        presenter.onExitNavigation()
        assertThat(mainController.isActionBarHidden).isFalse()
    }

    @Test fun onExitNavigation_shouldHideRoutePreviewView() {
        mainController.isRoutePreviewViewVisible = true
        presenter.onExitNavigation()
        assertThat(mainController.isRoutePreviewViewVisible).isFalse()
    }

    @Test fun onExitNavigation_shouldResetMapResponder() {
        mainController.mapHasPanResponder = true
        presenter.onExitNavigation()
        assertThat(mainController.mapHasPanResponder).isFalse()
    }

    @Test fun onExitNavigation_shouldSetDefaultCamera() {
        mainController.mapCameraType = MapController.CameraType.PERSPECTIVE
        presenter.onExitNavigation()
        assertThat(mainController.mapCameraType).isEqualTo(MapController.CameraType.ISOMETRIC)
    }

    @Test fun onExitNavigation_shouldAlignFindMeToBottom() {
        mainController.isFindMeAboveOptions = true
        presenter.onExitNavigation()
        assertThat(mainController.isFindMeAboveOptions).isFalse()
    }

    @Test fun onExitNavigation_shouldResetMapTilt() {
        mainController.tilt = 30f
        presenter.onExitNavigation()
        assertThat(mainController.tilt).isEqualTo(0f)
    }

    @Test fun centerOnCurrentFeature_shouldDoNothingForNoFeatures() {
        val result = Result()
        presenter.onSearchResultsAvailable(result)
        mainController.currentSearchItemPosition = 3
        val pos = LngLat(70.0, 40.0)
        mainController.lngLat = pos
        mainController.zoom = MainPresenter.ROUTING_ZOOM
        presenter.onSearchResultSelected(0)
        assertThat(mainController.currentSearchItemPosition).isEqualTo(3)
        assertThat(mainController.lngLat).isEqualTo(pos)
        assertThat(mainController.zoom).isEqualTo(MainPresenter.ROUTING_ZOOM)
    }

    @Test fun centerOnFeature_shouldDoNothingForNoFeatures() {
        val result = Result()
        presenter.onSearchResultsAvailable(result)
        mainController.currentSearchItemPosition = 3
        val pos = LngLat(70.0, 40.0)
        mainController.lngLat = pos
        mainController.zoom = MainPresenter.ROUTING_ZOOM
        presenter.onSearchResultTapped(0)
        assertThat(mainController.currentSearchItemPosition).isEqualTo(3)
        assertThat(mainController.lngLat).isEqualTo(pos)
        assertThat(mainController.zoom).isEqualTo(MainPresenter.ROUTING_ZOOM)
    }

    @Test fun onMapRotateEvent_shouldShowCompass() {
        presenter.onMapRotateEvent()
        assertThat(mainController.compassShowing)
    }

    @Test fun onMapRotateEvent_shouldRotateCompass() {
        presenter.onMapRotateEvent()
        assertThat(mainController.compassRotated)
    }

    class ResolutionLocationSettingsChecker : LocationSettingsChecker {
        override fun getLocationStatus(mapzenLocation: MapzenLocation,
            locationClientManager: LocationClientManager): Status {
            return Status(Status.RESOLUTION_REQUIRED)
        }

        override fun getLocationStatusCode(mapzenLocation: MapzenLocation,
            locationClientManager: LocationClientManager): Int {
            return getLocationStatus(mapzenLocation, locationClientManager).statusCode
        }

    }
}
