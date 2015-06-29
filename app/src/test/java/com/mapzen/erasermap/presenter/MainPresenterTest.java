package com.mapzen.erasermap.presenter;

import com.mapzen.erasermap.model.RoutePreviewEvent;
import com.mapzen.erasermap.view.ViewController;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Result;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static org.assertj.core.api.Assertions.assertThat;

public class MainPresenterTest {
    private MainPresenterImpl presenter;
    private TestViewController controller;

    @Before
    public void setUp() throws Exception {
        presenter = new MainPresenterImpl();
        controller = new TestViewController();
        presenter.setViewController(controller);
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
        assertThat(controller.searchResults).isEqualTo(features);
    }

    @Test
    public void onRestoreViewState_shouldRestorePreviousSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);

        TestViewController newController = new TestViewController();
        presenter.setViewController(newController);
        presenter.onRestoreViewState();
        assertThat(newController.searchResults).isEqualTo(features);
    }

    @Test
    public void onRestoreViewState_shouldRestoreRoutePreview() throws Exception {
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        TestViewController newController = new TestViewController();
        presenter.setViewController(newController);
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
        assertThat(controller.searchResults).isNull();
    }

    @Test
    public void onQuerySubmit_shouldShowProgress() throws Exception {
        presenter.onQuerySubmit();
        assertThat(controller.isProgressVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldHideProgress() throws Exception {
        controller.showProgress();
        presenter.onSearchResultsAvailable(new Result());
        assertThat(controller.isProgressVisible).isFalse();
    }

    @Test
    public void onExpandSearchView_shouldHideOverflowMenu() throws Exception {
        controller.isOverflowVisible = true;
        presenter.onExpandSearchView();
        assertThat(controller.isOverflowVisible).isFalse();
    }

    @Test
    public void onCollapseSearchView_shouldShowOverflowMenu() throws Exception {
        controller.isOverflowVisible = false;
        presenter.onCollapseSearchView();
        assertThat(controller.isOverflowVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldShowActionViewAll() throws Exception {
        controller.isViewAllVisible = false;
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        features.add(new Feature());
        features.add(new Feature());
        features.add(new Feature());
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        assertThat(controller.isViewAllVisible).isTrue();
    }

    @Test
    public void onCollapseSearchView_shouldHideActionViewAll() throws Exception {
        controller.isViewAllVisible = true;
        presenter.onCollapseSearchView();
        assertThat(controller.isViewAllVisible).isFalse();
    }

    @Test
    public void onRoutePreviewEvent_shouldCollapseSearchView() throws Exception {
        controller.isSearchVisible = true;
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        assertThat(controller.isSearchVisible).isFalse();
    }

    @Test
    public void onRoutePreviewEvent_shouldShowRoutePreview() throws Exception {
        controller.isRoutePreviewVisible = false;
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        assertThat(controller.isRoutePreviewVisible).isTrue();
    }

    @Test
    public void onBackPressed_shouldHideRoutePreview() throws Exception {
        presenter.onRoutePreviewEvent(new RoutePreviewEvent(getTestFeature()));
        presenter.onBackPressed();
        assertThat(controller.isRoutePreviewVisible).isFalse();
    }

    @Test
    public void onRoutingCircleClick_shouldMakeDirectionsVisible(){
        presenter.onRoutingCircleClick(true);
        assertThat(controller.isDirectionListVisible).isTrue();
    }

    @Test
    public void onRoutingCircleClick_shouldMakeRouingModeVisible(){
        presenter.onRoutingCircleClick(false);
        assertThat(controller.isRoutingModeVisible).isTrue();
    }

    private class TestViewController implements ViewController {
        private List<Feature> searchResults;
        private boolean isProgressVisible;
        private boolean isOverflowVisible;
        private boolean isViewAllVisible;
        private boolean isSearchVisible;
        private boolean isRoutePreviewVisible;
        private boolean isDirectionListVisible;
        private boolean isRoutingModeVisible;

        @Override public void showSearchResults(@NotNull List<? extends Feature> features) {
            searchResults = (List<Feature>) features;
        }

        @Override public void centerOnCurrentFeature(@NotNull List<? extends Feature> features) {
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

        @Override
        public void showDirectionList() { isDirectionListVisible = true;}

        @Override
        public void hideRoutingMode() { isRoutingModeVisible = false; }

        @Override
        public void showRoutingMode() { isRoutingModeVisible = true; }
    }
}
