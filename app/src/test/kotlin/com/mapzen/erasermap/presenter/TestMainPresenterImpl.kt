package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat

class TestMainPresenterImpl : MainPresenter {

    override var mainViewController: MainViewController? = null
    override var routeViewController: RouteViewController? = null
    override var currentSearchTerm: String? = null
    override var currentFeature: Feature? = null
    override var routingEnabled: Boolean = false
    override var resultListVisible: Boolean = false
    override var reverseGeo: Boolean = false
    override var reverseGeoLngLat: LngLat? = null

    override fun onSearchResultsAvailable(result: Result?) {
        throw UnsupportedOperationException()
    }

    override fun onReverseGeocodeResultsAvailable(searchResults: Result?) {
        throw UnsupportedOperationException()
    }

    override fun onPlaceSearchResultsAvailable(searchResults: Result?) {
        throw UnsupportedOperationException()
    }

    override fun onSearchResultSelected(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onSearchResultTapped(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onExpandSearchView() {
        throw UnsupportedOperationException()
    }

    override fun onCollapseSearchView() {
        throw UnsupportedOperationException()
    }

    override fun onClickViewList() {
        throw UnsupportedOperationException()
    }

    override fun onClickStartNavigation() {
        throw UnsupportedOperationException()
    }

    override fun onQuerySubmit() {
        throw UnsupportedOperationException()
    }

    override fun onViewAllSearchResults() {
        throw UnsupportedOperationException()
    }

    override fun updateLocation() {
        throw UnsupportedOperationException()
    }

    override fun onBackPressed() {
        throw UnsupportedOperationException()
    }

    override fun onRestoreViewState() {
        throw UnsupportedOperationException()
    }

    override fun onCreate() {
        throw UnsupportedOperationException()
    }

    override fun onResume() {
        throw UnsupportedOperationException()
    }

    override fun onPause() {
        throw UnsupportedOperationException()
    }

    override fun onFindMeButtonClick() {
        throw UnsupportedOperationException()
    }

    override fun onMuteClick() {
        throw UnsupportedOperationException()
    }

    override fun onCompassClick() {
        throw UnsupportedOperationException()
    }

    override fun getPeliasLocationProvider(): PeliasLocationProvider {
        throw UnsupportedOperationException()
    }

    override fun onReroute(location: Location) {
        throw UnsupportedOperationException()
    }

    override fun onMapMotionEvent(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun onReverseGeoRequested(screenX: Float?, screenY: Float?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun onPlaceSearchRequested(gid: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun onExitNavigation() {
        throw UnsupportedOperationException()
    }

}
