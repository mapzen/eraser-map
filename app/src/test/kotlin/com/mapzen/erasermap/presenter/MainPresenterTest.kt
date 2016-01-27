package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.model.LocationChangeEvent
import com.mapzen.erasermap.model.RouteEvent
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.model.TestAppSettings
import com.mapzen.erasermap.model.TestMapzenLocation
import com.mapzen.erasermap.model.TestRouteManager
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.DEFAULT
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTING
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH_RESULTS
import com.mapzen.erasermap.view.TestMainController
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

public class MainPresenterTest {
    private val mainController: TestMainController = TestMainController()
    private val routeController: TestRouteController = TestRouteController()
    private val mapzenLocation: TestMapzenLocation = TestMapzenLocation()
    private val routeManager: TestRouteManager = TestRouteManager()
    private val settings: TestAppSettings = TestAppSettings()
    private val bus: Bus = Bus()
    private val vsm: ViewStateManager = ViewStateManager()
    private val presenter = MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm)

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
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onReverseGeocodeResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onReverseGeocodeResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchRequested_shouldShowNonEmptyResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        features.add(feature)
        result.setFeatures(features)
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onPlaceSearchRequested_shouldOverridePlaceResult() {
        val result = Result()
        val features = ArrayList<Feature>()
        val feature = Feature()
        features.add(feature)
        result.setFeatures(features)
        presenter.onPlaceSearchResultsAvailable(result)
        assertThat(mainController.isPlaceResultOverridden).isTrue()
    }

    @Test fun onRestoreViewState_shouldRestorePreviousSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)

        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.searchResults).isEqualTo(features)
    }

    @Test fun onRestoreViewState_shouldRestoreRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.isRoutePreviewVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldShowRoutingMode() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.routingEnabled = true
        presenter.onRestoreViewState()
        assertThat(newController.isRoutingModeVisible).isTrue()
    }

    @Test fun onCollapseSearchView_shouldHideSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
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

    @Test fun onCollapseSearchView_shouldShowOverflowMenu() {
        mainController.isOverflowVisible = false
        presenter.onCollapseSearchView()
        assertThat(mainController.isOverflowVisible).isTrue()
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

    @Test fun onLocationChanged_shouldNotifyRouteControllerIfRoutingIsEnabled() {
        presenter.routingEnabled = false
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestLocation()))
        assertThat(routeController.location).isNull()

        presenter.routingEnabled = true
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestLocation()))
        assertThat(routeController.location).isNotNull()
    }

    @Test fun onSearchResultSelected_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(0)
        assertThat(mainController.isCenteredOnCurrentFeature).isTrue()
    }

    @Test fun onSearchResultTapped_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultTapped(0)
        assertThat(mainController.isCenteredOnTappedFeature).isTrue()
    }

    @Test fun onSlidingPanelOpen_shouldShowRouteDirectionList() {
        presenter.onSlidingPanelOpen()
        assertThat(routeController.isDirectionListVisible).isTrue()
    }

    @Test fun onSlidingPanelCollapse_shouldHideRouteDirectionList() {
        routeController.isDirectionListVisible = true
        presenter.onSlidingPanelCollapse()
        assertThat(routeController.isDirectionListVisible).isFalse()
    }

    @Test fun onPause_shouldDisconnectLocationUpdates() {
        presenter.onPause()
        assertThat(mapzenLocation.connected).isFalse()
    }

    @Test fun onPause_shouldNotDisconnectLocationUpdatesWhileRouting() {
        mapzenLocation.connected = true
        presenter.onClickStartNavigation()
        presenter.onSlidingPanelOpen()
        presenter.onPause()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onResume_shouldReconnectLocationClientAndInitLocationUpdates() {
        mapzenLocation.connected = false
        vsm.viewState = DEFAULT
        presenter.onResume()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onResume_shouldNotReconnectClientAndInitUpdatesWhileRouting() {
        mapzenLocation.connected = false
        presenter.onClickStartNavigation()
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

    @Test fun onCreate_shouldSetMapLocationFirstTimeInvoked() {
        presenter.onCreate()
        assertThat(mainController.location).isNotNull()
    }

    @Test fun onCreate_shouldNotSetMapLocationSecondTimeInvoked() {
        presenter.onCreate()
        mainController.location = null
        presenter.onCreate()
        assertThat(mainController.location).isNull()
    }

    @Test fun onCreate_shouldShowCurrentLocationSecondTimeInvoked() {
        presenter.onCreate()
        mainController.puckPosition = null
        presenter.onCreate()
        assertThat(mainController.puckPosition).isNotNull()
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
        assertThat(mainController.routeLine).isEqualTo(route)
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

    class RouteEventSubscriber {
        public var event: RouteEvent? = null

        @Subscribe fun onRouteEvent(event: RouteEvent) {
            this.event = event
        }
    }
}
