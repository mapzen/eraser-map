package com.mapzen.erasermap.presenter

import com.mapzen.android.lost.api.Status
import com.mapzen.erasermap.controller.TestMainController
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.erasermap.dummy.TestHelper.getTestAndroidLocation
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.model.IntentQuery
import com.mapzen.erasermap.model.IntentQueryParser
import com.mapzen.erasermap.model.LocationClientManager
import com.mapzen.erasermap.model.LocationConverter
import com.mapzen.erasermap.model.LocationSettingsChecker
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.TestAppSettings
import com.mapzen.erasermap.model.TestLostSettingsChecker
import com.mapzen.erasermap.model.TestMapzenLocation
import com.mapzen.erasermap.model.TestRouteManager
import com.mapzen.erasermap.model.ValhallaRouteManagerTest.TestRouteCallback
import com.mapzen.erasermap.model.event.LocationChangeEvent
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.model.event.RouteEvent
import com.mapzen.erasermap.model.event.RoutePreviewEvent
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.DEFAULT
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTING
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH_RESULTS
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
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
    private val presenter = MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm, iqp,
        converter, clientManager, locationSettingsChecker)

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
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onSearchResultsAvailable_shouldDeactivateFindMeTracking() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        mainController.isFindMeTrackingEnabled = true
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isFindMeTrackingEnabled).isFalse()
    }

    @Test fun onReverseGeocodeResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onReverseGeocodeResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchRequested_shouldShowNonEmptyResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        features.add(feature)
        result.features = features
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchRequested_shouldOverridePlaceResult() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        features.add(feature)
        result.features = features
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isPlaceResultOverridden).isTrue()
    }

    @Test fun onRestoreViewState_shouldRestorePreviousSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onSearchResultsAvailable(result)

        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.searchResults).isEqualTo(features)
    }

    @Test fun onRestoreViewState_shouldHideSettingsButtonWhileShowingSearchResults() {
        val result = Result()
        result.features = ArrayList<Feature>()
        presenter.onSearchResultsAvailable(result)
        mainController.isSettingsVisible = true
        presenter.onRestoreViewState()
        assertThat(mainController.isSettingsVisible).isFalse()
    }

    @Test fun onRestoreViewState_shouldRestoreRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.isRoutePreviewVisible).isTrue()
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
        presenter.onQuerySubmit()
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldHideProgress() {
        mainController.showProgress()
        presenter.onSearchResultsAvailable(Result())
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onSearchResultsAvailable_shouldShowActionViewAll() {
        mainController.isViewAllVisible = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(Feature())
        features.add(Feature())
        features.add(Feature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isViewAllVisible).isTrue()
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

    @Test fun onRoutePreviewEvent_shouldShowRoutePreview() {
        mainController.isRoutePreviewVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isRoutePreviewVisible).isTrue()
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
            converter, clientManager, resolutionSettingsChecker)
        testPresenter.mainViewController = mainController

        testPresenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.settingsApiTriggered).isTrue()
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

    @Test fun onClickStartNavigation_shouldPublishRouteEvent() {
        val subscriber = RouteEventSubscriber()
        presenter.bus.register(subscriber)
        presenter.onClickStartNavigation()
        assertThat(subscriber.event).isNotNull()
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
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(0)
        assertThat(mainController.isCenteredOnCurrentFeature).isTrue()
    }

    @Test fun onSearchResultTapped_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.features = features
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultTapped(0)
        assertThat(mainController.isCenteredOnTappedFeature).isTrue()
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

    @Test fun onRouteSuccess_shouldHideProgress() {
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
        val poiTapPoint = floatArrayOf(40.0f, 70.0f)
        val props = HashMap<String, String>()
        presenter.reverseGeo = true
        presenter.onFeaturePicked(props, poiTapPoint)
        assertThat(mainController.reverseGeolocatePoint?.longitude).isEqualTo(40.0)
        assertThat(mainController.reverseGeolocatePoint?.latitude).isEqualTo(70.0)
    }

    class RouteEventSubscriber {
        var event: RouteEvent? = null

        @Subscribe fun onRouteEvent(event: RouteEvent) {
            this.event = event
        }
    }

    class ResolutionLocationSettingsChecker: LocationSettingsChecker {
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
