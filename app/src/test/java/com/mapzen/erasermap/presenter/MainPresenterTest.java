package com.mapzen.erasermap.presenter;

import com.mapzen.erasermap.model.RoutePreviewEvent;
import com.mapzen.erasermap.view.MainViewController;
import com.mapzen.erasermap.view.RouteViewController;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Result;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static com.mapzen.erasermap.dummy.TestHelper.getTestLocation;
import static org.assertj.core.api.Assertions.assertThat;

public class MainPresenterTest {
    private MainPresenterImpl presenter;
    private TestMainController mainController;
    private TestRouteController routeController;

    @Before
    public void setUp() throws Exception {
        presenter = new MainPresenterImpl();
        mainController = new TestMainController();
        routeController = new TestRouteController();
        presenter.setMainViewController(mainController);
        presenter.setRouteViewController(routeController);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(presenter).isNotNull();
    }

    @Test
    public void onSearchResultsAvailable_shouldShowSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        assertThat(mainController.searchResults).isEqualTo(features);
    }

    @Test
    public void onReverseGeocodeResultsAvailable_shouldShowSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onReverseGeocodeResultsAvailable(result);
        assertThat(mainController.isReverseGeocodeVisible).isTrue();
    }

    @Test
    public void onRestoreViewState_shouldRestorePreviousSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);

        TestMainController newController = new TestMainController();
        presenter.setMainViewController(newController);
        presenter.onRestoreViewState();
        assertThat(newController.searchResults).isEqualTo(features);
    }

    @Test
    public void onRestoreViewState_shouldRestoreRoutePreview() throws Exception {
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        TestMainController newController = new TestMainController();
        presenter.setMainViewController(newController);
        presenter.onRestoreViewState();
        assertThat(newController.isRoutePreviewVisible).isTrue();
    }

    @Test
    public void onCollapseSearchView_shouldHideSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        presenter.onCollapseSearchView();
        assertThat(mainController.searchResults).isNull();
    }

    @Test
    public void onQuerySubmit_shouldShowProgress() throws Exception {
        presenter.onQuerySubmit();
        assertThat(mainController.isProgressVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldHideProgress() throws Exception {
        mainController.showProgress();
        presenter.onSearchResultsAvailable(new Result());
        assertThat(mainController.isProgressVisible).isFalse();
    }

    @Test
    public void onExpandSearchView_shouldHideOverflowMenu() throws Exception {
        mainController.isOverflowVisible = true;
        presenter.onExpandSearchView();
        assertThat(mainController.isOverflowVisible).isFalse();
    }

    @Test
    public void onCollapseSearchView_shouldShowOverflowMenu() throws Exception {
        mainController.isOverflowVisible = false;
        presenter.onCollapseSearchView();
        assertThat(mainController.isOverflowVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldShowActionViewAll() throws Exception {
        mainController.isViewAllVisible = false;
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        features.add(new Feature());
        features.add(new Feature());
        features.add(new Feature());
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        assertThat(mainController.isViewAllVisible).isTrue();
    }

    @Test
    public void onCollapseSearchView_shouldHideActionViewAll() throws Exception {
        mainController.isViewAllVisible = true;
        presenter.onCollapseSearchView();
        assertThat(mainController.isViewAllVisible).isFalse();
    }

    @Test
    public void onRoutePreviewEvent_shouldCollapseSearchView() throws Exception {
        mainController.isSearchVisible = true;
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        assertThat(mainController.isSearchVisible).isFalse();
    }

    @Test
    public void onRoutePreviewEvent_shouldShowRoutePreview() throws Exception {
        mainController.isRoutePreviewVisible = false;
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        assertThat(mainController.isRoutePreviewVisible).isTrue();
    }

    @Test
    public void onBackPressed_shouldHideRoutePreview() throws Exception {
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        presenter.onBackPressed();
        assertThat(mainController.isRoutePreviewVisible).isFalse();
    }

    @Test
    public void onRoutingCircleClick_shouldMakeDirectionsVisible() {
        presenter.onRoutingCircleClick(true);
        assertThat(mainController.isDirectionListVisible).isTrue();
    }

    @Test
    public void onRoutingCircleClick_shouldMakeRoutingModeVisible() {
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        presenter.onRoutingCircleClick(false);
        assertThat(mainController.isRoutingModeVisible).isTrue();
    }

    @Test
    public void onResumeRouting_shouldCenterMapOnCurrentLocation() throws Exception {
        presenter.onResumeRouting();
        assertThat(mainController.isCenteredOnCurrentLocation).isTrue();
    }

    @Test
    public void onLocationChanged_shouldNotifyRouteControllerIfRoutingIsEnabled() throws Exception {
        presenter.setRoutingEnabled(false);
        presenter.onLocationChanged(getTestLocation());
        assertThat(routeController.location).isNull();

        presenter.setRoutingEnabled(true);
        presenter.onLocationChanged(getTestLocation());
        assertThat(routeController.location).isNotNull();
    }

    @Test
    public void onLocationChanged_shouldCenterMapIfRoutingIsEnabled() throws Exception {
        presenter.setRoutingEnabled(false);
        presenter.onLocationChanged(getTestLocation());
        assertThat(mainController.location).isNull();

        presenter.setRoutingEnabled(true);
        presenter.onLocationChanged(getTestLocation());
        assertThat(mainController.location).isNotNull();
    }

    @Test
    public void onSearchResultSelected_shouldCenterOnCurrentFeature() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        presenter.onSearchResultSelected(0);
        assertThat(mainController.isCenteredOnCurrentFeature).isTrue();
    }

    @Test
    public void onSlidingPanelOpen_shouldShowRouteDirectionList() throws Exception {
        presenter.onSlidingPanelOpen();
        assertThat(routeController.isDirectionListVisible).isTrue();
    }

    @Test
    public void onSlidingPanelCollapse_shouldHideRouteDirectionList() throws Exception {
        routeController.isDirectionListVisible = true;
        presenter.onSlidingPanelCollapse();
        assertThat(routeController.isDirectionListVisible).isFalse();
    }

    private class TestMainController implements MainViewController {
        private List<Feature> searchResults;
        private Location location;
        private float zoom;

        private boolean isProgressVisible;
        private boolean isOverflowVisible;
        private boolean isViewAllVisible;
        private boolean isSearchVisible;
        private boolean isRoutePreviewVisible;
        private boolean isDirectionListVisible;
        private boolean isRoutingModeVisible;
        private boolean isCenteredOnCurrentLocation;
        private boolean isCenteredOnCurrentFeature;
        private boolean isReverseGeocodeVisible;

        @Override public void showSearchResults(@NotNull List<? extends Feature> features) {
            searchResults = (List<Feature>) features;
        }

        @Override public void centerOnCurrentFeature(@NotNull List<? extends Feature> features) {
            isCenteredOnCurrentFeature = true;
        }

        @Override public void hideSearchResults() {
            searchResults = null;
        }

        @Override public void showProgress() {
            isProgressVisible = true;
        }

        @Override public void hideProgress() {
            isProgressVisible = false;
        }

        @Override public void showOverflowMenu() {
            isOverflowVisible = true;
        }

        @Override public void hideOverflowMenu() {
            isOverflowVisible = false;
        }

        @Override public void showActionViewAll() {
            isViewAllVisible = true;
        }

        @Override public void hideActionViewAll() {
            isViewAllVisible = false;
        }

        @Override public void showAllSearchResults(@NotNull List<? extends Feature> features) {
        }

        @Override public void collapseSearchView() {
            isSearchVisible = false;
        }

        @Override public void showRoutePreview(@NotNull Feature feature) {
            isRoutePreviewVisible = true;
        }

        @Override public void hideRoutePreview() {
            isRoutePreviewVisible = false;
        }

        @Override public void shutDown() {
        }

        @Override public void showDirectionList() {
            isDirectionListVisible = true;
        }

        @Override public void hideRoutingMode() {
            isRoutingModeVisible = false;
        }

        @Override public void showRoutingMode(@NotNull Feature feature) {
            isRoutingModeVisible = true;
        }

        @Override public void centerMapOnCurrentLocation() {
            isCenteredOnCurrentLocation = true;
        }

        @Override public void centerMapOnCurrentLocation(float zoom) {
            isCenteredOnCurrentLocation = true;
        }

        @Override public void centerMapOnLocation(Location location, float zoom) {
            this.location = location;
            this.zoom = zoom;
        }

        @Override public void showReverseGeocodeFeature(@NotNull List<? extends Feature> features)
        { isReverseGeocodeVisible = true; }
    }

    private class TestRouteController implements RouteViewController {
        private Location location;
        private boolean isDirectionListVisible;

        @Override public void onLocationChanged(@NotNull Location location) {
            this.location = location;
        }

        @Override public void showDirectionList() {
            isDirectionListVisible = true;
        }

        @Override public void hideDirectionList() {
            isDirectionListVisible = false;
        }
    }
}
